package fr.ece.autolocapi.web.controller;

import static fr.ece.autolocapi.web.Http.notFound;

import fr.ece.autolocapi.domain.Agence;
import fr.ece.autolocapi.repository.AgenceRepository;
import fr.ece.autolocapi.web.dto.Dtos.AgenceDto;
import fr.ece.autolocapi.web.dto.Dtos.AgenceRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agences")
@Transactional(readOnly = true)
public class AgenceController {

    private final AgenceRepository agences;

    public AgenceController(AgenceRepository agences) {
        this.agences = agences;
    }

    @GetMapping
    public List<AgenceDto> list() {
        return agences.findAll(Sort.by("nom")).stream().map(AgenceDto::from).toList();
    }

    @GetMapping("/{id}")
    public AgenceDto get(@PathVariable Long id) {
        return AgenceDto.from(find(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public AgenceDto create(@Valid @RequestBody AgenceRequest r) {
        Agence a = new Agence();
        apply(a, r);
        return AgenceDto.from(agences.saveAndFlush(a));
    }

    @PutMapping("/{id}")
    @Transactional
    public AgenceDto update(@PathVariable Long id, @Valid @RequestBody AgenceRequest r) {
        Agence a = find(id);
        apply(a, r);
        return AgenceDto.from(agences.saveAndFlush(a));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@PathVariable Long id) {
        agences.delete(find(id));
        agences.flush();
    }

    private Agence find(Long id) {
        return agences.findById(id).orElseThrow(() -> notFound("Agence"));
    }

    private static void apply(Agence a, AgenceRequest r) {
        a.setNom(r.nom().trim());
        a.setVille(r.ville().trim());
        a.setAdresse(r.adresse().trim());
        a.setTelephone(r.telephone().trim());
    }
}
