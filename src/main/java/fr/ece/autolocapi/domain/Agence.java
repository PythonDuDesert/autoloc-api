package fr.ece.autolocapi.domain;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Entity
@Table(name = "agence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Agence {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long idAgence;
    @Column(nullable = false, length = 100) private String nom;
    @Column(nullable = false, length = 50)  private String ville;
    private String adresse;
    @Column(length = 20) private String telephone;

}
