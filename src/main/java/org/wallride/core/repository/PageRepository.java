package org.wallride.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.core.domain.Page;
import org.wallride.core.domain.Post;

import javax.persistence.LockModeType;
import java.util.Collection;
import java.util.List;

@Repository
@Transactional
public interface PageRepository extends JpaRepository<Page, Long>, PageRepositoryCustom {
	
	static final String DEFAULT_SELECT_QUERY = 
			"from Page page " + 
			"left join fetch page.cover cover "+
			"left join fetch page.author author " +
			"left join fetch page.parent parent " +
			"left join fetch page.children children ";

	@Query("select page.id from Page page order by page.id")
	List<Long> findId();
	
	@Query(DEFAULT_SELECT_QUERY + "where page.id in (:ids) ")
	List<Page> findByIdIn(@Param("ids") Collection<Long> ids);
	
	@Query(DEFAULT_SELECT_QUERY + "where page.id = :id and page.language = :language ")
	Page findById(@Param("id") Long id, @Param("language") String language);
	
	@Query(DEFAULT_SELECT_QUERY + "where page.language = :language order by page.lft")
	List<Page> findByLanguage(@Param("language") String language);

	@Query(DEFAULT_SELECT_QUERY + "where page.language = :language and page.status = :status order by page.lft")
	List<Page> findByLanguageAndStatus(@Param("language") String language, @Param("status") Post.Status status);

	@Query(DEFAULT_SELECT_QUERY + "where page.id = :id and page.language = :language ")
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Page findByIdForUpdate(@Param("id")Long id, @Param("language") String language);
	
	@Query(DEFAULT_SELECT_QUERY + "where page.code = :code and page.language = :language ")
	Page findByCode(@Param("code") String code, @Param("language") String language);

	@Query(DEFAULT_SELECT_QUERY + "where page.drafted = :drafted and page.id = (select max(page.id) from page where page.drafted = :drafted) ")
	Page findDraft(@Param("drafted") Page drafted);

	@Query("select count(page.id) from Page page where page.language = :language ")
	long count(@Param("language") String language);

	@Query("select count(page.id) from Page page where page.status = :status and page.language = :language ")
	long countByStatus(@Param("status") Post.Status status, @Param("language") String language);

	@Query("select coalesce(max(rgt), 0) from Page ")
	int findMaxRgt();

	@Modifying
	@Query("update Page set lft = lft + 2 where lft > :rgt ")
	void unshiftLft(@Param("rgt") int rgt);

	@Modifying
	@Query("update Page set rgt = rgt + 2 where rgt >= :rgt ")
	void unshiftRgt(@Param("rgt") int rgt);

	@Modifying
	@Query("update Page set rgt = rgt - 1, lft = lft - 1 where lft between :lft and :rgt ")
	void shiftLftRgt(@Param("lft") int lft, @Param("rgt") int rgt);

	@Modifying
	@Query("update Page set lft = lft - 2 where lft > :rgt ")
	void shiftLft(@Param("rgt") int rgt);

	@Modifying
	@Query("update Page set rgt = rgt - 2 where rgt > :rgt ")
	void shiftRgt(@Param("rgt") int rgt);

	@Modifying
	@Query("delete from Page page where page.drafted = :drafted ")
	void deleteByDrafted(@Param("drafted") Page dradted);
}
