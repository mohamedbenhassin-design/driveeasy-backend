# DriveEasy · Backend (microservices Spring Cloud)

Prototype de plateforme de location de véhicules pour la société **DriveEasy**, réalisé avec une
architecture microservices : **Java 21 · Spring Boot 3.5 · Spring Cloud 2025.0 (Eureka, Config, Gateway) · Zipkin**.

Les données sont stockées **uniquement en mémoire (RAM)**, sans base de données, comme l'exige le cahier des charges.
Un jeu de données de démonstration est chargé à chaque démarrage.

## Architecture

```
                    ┌────────────────────┐
  Angular :4200 ──▶ │  API Gateway :8080 │  routes lb://… + CORS + filtre X-Request-Id
                    └─────────┬──────────┘
            ┌─────────────────┼──────────────────┐
            ▼                 ▼                  ▼
  vehicule-service:8081  client-service:8082  location-service:8083
            ▲                 ▲                  │
            └──── appels REST synchrones ────────┘  (éligibilité, statut véhicule, fidélité)

  service-registry :8761 (Eureka) · config-server :8888 · Zipkin :9411 (Docker)
```

| Composant          | Port | Rôle |
|--------------------|------|------|
| service-registry   | 8761 | Annuaire Eureka (register-with-eureka=false, fetch-registry=false) |
| config-server      | 8888 | Configuration centralisée (`config-server/src/main/resources/config/*.yml`) |
| vehicule-service   | 8081 | Parc automobile, catégories, statut, disponibilité |
| client-service     | 8082 | Clients, permis, éligibilité, catégorie de fidélité |
| location-service   | 8083 | Cycle de vie des locations, tarification, statistiques, orchestration |
| api-gateway        | 8080 | Point d'entrée unique, load balancing `lb://`, CORS, filtre personnalisé |
| Zipkin             | 9411 | Traçage distribué (`docker compose up -d`) |

## Prérequis

- JDK **21**
- Maven 3.9+
- Docker (pour Zipkin)

## Lancement pas à pas

```bash
# 1. Compiler tous les modules (depuis driveeasy-backend/)
mvn clean package

# 2. Démarrer Zipkin
docker compose up -d

# 3. Démarrer les services DANS CET ORDRE (un terminal par service)
java -jar service-registry/target/service-registry-1.0.0.jar
java -jar config-server/target/config-server-1.0.0.jar
java -jar vehicule-service/target/vehicule-service-1.0.0.jar
java -jar client-service/target/client-service-1.0.0.jar
java -jar location-service/target/location-service-1.0.0.jar
java -jar api-gateway/target/api-gateway-1.0.0.jar
```

Sous Windows, le script `start-all.ps1` fait tout cela automatiquement :

```powershell
powershell -ExecutionPolicy Bypass -File .\start-all.ps1
```

> Après le démarrage, la gateway peut répondre `503` pendant ~30 secondes : c'est le temps qu'elle récupère
> le registre Eureka. Le tableau de bord Eureka (http://localhost:8761) doit afficher les 5 instances.

## Configuration centralisée

Chaque service ne contient qu'un `application.yml` minimal :

```yaml
spring:
  application.name: vehicule-service
  config.import: configserver:http://localhost:8888
```

Tout le reste est servi par le Config Server :

| Fichier                     | Contenu |
|-----------------------------|---------|
| `application.yml`           | commun : Eureka, Actuator, Zipkin (probabilité 1.0), format des logs |
| `application-prod.yml`      | profil `prod` : échantillonnage 10 %, pas de données de démo |
| `vehicule-service.yml`      | port 8081, stockage en mémoire |
| `client-service.yml`        | port 8082, âge minimum (21) |
| `location-service.yml`      | port 8083, tarification (coefficients, remises, pénalité, seuils de fidélité) |
| `api-gateway.yml`           | port 8080, routes, CORS |

Vérification : http://localhost:8888/location-service/default

**Configuration sur GitHub** : les mêmes fichiers sont publiés dans le dépôt
[driveeasy-config](https://github.com/mohamedbenhassin-design/driveeasy-config). Pour que le Config Server les lise
depuis GitHub, le lancer avec le profil `git` :

```bash
java -jar config-server/target/config-server-1.0.0.jar --spring.profiles.active=git
```

## API (via la gateway : `http://localhost:8080/api/...`)

### vehicule-service
| Méthode | URI | Description |
|---|---|---|
| GET | `/api/vehicules?categorie=&carburant=&statut=&prixMax=` | Liste filtrée |
| GET | `/api/vehicules/{id}` | Détail |
| POST | `/api/vehicules` | Création (409 si immatriculation existante) |
| PUT | `/api/vehicules/{id}` | Modification |
| DELETE | `/api/vehicules/{id}` | Suppression (409 si LOUE) |
| PATCH | `/api/vehicules/{id}/statut` | `{ "statut": "LOUE", "kilometrage": 12000 }` |
| GET | `/api/vehicules/disponibles?dateDebut=&dateFin=` | Véhicules réservables |

### client-service
| Méthode | URI | Description |
|---|---|---|
| GET | `/api/clients?q=` (ou `nom`, `cin`, `email`) | Recherche |
| GET | `/api/clients/{id}` | Détail (âge et validité du permis calculés) |
| POST / PUT | `/api/clients`, `/api/clients/{id}` | CIN, email, n° de permis uniques (409) |
| DELETE | `/api/clients/{id}` | 409 si location active (appel à location-service) |
| GET | `/api/clients/{id}/eligibilite` | âge ≥ 21 ans et permis valide |
| PATCH | `/api/clients/{id}/categorie` | `{ "categorieClient": "VIP" }` |

### location-service
| Méthode | URI | Description |
|---|---|---|
| GET | `/api/locations?statut=&clientId=&vehiculeId=&dateDebut=&dateFin=&page=0&size=10` | Liste paginée |
| GET | `/api/locations/{id}` | Détail enrichi (client + véhicule agrégés) |
| POST | `/api/locations` | Réservation (client éligible + véhicule disponible + pas de chevauchement) |
| POST | `/api/locations/simulation` | Simulation du prix |
| PATCH | `/api/locations/{id}/depart` | RESERVEE → EN_COURS, véhicule → LOUE |
| PATCH | `/api/locations/{id}/retour` | `{ "kmRetour": 27500, "dateRetourEffectif": "2026-11-22" }` |
| PATCH | `/api/locations/{id}/annulation` | RESERVEE → ANNULEE |
| GET | `/api/locations/statistiques` | CA, taux d'occupation, top véhicules |
| GET | `/api/locations/vehicules-disponibles?dateDebut=&dateFin=` | Véhicules libres sur la période |

### Règles de tarification
- Prix = nbJours × prixJournalier × coefficient catégorie − remise fidélité (FIDELE 5 %, VIP 10 %)
- Coefficients : ECONOMIQUE 1.0 · COMPACTE 1.1 · BERLINE 1.2 · SUV 1.3 · LUXE 1.5
- Pénalité de retard : 1,5 × prix journalier × jours de retard
- Fidélité : FIDELE après 5 locations terminées, VIP après 15 (mise à jour automatique au retour)

### Codes HTTP
`200` OK · `201` création · `204` suppression · `400` validation / règle métier · `404` introuvable ·
`409` conflit (unicité, statut) · `503` service distant indisponible · `500` erreur interne.
Les erreurs ont un format JSON uniforme : `{ timestamp, status, error, message, path, details }`.

### Cohérence en cas d'échec inter-services
- **Départ** : le véhicule passe à LOUE d'abord ; si l'appel échoue, la location reste RESERVEE.
  Si l'enregistrement local échoue ensuite, le véhicule est remis DISPONIBLE (compensation).
- **Retour** : le véhicule est remis DISPONIBLE avant de clôturer ; si l'appel échoue, la location reste EN_COURS.
- **Fidélité** : non bloquante, un échec est journalisé sans annuler le retour.

## Jeu de données de démonstration

- **8 véhicules** (dont la Golf `198 TU 8899` LOUE et le Duster `190 TU 3344` en MAINTENANCE)
- **6 clients** : Sami Ben Ali (FIDELE), Ines Trabelsi, Youssef Gharbi (VIP), Leila Hamdi,
  Amira Mansour (**non éligible : 19 ans**), Karim Jaziri (**non éligible : permis expiré**)
- **9 locations** : 7 terminées (historique pour les statistiques), 1 en cours, 1 réservée

Aucun compte de test : l'application n'a pas d'authentification.

## Traçage Zipkin

Tous les services exportent leurs traces vers `http://localhost:9411` (probabilité 1.0).
Exemple de requête traversant gateway → location-service → client-service + vehicule-service :

```bash
curl -X POST http://localhost:8080/api/locations/simulation -H "Content-Type: application/json" \
  -d '{"clientId":1,"vehiculeId":5,"dateDebut":"2026-12-01","dateFinPrevue":"2026-12-04"}'
```

Puis ouvrir http://localhost:9411 → *Run query* → sélectionner la trace pour voir le temps passé dans chaque service.

## Filtre personnalisé de la gateway

`RequestLoggingFilter` (GlobalFilter) ajoute un en-tête `X-Request-Id` transmis aux microservices,
renvoie `X-Response-Time` et journalise chaque requête (méthode, chemin, statut, durée).

## Tests

```bash
mvn test
```
Tests unitaires de la tarification : `location-service/src/test/.../TarificationServiceTest.java`.

## Captures d'écran

_À ajouter : tableau de bord Eureka, requêtes Postman, traces Zipkin._
