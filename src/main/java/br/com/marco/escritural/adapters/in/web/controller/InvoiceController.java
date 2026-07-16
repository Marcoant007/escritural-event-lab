package br.com.marco.escritural.adapters.in.web.controller;

import br.com.marco.escritural.adapters.in.web.dto.request.IssueInvoiceRequest;
import br.com.marco.escritural.adapters.in.web.dto.response.InvoiceResponse;
import br.com.marco.escritural.adapters.in.web.mapper.InvoiceWebMapper;
import br.com.marco.escritural.application.ports.in.AcceptInvoiceUseCase;
import br.com.marco.escritural.application.ports.in.FindInvoiceUseCase;
import br.com.marco.escritural.application.ports.in.IssueInvoiceUseCase;
import br.com.marco.escritural.application.ports.in.PresentInvoiceUseCase;
import br.com.marco.escritural.application.ports.in.RejectInvoiceUseCase;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.UUID;

@Path("/invoices")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class InvoiceController {
    private final IssueInvoiceUseCase issueInvoice;
    private final FindInvoiceUseCase findInvoice;
    private final PresentInvoiceUseCase presentInvoice;
    private final AcceptInvoiceUseCase acceptInvoice;
    private final RejectInvoiceUseCase rejectInvoice;
    private final InvoiceWebMapper mapper;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response issue(@Valid IssueInvoiceRequest request) {
        InvoiceResponse response = mapper.toResponse(issueInvoice.execute(mapper.toCommand(request)));
        return Response.created(URI.create("/invoices/" + response.id())).entity(response).build();
    }

    @GET
    @Path("/{id}")
    public InvoiceResponse findById(@PathParam("id") UUID id) {
        return mapper.toResponse(findInvoice.findById(id));
    }

    @POST @Path("/{id}/presentation")
    public InvoiceResponse present(@PathParam("id") UUID id) {
        return mapper.toResponse(presentInvoice.present(id));
    }

    @POST @Path("/{id}/acceptance")
    public InvoiceResponse accept(@PathParam("id") UUID id) {
        return mapper.toResponse(acceptInvoice.accept(id));
    }

    @POST @Path("/{id}/rejection")
    public InvoiceResponse reject(@PathParam("id") UUID id) {
        return mapper.toResponse(rejectInvoice.reject(id));
    }
}
