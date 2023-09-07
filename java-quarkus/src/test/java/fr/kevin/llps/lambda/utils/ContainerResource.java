package fr.kevin.llps.lambda.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.kevin.llps.lambda.service.DatabaseSecret;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import java.util.Map;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SECRETSMANAGER;

public class ContainerResource implements QuarkusTestResourceLifecycleManager {

    public static final MySQLContainer<?> MYSQL_CONTAINER;
    public static final LocalStackContainer LOCAL_STACK_CONTAINER;

    static {
        MYSQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse("mysql:5.7"))
                .withDatabaseName("conferences")
                .withInitScript("init_db.sql");

        DockerImageName localstackImage = DockerImageName.parse("localstack/localstack:1.4.0");

        LOCAL_STACK_CONTAINER = new LocalStackContainer(localstackImage).withServices(SECRETSMANAGER);
    }

    @Override
    public Map<String, String> start() {
        MYSQL_CONTAINER.start();
        LOCAL_STACK_CONTAINER.start();

        initSecretManager();

        System.setProperty("aws.accessKeyId", LOCAL_STACK_CONTAINER.getAccessKey());
        System.setProperty("aws.secretAccessKey", LOCAL_STACK_CONTAINER.getSecretKey());
        return Map.of(
                "secret.name", "secretName",
                "aws.region", LOCAL_STACK_CONTAINER.getRegion(),
                "secret.manager.url.override", LOCAL_STACK_CONTAINER.getEndpointOverride(SECRETSMANAGER).toString(),
                "aws.accessKeyId", LOCAL_STACK_CONTAINER.getAccessKey(),
                "aws.secretAccessKey", LOCAL_STACK_CONTAINER.getSecretKey());
    }

    @Override
    public void stop() {
        MYSQL_CONTAINER.stop();
        LOCAL_STACK_CONTAINER.stop();
    }

    private void initSecretManager() {
        SecretsManagerClient secretsManagerClient = SecretsManagerClient
                .builder()
                .endpointOverride(LOCAL_STACK_CONTAINER.getEndpointOverride(SECRETSMANAGER))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        LOCAL_STACK_CONTAINER.getAccessKey(), LOCAL_STACK_CONTAINER.getSecretKey())))
                .region(Region.of(LOCAL_STACK_CONTAINER.getRegion()))
                .build();

        DatabaseSecret dbSecret = new DatabaseSecret(
                MYSQL_CONTAINER.getUsername(),
                MYSQL_CONTAINER.getPassword(),
                "mysql",
                "conferences",
                MYSQL_CONTAINER.getHost(),
                MYSQL_CONTAINER.getFirstMappedPort().toString());

        try {
            String jsonSecret = new ObjectMapper().writeValueAsString(dbSecret);
            secretsManagerClient.createSecret(c -> c.name("secretName").secretString(jsonSecret));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

}
