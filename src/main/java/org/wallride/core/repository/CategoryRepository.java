package org.wallride.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.core.domain.Category;

import javax.persistence.LockModeType;
import java.util.Collection;
import java.util.List;

@Repository
@Transactional
public interface CategoryRepository extends JpaRepository<Category, Long> {
	
	static final String DEFAULT_SELECT_QUERY = 
			"from Category category " +
			"left join fetch category.parent parent " +
			"left join fetch category.children children ";

	@Query("select category.id from Category category order by category.id")
	List<Long> findId();

	@Query(DEFAULT_SELECT_QUERY + "where category.id in (:ids) ")
	List<Category> findByIdIn(@Param("ids") Collection<Long> ids);

	@Query(DEFAULT_SELECT_QUERY + "where category.language = :language order by category.lft")
	List<Category> findByLanguage(@Param("language") String language);

	@Query(
			DEFAULT_SELECT_QUERY +
			"where category.language = :language " +
			"and category.id in (select category.id from Article article inner join article.categories category where article.status = 'PUBLISHED') " +
			"order by category.lft")
	List<Category> findByLanguageAndHasArticle(@Param("language") String language);

	@Query(DEFAULT_SELECT_QUERY + "where category.id = :id and category.language = :language ")
	Category findById(@Param("id") Long id, @Param("language") String language);
	
	@Query(DEFAULT_SELECT_QUERY + "where category.id = :id and category.language = :language ")
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Category findByIdForUpdate(@Param("id") Long id, @Param("language") String language);

	@Query(DEFAULT_SELECT_QUERY + "where category.code = :code and category.language = :language ")
	Category findByCode(@Param("code") String code, @Param("language") String language);

	@Query("select coalesce(max(rgt), 0) from Category ")
	int findMaxRgt();

	@Modifying
	@Query("update Category set lft = lft + 2 where lft > :rgt ")
	void unshiftLft(@Param("rgt") int rgt);

	@Modifying
	@Query("update Category set rgt = rgt + 2 where rgt >= :rgt ")
	void unshiftRgt(@Param("rgt") int rgt);

	@Modifying
	@Query("update Category set rgt = rgt - 1, lft = lft - 1 where lft between :lft and :rgt ")
	void shiftLftRgt(@Param("lft") int lft, @Param("rgt") int rgt);

	@Modifying
	@Query("update Category set lft = lft - 2 where lft > :rgt ")
	void shiftLft(@Param("rgt") int rgt);

	@Modifying
	@Query("update Category set rgt = rgt - 2 where rgt > :rgt ")
	void shiftRgt(@Param("rgt") int rgt);

//	@Modifying
//	@Query(
//			"update Category set " +
//			"lft = case when lft > :rgt then lft+2 else lft end, " +
//			"rgt = case when rgt >= :rgt then rgt+2 else rgt end " +
//			"where rgt >= :rgt")
//	void updateLftRgt(@Param("rgt") int rgt);

//	@Query("select coalesce(max(category.sort), 0) from Category category where category.depth <= :depth and category.language = :language ")
//	int findMaxSortByDepth(@Param("depth") int depth, @Param("language") String language);
//
//	@Modifying
//	@Query("update Category set sort = sort + 1 where sort >= :sort and language = :language ")
//	void incrementSortBySortGreaterThanEqual(@Param("sort") int sort, @Param("language") String language);
//
//	@Modifying
//	@Query("update Category set sort = sort - 1 where sort > :sort and language = :language ")
//	void decrementSortBySortGreaterThan(@Param("sort") int sort, @Param("language") String language);
}
