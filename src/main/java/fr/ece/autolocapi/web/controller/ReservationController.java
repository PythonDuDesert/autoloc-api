package fr.ece.autolocapi.web.controller;

import static fr.ece.autolocapi.web.Http.badRequest;
import static fr.ece.autolocapi.web.Http.conflict;
import static fr.ece.autolocapi.web.Http.notFound;

import fr.ece.autolocapi.domain.Reservation;
import fr.ece.autolocapi.domain.StatutReservation;
import fr.ece.autolocapi.domain.StatutVehicule;
import fr.ece.autolocapi.domain.Vehicule;
import fr.ece.autolocapi.repository.ClientRepository;
import fr.ece.autolocapi.repository.ReservationRepository;
import fr.ece.autolocapi.repository.VehiculeRepository;
import fr.ece.autolocapi.web.dto.Dtos.ReservationDto;
import fr.ece.autolocapi.web.dto.Dtos.ReservationRequest;
import fr.ece.autolocapi.web.dto.Dtos.StatutRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@Transactional(readOnly = true)
public class ReservationController {

    /** Statuts qui bloquent un véhicule sur une période. */
    private static final Set<StatutReservation> ACTIVES =
            Set.of(StatutReservation.En_ATTENTE, StatutReservation.CONFIRMEE);

    /** Transitions de statut autorisées (réservées à l'administrateur, sauf l'annulation). */
    private static final Map<StatutReservation, Set<StatutReservation>> TRANSITIONS = Map.of(
            StatutReservation.En_ATTENTE, Set.of(StatutReservation.CONFIRMEE, StatutReservation.ANNULEE),
            StatutReservation.CONFIRMEE, Set.of(StatutReservation.TERMINEE, StatutReservation.ANNULEE),
            StatutReservation.ANNULEE, Set.of(),
            StatutReservation.TERMINEE, Set.of());

    private final ReservationRepository reservations;
    private final ClientRepository clients;
    private final VehiculeRepository vehicules;

    public ReservationController(ReservationRepository reservations, ClientRepository clients,
                                 VehiculeRepository vehicules) {
        this.reservations = reservations;
        this.clients = clients;
        this.vehicules = vehicules;
    }

    @GetMapping
    public List<ReservationDto> list() {
        return reservations.findAllByOrderByIdReservationDesc().stream().map(ReservationDto::from).toList();
    }

    @GetMapping("/{id}")
    public ReservationDto get(@PathVariable Long id) {
        return ReservationDto.from(find(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ReservationDto create(@Valid @RequestBody ReservationRequest r) {
        Reservation res = new Reservation();
        apply(res, r, -1L, null);
        res.setStatut(StatutReservation.En_ATTENTE);
        return ReservationDto.from(reservations.saveAndFlush(res));
    }

    @PutMapping("/{id}")
    @Transactional
    public ReservationDto update(@PathVariable Long id, @Valid @RequestBody ReservationRequest r) {
        Reservation res = find(id);
        if (!ACTIVES.contains(res.getStatut())) {
            throw conflict("Une réservation annulée ou terminée ne peut plus être modifiée.");
        }
        apply(res, r, id, res.getDateDebut());
        return ReservationDto.from(reservations.saveAndFlush(res));
    }

    /** Annulation : autorisée à tout utilisateur authentifié. */
    @PatchMapping("/{id}/annuler")
    @Transactional
    public ReservationDto cancel(@PathVariable Long id) {
        Reservation res = find(id);
        if (res.getStatut() == StatutReservation.TERMINEE) {
            throw conflict("Une réservation terminée ne peut pas être annulée.");
        }
        res.setStatut(StatutReservation.ANNULEE);
        return ReservationDto.from(reservations.saveAndFlush(res));
    }

    /** Changement de statut (confirmer, terminer) : réservé à l'administrateur (voir SecurityConfig). */
    @PatchMapping("/{id}/statut")
    @Transactional
    public ReservationDto changeStatut(@PathVariable Long id, @Valid @RequestBody StatutRequest r) {
        Reservation res = find(id);
        if (!TRANSITIONS.get(res.getStatut()).contains(r.statut())) {
            throw conflict("Transition de statut impossible : " + res.getStatut() + " → " + r.statut() + ".");
        }
        res.setStatut(r.statut());
        return ReservationDto.from(reservations.saveAndFlush(res));
    }

    private Reservation find(Long id) {
        return reservations.findById(id).orElseThrow(() -> notFound("Réservation"));
    }

    private void apply(Reservation res, ReservationRequest r, Long selfId, LocalDate previousStart) {
        if (r.dateFin().isBefore(r.dateDebut())) {
            throw badRequest("La date de fin doit être postérieure ou égale à la date de début.");
        }
        if (r.dateDebut().isBefore(LocalDate.now()) && !r.dateDebut().equals(previousStart)) {
            throw badRequest("La date de début ne peut pas être dans le passé.");
        }
        var client = clients.findById(r.idClient()).orElseThrow(() -> notFound("Client"));
        Vehicule vehicule = vehicules.findById(r.idVehicule()).orElseThrow(() -> notFound("Véhicule"));
        if (vehicule.getStatut() == StatutVehicule.MAINTENANCE) {
            throw conflict("Ce véhicule est actuellement en maintenance.");
        }
        if (reservations.countOverlaps(vehicule.getIdVehicule(), ACTIVES, selfId, r.dateDebut(), r.dateFin()) > 0) {
            throw conflict("Ce véhicule est déjà réservé sur tout ou partie de cette période.");
        }
        res.setClient(client);
        res.setVehicule(vehicule);
        res.setDateDebut(r.dateDebut());
        res.setDateFin(r.dateFin());
    }
}
