package br.com.marco.escritural.adapters.in.web.dto.response;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ErrorResponse(String code, String message, Instant timestamp) {
}
