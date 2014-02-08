package org.wallride.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.domain.Setting;

import javax.persistence.LockModeType;

@Repository
@Transactional
public interface SettingRepository extends JpaRepository<Setting, String> {
	
	static final String DEFAULT_SELECT_QUERY = 
			"from Setting setting ";

	@Query(DEFAULT_SELECT_QUERY + "where setting.key = :key ")
	Setting findByKey(@Param("key") String key);
	
	@Query(DEFAULT_SELECT_QUERY + "where setting.key = :key ")
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Setting findByKeyForUpdate(@Param("key") String key);
}
