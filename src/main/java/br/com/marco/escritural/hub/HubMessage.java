package br.com.marco.escritural.hub;

public record HubMessage(String source, String payload, String receivedAt) {
}

