# Spring Security — Notes

## Objectif

Ajouter une authentification et une autorisation par rôles à l'API REST AutoLoc avec Spring Security.

## Configuration

Spring Security est ajouté avec :

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

La configuration utilise :

- une authentification en mémoire (`InMemoryUserDetailsManager`) ;
- deux utilisateurs ;
- des mots de passe encodés avec BCrypt ;
- HTTP Basic ;
- une autorisation basée sur les rôles.

### Utilisateurs

| Utilisateur | Mot de passe | Rôle |
|---|---|---|
| `student` | `student123` | USER |
| `admin` | `admin123` | ADMIN |

Ces identifiants sont uniquement destinés à l'exercice et ne doivent pas être utilisés en production.

## Règles d'accès

```text
/api/public/**  -> accès libre
/api/admin/**   -> rôle ADMIN requis
/api/**         -> rôle USER ou ADMIN requis
autres routes   -> authentification requise
```

L'ordre des règles est important : `/api/admin/**` doit être évalué avant `/api/**`, car `/api/**` est une règle plus générale.

## Résultats des tests

| Requête | Authentification | Résultat attendu | Résultat |
|---|---|---:|---:|
| `GET /api/public/ping` | Aucune | 200 | 200 OK |
| `GET /api/hello` | Aucune | 401 | 401 Unauthorized |
| `GET /api/hello` | `student / student123` | 200 | 200 OK |
| `GET /api/admin/report` | `student / student123` | 403 | 403 Forbidden |
| `GET /api/admin/report` | `admin / admin123` | 200 | 200 OK |

### Différence entre 401 et 403

- **401 Unauthorized** : aucune authentification valide n'a été fournie.
- **403 Forbidden** : l'utilisateur est authentifié mais ne possède pas les droits nécessaires.

## Pourquoi BCrypt ?

Les mots de passe ne sont pas stockés en clair. BCrypt permet de stocker une empreinte du mot de passe et applique un mécanisme de salage afin de rendre les attaques par pré-calcul plus difficiles.

## Pourquoi l'authentification en mémoire ?

L'authentification en mémoire est adaptée à cet exercice car elle permet de mettre rapidement en place les rôles sans ajouter une nouvelle entité ou une table utilisateur.

Elle n'est pas adaptée à une application de production : les utilisateurs ne sont pas persistés dans la base de données et la gestion des comptes est limitée.

## Passage à une authentification avec MySQL

Pour utiliser des utilisateurs et des rôles stockés dans MySQL, il faudrait notamment :

1. créer une entité `Utilisateur` ;
2. créer une entité ou une représentation des rôles ;
3. créer les tables correspondantes ;
4. créer un repository JPA ;
5. implémenter un `UserDetailsService` qui charge l'utilisateur depuis la base ;
6. encoder les mots de passe avec BCrypt ;
7. configurer Spring Security pour utiliser ce `UserDetailsService`.

## Sécurité

Les mots de passe utilisés dans cet exercice sont des identifiants de démonstration. Aucun mot de passe réel ne doit être commité dans le dépôt.

## Validation

La configuration Spring Security a été validée avec les trois niveaux d'accès demandés :

- accès public ;
- accès authentifié USER/ADMIN ;
- accès réservé à ADMIN.
