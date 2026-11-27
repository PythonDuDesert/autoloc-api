# API REST AutoLoc : endpoints métier

Tous les endpoints sont sous `/api/**` et utilisent HTTP Basic (comptes de la table `utilisateur`).
Réponses d'erreur JSON : `{ "status": 409, "message": "…", "errors": { "champ": "message" } }`.
Les DTO (`web/dto/Dtos.java`) évitent d'exposer les entités JPA et leurs relations bidirectionnelles.

| Ressource | Méthode et route | Rôle |
|---|---|---|
| Véhicules | `GET /api/vehicules`, `GET /api/vehicules/{id}` | USER, ADMIN |
| | `POST /api/vehicules`, `PUT /api/vehicules/{id}`, `DELETE /api/vehicules/{id}` | ADMIN |
| Équipements | `GET /api/equipements` | USER, ADMIN |
| | `POST /api/equipements`, `DELETE /api/equipements/{id}` | ADMIN |
| Réservations | `GET /api/reservations`, `GET /api/reservations/{id}` | USER, ADMIN |
| | `POST /api/reservations`, `PUT /api/reservations/{id}` | USER, ADMIN |
| | `PATCH /api/reservations/{id}/annuler` | USER, ADMIN |
| | `PATCH /api/reservations/{id}/statut` (corps `{"statut":"CONFIRMEE"}`) | ADMIN |
| Clients | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` sur `/api/clients` | ADMIN |
| Agences | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` sur `/api/agences` | ADMIN |
| Contrats | `GET /api/contrats`, `GET /api/contrats/{id}`, `POST /api/contrats` (corps `{"idReservation":1}`) | ADMIN |
| Paiements | `GET /api/paiements[?contrat=ID]`, `POST /api/paiements` | ADMIN |
| Maintenance | `GET /api/maintenances`, `POST /api/maintenances`, `DELETE /api/maintenances/{id}` | ADMIN |

## Règles métier
- Une réservation est créée au statut `En_ATTENTE`. Transitions autorisées : `En_ATTENTE → CONFIRMEE | ANNULEE`, `CONFIRMEE → TERMINEE | ANNULEE`.
- Refus (409) si le véhicule est en `MAINTENANCE` ou déjà réservé (`En_ATTENTE`/`CONFIRMEE`) sur une période qui chevauche.
- Refus (400) si la date de fin précède le début, ou si le début est dans le passé.
- Un contrat ne peut être créé que pour une réservation `CONFIRMEE` sans contrat ; montant = tarif journalier × nombre de jours (minimum 1).
- Email, numéro de permis et immatriculation sont uniques (409 sinon).
