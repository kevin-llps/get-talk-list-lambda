package fr.kevin.llps.lambda.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecretManagerServiceTest {

    @Test
    void shouldGetSecret() throws JsonProcessingException {
        SecretManagerService secretManagerService = new SecretManagerService();

        DatabaseSecret expectedDatabaseSecret = new DatabaseSecret("user", "password", "mysql", "database", "host", "port");

        String secretName = "secretName";
        String expectedSecret = "{\"host\": \"host\", \"username\": \"user\", \"password\":\"password\", \"port\": \"port\", \"dbname\": \"database\", \"engine\": \"mysql\" }";

        SecretsManagerClient secretsManagerClient = mock(SecretsManagerClient.class);

        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .versionStage("AWSCURRENT")
                .build();

        GetSecretValueResponse getSecretValueResponse = GetSecretValueResponse.builder()
                .secretString(expectedSecret)
                .build();

        when(secretsManagerClient.getSecretValue(getSecretValueRequest)).thenReturn(getSecretValueResponse);

        DatabaseSecret databaseSecret = secretManagerService.getSecret(secretsManagerClient, secretName);

        assertThat(databaseSecret).isEqualTo(expectedDatabaseSecret);
    }

}
