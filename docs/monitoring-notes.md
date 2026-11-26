# Monitoring — Prometheus & Grafana

## Architecture
Spring Boot Actuator + Micrometer exposent les métriques au format Prometheus sur `/actuator/prometheus`.
Prometheus (conteneur Docker) scrape cet endpoint toutes les 5 s via `host.docker.internal:8080`.
Grafana (conteneur Docker) interroge Prometheus (`http://prometheus:9090`) et affiche le dashboard
« AutoLoc — Application Monitoring ».

App Spring Boot → /actuator/prometheus → Prometheus (9090) → Grafana (3000)

## Métriques sélectionnées
| Panel | Requête PromQL | Rôle |
|---|---|---|
| JVM memory used | `jvm_memory_used_bytes` | Mémoire heap / non-heap utilisée par la JVM |
| CPU usage | `process_cpu_usage` | Charge CPU du processus Spring Boot |
| HTTP request rate | `sum(rate(http_server_requests_seconds_count[1m]))` | Requêtes HTTP par seconde |
| HTTP p95 latency | `histogram_quantile(0.95, sum by (le) (rate(http_server_requests_seconds_bucket[5m])))` | Latence au 95e percentile |
| Live threads | `jvm_threads_live_threads` | Nombre de threads JVM actifs |

Lors du test de charge (requêtes répétées sur `/api/public/ping` et `/api/hello`), le débit passe
d'environ 0,2 à 0,55 req/s et la latence p95 reste autour de 0,13 s.

## Problèmes rencontrés et résolus
- **Endpoints Actuator protégés par Spring Security** : `/actuator/health` renvoyait une demande
  d'authentification. Solution : `.requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()`
  placé avant les règles plus larges.
- **Dépendances ignorées** : les dépendances actuator/prometheus avaient été ajoutées en dehors de la
  balise `<dependencies>` du `pom.xml`. Solution : les déplacer à l'intérieur puis recharger Maven.
- **docker-compose invalide** (`services.services.grafana false schema`) : clé `services:` dupliquée.
- **Panel p95 vide** : activation des buckets d'histogramme avec
  `management.metrics.distribution.percentiles-histogram.http.server.requests=true`.

## Sécurité
Les identifiants Grafana par défaut (admin/admin) ont été changés. En production, les endpoints
Actuator ne doivent pas être exposés publiquement (authentification du scrape Prometheus).