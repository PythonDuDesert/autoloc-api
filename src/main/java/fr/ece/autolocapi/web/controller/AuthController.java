package fr.ece.autolocapi.web.controller;

import fr.ece.autolocapi.domain.RoleUtilisateur;
import fr.ece.autolocapi.domain.Utilisateur;
import fr.ece.autolocapi.repository.UtilisateurRepository;
import fr.ece.autolocapi.web.dto.RegisterRequest;
import fr.ece.autolocapi.web.dto.RegisterResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class AuthController {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        String username = request.username().trim();

        if (utilisateurRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Ce nom d'utilisateur existe déjà."));
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUsername(username);
        utilisateur.setPassword(passwordEncoder.encode(request.password()));
        utilisateur.setRole(RoleUtilisateur.USER);

        Utilisateur saved = utilisateurRepository.save(utilisateur);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new RegisterResponse(
                        saved.getIdUtilisateur(),
                        saved.getUsername(),
                        saved.getRole().name(),
                        "Compte créé avec succès."
                )
        );
    }
}
