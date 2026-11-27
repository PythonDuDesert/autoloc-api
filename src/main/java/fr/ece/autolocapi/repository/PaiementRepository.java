package fr.ece.autolocapi.repository;

import fr.ece.autolocapi.domain.Paiement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    List<Paiement> findAllByOrderByDatePaiementDescIdPaiementDesc();
    List<Paiement> findByContrat_IdContratOrderByDatePaiementDescIdPaiementDesc(Long idContrat);
}
