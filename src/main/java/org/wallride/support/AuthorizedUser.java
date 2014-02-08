package org.wallride.support;

import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.wallride.domain.User;

import java.util.Collection;

@SuppressWarnings("serial")
public class AuthorizedUser extends User implements UserDetails {
	
	private final Collection<GrantedAuthority> authorities;

	public AuthorizedUser(User user) {
		BeanUtils.copyProperties(user, this);
		authorities = AuthorityUtils.createAuthorityList("ROLE_USER");
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getUsername() {
		return getLoginId();
	}

	@Override
	public String getPassword() {
		return getLoginPassword();
	}
	
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
