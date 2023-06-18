package com.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

public class StateFullMockTest {


    WireMockServer wireMockServer = new WireMockServer(wireMockConfig().port(8116));

    @Before
    public void startUp() {
        if (!wireMockServer.isRunning()) {
            wireMockServer.start();
        }
        stubForAddingEmployeeToDB();
    }

    public void stubForAddingEmployeeToDB() {
        wireMockServer.stubFor(get(urlPathEqualTo("/employees"))
                .inScenario("Adding a new employee to DB")
                .whenScenarioStateIs(Scenario.STARTED)
                .withHeader("content-type", containing("application/json"))
                .willReturn(ok().withHeader("content-type", "application/json")
                        .withBody("{\n" +
                                "\t\"employees\": [{\n" +
                                "\t\t\t\"id\": 1,\n" +
                                "\t\t\t\"name\": \"John Doe\",\n" +
                                "\t\t\t\"age\": 30,\n" +
                                "\t\t\t\"position\": \"Manager\"\n" +
                                "\t\t},\n" +
                                "\t\t{\n" +
                                "\t\t\t\"id\": 2,\n" +
                                "\t\t\t\"name\": \"Jane Smith\",\n" +
                                "\t\t\t\"age\": 25,\n" +
                                "\t\t\t\"position\": \"Developer\"\n" +
                                "\t\t}\n" +
                                "\t]\n" +
                                "}"))
                .willSetStateTo("Adding the employee"));

        wireMockServer.stubFor(post(urlPathEqualTo("/employee"))
                .inScenario("Adding a new employee to DB")
                .whenScenarioStateIs("Adding the employee")
                .withHeader("content-type", containing("application/json"))
                .withRequestBody(matchingJsonPath("id"))
                .willReturn(created().withHeader("content-type", "application/json"))
                .willSetStateTo("Employee Added to DB successfully"));


        wireMockServer.stubFor(get(urlPathEqualTo("/employees"))
                .inScenario("Adding a new employee to DB")
                .whenScenarioStateIs("Employee Added to DB successfully")
                .withHeader("content-type", containing("application/json"))
                .willReturn(ok().withHeader("content-type", "application/json")
                        .withBody("{\n" +
                                "\t\"employees\": [{\n" +
                                "\t\t\t\"id\": 1,\n" +
                                "\t\t\t\"name\": \"John Doe\",\n" +
                                "\t\t\t\"age\": 30,\n" +
                                "\t\t\t\"position\": \"Manager\"\n" +
                                "\t\t},\n" +
                                "\t\t{\n" +
                                "\t\t\t\"id\": 2,\n" +
                                "\t\t\t\"name\": \"Jane Smith\",\n" +
                                "\t\t\t\"age\": 25,\n" +
                                "\t\t\t\"position\": \"Developer\"\n" +
                                "\t\t},\n" +
                                "\t\t{\n" +
                                "\t\t\t\"id\": 3,\n" +
                                "\t\t\t\"name\": \"Alex Johnson\",\n" +
                                "\t\t\t\"age\": 35,\n" +
                                "\t\t\t\"position\": \"Analyst\"\n" +
                                "\t\t}\n" +
                                "\t]\n" +
                                "}")));

    }

    @Test
    public void testAddNewEmployeeService() {
        baseURI = "http://localhost:8116";

        // Checking the existing employees in the DB using get employees service
        Response responseBeforeAddingEmployee = given()
                .header("content-type", "application/json")
                .get("/employees");

        assertEquals("status code is not as expected", 200, responseBeforeAddingEmployee.getStatusCode());
        ArrayList employeeList = responseBeforeAddingEmployee.jsonPath().get("employees");
        assertEquals("employee count is not matching before we add a new employee", 2, employeeList.size());

        // Adding a new employee to DB using create employee service (POST) Call
        Response addEmployeeResponse = given().header("content-type", "application/json")
                .body("{\n" +
                        "\t\t\t\"id\": 3,\n" +
                        "\t\t\t\"name\": \"Alex Johnson\",\n" +
                        "\t\t\t\"age\": 35,\n" +
                        "\t\t\t\"position\": \"Analyst\"\n" +
                        "\t\t}")
                .post("/employee");

        assertEquals("status code is not as expected", 201, addEmployeeResponse.getStatusCode());

        // Checking the employee data to see if the new employee is added to DB using get employees service
        Response responseAfterAddingEmployee = given()
                .header("content-type", "application/json")
                .get("/employees");

        assertEquals("status code is not as expected", 200, responseBeforeAddingEmployee.getStatusCode());
        ArrayList employeeUpdatedList = responseAfterAddingEmployee.jsonPath().get("employees");
        assertEquals("employee count is not matching before we add a new employee", 3, employeeUpdatedList.size());


    }

    @After
    public void tearDown() {
        if (wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }
}
