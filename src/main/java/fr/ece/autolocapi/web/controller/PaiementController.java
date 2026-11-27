package fr.ece.autolocapi.web.controller;

import static fr.ece.autolocapi.web.Http.notFound;

import fr.ece.autolocapi.domain.Paiement;
import fr.ece.autolocapi.repository.ContratRepository;
import fr.ece.autolocapi.repository.PaiementRepository;
import fr.ece.autolocapi.web.dto.Dtos.PaiementDto;
import fr.ece.autolocapi.web.dto.Dtos.PaiementRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/** Accès réservé à l'administrateur (voir SecurityConfig). */
@RestController
@RequestMapping("/api/paiements")
@Transactional(readOnly = true)
public class PaiementController {

    private final PaiementRepository paiements;
    private final ContratRepository contrats;

    public PaiementController(PaiementRepository paiements, ContratRepository contrats) {
        this.paiements = paiements;
        this.contrats = contrats;
    }

    /** Liste des paiements, filtrable par contrat : GET /api/paiements?contrat=3 */
    @GetMapping
    public List<PaiementDto> list(@RequestParam(required = false) Long contrat) {
        List<Paiement> list = contrat == null
                ? paiements.findAllByOrderByDatePaiementDescIdPaiementDesc()
                : paiements.findByContrat_IdContratOrderByDatePaiementDescIdPaiementDesc(contrat);
        return list.stream().map(PaiementDto::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public PaiementDto create(@Valid @RequestBody PaiementRequest r) {
        Paiement p = new Paiement();
        p.setContrat(contrats.findById(r.idContrat()).orElseThrow(() -> notFound("Contrat")));
        p.setMontant(r.montant());
        p.setDatePaiement(r.datePaiement() == null ? LocalDate.now() : r.datePaiement());
        p.setModePaiement(r.modePaiement());
        return PaiementDto.from(paiements.saveAndFlush(p));
    }
}
