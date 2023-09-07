package fr.kevin.llps.lambda.service;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record DatabaseSecret(String username,
                             String password,
                             String engine,
                             String dbname,
                             String host,
                             String port) {

}
