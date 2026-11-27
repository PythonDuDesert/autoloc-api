package fr.ece.autolocapi.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "utilisateur",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_utilisateur_username",
                columnNames = "username"
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUtilisateur;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleUtilisateur role = RoleUtilisateur.USER;

    /**
     * Optional link to the business Client record.
     * Credentials stay in utilisateur, not in client.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "client_id_client",
            unique = true,
            foreignKey = @ForeignKey(name = "fk_utilisateur_client")
    )
    private Client client;
}
