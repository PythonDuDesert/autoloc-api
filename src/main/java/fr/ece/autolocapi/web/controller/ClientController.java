package fr.ece.autolocapi.web.controller;

import static fr.ece.autolocapi.web.Http.conflict;
import static fr.ece.autolocapi.web.Http.notFound;

import fr.ece.autolocapi.domain.Client;
import fr.ece.autolocapi.repository.ClientRepository;
import fr.ece.autolocapi.web.dto.Dtos.ClientDto;
import fr.ece.autolocapi.web.dto.Dtos.ClientRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
@Transactional(readOnly = true)
public class ClientController {

    private final ClientRepository clients;

    public ClientController(ClientRepository clients) {
        this.clients = clients;
    }

    @GetMapping
    public List<ClientDto> list() {
        return clients.findAll(Sort.by("nom", "prenom")).stream().map(ClientDto::from).toList();
    }

    @GetMapping("/{id}")
    public ClientDto get(@PathVariable Long id) {
        return ClientDto.from(find(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ClientDto create(@Valid @RequestBody ClientRequest r) {
        Client c = new Client();
        c.setDateInscription(LocalDate.now());
        apply(c, r, -1L);
        return ClientDto.from(clients.saveAndFlush(c));
    }

    @PutMapping("/{id}")
    @Transactional
    public ClientDto update(@PathVariable Long id, @Valid @RequestBody ClientRequest r) {
        Client c = find(id);
        apply(c, r, id);
        return ClientDto.from(clients.saveAndFlush(c));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@PathVariable Long id) {
        clients.delete(find(id));
        clients.flush();
    }

    private Client find(Long id) {
        return clients.findById(id).orElseThrow(() -> notFound("Client"));
    }

    private void apply(Client c, ClientRequest r, Long selfId) {
        String email = r.email().trim();
        String permis = r.numPermis().trim();
        if (clients.existsByEmailIgnoreCaseAndIdClientNot(email, selfId)) {
            throw conflict("Un client avec cet email existe déjà.");
        }
        if (clients.existsByNumPermisIgnoreCaseAndIdClientNot(permis, selfId)) {
            throw conflict("Un client avec ce numéro de permis existe déjà.");
        }
        c.setNom(r.nom().trim());
        c.setPrenom(r.prenom().trim());
        c.setEmail(email);
        c.setTelephone(r.telephone().trim());
        c.setNumPermis(permis);
    }
}
