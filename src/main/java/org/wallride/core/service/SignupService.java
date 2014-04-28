package org.wallride.core.service;

import org.joda.time.LocalDateTime;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.wallride.core.domain.User;
import org.wallride.core.domain.UserInvitation;
import org.wallride.core.repository.UserInvitationRepository;
import org.wallride.core.repository.UserRepository;
import org.wallride.core.support.AuthorizedUser;
import org.wallride.web.support.HttpForbiddenException;

import javax.inject.Inject;

@Service
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
	public AuthorizedUser signup(SignupRequest request, BindingResult errors) throws BindException {
		UserInvitation invitation = userInvitationRepository.findByTokenForUpdate(request.getToken());
		boolean valid = false;
		if (invitation != null) {
			valid = validateInvitation(invitation);
		}
		if (!valid) {
			throw new HttpForbiddenException();
		}

		User duplicate = userRepository.findByLoginId(request.getLoginId());
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
		user.setLoginId(request.getLoginId());
		StandardPasswordEncoder passwordEncoder = new StandardPasswordEncoder();
		user.setLoginPassword(passwordEncoder.encode(request.getLoginPassword()));
		user.getName().setFirstName(request.getName().getFirstName());
		user.getName().setLastName(request.getName().getLastName());
		user.setEmail(request.getEmail());
		user.setCreatedAt(now);
		user.setUpdatedAt(now);
		user = userRepository.saveAndFlush(user);

		AuthorizedUser authorizedUser = new AuthorizedUser(user);
//		Authentication auth = new UsernamePasswordAuthenticationToken(authorizedUser, null, authorizedUser.getAuthorities());
//		SecurityContextHolder.getContext().setAuthentication(auth);

		return authorizedUser;
	}
}
