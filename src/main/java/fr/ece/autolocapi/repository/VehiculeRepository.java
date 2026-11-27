package fr.ece.autolocapi.repository;

import fr.ece.autolocapi.domain.Vehicule;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    @EntityGraph(attributePaths = {"agence", "equipements"})
    List<Vehicule> findAllByOrderByMarqueAscModeleAsc();

    @Override
    @EntityGraph(attributePaths = {"agence", "equipements"})
    Optional<Vehicule> findById(Long id);

    boolean existsByImmatriculationIgnoreCaseAndIdVehiculeNot(String immatriculation, Long idVehicule);
}
