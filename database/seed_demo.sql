-- AutoLoc : données de démonstration (à exécuter UNE seule fois, après le premier démarrage de l'API).
-- Les dates sont relatives à aujourd'hui pour que la démo reste cohérente.
-- Les comptes student / admin sont créés par UtilisateurDataInitializer : rien à insérer ici.

USE autoloc_db;

INSERT INTO agence (nom, ville, adresse, telephone) VALUES
  ('AutoLoc Paris Centre', 'Paris', '12 rue de Rivoli', '01 40 00 11 22'),
  ('AutoLoc Lyon Part-Dieu', 'Lyon', '5 cours Lafayette', '04 72 00 33 44'),
  ('AutoLoc Marseille Gare', 'Marseille', '28 boulevard d''Athènes', '04 91 00 55 66');

INSERT INTO equipement (libelle) VALUES
  ('Climatisation'), ('GPS'), ('Bluetooth'), ('Caméra de recul'), ('Siège bébé'), ('Attelage');

INSERT INTO vehicule (immatriculation, marque, modele, categorie, tarif_journalier, statut, agence_id_agence) VALUES
  ('AB-123-CD', 'Peugeot', '208', 'CITADINE', 39.00, 'DISPONIBLE', (SELECT id_agence FROM agence WHERE nom = 'AutoLoc Paris Centre')),
  ('CD-456-EF', 'Renault', 'Clio', 'CITADINE', 42.00, 'DISPONIBLE', (SELECT id_agence FROM agence WHERE nom = 'AutoLoc Lyon Part-Dieu')),
  ('EF-789-GH', 'Volkswagen', 'Golf', 'BERLINE', 59.00, 'DISPONIBLE', (SELECT id_agence FROM agence WHERE nom = 'AutoLoc Paris Centre')),
  ('GH-012-IJ', 'Peugeot', '3008', 'SUV', 79.00, 'LOUE', (SELECT id_agence FROM agence WHERE nom = 'AutoLoc Lyon Part-Dieu')),
  ('IJ-345-KL', 'Toyota', 'RAV4', 'SUV', 85.00, 'DISPONIBLE', (SELECT id_agence FROM agence WHERE nom = 'AutoLoc Marseille Gare')),
  ('KL-678-MN', 'Renault', 'Master', 'UTILITAIRE', 95.00, 'MAINTENANCE', (SELECT id_agence FROM agence WHERE nom = 'AutoLoc Marseille Gare'));

INSERT INTO vehicle_equipment (id_vehicule, id_equipement)
SELECT v.id_vehicule, e.id_equipement
FROM vehicule v JOIN equipement e
WHERE (v.immatriculation = 'AB-123-CD' AND e.libelle IN ('Climatisation', 'Bluetooth'))
   OR (v.immatriculation = 'CD-456-EF' AND e.libelle IN ('Climatisation', 'GPS'))
   OR (v.immatriculation = 'EF-789-GH' AND e.libelle IN ('Climatisation', 'GPS', 'Bluetooth'))
   OR (v.immatriculation = 'GH-012-IJ' AND e.libelle IN ('Climatisation', 'GPS', 'Caméra de recul'))
   OR (v.immatriculation = 'IJ-345-KL' AND e.libelle IN ('Climatisation', 'GPS', 'Attelage'))
   OR (v.immatriculation = 'KL-678-MN' AND e.libelle IN ('Attelage'));

INSERT INTO client (nom, prenom, email, telephone, num_permis, date_inscription) VALUES
  ('Martin', 'Claire', 'claire.martin@example.com', '06 11 22 33 44', 'P-100001', CURDATE() - INTERVAL 90 DAY),
  ('Durand', 'Hugo', 'hugo.durand@example.com', '06 55 66 77 88', 'P-100002', CURDATE() - INTERVAL 40 DAY),
  ('Bernard', 'Inès', 'ines.bernard@example.com', '06 99 00 11 22', 'P-100003', CURDATE() - INTERVAL 10 DAY);

INSERT INTO reservation (date_debut, date_fin, statut, client_id_client, vehicule_id_vehicule) VALUES
  (CURDATE() - INTERVAL 3 DAY, CURDATE() + INTERVAL 2 DAY, 'CONFIRMEE',
     (SELECT id_client FROM client WHERE email = 'claire.martin@example.com'),
     (SELECT id_vehicule FROM vehicule WHERE immatriculation = 'GH-012-IJ')),
  (CURDATE() + INTERVAL 5 DAY, CURDATE() + INTERVAL 9 DAY, 'En_ATTENTE',
     (SELECT id_client FROM client WHERE email = 'hugo.durand@example.com'),
     (SELECT id_vehicule FROM vehicule WHERE immatriculation = 'EF-789-GH')),
  (CURDATE() - INTERVAL 20 DAY, CURDATE() - INTERVAL 15 DAY, 'TERMINEE',
     (SELECT id_client FROM client WHERE email = 'ines.bernard@example.com'),
     (SELECT id_vehicule FROM vehicule WHERE immatriculation = 'AB-123-CD')),
  (CURDATE() + INTERVAL 12 DAY, CURDATE() + INTERVAL 14 DAY, 'ANNULEE',
     (SELECT id_client FROM client WHERE email = 'claire.martin@example.com'),
     (SELECT id_vehicule FROM vehicule WHERE immatriculation = 'CD-456-EF'));

-- Contrats des réservations confirmée et terminée (montant = tarif journalier x nombre de jours)
INSERT INTO contrat (date_signature, montant_total, valide, id_reservation) VALUES
  (CURDATE() - INTERVAL 3 DAY, 395.00, b'1',
     (SELECT r.id_reservation FROM reservation r JOIN vehicule v ON v.id_vehicule = r.vehicule_id_vehicule WHERE v.immatriculation = 'GH-012-IJ')),
  (CURDATE() - INTERVAL 20 DAY, 195.00, b'1',
     (SELECT r.id_reservation FROM reservation r JOIN vehicule v ON v.id_vehicule = r.vehicule_id_vehicule WHERE v.immatriculation = 'AB-123-CD'));

INSERT INTO paiement (montant, date_paiement, mode_paiement, contrat_id_contrat) VALUES
  (150.00, CURDATE() - INTERVAL 3 DAY, 'CARTE',
     (SELECT c.id_contrat FROM contrat c JOIN reservation r ON r.id_reservation = c.id_reservation JOIN vehicule v ON v.id_vehicule = r.vehicule_id_vehicule WHERE v.immatriculation = 'GH-012-IJ')),
  (195.00, CURDATE() - INTERVAL 20 DAY, 'VIREMENT',
     (SELECT c.id_contrat FROM contrat c JOIN reservation r ON r.id_reservation = c.id_reservation JOIN vehicule v ON v.id_vehicule = r.vehicule_id_vehicule WHERE v.immatriculation = 'AB-123-CD'));

INSERT INTO maintenance (date_debut, date_fin, description, vehicule_id_vehicule) VALUES
  (CURDATE() - INTERVAL 2 DAY, CURDATE() + INTERVAL 4 DAY, 'Remplacement des plaquettes de frein et révision complète',
     (SELECT id_vehicule FROM vehicule WHERE immatriculation = 'KL-678-MN')),
  (CURDATE() - INTERVAL 60 DAY, CURDATE() - INTERVAL 58 DAY, 'Changement des pneus avant',
     (SELECT id_vehicule FROM vehicule WHERE immatriculation = 'GH-012-IJ'));
