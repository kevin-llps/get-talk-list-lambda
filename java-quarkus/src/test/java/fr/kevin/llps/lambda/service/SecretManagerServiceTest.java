package fr.kevin.llps.lambda.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecretManagerServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SecretsManagerClient secretsManagerClient;

    @InjectMocks
    private SecretManagerService secretManagerService;

    @Test
    void shouldGetSecret() throws JsonProcessingException {
        DatabaseSecret expectedDatabaseSecret = new DatabaseSecret("tata", "password", "toto", "base", "localhost", "82");

        String secretName = "secretName";
        String expectedSecret = "secret";

        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .versionStage("AWSCURRENT")
                .build();

        GetSecretValueResponse getSecretValueResponse = GetSecretValueResponse.builder()
                .secretString(expectedSecret)
                .build();

        when(secretsManagerClient.getSecretValue(getSecretValueRequest)).thenReturn(getSecretValueResponse);
        when(objectMapper.readValue(expectedSecret, DatabaseSecret.class)).thenReturn(expectedDatabaseSecret);

        DatabaseSecret databaseSecret = secretManagerService.getSecret(secretName);

        assertThat(databaseSecret).isEqualTo(expectedDatabaseSecret);
    }

}
