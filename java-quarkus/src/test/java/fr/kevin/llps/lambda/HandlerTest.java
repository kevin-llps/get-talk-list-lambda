package fr.kevin.llps.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.kevin.llps.lambda.domain.Talk;
import fr.kevin.llps.lambda.service.TalkService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private TalkService talkService;

    @InjectMocks
    private Handler handler;

    @Test
    void shouldHandleRequest() throws JsonProcessingException {
        String expectedTalks = "talks";

        Talk talk1 = new Talk("conf1", LocalDateTime.of(2023, 2, 5, 19, 0, 0), "jean.dupont", "Description conf1");
        Talk talk2 = new Talk("conf2", LocalDateTime.of(2023, 6, 28, 19, 0, 0), "alice.dupont", "Description conf2");

        List<Talk> talks = List.of(talk1, talk2);

        APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent = mock(APIGatewayProxyRequestEvent.class);

        when(talkService.getTalks()).thenReturn(talks);
        when(objectMapper.writeValueAsString(talks)).thenReturn(expectedTalks);

        APIGatewayProxyResponseEvent apiGatewayProxyResponseEvent = handler.handleRequest(apiGatewayProxyRequestEvent, null);

        assertThat(apiGatewayProxyResponseEvent.getStatusCode()).isEqualTo(200);
        assertThat(apiGatewayProxyResponseEvent.getIsBase64Encoded()).isFalse();
        assertThat(apiGatewayProxyResponseEvent.getHeaders()).containsExactly(Map.entry("Content-Type", "application/json"));
        assertThat(apiGatewayProxyResponseEvent.getBody()).isEqualTo(expectedTalks);

        verifyNoMoreInteractions(talkService, objectMapper);
    }

}
