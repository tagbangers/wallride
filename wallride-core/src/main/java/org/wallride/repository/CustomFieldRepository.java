package org.wallride.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.domain.CustomField;

import javax.persistence.LockModeType;
import java.util.List;

@Repository
@Transactional
public interface CustomFieldRepository extends JpaRepository<CustomField, Long>, CustomFieldRepositoryCustom {

	CustomField findOneById(String id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	CustomField findOneForUpdateById(Long id);

	@EntityGraph(value = CustomField.DEEP_GRAPH_NAME, type = EntityGraph.EntityGraphType.FETCH)
	CustomField findOneByIdAndLanguage(Long id, String language);

	@EntityGraph(value = CustomField.DEEP_GRAPH_NAME, type = EntityGraph.EntityGraphType.FETCH)
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	CustomField findOneForUpdateByIdAndLanguage(Long id, String language);

	@EntityGraph(value = CustomField.DEEP_GRAPH_NAME, type = EntityGraph.EntityGraphType.FETCH)
	CustomField findOneByNameAndLanguage(String name, String language);

	@EntityGraph(value = CustomField.DEEP_GRAPH_NAME, type = EntityGraph.EntityGraphType.FETCH)
	CustomField findOneByCodeAndLanguage(String code, String language);

	@EntityGraph(value = CustomField.DEEP_GRAPH_NAME, type = EntityGraph.EntityGraphType.FETCH)
	List<CustomField> findAllByLanguage(String language);

	@Query("select coalesce(max(idx), 0) from CustomField customField where customField.language = :language ")
	int findMaxIdxByLanguage(@Param("language")String language);

	@Query("select count(customField.idx) from CustomField customField where customField.language = :language ")
	int count(@Param("language") String language);

	@Modifying
	@Query("update CustomField customField set customField.idx = null where customField.language = :language ")
	void updateNullByLanguage(@Param("language") String language);
}
