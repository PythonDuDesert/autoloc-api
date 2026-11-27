package fr.ece.autolocapi.web.controller;

import static fr.ece.autolocapi.web.Http.conflict;
import static fr.ece.autolocapi.web.Http.notFound;

import fr.ece.autolocapi.domain.Contrat;
import fr.ece.autolocapi.domain.Reservation;
import fr.ece.autolocapi.domain.StatutReservation;
import fr.ece.autolocapi.repository.ContratRepository;
import fr.ece.autolocapi.repository.ReservationRepository;
import fr.ece.autolocapi.web.dto.Dtos.ContratDto;
import fr.ece.autolocapi.web.dto.Dtos.ContratRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/** Accès réservé à l'administrateur (voir SecurityConfig). */
@RestController
@RequestMapping("/api/contrats")
@Transactional(readOnly = true)
public class ContratController {

    private final ContratRepository contrats;
    private final ReservationRepository reservations;

    public ContratController(ContratRepository contrats, ReservationRepository reservations) {
        this.contrats = contrats;
        this.reservations = reservations;
    }

    @GetMapping
    public List<ContratDto> list() {
        return contrats.findAllByOrderByIdContratDesc().stream().map(ContratDto::from).toList();
    }

    @GetMapping("/{id}")
    public ContratDto get(@PathVariable Long id) {
        return ContratDto.from(contrats.findById(id).orElseThrow(() -> notFound("Contrat")));
    }

    /** Crée le contrat d'une réservation confirmée ; le montant = tarif journalier × nombre de jours (min. 1). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ContratDto create(@Valid @RequestBody ContratRequest r) {
        Reservation res = reservations.findById(r.idReservation()).orElseThrow(() -> notFound("Réservation"));
        if (res.getStatut() != StatutReservation.CONFIRMEE) {
            throw conflict("Seule une réservation confirmée peut faire l'objet d'un contrat.");
        }
        if (contrats.existsByReservation_IdReservation(res.getIdReservation())) {
            throw conflict("Cette réservation a déjà un contrat.");
        }
        long jours = Math.max(1, ChronoUnit.DAYS.between(res.getDateDebut(), res.getDateFin()));
        Contrat c = new Contrat();
        c.setDateSignature(LocalDate.now());
        c.setMontantTotal(res.getVehicule().getTarifJournalier().multiply(BigDecimal.valueOf(jours)));
        c.setValide(r.valide() == null || r.valide());
        c.setReservation(res);
        return ContratDto.from(contrats.saveAndFlush(c));
    }
}
