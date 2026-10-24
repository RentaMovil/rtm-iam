package com.rentamovil.iam.infrastructure.web.auth;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.rentamovil.iam.domain.entity.AuthenticatedUser;
import com.rentamovil.common.exception.InvalidTokenException;
import com.rentamovil.iam.domain.port.out.TokenProviderPort;

import lombok.RequiredArgsConstructor;

// Lee el access token del header Authorization, lo valida y entrega el AuthenticatedUser
// a los parámetros marcados con @CurrentUser. Si falta o no es válido responde 401.
@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String BEARER = "Bearer ";

    private final TokenProviderPort tokenProviderPort;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType().equals(AuthenticatedUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String authorization = webRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER)) {
            throw new InvalidTokenException("Falta el token de acceso");
        }
        return tokenProviderPort.validateAccessToken(authorization.substring(BEARER.length()));
    }
}
