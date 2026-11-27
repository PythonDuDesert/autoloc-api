-- AutoLoc : lier des comptes (table utilisateur) à des clients (table client).
-- Un compte USER ne voit et ne gère que les réservations du client auquel il est lié.
-- Un compte sans client lié voit une liste vide et ne peut pas encore réserver.
-- Les ADMIN voient tout, sans lien nécessaire. Un client ne peut être lié qu'à un seul compte.
USE autoloc_db;

UPDATE utilisateur
SET client_id_client = (SELECT id_client FROM client WHERE email = 'claire.martin@example.com')
WHERE username = 'student';

UPDATE utilisateur
SET client_id_client = (SELECT id_client FROM client WHERE email = 'hugo.durand@example.com')
WHERE username = 'olivier';

-- Modèle pour un autre compte (remplacer le username et l'email du client) :
-- UPDATE utilisateur
-- SET client_id_client = (SELECT id_client FROM client WHERE email = 'ines.bernard@example.com')
-- WHERE username = 'erwan';

-- Vérification
SELECT u.username, u.role, c.prenom, c.nom
FROM utilisateur u LEFT JOIN client c ON c.id_client = u.client_id_client;
