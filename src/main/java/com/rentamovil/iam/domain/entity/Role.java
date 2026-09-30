package com.rentamovil.iam.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    // Nombres de los 3 roles fijos (iam.role, sembrados por rtm-iam-db)
    public static final String CLIENT = "CLIENT";
    public static final String ADMIN = "ADMIN";
    public static final String SUPER_ADMIN = "SUPER_ADMIN";

    private Integer id;
    private String name;
    private String description;
}
