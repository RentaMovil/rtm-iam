package com.rentamovil.iam.domain.port.out;

import com.rentamovil.iam.domain.entity.Session;

public interface SessionRepositoryPort {

    Session save(Session session);
}
