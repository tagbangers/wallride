package org.wallride.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.domain.User;

import javax.persistence.LockModeType;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
	
	static final String DEFAULT_SELECT_QUERY = 
			"from User user ";

	@Query("select user.id from User user order by user.id")
	List<Long> findId();
	
	@Query(DEFAULT_SELECT_QUERY + "where user.id in (:ids) ")
	List<User> findByIdIn(@Param("ids") Collection<Long> ids);
	
	@Query(DEFAULT_SELECT_QUERY + "where user.id = :id ")
	User findById(@Param("id") Long id);
	
	@Query(DEFAULT_SELECT_QUERY + "where user.id = :id ")
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	User findByIdForUpdate(@Param("id")Long id);
	
//	@Query(DEFAULT_SELECT_QUERY + "where user.code = :code ")
//	User findByCode(@Param("code") String code);
//	
//	@Query(DEFAULT_SELECT_QUERY + "where user.code = :code ")
//	@Lock(LockModeType.PESSIMISTIC_WRITE)
//	User findByCodeForUpdate(@Param("code") String code);

	@Query(DEFAULT_SELECT_QUERY + "where user.loginId = :loginId ")
	User findByLoginId(@Param("loginId") String loginId);

	@Modifying
	@Query("update User set lastLoginTime = :lastLoginTime where id = :id ")
	int updateLastLoginTime(@Param("id") long id, @Param("lastLoginTime") Date lastLoginTime);
}
