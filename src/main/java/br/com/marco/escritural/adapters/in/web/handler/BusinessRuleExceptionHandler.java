package br.com.marco.escritural.adapters.in.web.handler;

import br.com.marco.escritural.adapters.in.web.dto.response.ErrorResponse;
import br.com.marco.escritural.domain.exception.BusinessRuleException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.Instant;

@Provider
public class BusinessRuleExceptionHandler implements ExceptionMapper<BusinessRuleException> {
    @Override
    public Response toResponse(BusinessRuleException exception) {
        ErrorResponse body = ErrorResponse.builder().code("INVALID_BUSINESS_DATA")
                .message(exception.getMessage()).timestamp(Instant.now()).build();
        return Response.status(Response.Status.BAD_REQUEST).entity(body).build();
    }
}
