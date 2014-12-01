package org.wallride.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.core.domain.PasswordResetToken;

@Repository
@Transactional
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {

	static final String DEFAULT_SELECT_QUERY =
			"from PasswordResetToken passwordResetToken " +
			"left join fetch passwordResetToken.user user ";

	@Query(DEFAULT_SELECT_QUERY + "where passwordResetToken.token = :token ")
	PasswordResetToken findByToken(@Param("token") String token);
}
