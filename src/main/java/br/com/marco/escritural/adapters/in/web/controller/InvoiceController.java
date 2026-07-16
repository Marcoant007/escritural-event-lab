package br.com.marco.escritural.adapters.in.web.controller;

import br.com.marco.escritural.adapters.in.web.dto.request.IssueInvoiceRequest;
import br.com.marco.escritural.adapters.in.web.dto.response.ErrorResponse;
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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.UUID;

@Path("/invoices")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Ciclo de vida da duplicata escritural")
public class InvoiceController {
    private final IssueInvoiceUseCase issueInvoice;
    private final FindInvoiceUseCase findInvoice;
    private final PresentInvoiceUseCase presentInvoice;
    private final AcceptInvoiceUseCase acceptInvoice;
    private final RejectInvoiceUseCase rejectInvoice;
    private final InvoiceWebMapper mapper;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Emitir duplicata", description = "Cria uma nova duplicata no status ISSUED.")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Duplicata emitida",
                    content = @Content(schema = @Schema(implementation = InvoiceResponse.class))),
            @APIResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Número de duplicata já existe",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response issue(@Valid IssueInvoiceRequest request) {
        InvoiceResponse response = mapper.toResponse(issueInvoice.execute(mapper.toCommand(request)));
        return Response.created(URI.create("/invoices/" + response.id())).entity(response).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Consultar duplicata", description = "Busca uma duplicata pelo id.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Duplicata encontrada",
                    content = @Content(schema = @Schema(implementation = InvoiceResponse.class))),
            @APIResponse(responseCode = "404", description = "Duplicata não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public InvoiceResponse findById(@PathParam("id") UUID id) {
        return mapper.toResponse(findInvoice.findById(id));
    }

    @POST @Path("/{id}/presentation")
    @Operation(summary = "Apresentar duplicata", description = "Transição ISSUED -> PRESENTED.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Duplicata apresentada",
                    content = @Content(schema = @Schema(implementation = InvoiceResponse.class))),
            @APIResponse(responseCode = "404", description = "Duplicata não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Transição inválida para o status atual",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public InvoiceResponse present(@PathParam("id") UUID id) {
        return mapper.toResponse(presentInvoice.present(id));
    }

    @POST @Path("/{id}/acceptance")
    @Operation(summary = "Aceitar duplicata", description = "Transição PRESENTED -> ACCEPTED.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Duplicata aceita",
                    content = @Content(schema = @Schema(implementation = InvoiceResponse.class))),
            @APIResponse(responseCode = "404", description = "Duplicata não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Transição inválida para o status atual",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public InvoiceResponse accept(@PathParam("id") UUID id) {
        return mapper.toResponse(acceptInvoice.accept(id));
    }

    @POST @Path("/{id}/rejection")
    @Operation(summary = "Recusar duplicata", description = "Transição PRESENTED -> REJECTED.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Duplicata recusada",
                    content = @Content(schema = @Schema(implementation = InvoiceResponse.class))),
            @APIResponse(responseCode = "404", description = "Duplicata não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Transição inválida para o status atual",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public InvoiceResponse reject(@PathParam("id") UUID id) {
        return mapper.toResponse(rejectInvoice.reject(id));
    }
}
