package com.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

public class PositiveStubTest {

    WireMockServer wireMockServer = new WireMockServer(wireMockConfig().port(8116));

    @Before
    public void setUp() {
        wireMockServer.start();
        wireMockServer.stubFor(get(urlPathEqualTo("/employee"))
                .withHeader("content-type", containing("application/json"))
                .withQueryParam("id", matching("[0-9]*"))
                .willReturn(ok()
                        .withHeader("content-type", "application/json")
                        .withBody("{\n" +
                                "  \"employees\": [\n" +
                                "    {\n" +
                                "      \"id\": 1,\n" +
                                "      \"name\": \"John Doe\",\n" +
                                "      \"age\": 30,\n" +
                                "      \"position\": \"Manager\"\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"id\": 2,\n" +
                                "      \"name\": \"Jane Smith\",\n" +
                                "      \"age\": 25,\n" +
                                "      \"position\": \"Developer\"\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"id\": 3,\n" +
                                "      \"name\": \"Alex Johnson\",\n" +
                                "      \"age\": 35,\n" +
                                "      \"position\": \"Analyst\"\n" +
                                "    }\n" +
                                "  ]\n" +
                                "}")));
    }

    @After
    public void tearDown() {
        if (wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @Test
    public void testGetEmployeeService() {
        baseURI = "http://localhost:8116";
        Response response = given().queryParam("id", 2)
                .header("content-type", "application/json")
                .get("/employee");

        assertEquals("status code is not as expected", 200, response.getStatusCode());
        JsonPath jsonPath = response.jsonPath();
        assertEquals("employee name doesn't exists in the response", "John Doe", jsonPath.get("employees[0].name"));

    }

}
