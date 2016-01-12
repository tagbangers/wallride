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
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.core.domain.User;

import javax.persistence.LockModeType;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
	
//	@EntityGraph(value = User.DEEP_GRAPH_NAME, type = EntityGraph.EntityGraphType.FETCH)
	User findOneById(Long id);

//	@EntityGraph(value = User.DEEP_GRAPH_NAME, type = EntityGraph.EntityGraphType.FETCH)
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	User findOneForUpdateById(Long id);

//	@EntityGraph(value = User.DEEP_GRAPH_NAME, type = EntityGraph.EntityGraphType.FETCH)
	User findOneByLoginId(String loginId);

//	@EntityGraph(value = User.DEEP_GRAPH_NAME, type = EntityGraph.EntityGraphType.FETCH)
	User findOneByEmail(String email);

//	@EntityGraph(value = User.SHALLOW_GRAPH_NAME, type = EntityGraph.EntityGraphType.FETCH)
	List<User> findAllByIdIn(Collection<Long> ids);
	
	@Modifying
	@Query("update User set lastLoginTime = :lastLoginTime where id = :id ")
	int updateLastLoginTime(@Param("id") long id, @Param("lastLoginTime") Date lastLoginTime);
}
