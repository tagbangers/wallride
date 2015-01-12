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

package org.wallride.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.core.domain.UserInvitation;

import javax.persistence.LockModeType;
import java.util.Collection;
import java.util.List;

@Repository
@Transactional
public interface UserInvitationRepository extends JpaRepository<UserInvitation, String> {
	
	static final String DEFAULT_SELECT_QUERY = 
			"from UserInvitation invitation ";

	@Query("select invitation.token from UserInvitation invitation order by invitation.token ")
	List<Long> findToken();
	
	@Query(DEFAULT_SELECT_QUERY + "where invitation.token in (:tokens) ")
	List<UserInvitation> findByTokenIn(@Param("tokens") Collection<String> tokens);
	
	@Query(DEFAULT_SELECT_QUERY + "where invitation.token = :token ")
	UserInvitation findByToken(@Param("token") String token);
	
	@Query(DEFAULT_SELECT_QUERY + "where invitation.token = :token ")
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	UserInvitation findByTokenForUpdate(@Param("token") String token);
	
}
