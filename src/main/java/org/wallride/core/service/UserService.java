/*
 * Copyright 2014 Tagbangers, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wallride.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.wallride.core.domain.Blog;
import org.wallride.core.domain.PasswordResetToken;
import org.wallride.core.domain.User;
import org.wallride.core.domain.UserInvitation;
import org.wallride.core.repository.PasswordResetTokenRepository;
import org.wallride.core.repository.UserInvitationRepository;
import org.wallride.core.repository.UserRepository;
import org.wallride.core.support.AuthorizedUser;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.ValidationException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional(rollbackFor=Exception.class)
public class UserService {

	@Inject
	private BlogService blogService;

	@Inject
	private MessageCodesResolver messageCodesResolver;

	@Inject
	private PlatformTransactionManager transactionManager;

	@Inject
	private JavaMailSender mailSender;

	@Inject
	@Qualifier("emailTemplateEngine")
	private TemplateEngine templateEngine;

	@Inject
	private MessageSourceAccessor messageSourceAccessor;

	@Inject
	private Environment environment;

	@Inject
	private MailProperties mailProperties;

	@Resource
	private UserRepository userRepository;
	@Resource
	private UserInvitationRepository userInvitationRepository;
	@Resource
	private PasswordResetTokenRepository passwordResetTokenRepository;

	private static Logger logger = LoggerFactory.getLogger(UserService.class);

	public PasswordResetToken createPasswordResetToken(PasswordResetTokenCreateRequest request) {
		User user = userRepository.findByEmail(request.getEmail());
		if (user == null) {
			throw new EmailNotFoundException();
		}

		LocalDateTime now = LocalDateTime.now();
		PasswordResetToken passwordResetToken = new PasswordResetToken();
		passwordResetToken.setUser(user);
		passwordResetToken.setEmail(user.getEmail());
		passwordResetToken.setExpiredAt(now.plusHours(24));
		passwordResetToken.setCreatedAt(now);
		passwordResetToken.setCreatedBy(user.toString());
		passwordResetToken.setUpdatedAt(now);
		passwordResetToken.setUpdatedBy(user.toString());
		passwordResetToken = passwordResetTokenRepository.saveAndFlush(passwordResetToken);

		try {
			Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
			String blogTitle = blog.getTitle(LocaleContextHolder.getLocale().getLanguage());

			ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
			if (blog.isMultiLanguage()) {
				builder.path("/{language}");
			}
			builder.path("/password-reset");
			builder.path("/{token}");

			Map<String, Object> urlVariables = new LinkedHashMap<>();
			urlVariables.put("language", request.getLanguage());
			urlVariables.put("token", passwordResetToken.getToken());
			String resetLink = builder.buildAndExpand(urlVariables).toString();

			Context ctx = new Context(LocaleContextHolder.getLocale());
			ctx.setVariable("passwordResetToken", passwordResetToken);
			ctx.setVariable("resetLink", resetLink);

			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // true = multipart
			message.setSubject(MessageFormat.format(
					messageSourceAccessor.getMessage("PasswordResetSubject", LocaleContextHolder.getLocale()),
					blogTitle));
			message.setFrom(mailProperties.getProperties().get("mail.from"));
			message.setTo(passwordResetToken.getEmail());

			String htmlContent = templateEngine.process("password-reset", ctx);
			message.setText(htmlContent, true); // true = isHtml

			mailSender.send(mimeMessage);
		} catch (MessagingException e) {
			throw new ServiceException(e);
		}

		return passwordResetToken;
	}

	@CacheEvict(value="users", allEntries=true)
	public User updateUser(UserUpdateRequest form, Errors errors, AuthorizedUser authorizedUser) throws ValidationException {
		User user = userRepository.findByIdForUpdate(form.getId());
		user.setName(form.getName());
		user.setNickname(form.getNickname());
		user.setEmail(form.getEmail());
		user.setDescription(form.getDescription());

		user = userRepository.saveAndFlush(user);
		return user;
	}

	@CacheEvict(value="users", allEntries=true)
	public User updateProfile(ProfileUpdateRequest request, AuthorizedUser updatedBy) {
		User user = userRepository.findByIdForUpdate(request.getUserId());
		if (user == null) {
			throw new IllegalArgumentException("The user does not exist");
		}

		User duplicate;
		if (!ObjectUtils.nullSafeEquals(request.getEmail(), user.getEmail())) {
			duplicate = userRepository.findByEmail(request.getEmail());
			if (duplicate != null) {
				throw new DuplicateEmailException(request.getEmail());
			}
		}
		if (!ObjectUtils.nullSafeEquals(request.getLoginId(), user.getLoginId())) {
			duplicate = userRepository.findByLoginId(request.getLoginId());
			if (duplicate != null) {
				throw new DuplicateLoginIdException(request.getLoginId());
			}
		}

		user.setEmail(request.getEmail());
		user.setLoginId(request.getLoginId());
		user.setName(request.getName());
		user.setUpdatedAt(LocalDateTime.now());
		user.setUpdatedBy(updatedBy.toString());
		return userRepository.saveAndFlush(user);
	}

	@CacheEvict(value="users", allEntries=true)
	public User updatePassword(PasswordUpdateRequest request, PasswordResetToken passwordResetToken) {
		User user = userRepository.findByIdForUpdate(request.getUserId());
		if (user == null) {
			throw new IllegalArgumentException("The user does not exist");
		}
		PasswordEncoder passwordEncoder = new StandardPasswordEncoder();
		user.setLoginPassword(passwordEncoder.encode(request.getPassword()));
		user.setUpdatedAt(LocalDateTime.now());
		user.setUpdatedBy(passwordResetToken.getUser().toString());
		user = userRepository.saveAndFlush(user);

		passwordResetTokenRepository.delete(passwordResetToken);

		try {
			Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
			String blogTitle = blog.getTitle(LocaleContextHolder.getLocale().getLanguage());

			ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
			if (blog.isMultiLanguage()) {
				builder.path("/{language}");
			}
			builder.path("/login");

			Map<String, Object> urlVariables = new LinkedHashMap<>();
			urlVariables.put("language", request.getLanguage());
			urlVariables.put("token", passwordResetToken.getToken());
			String loginLink = builder.buildAndExpand(urlVariables).toString();

			Context ctx = new Context(LocaleContextHolder.getLocale());
			ctx.setVariable("passwordResetToken", passwordResetToken);
			ctx.setVariable("resetLink", loginLink);

			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // true = multipart
			message.setSubject(MessageFormat.format(
					messageSourceAccessor.getMessage("PasswordChangedSubject", LocaleContextHolder.getLocale()),
					blogTitle));
			message.setFrom(mailProperties.getProperties().get("mail.from"));
			message.setTo(passwordResetToken.getEmail());

			String htmlContent = templateEngine.process("password-changed", ctx);
			message.setText(htmlContent, true); // true = isHtml

			mailSender.send(mimeMessage);
		} catch (MessagingException e) {
			throw new ServiceException(e);
		}

		return user;
	}

	@CacheEvict(value="users", allEntries=true)
	public User updatePassword(PasswordUpdateRequest request, AuthorizedUser updatedBy) {
		User user = userRepository.findByIdForUpdate(request.getUserId());
		if (user == null) {
			throw new IllegalArgumentException("The user does not exist");
		}
		PasswordEncoder passwordEncoder = new StandardPasswordEncoder();
		user.setLoginPassword(passwordEncoder.encode(request.getPassword()));
		user.setUpdatedAt(LocalDateTime.now());
		user.setUpdatedBy(updatedBy.toString());
		return userRepository.saveAndFlush(user);
	}

	@CacheEvict(value="users", allEntries=true)
	public User deleteUser(UserDeleteRequest form, BindingResult result) throws BindException {
		User user = userRepository.findByIdForUpdate(form.getId());
		userRepository.delete(user);
		return user;
	}

	@CacheEvict(value="users", allEntries=true)
	@Transactional(propagation= Propagation.NOT_SUPPORTED)
	public List<User> bulkDeleteUser(UserBulkDeleteRequest bulkDeleteForm, BindingResult result) {
		List<User> users = new ArrayList<>();
		for (long id : bulkDeleteForm.getIds()) {
			final UserDeleteRequest deleteRequest = new UserDeleteRequest.Builder()
					.id(id)
					.build();

			final BeanPropertyBindingResult r = new BeanPropertyBindingResult(deleteRequest, "request");
			r.setMessageCodesResolver(messageCodesResolver);

			TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
			transactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
			User user = null;
			try {
				user = transactionTemplate.execute(new TransactionCallback<User>() {
					public User doInTransaction(TransactionStatus status) {
						try {
							return deleteUser(deleteRequest, r);
						}
						catch (BindException e) {
							throw new RuntimeException(e);
						}
					}
				});
				users.add(user);
			}
			catch (Exception e) {
				logger.debug("Errors: {}", r);
				result.addAllErrors(r);
			}
		}
		return users;
	}

	@CacheEvict(value="users", allEntries=true)
	public List<UserInvitation> inviteUsers(UserInvitationCreateRequest form, BindingResult result, AuthorizedUser authorizedUser) throws MessagingException {
		String[] recipients = StringUtils.commaDelimitedListToStringArray(form.getInvitees());

		LocalDateTime now = LocalDateTime.now();

		List<UserInvitation> invitations = new ArrayList<>();
		for (String recipient : recipients) {
			UserInvitation invitation = new UserInvitation();
			invitation.setEmail(recipient);
			invitation.setMessage(form.getMessage());
			invitation.setExpiredAt(now.plusHours(72));
			invitation.setCreatedAt(now);
			invitation.setCreatedBy(authorizedUser.toString());
			invitation.setUpdatedAt(now);
			invitation.setUpdatedBy(authorizedUser.toString());
			invitation = userInvitationRepository.saveAndFlush(invitation);
			invitations.add(invitation);
		}

		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
		for (UserInvitation invitation : invitations) {
			String websiteTitle = blog.getTitle(LocaleContextHolder.getLocale().getLanguage());
			String signupLink = ServletUriComponentsBuilder.fromCurrentContextPath()
					.path("/_admin/signup")
					.queryParam("token", invitation.getToken())
					.buildAndExpand().toString();

			final Context ctx = new Context(LocaleContextHolder.getLocale());
			ctx.setVariable("websiteTitle", websiteTitle);
			ctx.setVariable("authorizedUser", authorizedUser);
			ctx.setVariable("signupLink", signupLink);
			ctx.setVariable("invitation", invitation);

			final MimeMessage mimeMessage = mailSender.createMimeMessage();
			final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // true = multipart
			message.setSubject(MessageFormat.format(
					messageSourceAccessor.getMessage("InvitationMessageTitle", LocaleContextHolder.getLocale()),
					authorizedUser.toString(),
					websiteTitle));
			message.setFrom(authorizedUser.getEmail());
			message.setTo(invitation.getEmail());

			final String htmlContent = templateEngine.process("user-invite", ctx);
			message.setText(htmlContent, true); // true = isHtml

			mailSender.send(mimeMessage);
		}

		return invitations;
	}

	@CacheEvict(value="users", allEntries=true)
	public UserInvitation inviteAgain(UserInvitationResendRequest form, BindingResult result, AuthorizedUser authorizedUser) throws MessagingException {
		LocalDateTime now = LocalDateTime.now();

		UserInvitation invitation = userInvitationRepository.findByTokenForUpdate(form.getToken());
		invitation.setExpiredAt(now.plusHours(72));
		invitation.setUpdatedAt(now);
		invitation.setUpdatedBy(authorizedUser.toString());
		invitation = userInvitationRepository.saveAndFlush(invitation);

		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
		String websiteTitle = blog.getTitle(LocaleContextHolder.getLocale().getLanguage());
		String signupLink = ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/_admin/signup")
				.queryParam("token", invitation.getToken())
				.buildAndExpand().toString();

		final Context ctx = new Context(LocaleContextHolder.getLocale());
		ctx.setVariable("websiteTitle", websiteTitle);
		ctx.setVariable("authorizedUser", authorizedUser);
		ctx.setVariable("signupLink", signupLink);
		ctx.setVariable("invitation", invitation);

		final MimeMessage mimeMessage = mailSender.createMimeMessage();
		final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // true = multipart
		message.setSubject(MessageFormat.format(
				messageSourceAccessor.getMessage("InvitationMessageTitle", LocaleContextHolder.getLocale()),
				authorizedUser.toString(),
				websiteTitle));
		message.setFrom(authorizedUser.getEmail());
		message.setTo(invitation.getEmail());

		final String htmlContent = templateEngine.process("user-invite", ctx);
		message.setText(htmlContent, true); // true = isHtml

		mailSender.send(mimeMessage);

		return invitation;
	}


	@CacheEvict(value="users", allEntries=true)
	public UserInvitation deleteUserInvitation(UserInvitationDeleteRequest request) {
		UserInvitation invitation = userInvitationRepository.findByTokenForUpdate(request.getToken());
		userInvitationRepository.delete(invitation);
		return invitation;
	}

	public Page<User> readUsers(UserSearchRequest request) {
		Pageable pageable = new PageRequest(0, 10);
		return readUsers(request, pageable);
	}

	public Page<User> readUsers(UserSearchRequest request, Pageable pageable) {
		return userRepository.search(request, pageable);
	}

	private List<User> readUsers(Collection<Long> ids) {
		Set<User> results = new LinkedHashSet<User>(userRepository.findByIdIn(ids));
		List<User> users = new ArrayList<>();
		for (long id : ids) {
			for (User user : results) {
				if (id == user.getId()) {
					users.add(user);
					break;
				}
			}
		}
		return users;
	}

//	@Cacheable(value="users", key="'id.'+#id")
	public User readUserById(long id) {
		return userRepository.findOne(id);
	}

	public User readUserByLoginId(String loginId) {
		return userRepository.findByLoginId(loginId);
	}

//	@Cacheable(value="users", key="'invitations.list'")
	public List<UserInvitation> readUserInvitations() {
		return userInvitationRepository.findAll(new Sort(Sort.Direction.DESC, "createdAt"));
	}

	public PasswordResetToken readPasswordResetToken(String token) {
		return passwordResetTokenRepository.findByToken(token);
	}
}
