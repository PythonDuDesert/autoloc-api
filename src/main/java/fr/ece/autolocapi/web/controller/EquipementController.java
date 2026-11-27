package fr.ece.autolocapi.web.controller;

import static fr.ece.autolocapi.web.Http.conflict;
import static fr.ece.autolocapi.web.Http.notFound;

import fr.ece.autolocapi.domain.Equipement;
import fr.ece.autolocapi.repository.EquipementRepository;
import fr.ece.autolocapi.web.dto.Dtos.EquipementDto;
import fr.ece.autolocapi.web.dto.Dtos.EquipementRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/equipements")
@Transactional(readOnly = true)
public class EquipementController {

    private final EquipementRepository equipements;

    public EquipementController(EquipementRepository equipements) {
        this.equipements = equipements;
    }

    @GetMapping
    public List<EquipementDto> list() {
        return equipements.findAll(Sort.by("libelle")).stream().map(EquipementDto::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public EquipementDto create(@Valid @RequestBody EquipementRequest r) {
        String libelle = r.libelle().trim();
        if (equipements.existsByLibelleIgnoreCase(libelle)) {
            throw conflict("Cet équipement existe déjà.");
        }
        Equipement e = new Equipement();
        e.setLibelle(libelle);
        return EquipementDto.from(equipements.saveAndFlush(e));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@PathVariable Long id) {
        Equipement e = equipements.findById(id).orElseThrow(() -> notFound("Équipement"));
        // Détache l'équipement des véhicules avant suppression (côté propriétaire de la relation).
        e.getVehicules().forEach(v -> v.getEquipements().remove(e));
        equipements.delete(e);
        equipements.flush();
    }
}
