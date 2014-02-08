package org.wallride.web.admin;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.wallride.core.support.AuthorizedUser;

public class AuthorizedUserMethodArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return AuthorizedUser.class.equals(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(
			MethodParameter parameter,
			ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest,
			WebDataBinderFactory binderFactory)
			throws Exception {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		AuthorizedUser authorizedStaff = null;
		if (auth != null && auth.getPrincipal() instanceof AuthorizedUser) {
			authorizedStaff = (AuthorizedUser) auth.getPrincipal();
		}
		return authorizedStaff;
	}
}
