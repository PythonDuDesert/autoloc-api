package fr.ece.autolocapi.web.dto;

public record RegisterResponse(
        Long idUtilisateur,
        String username,
        String role,
        String message
) {
}
