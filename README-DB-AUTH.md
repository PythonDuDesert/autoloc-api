# AutoLoc — authentification MySQL persistante

## Ce qui change

L'inscription n'utilise plus `InMemoryUserDetailsManager`.

Le compte est enregistré dans MySQL, table `utilisateur`, avec :

- `username` unique
- `password` stocké sous forme BCrypt
- `role` (`USER` ou `ADMIN`)
- `client_id_client` optionnel pour relier ultérieurement un compte à un client métier

La table `client` reste dédiée aux données métier. Aucun mot de passe n'est ajouté dans `client`.

## Installation

1. Garder la base `autoloc_db` existante.
2. Exécuter `database/autoloc_db_auth.sql` dans MySQL/phpMyAdmin.
3. Copier les fichiers Java dans le projet Spring Boot en respectant les chemins.
4. Remplacer `SecurityConfig.java`.
5. Remplacer `src/main/resources/static/js/api.js` et `app.js`.
6. Lancer :

   `mvnw.cmd spring-boot:run`

Avec `spring.jpa.hibernate.ddl-auto=update`, Hibernate vérifiera aussi la structure de `utilisateur`.

## Comptes de démonstration

Au démarrage, `UtilisateurDataInitializer` crée dans MySQL s'ils n'existent pas :

- `student / student123` → USER
- `admin / admin123` → ADMIN

Les mots de passe sont hashés en BCrypt avant insertion.

## Inscription

Le frontend appelle :

`POST /api/public/register`

Exemple JSON :

```json
{
  "username": "olivier",
  "password": "motdepasse123"
}
```

Réponse attendue : HTTP 201.

Un second compte avec le même username reçoit HTTP 409.

## Connexion

La connexion continue à utiliser HTTP Basic, mais Spring Security charge maintenant les utilisateurs depuis `utilisateur` via JPA.

Le frontend ne stocke pas le mot de passe dans `sessionStorage`.
