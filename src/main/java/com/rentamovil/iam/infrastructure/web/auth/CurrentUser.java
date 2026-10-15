package com.rentamovil.iam.infrastructure.web.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Se pone en un parámetro AuthenticatedUser del controller: el endpoint exige
// "Authorization: Bearer <token>" y recibe quién está llamando.
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}
