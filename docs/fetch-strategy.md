# Fetch Strategy & Cascade

## Principes généraux

Les associations JPA utilisent `FetchType.LAZY` par défaut dans ce projet afin d'éviter de charger inutilement les entités associées.

Le chargement des données liées est donc effectué uniquement lorsqu'elles sont réellement nécessaires.

Les cascades sont utilisées uniquement lorsque le cycle de vie de l'entité associée dépend de l'entité principale.

---

## Associations

### Agency → Vehicle

- **Mapping :** `@OneToMany` / `@ManyToOne`
- **Fetch :** `LAZY`
- **Cascade :** aucune

Les véhicules sont liés à une agence, mais leur cycle de vie est indépendant. La suppression d'une agence ne doit donc pas entraîner la suppression des véhicules.

---

### Agency → Employee

- **Mapping :** `@OneToMany` / `@ManyToOne`
- **Fetch :** `LAZY`
- **Cascade :** aucune

Les employés sont associés à une agence mais leur cycle de vie est indépendant de celui de l'agence. Aucune cascade n'est donc nécessaire.

---

### Vehicle ↔ Equipment

- **Mapping :** `@ManyToMany`
- **Fetch :** `LAZY`
- **Cascade :** aucune

Les équipements peuvent être associés à plusieurs véhicules et leur cycle de vie est indépendant. Une suppression d'un véhicule ne doit pas supprimer un équipement partagé.

La table de jointure `vehicle_equipment` est gérée automatiquement par Hibernate.

---

### Customer → Reservation

- **Mapping :** `@OneToMany` / `@ManyToOne`
- **Fetch :** `LAZY`
- **Cascade :** `PERSIST`

Une réservation appartient à un client. La cascade `PERSIST` permet de persister les réservations associées lors de la création d'un client, sans propager les opérations de suppression.

---

### Reservation → Vehicle

- **Mapping :** `@ManyToOne`
- **Fetch :** `LAZY`
- **Cascade :** aucune

Une réservation référence un véhicule existant. La réservation ne contrôle pas le cycle de vie du véhicule.

---

### Reservation ↔ Contract

- **Mapping :** `@OneToOne`
- **Fetch :** `LAZY`
- **Cascade :** `ALL` côté `Reservation`

Le contrat est associé à une réservation et son cycle de vie est lié à celle-ci. La cascade `ALL` permet de propager les opérations JPA du côté de la réservation vers le contrat.

Le contrat possède la clé étrangère et constitue donc le côté propriétaire de l'association.

---

### Contract → Payment

- **Mapping :** `@OneToMany` / `@ManyToOne`
- **Fetch :** `LAZY`
- **Cascade :** `ALL`
- **Orphan removal :** `true` recommandé

Un paiement dépend du contrat auquel il appartient. Le contrat constitue donc l'entité principale et les paiements sont des éléments de sa composition.

La cascade `ALL` permet de propager les opérations sur les paiements. `orphanRemoval = true` permet de supprimer un paiement lorsqu'il est retiré de la collection du contrat.

---

### Vehicle → Maintenance

- **Mapping :** `@OneToMany` / `@ManyToOne`
- **Fetch :** `LAZY`
- **Cascade :** `PERSIST`

Une maintenance est liée à un véhicule. La cascade `PERSIST` permet de persister une nouvelle maintenance avec le véhicule, sans rendre le véhicule dépendant du cycle de vie des opérations de suppression.

---

## Synthèse

| Association | Fetch | Cascade |
|---|---|---|
| Agency → Vehicle | LAZY | Aucune |
| Agency → Employee | LAZY | Aucune |
| Vehicle ↔ Equipment | LAZY | Aucune |
| Customer → Reservation | LAZY | PERSIST |
| Reservation → Vehicle | LAZY | Aucune |
| Reservation ↔ Contract | LAZY | ALL |
| Contract → Payment | LAZY | ALL |
| Vehicle → Maintenance | LAZY | PERSIST |

Le choix général est donc de privilégier `LAZY` afin de limiter les chargements inutiles et de réserver les cascades aux relations pour lesquelles le cycle de vie des entités est réellement lié.