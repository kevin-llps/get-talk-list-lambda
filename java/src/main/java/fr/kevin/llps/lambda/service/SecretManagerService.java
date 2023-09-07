package fr.kevin.llps.lambda.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import static fr.kevin.llps.lambda.configuration.Configuration.objectMapper;

public class SecretManagerService {

    private static final String VERSION_STAGE = "AWSCURRENT";

    public DatabaseSecret getSecret(SecretsManagerClient secretsManagerClient, String secretName) throws JsonProcessingException {
        ObjectMapper objectMapper = objectMapper();

        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .versionStage(VERSION_STAGE)
                .build();

        GetSecretValueResponse getSecretValueResponse = secretsManagerClient.getSecretValue(getSecretValueRequest);

        return objectMapper.readValue(getSecretValueResponse.secretString(), DatabaseSecret.class);
    }

}
