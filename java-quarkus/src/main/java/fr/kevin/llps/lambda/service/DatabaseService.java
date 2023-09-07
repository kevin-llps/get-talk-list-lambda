package fr.kevin.llps.lambda.service;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Subsegment;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class DatabaseService {

    private final SecretManagerService secretManagerService;

    public Connection openConnection(String dbSecretName) throws JsonProcessingException {
        DatabaseSecret databaseSecret = secretManagerService.getSecret(dbSecretName);
        Connection connection = null;

        Subsegment databaseConnectionSubsegment = AWSXRay.beginSubsegment("Database connection");
        try {
            String url = "jdbc:" + databaseSecret.engine() + "://" + databaseSecret.host() + ":" + databaseSecret.port() + "/" +
                    databaseSecret.dbname();

            connection = DriverManager.getConnection(url, databaseSecret.username(), databaseSecret.password());
            connection.setAutoCommit(false);

            return connection;
        } catch (SQLException e) {
            databaseConnectionSubsegment.addException(e);
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e2) {
                    log.error("Impossible to close connection", e2);
                    databaseConnectionSubsegment.addException(e2);
                }
            }

            throw new RuntimeException(e);
        } finally {
            AWSXRay.endSubsegment(databaseConnectionSubsegment);
        }
    }


}
