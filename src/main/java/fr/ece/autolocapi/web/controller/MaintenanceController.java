package fr.ece.autolocapi.web.controller;

import static fr.ece.autolocapi.web.Http.badRequest;
import static fr.ece.autolocapi.web.Http.notFound;

import fr.ece.autolocapi.domain.Maintenance;
import fr.ece.autolocapi.repository.MaintenanceRepository;
import fr.ece.autolocapi.repository.VehiculeRepository;
import fr.ece.autolocapi.web.dto.Dtos.MaintenanceDto;
import fr.ece.autolocapi.web.dto.Dtos.MaintenanceRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/** Accès réservé à l'administrateur (voir SecurityConfig). */
@RestController
@RequestMapping("/api/maintenances")
@Transactional(readOnly = true)
public class MaintenanceController {

    private final MaintenanceRepository maintenances;
    private final VehiculeRepository vehicules;

    public MaintenanceController(MaintenanceRepository maintenances, VehiculeRepository vehicules) {
        this.maintenances = maintenances;
        this.vehicules = vehicules;
    }

    @GetMapping
    public List<MaintenanceDto> list() {
        return maintenances.findAllByOrderByDateDebutDescIdMaintenanceDesc().stream()
                .map(MaintenanceDto::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public MaintenanceDto create(@Valid @RequestBody MaintenanceRequest r) {
        if (r.dateFin().isBefore(r.dateDebut())) {
            throw badRequest("La date de fin doit être postérieure ou égale à la date de début.");
        }
        Maintenance m = new Maintenance();
        m.setVehicule(vehicules.findById(r.idVehicule()).orElseThrow(() -> notFound("Véhicule")));
        m.setDateDebut(r.dateDebut());
        m.setDateFin(r.dateFin());
        m.setDescription(r.description().trim());
        return MaintenanceDto.from(maintenances.saveAndFlush(m));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@PathVariable Long id) {
        maintenances.delete(maintenances.findById(id).orElseThrow(() -> notFound("Maintenance")));
        maintenances.flush();
    }
}
