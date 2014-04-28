package org.wallride.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.wallride.core.domain.User;

public interface UserRepositoryCustom {

	Page<User> findByFullTextSearchTerm(UserFullTextSearchTerm term, Pageable pageable);
}
