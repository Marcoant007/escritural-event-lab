package br.com.marco.escritural.adapters.in.web.handler;

import br.com.marco.escritural.adapters.in.web.dto.response.ErrorResponse;
import br.com.marco.escritural.application.exception.DuplicateInvoiceException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.Instant;

@Provider
public class DuplicateInvoiceExceptionHandler implements ExceptionMapper<DuplicateInvoiceException> {
    @Override
    public Response toResponse(DuplicateInvoiceException exception) {
        ErrorResponse body = ErrorResponse.builder().code("DUPLICATE_INVOICE")
                .message(exception.getMessage()).timestamp(Instant.now()).build();
        return Response.status(Response.Status.CONFLICT).entity(body).build();
    }
}
