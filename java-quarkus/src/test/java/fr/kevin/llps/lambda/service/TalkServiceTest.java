package fr.kevin.llps.lambda.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.kevin.llps.lambda.domain.Talk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TalkServiceTest {

    private static final String SECRET_NAME = "secretName";

    private static final String SELECT_TALKS = "SELECT t.date, t.title, " +
            "s.username, t.description " +
            "FROM talk t JOIN speaker s " +
            "ON t.speaker_id = s.id " +
            "ORDER BY t.date,t.title;";

    @Mock
    private DatabaseService databaseService;

    private TalkService talkService;

    @BeforeEach
    void setUp() {
        talkService = new TalkService(databaseService, SECRET_NAME);
    }

    @Test
    void shouldGetTalks() throws JsonProcessingException, SQLException {
        Talk talk1 = new Talk("conf1", LocalDateTime.of(2023, 2, 5, 19, 0, 0), "jean.dupont", "Description conf1");
        Talk talk2 = new Talk("conf2", LocalDateTime.of(2023, 6, 28, 19, 0, 0), "alice.dupont", "Description conf2");

        List<Talk> expectedTalks = List.of(talk1, talk2);

        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(databaseService.openConnection(SECRET_NAME)).thenReturn(connection);
        when(connection.prepareStatement(SELECT_TALKS)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(resultSet.getString(1)).thenReturn("2023-02-05 19:00:00").thenReturn("2023-06-28 19:00:00");
        when(resultSet.getString(2)).thenReturn("conf1").thenReturn("conf2");
        when(resultSet.getString(3)).thenReturn("jean.dupont").thenReturn("alice.dupont");
        when(resultSet.getString(4)).thenReturn("Description conf1").thenReturn("Description conf2");

        List<Talk> talks = talkService.getTalks();

        assertThat(talks).containsExactlyInAnyOrderElementsOf(expectedTalks);

        verifyNoMoreInteractions(databaseService);
    }

}
