package fr.ece.autolocapi.web.dto;

import fr.ece.autolocapi.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * DTO de l'API REST. On n'expose jamais les entités JPA directement :
 * leurs relations bidirectionnelles LAZY provoqueraient des boucles de sérialisation JSON.
 */
public final class Dtos {
    private Dtos() {}

    // ------------------------------------------------------------------ Agence
    public record AgenceRef(Long idAgence, String nom, String ville, String adresse, String telephone) {
        public static AgenceRef from(Agence a) {
            return a == null ? null
                    : new AgenceRef(a.getIdAgence(), a.getNom(), a.getVille(), a.getAdresse(), a.getTelephone());
        }
    }

    public record AgenceDto(Long idAgence, String nom, String ville, String adresse, String telephone, int nbVehicules) {
        public static AgenceDto from(Agence a) {
            return new AgenceDto(a.getIdAgence(), a.getNom(), a.getVille(), a.getAdresse(), a.getTelephone(),
                    a.getVehicules().size());
        }
    }

    public record AgenceRequest(
            @NotBlank @Size(max = 100) String nom,
            @NotBlank @Size(max = 80) String ville,
            @NotBlank @Size(max = 150) String adresse,
            @NotBlank @Size(max = 30) String telephone) {}

    // -------------------------------------------------------------- Equipement
    public record EquipementDto(Long idEquipement, String libelle) {
        public static EquipementDto from(Equipement e) {
            return new EquipementDto(e.getIdEquipement(), e.getLibelle());
        }
    }

    public record EquipementRequest(@NotBlank @Size(max = 100) String libelle) {}

    // ---------------------------------------------------------------- Vehicule
    public record VehiculeRef(Long idVehicule, String immatriculation, String marque, String modele,
                              BigDecimal tarifJournalier) {
        public static VehiculeRef from(Vehicule v) {
            return v == null ? null
                    : new VehiculeRef(v.getIdVehicule(), v.getImmatriculation(), v.getMarque(), v.getModele(),
                    v.getTarifJournalier());
        }
    }

    public record VehiculeDto(Long idVehicule, String immatriculation, String marque, String modele,
                              CategorieVehicule categorie, BigDecimal tarifJournalier, StatutVehicule statut,
                              AgenceRef agence, List<EquipementDto> equipements) {
        public static VehiculeDto from(Vehicule v) {
            return new VehiculeDto(v.getIdVehicule(), v.getImmatriculation(), v.getMarque(), v.getModele(),
                    v.getCategorie(), v.getTarifJournalier(), v.getStatut(), AgenceRef.from(v.getAgence()),
                    v.getEquipements().stream().map(EquipementDto::from)
                            .sorted(Comparator.comparing(EquipementDto::libelle)).toList());
        }
    }

    public record VehiculeRequest(
            @NotBlank @Size(max = 20) String immatriculation,
            @NotBlank @Size(max = 50) String marque,
            @NotBlank @Size(max = 50) String modele,
            @NotNull CategorieVehicule categorie,
            @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal tarifJournalier,
            @NotNull StatutVehicule statut,
            Long idAgence,
            Set<Long> equipementIds) {}

    // ------------------------------------------------------------------ Client
    public record ClientRef(Long idClient, String nom, String prenom) {
        public static ClientRef from(Client c) {
            return c == null ? null : new ClientRef(c.getIdClient(), c.getNom(), c.getPrenom());
        }
    }

    public record ClientDto(Long idClient, String nom, String prenom, String email, String telephone,
                            String numPermis, LocalDate dateInscription) {
        public static ClientDto from(Client c) {
            return new ClientDto(c.getIdClient(), c.getNom(), c.getPrenom(), c.getEmail(), c.getTelephone(),
                    c.getNumPermis(), c.getDateInscription());
        }
    }

    public record ClientRequest(
            @NotBlank @Size(max = 80) String nom,
            @NotBlank @Size(max = 80) String prenom,
            @NotBlank @Email @Size(max = 120) String email,
            @NotBlank @Size(max = 30) String telephone,
            @NotBlank @Size(max = 50) String numPermis) {}

    // ------------------------------------------------------------- Reservation
    public record ReservationDto(Long idReservation, LocalDate dateDebut, LocalDate dateFin,
                                 StatutReservation statut, ClientRef client, VehiculeRef vehicule, Long idContrat,
                                 String utilisateur) {
        public static ReservationDto from(Reservation r) {
            return new ReservationDto(r.getIdReservation(), r.getDateDebut(), r.getDateFin(), r.getStatut(),
                    ClientRef.from(r.getClient()), VehiculeRef.from(r.getVehicule()),
                    r.getContrat() == null ? null : r.getContrat().getIdContrat(),
                    r.getUtilisateur() == null ? null : r.getUtilisateur().getUsername());
        }
    }

    public record ReservationRequest(
            Long idClient,   // obligatoire pour ADMIN ; ignoré pour un USER (son client lié est utilisé)
            @NotNull Long idVehicule,
            @NotNull LocalDate dateDebut,
            @NotNull LocalDate dateFin) {}

    /** Réservation par l'utilisateur connecté : le compte vient de l'authentification, jamais du corps. */
    public record MaReservationRequest(
            @NotNull Long idVehicule,
            @NotNull LocalDate dateDebut,
            @NotNull LocalDate dateFin) {}

    public record StatutRequest(@NotNull StatutReservation statut) {}

    // ----------------------------------------------------------------- Contrat
    public record ReservationRef(Long idReservation, LocalDate dateDebut, LocalDate dateFin,
                                 StatutReservation statut, ClientRef client, VehiculeRef vehicule) {
        public static ReservationRef from(Reservation r) {
            return r == null ? null
                    : new ReservationRef(r.getIdReservation(), r.getDateDebut(), r.getDateFin(), r.getStatut(),
                    ClientRef.from(r.getClient()), VehiculeRef.from(r.getVehicule()));
        }
    }

    public record ContratDto(Long idContrat, LocalDate dateSignature, BigDecimal montantTotal, Boolean valide,
                             ReservationRef reservation, BigDecimal totalPaye, int nbPaiements) {
        public static ContratDto from(Contrat c) {
            BigDecimal paye = c.getPaiements().stream().map(Paiement::getMontant)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new ContratDto(c.getIdContrat(), c.getDateSignature(), c.getMontantTotal(), c.getValide(),
                    ReservationRef.from(c.getReservation()), paye, c.getPaiements().size());
        }
    }

    public record ContratRequest(@NotNull Long idReservation, Boolean valide) {}

    // ---------------------------------------------------------------- Paiement
    public record PaiementDto(Long idPaiement, BigDecimal montant, LocalDate datePaiement,
                              ModePaiement modePaiement, Long idContrat) {
        public static PaiementDto from(Paiement p) {
            return new PaiementDto(p.getIdPaiement(), p.getMontant(), p.getDatePaiement(), p.getModePaiement(),
                    p.getContrat() == null ? null : p.getContrat().getIdContrat());
        }
    }

    public record PaiementRequest(
            @NotNull Long idContrat,
            @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal montant,
            LocalDate datePaiement,
            @NotNull ModePaiement modePaiement) {}

    // ------------------------------------------------------------- Maintenance
    public record MaintenanceDto(Long idMaintenance, LocalDate dateDebut, LocalDate dateFin, String description,
                                 VehiculeRef vehicule) {
        public static MaintenanceDto from(Maintenance m) {
            return new MaintenanceDto(m.getIdMaintenance(), m.getDateDebut(), m.getDateFin(), m.getDescription(),
                    VehiculeRef.from(m.getVehicule()));
        }
    }

    public record MaintenanceRequest(
            @NotNull Long idVehicule,
            @NotNull LocalDate dateDebut,
            @NotNull LocalDate dateFin,
            @NotBlank @Size(max = 255) String description) {}

}
