-- AutoLoc : extension de autoloc_db.sql pour l'authentification persistante.
-- Cette table est volontairement séparée de client :
-- client = données métier ; utilisateur = identifiants + rôle.

USE autoloc_db;

CREATE TABLE IF NOT EXISTS utilisateur (
    id_utilisateur BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    client_id_client BIGINT NULL,
    PRIMARY KEY (id_utilisateur),
    UNIQUE KEY uk_utilisateur_username (username),
    UNIQUE KEY uk_utilisateur_client (client_id_client),
    CONSTRAINT fk_utilisateur_client
        FOREIGN KEY (client_id_client)
        REFERENCES client(id_client)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

-- Les comptes student/admin sont créés automatiquement par
-- UtilisateurDataInitializer avec des mots de passe BCrypt.
-- Il n'est donc PAS nécessaire de stocker des mots de passe en clair ici.
