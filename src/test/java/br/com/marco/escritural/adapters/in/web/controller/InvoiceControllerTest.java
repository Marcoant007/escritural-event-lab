package br.com.marco.escritural.adapters.in.web.controller;

import br.com.marco.escritural.adapters.in.web.dto.request.IssueInvoiceRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class InvoiceControllerTest {

    @Test
    void shouldIssueValidInvoice() {
        given()
                .contentType(ContentType.JSON)
                .body(validRequestBuilder(uniqueNumber()).build())
        .when()
                .post("/invoices")
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("status", equalTo("ISSUED"));
    }

    @Test
    void shouldReturn400WhenAmountIsInvalid() {
        given()
                .contentType(ContentType.JSON)
                .body(validRequestBuilder(uniqueNumber()).amount(BigDecimal.ZERO).build())
        .when()
                .post("/invoices")
        .then()
                .statusCode(400);
    }

    @Test
    void shouldReturn400WhenDueDateBeforeIssueDate() {
        given()
                .contentType(ContentType.JSON)
                .body(validRequestBuilder(uniqueNumber())
                        .dueDate(LocalDate.now().minusDays(1))
                        .build())
        .when()
                .post("/invoices")
        .then()
                .statusCode(400);
    }

    @Test
    void shouldReturn409WhenNumberIsDuplicated() {
        String number = uniqueNumber();
        issue(number);

        given()
                .contentType(ContentType.JSON)
                .body(validRequestBuilder(number).build())
        .when()
                .post("/invoices")
        .then()
                .statusCode(409);
    }

    @Test
    void shouldFindInvoiceById() {
        String number = uniqueNumber();
        String id = issue(number).jsonPath().getString("id");

        given()
        .when()
                .get("/invoices/{id}", id)
        .then()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("number", equalTo(number));
    }

    @Test
    void shouldReturn404WhenInvoiceDoesNotExist() {
        given()
        .when()
                .get("/invoices/{id}", UUID.randomUUID())
        .then()
                .statusCode(404);
    }

    @Test
    void shouldPresentIssuedInvoice() {
        String id = issue(uniqueNumber()).jsonPath().getString("id");

        given()
        .when()
                .post("/invoices/{id}/presentation", id)
        .then()
                .statusCode(200)
                .body("status", equalTo("PRESENTED"));
    }

    @Test
    void shouldAcceptPresentedInvoice() {
        String id = presentedInvoiceId(uniqueNumber());

        given()
        .when()
                .post("/invoices/{id}/acceptance", id)
        .then()
                .statusCode(200)
                .body("status", equalTo("ACCEPTED"));
    }

    @Test
    void shouldRejectPresentedInvoice() {
        String id = presentedInvoiceId(uniqueNumber());

        given()
        .when()
                .post("/invoices/{id}/rejection", id)
        .then()
                .statusCode(200)
                .body("status", equalTo("REJECTED"));
    }

    @Test
    void shouldReturn409WhenAcceptingInvoiceThatWasNotPresented() {
        String id = issue(uniqueNumber()).jsonPath().getString("id");

        given()
        .when()
                .post("/invoices/{id}/acceptance", id)
        .then()
                .statusCode(409);
    }

    @Test
    void shouldReturn409WhenPresentingInvoiceTwice() {
        String id = presentedInvoiceId(uniqueNumber());

        given()
        .when()
                .post("/invoices/{id}/presentation", id)
        .then()
                .statusCode(409);
    }

    @Test
    void shouldReturn404WhenPresentingNonExistentInvoice() {
        given()
        .when()
                .post("/invoices/{id}/presentation", UUID.randomUUID())
        .then()
                .statusCode(404);
    }

    private String presentedInvoiceId(String number) {
        String id = issue(number).jsonPath().getString("id");
        given()
        .when()
                .post("/invoices/{id}/presentation", id)
        .then()
                .statusCode(200);
        return id;
    }

    private Response issue(String number) {
        return given()
                .contentType(ContentType.JSON)
                .body(validRequestBuilder(number).build())
        .when()
                .post("/invoices")
        .then()
                .statusCode(201)
                .extract()
                .response();
    }

    private IssueInvoiceRequest.IssueInvoiceRequestBuilder validRequestBuilder(String number) {
        return IssueInvoiceRequest.builder()
                .number(number)
                .issuerDocument("12345678901")
                .payerDocument("10987654321")
                .amount(new BigDecimal("100.00"))
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30));
    }

    private String uniqueNumber() {
        return "INV-" + UUID.randomUUID();
    }
}
