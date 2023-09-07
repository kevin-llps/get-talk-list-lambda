package fr.kevin.llps.lambda.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Singleton
@RequiredArgsConstructor
public class SecretManagerService {

    private static final String VERSION_STAGE = "AWSCURRENT";

    private final ObjectMapper objectMapper;
    private final SecretsManagerClient client;

    public DatabaseSecret getSecret(String secretName) throws JsonProcessingException {
        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .versionStage(VERSION_STAGE)
                .build();

        GetSecretValueResponse getSecretValueResponse = client.getSecretValue(getSecretValueRequest);

        return objectMapper.readValue(getSecretValueResponse.secretString(), DatabaseSecret.class);
    }

}
