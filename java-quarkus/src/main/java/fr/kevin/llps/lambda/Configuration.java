package fr.kevin.llps.lambda;

import com.amazonaws.xray.interceptors.TracingInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import java.net.URI;

import static com.fasterxml.jackson.databind.DeserializationFeature.*;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

@Singleton
public class Configuration {

    @Produces
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.disable(FAIL_ON_EMPTY_BEANS);
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.findAndRegisterModules();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(FAIL_ON_NULL_FOR_PRIMITIVES, true);

        return objectMapper;
    }

    @Produces
    public SecretsManagerClient secretsManagerClient(@ConfigProperty(name = "aws.region") String region,
                                                     @ConfigProperty(name = "secret.manager.url.override") String url) {
        return SecretsManagerClient.builder()
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .region(Region.of(region))
                .endpointOverride(URI.create(url))
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .addExecutionInterceptor(new TracingInterceptor())
                        .build())
                .build();
    }

}
