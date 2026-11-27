package fr.ece.autolocapi.config;

import fr.ece.autolocapi.domain.RoleUtilisateur;
import fr.ece.autolocapi.domain.Utilisateur;
import fr.ece.autolocapi.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UtilisateurDataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createIfMissing("student", "student123", RoleUtilisateur.USER);
        createIfMissing("admin", "admin123", RoleUtilisateur.ADMIN);
    }

    private void createIfMissing(
            String username,
            String password,
            RoleUtilisateur role) {

        if (!utilisateurRepository.existsByUsername(username)) {
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setUsername(username);
            utilisateur.setPassword(passwordEncoder.encode(password));
            utilisateur.setRole(role);
            utilisateurRepository.save(utilisateur);
        }
    }
}
