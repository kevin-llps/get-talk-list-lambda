package fr.kevin.llps.lambda.service;

public record DatabaseSecret(String username,
                             String password,
                             String engine,
                             String dbname,
                             String host,
                             String port) {

}
