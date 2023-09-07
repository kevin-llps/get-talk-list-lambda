package fr.kevin.llps.lambda;

import fr.kevin.llps.lambda.utils.ContainerResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static fr.kevin.llps.lambda.utils.TestUtils.readFilename;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@QuarkusTestResource(ContainerResource.class)
public class LambdaHandlerTest {

    private static final String APPLICATION_JSON = "application/json";

    @Test
    public void shouldRunLambda() {
        Response response = given()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(readFilename("/json/request.json"))
                .when()
                .post();

        assertThat(response.getBody().asString()).isEqualToIgnoringWhitespace(readFilename("/json/response.json"));
    }

}
