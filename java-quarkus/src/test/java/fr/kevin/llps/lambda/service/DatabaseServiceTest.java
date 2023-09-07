package fr.kevin.llps.lambda.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseServiceTest {

    @Mock
    private Driver driver;

    @Mock
    private SecretManagerService secretManagerService;

    @InjectMocks
    private DatabaseService databaseService;

    @BeforeEach
    void setUp() throws SQLException {
        DriverManager.registerDriver(driver);
    }

    @AfterEach
    void tearDown() throws SQLException {
        DriverManager.deregisterDriver(driver);
    }

    @Test
    void shouldOpenConnection() throws SQLException, JsonProcessingException {
        DatabaseSecret dbSecret = new DatabaseSecret("tata", "password", "toto", "base", "localhost", "82");

        String secretName = "secret";

        String url = "jdbc:" + dbSecret.engine() + "://" + dbSecret.host() + ":" + dbSecret.port() + "/" +
                dbSecret.dbname();
        Properties info = new Properties();
        info.put("user", dbSecret.username());
        info.put("password", dbSecret.password());

        when(secretManagerService.getSecret(secretName)).thenReturn(dbSecret);

        Connection expectedConnection = mock(Connection.class);
        when(driver.connect(url, info)).thenReturn(expectedConnection);

        Connection actualConnection = databaseService.openConnection(secretName);

        assertThat(actualConnection).isEqualTo(expectedConnection);

        verify(secretManagerService).getSecret(secretName);
        verify(driver).connect(url, info);
        verify(expectedConnection).setAutoCommit(false);
    }

    @Test
    void openConnection_shouldThrowLambdaException_whenSqlExceptionIsThrown() throws SQLException, JsonProcessingException {
        DatabaseSecret dbSecret = new DatabaseSecret("tata", "password", "toto", "base", "localhost", "82");

        String secretName = "secret";

        Properties info = new Properties();
        info.put("user", dbSecret.username());
        info.put("password", dbSecret.password());
        String url = "jdbc:" + dbSecret.engine() + "://" + dbSecret.host() + ":" + dbSecret.port() + "/" +
                dbSecret.dbname();

        when(secretManagerService.getSecret(secretName)).thenReturn(dbSecret);

        Connection connection = mock(Connection.class);
        when(driver.connect(url, info)).thenReturn(connection);

        doThrow(new SQLException()).when(connection).setAutoCommit(false);

        doThrow(new SQLException()).when(connection).close();

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> databaseService.openConnection(secretName));

        verify(secretManagerService).getSecret(secretName);

        verify(driver).connect(url, info);
        verify(connection).setAutoCommit(false);
        verify(connection).close();
    }

    @Test
    void openConnection_shouldNotCloseConnection_whenGetConnectionThrowException() throws SQLException, JsonProcessingException {
        DatabaseSecret dbSecret = new DatabaseSecret("tata", "password", "toto", "base", "localhost", "82");

        String secretName = "secret";

        Properties info = new Properties();
        info.put("user", dbSecret.username());
        info.put("password", dbSecret.password());
        String url = "jdbc:" + dbSecret.engine() + "://" + dbSecret.host() + ":" + dbSecret.port() + "/" +
                dbSecret.dbname();

        when(secretManagerService.getSecret(secretName)).thenReturn(dbSecret);

        when(driver.connect(url, info)).thenThrow(new SQLException());

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> databaseService.openConnection(secretName));

        verify(secretManagerService).getSecret(secretName);

        verify(driver).connect(url, info);
    }

}
