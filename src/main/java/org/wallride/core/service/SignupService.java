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

import org.joda.time.LocalDateTime;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.core.domain.User;
import org.wallride.core.domain.UserInvitation;
import org.wallride.core.repository.UserInvitationRepository;
import org.wallride.core.repository.UserRepository;
import org.wallride.core.support.AuthorizedUser;
import org.wallride.web.support.HttpForbiddenException;

import javax.annotation.Resource;

@Service
@Transactional(rollbackFor=Exception.class)
public class SignupService {

	@Resource
	private UserRepository userRepository;
	@Resource
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
	public AuthorizedUser signup(SignupRequest request, User.Role role) throws ServiceException {
		return signup(request, role, null);
	}

	@CacheEvict(value="users", allEntries=true)
	public AuthorizedUser signup(SignupRequest request, User.Role role, String token) throws ServiceException {
		UserInvitation invitation = null;
		if (token != null) {
			invitation = userInvitationRepository.findByTokenForUpdate(token);
			if (invitation == null) {
				throw new HttpForbiddenException();
			}
			if (!validateInvitation(invitation)) {
				throw new HttpForbiddenException();
			}
		}

		User duplicate;
		duplicate = userRepository.findByLoginId(request.getLoginId());
		if (duplicate != null) {
			throw new DuplicateLoginIdException(request.getLoginId());
		}
		duplicate = userRepository.findByEmail(request.getEmail());
		if (duplicate != null) {
			throw new DuplicateEmailException(request.getEmail());
		}

		LocalDateTime now = new LocalDateTime();
		if (invitation != null) {
			invitation.setAccepted(true);
			invitation.setAcceptedAt(now);
			userInvitationRepository.saveAndFlush(invitation);
		}

		User user = new User();
		user.setLoginId(request.getLoginId());
		StandardPasswordEncoder passwordEncoder = new StandardPasswordEncoder();
		user.setLoginPassword(passwordEncoder.encode(request.getLoginPassword()));
		user.getName().setFirstName(request.getName().getFirstName());
		user.getName().setLastName(request.getName().getLastName());
		user.setEmail(request.getEmail());
		user.getRoles().add(role);
		user.setCreatedAt(now);
		user.setUpdatedAt(now);
		user = userRepository.saveAndFlush(user);

		AuthorizedUser authorizedUser = new AuthorizedUser(user);
//		Authentication auth = new UsernamePasswordAuthenticationToken(authorizedUser, null, authorizedUser.getAuthorities());
//		SecurityContextHolder.getContext().setAuthentication(auth);

		return authorizedUser;
	}
}
