package fr.ece.autolocapi.repository;

import fr.ece.autolocapi.domain.Contrat;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratRepository extends JpaRepository<Contrat, Long> {

    @EntityGraph(attributePaths = {"reservation", "reservation.client", "reservation.vehicule"})
    List<Contrat> findAllByOrderByIdContratDesc();

    @Override
    @EntityGraph(attributePaths = {"reservation", "reservation.client", "reservation.vehicule"})
    Optional<Contrat> findById(Long id);

    boolean existsByReservation_IdReservation(Long idReservation);
}
