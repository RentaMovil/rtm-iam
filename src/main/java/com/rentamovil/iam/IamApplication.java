package com.rentamovil.iam;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IamApplication {

	public static void main(String[] args) {
		// Todas las fechas en UTC (modeling-conventions.md): la base es compartida entre servicios,
		// y así LocalDateTime.now() da la misma hora sin importar dónde corra el servicio.
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(IamApplication.class, args);
	}

}
