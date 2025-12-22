package com.events.app.myevents.DTO;

public record LoginRequest(
        String username,
        String password
) {
}