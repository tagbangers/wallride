package org.wallride.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.core.domain.Media;

import javax.persistence.LockModeType;
import java.util.Collection;
import java.util.List;

@Repository
@Transactional
public interface MediaRepository extends JpaRepository<Media, String> {
	
	static final String DEFAULT_SELECT_QUERY = 
			"from Media media ";

	@Query("select media.id from Media media order by media.id")
	List<String> findId();
	
	@Query(DEFAULT_SELECT_QUERY + "where media.id in (:ids) ")
	List<Media> findByIdIn(@Param("ids") Collection<String> ids);
	
	@Query(DEFAULT_SELECT_QUERY + "where media.id = :id ")
	Media findById(@Param("id") String id);
	
	@Query(DEFAULT_SELECT_QUERY + "where media.id = :id ")
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Media findByIdForUpdate(@Param("id") String id);
}
