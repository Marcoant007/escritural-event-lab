package br.com.marco.escritural.adapters.in.web.handler;

import br.com.marco.escritural.adapters.in.web.dto.response.ErrorResponse;
import br.com.marco.escritural.application.exception.InvoiceNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.Instant;

@Provider
public class InvoiceNotFoundExceptionHandler implements ExceptionMapper<InvoiceNotFoundException> {
    @Override
    public Response toResponse(InvoiceNotFoundException exception) {
        ErrorResponse body = ErrorResponse.builder().code("INVOICE_NOT_FOUND")
                .message(exception.getMessage()).timestamp(Instant.now()).build();
        return Response.status(Response.Status.NOT_FOUND).entity(body).build();
    }
}
