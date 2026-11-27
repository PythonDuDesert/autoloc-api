package fr.ece.autolocapi.repository;

import fr.ece.autolocapi.domain.Maintenance;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {

    @EntityGraph(attributePaths = {"vehicule"})
    List<Maintenance> findAllByOrderByDateDebutDescIdMaintenanceDesc();
}
