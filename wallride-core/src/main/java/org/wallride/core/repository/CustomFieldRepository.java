package org.wallride.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.core.domain.CustomField;

import javax.persistence.LockModeType;

@Repository
@Transactional
public interface CustomFieldRepository extends JpaRepository<CustomField, Long> {

	CustomField findOneById(String id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	CustomField findOneForUpdateById(String id);

}
