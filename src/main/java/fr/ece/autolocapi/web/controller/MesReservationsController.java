package fr.ece.autolocapi.web.controller;

import static fr.ece.autolocapi.web.Http.badRequest;
import static fr.ece.autolocapi.web.Http.conflict;
import static fr.ece.autolocapi.web.Http.notFound;

import fr.ece.autolocapi.domain.Reservation;
import fr.ece.autolocapi.domain.StatutReservation;
import fr.ece.autolocapi.domain.StatutVehicule;
import fr.ece.autolocapi.domain.Utilisateur;
import fr.ece.autolocapi.domain.Vehicule;
import fr.ece.autolocapi.repository.ReservationRepository;
import fr.ece.autolocapi.repository.UtilisateurRepository;
import fr.ece.autolocapi.repository.VehiculeRepository;
import fr.ece.autolocapi.web.dto.Dtos.MaReservationRequest;
import fr.ece.autolocapi.web.dto.Dtos.ReservationDto;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/** The logged-in account's reservations: a user only sees and changes their own. */
@RestController
@RequestMapping("/api/mes-reservations")
@Transactional(readOnly = true)
public class MesReservationsController {

    /** Statuses that block a vehicle and count as "a reservation in progress". */
    private static final Set<StatutReservation> ACTIVES =
            Set.of(StatutReservation.En_ATTENTE, StatutReservation.CONFIRMEE);

    private final ReservationRepository reservations;
    private final VehiculeRepository vehicules;
    private final UtilisateurRepository utilisateurs;

    public MesReservationsController(ReservationRepository reservations, VehiculeRepository vehicules,
                                     UtilisateurRepository utilisateurs) {
        this.reservations = reservations;
        this.vehicules = vehicules;
        this.utilisateurs = utilisateurs;
    }

    @GetMapping
    public List<ReservationDto> list(Principal principal) {
        return reservations.findByUtilisateurUsernameOrderByDateDebutDesc(principal.getName())
                .stream().map(ReservationDto::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ReservationDto create(@Valid @RequestBody MaReservationRequest r, Principal principal) {
        LocalDate today = LocalDate.now();
        if (r.dateDebut().isBefore(today)) {
            throw badRequest("La date de début ne peut pas être dans le passé.");
        }
        if (r.dateFin().isBefore(r.dateDebut())) {
            throw badRequest("La date de fin doit être postérieure ou égale à la date de début.");
        }

        Utilisateur user = utilisateurs.findByUsername(principal.getName())
                .orElseThrow(() -> notFound("Utilisateur"));

        if (reservations.existsByUtilisateurUsernameAndStatutInAndDateFinGreaterThanEqual(
                user.getUsername(), ACTIVES, today)) {
            throw conflict("Vous avez déjà une réservation en cours. Annulez-la avant d'en faire une nouvelle.");
        }

        Vehicule vehicule = vehicules.findById(r.idVehicule()).orElseThrow(() -> notFound("Véhicule"));
        if (vehicule.getStatut() == StatutVehicule.MAINTENANCE) {
            throw conflict("Ce véhicule est actuellement en maintenance.");
        }
        if (reservations.countOverlaps(vehicule.getIdVehicule(), ACTIVES, -1L, r.dateDebut(), r.dateFin()) > 0) {
            throw conflict("Ce véhicule est déjà réservé sur tout ou partie de cette période.");
        }

        Reservation res = new Reservation();
        res.setUtilisateur(user);
        res.setClient(user.getClient());   // null if the account isn't linked to a client record
        res.setVehicule(vehicule);
        res.setDateDebut(r.dateDebut());
        res.setDateFin(r.dateFin());
        res.setStatut(StatutReservation.En_ATTENTE);
        return ReservationDto.from(reservations.saveAndFlush(res));
    }

    @PatchMapping("/{id}/annuler")
    @Transactional
    public ReservationDto cancel(@PathVariable Long id, Principal principal) {
        Reservation res = reservations.findByIdReservationAndUtilisateurUsername(id, principal.getName())
                .orElseThrow(() -> notFound("Réservation"));
        if (!ACTIVES.contains(res.getStatut())) {
            throw conflict("Cette réservation est déjà annulée ou terminée.");
        }
        if (!LocalDate.now().isBefore(res.getDateDebut())) {
            throw conflict("La location a déjà commencé : l'annulation n'est plus possible.");
        }
        res.setStatut(StatutReservation.ANNULEE);
        return ReservationDto.from(reservations.saveAndFlush(res));
    }
}