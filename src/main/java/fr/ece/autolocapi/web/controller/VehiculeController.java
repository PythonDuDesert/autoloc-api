package fr.ece.autolocapi.web.controller;

import static fr.ece.autolocapi.web.Http.conflict;
import static fr.ece.autolocapi.web.Http.notFound;

import fr.ece.autolocapi.domain.Vehicule;
import fr.ece.autolocapi.repository.AgenceRepository;
import fr.ece.autolocapi.repository.EquipementRepository;
import fr.ece.autolocapi.repository.VehiculeRepository;
import fr.ece.autolocapi.web.dto.Dtos.VehiculeDto;
import fr.ece.autolocapi.web.dto.Dtos.VehiculeRequest;
import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicules")
@Transactional(readOnly = true)
public class VehiculeController {

    private final VehiculeRepository vehicules;
    private final AgenceRepository agences;
    private final EquipementRepository equipements;

    public VehiculeController(VehiculeRepository vehicules, AgenceRepository agences,
                              EquipementRepository equipements) {
        this.vehicules = vehicules;
        this.agences = agences;
        this.equipements = equipements;
    }

    @GetMapping
    public List<VehiculeDto> list() {
        return vehicules.findAllByOrderByMarqueAscModeleAsc().stream().map(VehiculeDto::from).toList();
    }

    @GetMapping("/{id}")
    public VehiculeDto get(@PathVariable Long id) {
        return VehiculeDto.from(find(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public VehiculeDto create(@Valid @RequestBody VehiculeRequest r) {
        Vehicule v = new Vehicule();
        apply(v, r, -1L);
        return VehiculeDto.from(vehicules.saveAndFlush(v));
    }

    @PutMapping("/{id}")
    @Transactional
    public VehiculeDto update(@PathVariable Long id, @Valid @RequestBody VehiculeRequest r) {
        Vehicule v = find(id);
        apply(v, r, id);
        return VehiculeDto.from(vehicules.saveAndFlush(v));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@PathVariable Long id) {
        vehicules.delete(find(id));
        vehicules.flush();
    }

    private Vehicule find(Long id) {
        return vehicules.findById(id).orElseThrow(() -> notFound("Véhicule"));
    }

    private void apply(Vehicule v, VehiculeRequest r, Long selfId) {
        String immat = r.immatriculation().trim().toUpperCase();
        if (vehicules.existsByImmatriculationIgnoreCaseAndIdVehiculeNot(immat, selfId)) {
            throw conflict("Un véhicule avec cette immatriculation existe déjà.");
        }
        v.setImmatriculation(immat);
        v.setMarque(r.marque().trim());
        v.setModele(r.modele().trim());
        v.setCategorie(r.categorie());
        v.setTarifJournalier(r.tarifJournalier());
        v.setStatut(r.statut());
        v.setAgence(r.idAgence() == null ? null
                : agences.findById(r.idAgence()).orElseThrow(() -> notFound("Agence")));
        Set<Long> ids = r.equipementIds() == null ? Set.of() : r.equipementIds();
        v.setEquipements(new HashSet<>(equipements.findAllById(ids)));
    }
}
