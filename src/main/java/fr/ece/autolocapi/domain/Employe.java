package fr.ece.autolocapi.domain;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Entity
@Table(name = "employe")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employe {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long idEmploye;
    @Column(nullable = false, length = 50) private String nom;
    @Column(nullable = false, length = 50) private String prenom;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private RoleEmploye role;
}
