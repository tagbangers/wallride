package org.wallride.core.service;

import org.joda.time.LocalDateTime;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.wallride.core.domain.User;
import org.wallride.core.domain.UserInvitation;
import org.wallride.core.repository.UserInvitationRepository;
import org.wallride.core.repository.UserRepository;
import org.wallride.core.support.AuthorizedUser;
import org.wallride.web.HttpForbiddenException;

import javax.inject.Inject;

@Service @Lazy
@Transactional(rollbackFor=Exception.class)
public class SignupService {

	@Inject
	private UserRepository userRepository;

	@Inject
	private UserInvitationRepository userInvitationRepository;

	public UserInvitation readUserInvitation(String token) {
		return userInvitationRepository.findByToken(token);
	}

	public boolean validateInvitation(UserInvitation invitation) {
		if (invitation == null) {
			return false;
		}
		if (invitation.isAccepted()) {
			return false;
		}
		LocalDateTime now = new LocalDateTime();
		if (now.isAfter(invitation.getExpiredAt())) {
			return false;
		}
		return true;
	}

	@CacheEvict(value="users", allEntries=true)
	public AuthorizedUser signup(SignupRequest form, BindingResult errors) throws BindException {
		UserInvitation invitation = userInvitationRepository.findByTokenForUpdate(form.getToken());
		boolean valid = false;
		if (invitation != null) {
			valid = validateInvitation(invitation);
		}
		if (!valid) {
			throw new HttpForbiddenException();
		}

		User duplicate = userRepository.findByLoginId(form.getLoginId());
		if (duplicate != null) {
			errors.rejectValue("loginId", "NotDuplicate");
		}
		if (errors.hasErrors()) {
			throw new BindException(errors);
		}

		LocalDateTime now = new LocalDateTime();
		invitation.setAccepted(true);
		invitation.setAcceptedAt(now);
		userInvitationRepository.saveAndFlush(invitation);

		User user = new User();
		user.setLoginId(form.getLoginId());
		Md5PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
		user.setLoginPassword(passwordEncoder.encodePassword(form.getLoginPassword(), null));
		user.getName().setFirstName(form.getName().getFirstName());
		user.getName().setLastName(form.getName().getLastName());
		user.setEmail(form.getEmail());
		user.setCreatedAt(now);
		user.setUpdatedAt(now);
		user = userRepository.saveAndFlush(user);

		AuthorizedUser authorizedUser = new AuthorizedUser(user);
//		Authentication auth = new UsernamePasswordAuthenticationToken(authorizedUser, null, authorizedUser.getAuthorities());
//		SecurityContextHolder.getContext().setAuthentication(auth);

		return authorizedUser;
	}
}
