package org.wallride.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.wallride.core.domain.User;
import org.wallride.core.service.UserSearchRequest;

public interface UserRepositoryCustom {

	Page<User> search(UserSearchRequest request, Pageable pageable);
}
