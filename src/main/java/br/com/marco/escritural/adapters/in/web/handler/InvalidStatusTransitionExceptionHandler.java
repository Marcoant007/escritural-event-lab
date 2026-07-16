package br.com.marco.escritural.adapters.in.web.handler;

import br.com.marco.escritural.adapters.in.web.dto.response.ErrorResponse;
import br.com.marco.escritural.domain.exception.InvalidStatusTransitionException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.Instant;

@Provider
public class InvalidStatusTransitionExceptionHandler implements ExceptionMapper<InvalidStatusTransitionException> {
    @Override
    public Response toResponse(InvalidStatusTransitionException exception) {
        ErrorResponse body = ErrorResponse.builder().code("INVALID_STATUS_TRANSITION")
                .message(exception.getMessage()).timestamp(Instant.now()).build();
        return Response.status(Response.Status.CONFLICT).entity(body).build();
    }
}
