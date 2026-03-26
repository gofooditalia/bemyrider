# Product Requirements Document (PRD): Bacheca Annunci e Ingaggi (Job Board)

## 1. Introduzione e Obiettivi
L'obiettivo di questa funzionalità è evolvere il modello di ingaggio di **Bemyrider** da una richiesta diretta (1:1) a un modello marketplace/bacheca (1:N). 
Attualmente, l'esercente deve contattare singolarmente ogni rider. Con la **Bacheca**, l'esercente pubblica un annuncio e i rider interessati si candidano, permettendo all'esercente di scegliere il profilo migliore in base a rating, velocità e veicolo.

---

## 2. Attori e Flussi Utente

### A. Esercente (Customer)
1. Crea un annuncio dettagliato.
2. Riceve notifiche push per ogni candidatura.
3. Visualizza i candidati e i loro profili.
4. Clicca su "Ingaggia" -> Il Job si chiude e si genera un `ServiceRequest` per il pagamento.

### B. Rider (Partner)
1. Riceve push per nuovi Job nell'area.
2. Consulta la lista "Bacheca".
3. Si candida con un clic.

---

## 3. Database Schema Design (Suggerito)

### Tabella `jobs`
Memorizza i dati degli annunci pubblicati dagli esercenti.

| Campo | Tipo | Note |
| :--- | :--- | :--- |
| `id` | INT (PK) | Auto increment |
| `customer_id` | INT (FK) | ID dell'esercente (User Table) |
| `title` | VARCHAR(100) | Titolo annuncio |
| `description` | TEXT | Dettagli del lavoro |
| `vehicle_required` | ENUM | 'auto', 'moto', 'bici', 'indifferente' |
| `start_at` | DATETIME | Inizio prestazione |
| `end_at` | DATETIME | Fine prestazione |
| `compensation` | DECIMAL(10,2) | Importo offerto |
| `compensation_type`| ENUM | 'fisso', 'orario' |
| `address` | VARCHAR(255) | Indirizzo sede |
| `latitude` | DOUBLE | Per filtri geografici |
| `longitude` | DOUBLE | Per filtri geografici |
| `status` | ENUM | 'open', 'selected', 'completed', 'cancelled' |
| `service_request_id`| INT (FK) | Collegamento a prenotazione reale (Nullable) |
| `created_at` | TIMESTAMP | Data creazione |

### Tabella `job_applications`
Gestisce le candidature dei rider agli annunci.

| Campo | Tipo | Note |
| :--- | :--- | :--- |
| `id` | INT (PK) | Auto increment |
| `job_id` | INT (FK) | Riferimento a `jobs.id` |
| `rider_id` | INT (FK) | ID del rider (User Table) |
| `status` | ENUM | 'pending', 'hired', 'rejected' |
| `applied_at` | TIMESTAMP | Data candidatura |

---

## 4. Specifiche API (Contratto Tecnico)

### Base URL: `/api/v1/jobs/`

#### 1. `POST /create` (Customer)
Input: Oggetto Job completo.
Azione: Salva record + Push ai Rider (Filtro per città/raggio).

#### 2. `GET /available` (Rider)
Input: `user_id`, `lat`, `lng`.
Output: Lista annunci con stato `open`.

#### 3. `POST /apply` (Rider)
Input: `job_id`.
Azione: Inserimento in `job_applications` + Push al Customer.

#### 4. `GET /{job_id}/applicants` (Customer)
Output: Join tra `job_applications` e `users` per mostrare nomi, foto e rating dei candidati.

#### 5. `POST /hire` (Customer)
Input: `job_id`, `rider_id`.
**Logica Cruciale:**
1. Aggiorna `job_applications.status = 'hired'` per il rider scelto.
2. Aggiorna `jobs.status = 'selected'`.
3. **Crea record in `service_requests`** copiando i dati (data, ora, indirizzo, compenso).
4. Ritorna `serviceRequestId` all'app.

---

## 5. Logica Notifiche Push (FCM)

- **NEW_JOB:** payload `{ "type": "NEW_JOB", "job_id": "ID", "click_action": "JOB_BOARD" }`
- **JOB_APPLICATION:** payload `{ "type": "JOB_APPLICATION", "job_id": "ID", "click_action": "JOB_APPLICANTS" }`

---
**Creato da Gemini per il team Backend Bemyrider - 2024**
