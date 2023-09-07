package fr.kevin.llps.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Subsegment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.kevin.llps.lambda.domain.Talk;
import fr.kevin.llps.lambda.service.TalkService;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import java.util.List;
import java.util.Map;

import static fr.kevin.llps.lambda.configuration.Configuration.objectMapper;
import static fr.kevin.llps.lambda.configuration.Configuration.secretsManagerClient;
import static fr.kevin.llps.lambda.utils.Utils.getEnvironmentVarValue;
import static java.net.HttpURLConnection.HTTP_OK;
import static software.amazon.awssdk.http.Header.CONTENT_TYPE;

@Slf4j
public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String APPLICATION_JSON = "application/json";

    private final TalkService talkService;

    public Handler() {
        this.talkService = new TalkService();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {
        Subsegment handlerSubsegment = AWSXRay.beginSubsegment("Handler");

        ObjectMapper objectMapper = objectMapper();

        log.info("event = {}", apiGatewayProxyRequestEvent);
        log.info("context = {}", context);

        String region = getEnvironmentVarValue("AWS_REGION");
        String secretName = getEnvironmentVarValue("SECRET_NAME");
        String secretsManagerUrl = getEnvironmentVarValue("SECRET_MANAGER_URL_OVERRIDE");

        SecretsManagerClient secretsManagerClient = secretsManagerClient(region, secretsManagerUrl);

        List<Talk> talks = talkService.getTalks(secretsManagerClient, secretName);

        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HTTP_OK)
                    .withIsBase64Encoded(false)
                    .withHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON))
                    .withBody(objectMapper.writeValueAsString(talks));
        } catch (JsonProcessingException e) {
            handlerSubsegment.addException(e);
            throw new RuntimeException(e);
        } finally {
            AWSXRay.endSubsegment(handlerSubsegment);
        }
    }
}
