package fr.ece.autolocapi.repository;

import fr.ece.autolocapi.domain.Reservation;
import fr.ece.autolocapi.domain.StatutReservation;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @EntityGraph(attributePaths = {"client", "vehicule"})
    List<Reservation> findAllByOrderByIdReservationDesc();

    @Override
    @EntityGraph(attributePaths = {"client", "vehicule"})
    Optional<Reservation> findById(Long id);

    @EntityGraph(attributePaths = {"client", "vehicule"})
    List<Reservation> findByClient_IdClientOrderByIdReservationDesc(Long idClient);

    /** Nombre de réservations actives du véhicule qui chevauchent la période [debut, fin]. */
    @Query("""
            select count(r) from Reservation r
            where r.vehicule.idVehicule = :idVehicule
              and r.statut in :statuts
              and r.idReservation <> :excludeId
              and r.dateDebut <= :fin
              and r.dateFin >= :debut
            """)
    long countOverlaps(@Param("idVehicule") Long idVehicule,
                       @Param("statuts") Collection<StatutReservation> statuts,
                       @Param("excludeId") Long excludeId,
                       @Param("debut") LocalDate debut,
                       @Param("fin") LocalDate fin);
}
