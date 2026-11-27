package fr.ece.autolocapi.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Fabriques d'exceptions HTTP pour garder les contrôleurs lisibles. */
public final class Http {
    private Http() {}

    public static ResponseStatusException notFound(String what) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, what + " introuvable");
    }

    public static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    public static ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
