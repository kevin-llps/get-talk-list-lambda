package fr.kevin.llps.lambda.service;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Subsegment;
import com.fasterxml.jackson.core.JsonProcessingException;
import fr.kevin.llps.lambda.domain.Talk;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TalkService {

    private static final String SELECT_TALKS = "SELECT t.date, t.title, " +
            "s.username, t.description " +
            "FROM talk t JOIN speaker s " +
            "ON t.speaker_id = s.id " +
            "ORDER BY t.date,t.title;";

    private final DatabaseService databaseService;

    public TalkService(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public TalkService() {
        this(new DatabaseService());
    }

    public List<Talk> getTalks(SecretsManagerClient secretsManagerClient, String secretName) {
        Subsegment getTalksSubsegment = AWSXRay.beginSubsegment("Get Talks");

        try (Connection connection = databaseService.openConnection(secretsManagerClient, secretName);
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_TALKS);
             Subsegment executionSqlQuerySubsegment = AWSXRay.beginSubsegment("Execute : Get Talks SQL query");
             ResultSet resultSet = preparedStatement.executeQuery()) {

            AWSXRay.endSubsegment(executionSqlQuerySubsegment);

            Subsegment mappingSubSegment = AWSXRay.beginSubsegment("Map ResultSet to Talk list");

            List<Talk> talks = mapToTalkList(resultSet);

            AWSXRay.endSubsegment(mappingSubSegment);

            return talks;
        } catch (SQLException | JsonProcessingException e) {
            getTalksSubsegment.addException(e);
            throw new RuntimeException(e);
        } finally {
            AWSXRay.endSubsegment(getTalksSubsegment);
        }
    }

    private List<Talk> mapToTalkList(ResultSet resultSet) throws SQLException {
        List<Talk> talks = new ArrayList<>();

        while (resultSet.next()) {
            talks.add(mapToTalk(resultSet));
        }

        return talks;
    }

    private Talk mapToTalk(ResultSet resultSet) throws SQLException {
        LocalDateTime date = LocalDateTime.parse(resultSet.getString(1), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String title = resultSet.getString(2);
        String speaker = resultSet.getString(3);
        String description = resultSet.getString(4);

        return new Talk(title, date, speaker, description);
    }

}
