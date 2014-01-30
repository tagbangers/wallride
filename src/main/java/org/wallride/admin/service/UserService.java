package org.wallride.admin.service;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.validation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.wallride.admin.support.AuthorizedUser;
import org.wallride.admin.web.user.*;
import org.wallride.core.domain.Setting;
import org.wallride.core.domain.User;
import org.wallride.core.domain.UserInvitation;
import org.wallride.core.repository.UserInvitationRepository;
import org.wallride.core.repository.UserRepository;
import org.wallride.core.support.Paginator;
import org.wallride.core.support.Settings;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ValidationException;
import java.text.MessageFormat;
import java.util.*;

@Service @Lazy
@Transactional(rollbackFor=Exception.class)
public class UserService {

	@Inject
	private Settings settings;

	@Inject
	private UserRepository userRepository;

	@Inject
	private UserInvitationRepository userInvitationRepository;

	@Inject
	private MessageCodesResolver messageCodesResolver;

	@Inject
	private PlatformTransactionManager transactionManager;

	@Inject
	private JavaMailSender mailSender;

	@Inject
	@Qualifier("adminTemplateEngine")
	private TemplateEngine templateEngine;

	@Inject
	private Environment environment;

	@Inject
	private MessageSourceAccessor messageSourceAccessor;

	@PersistenceContext
	private EntityManager entityManager;

	private static Logger logger = LoggerFactory.getLogger(UserService.class);

	@CacheEvict(value="users", allEntries=true)
	public User updateUser(UserEditForm form, Errors errors, AuthorizedUser authorizedUser) throws ValidationException {
		User user = userRepository.findByIdForUpdate(form.getId());
//		user.setTitle(form.getTitle());
//		user.setBody(form.getBody());

		user = userRepository.saveAndFlush(user);
		return user;
	}

	@CacheEvict(value="users", allEntries=true)
	public User deleteUser(UserDeleteForm form, BindingResult result) throws BindException {
		User user = userRepository.findByIdForUpdate(form.getId());
		userRepository.delete(user);
		return user;
	}

	@CacheEvict(value="users", allEntries=true)
	@Transactional(propagation= Propagation.NOT_SUPPORTED)
	public List<User> bulkDeleteUser(UserBulkDeleteForm bulkDeleteForm, BindingResult result) {
		List<User> users = new ArrayList<>();
		for (long id : bulkDeleteForm.getIds()) {
			final UserDeleteForm deleteForm = new UserDeleteForm();
			deleteForm.setId(id);
			deleteForm.setConfirmed(bulkDeleteForm.isConfirmed());

			final BeanPropertyBindingResult r = new BeanPropertyBindingResult(deleteForm, "form");
			r.setMessageCodesResolver(messageCodesResolver);

			TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
			transactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
			User user = null;
			try {
				user = transactionTemplate.execute(new TransactionCallback<User>() {
					public User doInTransaction(TransactionStatus status) {
						try {
							return deleteUser(deleteForm, r);
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
	public List<UserInvitation> inviteUsers(UserInvitationCreateForm form, BindingResult result, AuthorizedUser authorizedUser) throws MessagingException {
		String[] recipients = StringUtils.commaDelimitedListToStringArray(form.getInvitees());

		LocalDateTime now = new LocalDateTime();

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

		for (UserInvitation invitation : invitations) {
			String websiteTitle = settings.readSettingAsString(Setting.Key.WEBSITE_TITLE, LocaleContextHolder.getLocale().getLanguage());
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
	public UserInvitation inviteAgain(UserInvitationResendForm form, BindingResult result, AuthorizedUser authorizedUser) throws MessagingException {
		LocalDateTime now = new LocalDateTime();

		UserInvitation invitation = userInvitationRepository.findByTokenForUpdate(form.getToken());
		invitation.setExpiredAt(now.plusHours(72));
		invitation.setUpdatedAt(now);
		invitation.setUpdatedBy(authorizedUser.toString());
		invitation = userInvitationRepository.saveAndFlush(invitation);

		String websiteTitle = settings.readSettingAsString(Setting.Key.WEBSITE_TITLE, LocaleContextHolder.getLocale().getLanguage());
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
	public UserInvitation deleteUserInvitation(UserInvitationDeleteForm form) {
		UserInvitation invitation = userInvitationRepository.findByTokenForUpdate(form.getToken());
		userInvitationRepository.delete(invitation);
		return invitation;
	}

//	@Cacheable(value="users", key="'id.'+#form")
	public List<Long> searchUsers(UserSearchForm form) {
		if (form.isEmpty()) {
			return userRepository.findId();
		}
		return userRepository.findByFullTextSearchTerm(form.toFullTextSearchTerm());
	}
	
//	@Cacheable(value="users", key="'list.'+#paginator")
	public List<User> readUsers(Paginator<Long> paginator) {
		if (paginator == null || !paginator.hasElement()) return new ArrayList<User>();
		return readUsers(paginator.getElements());
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

//	@Cacheable(value="users", key="'invitations.list'")
	public List<UserInvitation> readUserInvitations() {
		return userInvitationRepository.findAll(new Sort(Sort.Direction.DESC, "createdAt"));
	}
}
