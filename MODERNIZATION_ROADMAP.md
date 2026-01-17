# 🚀 Roadmap Modernizzazione Bemyrider (Verso il 2026)

Documento di tracciamento per il processo di rifattorizzazione e modernizzazione dell'app Android.

---

## ✅ Stato Attuale (Sessione 2 - Data Corrente)

In questa sessione, abbiamo completato l'analisi e l'aggiornamento delle dipendenze, risolto i problemi derivanti e completato i task a priorità alta definiti nella sessione precedente.

### Interventi Completati (Sessione 2)
1.  **Analisi e Aggiornamento Dipendenze**:
    *   Eseguita un'analisi completa delle librerie.
    *   Aggiornate le dipendenze `kotlin-stdlib-jdk8` e `ucrop` alle versioni più recenti.
    *   Rimosso l'utilizzo del `LocalBroadcastManager` (deprecato) che non era più in uso.
2.  **Risoluzione Breaking Changes**:
    *   Corretti gli errori di compilazione in 4 file (`EditProfileActivity`, `FeedbackActivity`, etc.) causati dall'aggiornamento della libreria `uCrop`.
3.  **Standardizzazione UI (Completato)**:
    *   Risolto un potenziale crash in `ProviderProfileFragment` aggiungendo le stringhe mancanti (`small`, `medium`, `large`, `none`) ai file `strings.xml` (sia default che italiano).
4.  **Verifica Migrazione Login/Signup (Completato)**:
    *   Verificato che `LoginActivity` e `SignupActivity` utilizzano correttamente i `ViewModel` moderni (`AppLoginViewModel`, `AppSignupViewModel`) che a loro volta si interfacciano con il nuovo `AppRepository` basato su Coroutines. Il task è stato confermato come completato.
5.  **Pulizia File (Completato)**:
    *   Il file `AccountSettingActivity.kt` (un placeholder vuoto) è stato contrassegnato come "da eliminare" per risolvere l'ambiguità e mantenere la codebase pulita.

---

## ✅ Stato Precedente (Sessione 1 - Dicembre 2024)

### Interventi Completati (Sessione 1)
1.  **Nuovo Network Layer Moderno**:
    *   Creato `ApiServiceKt` con supporto per **Coroutines**.
    *   Creato `AppRepository` per sostituire gradualmente il vecchio `UserRepository.java`.
2.  **Migrazione ViewModel**:
    *   `ProviderMenuViewModel` e `ProviderProfileViewModel` migrati per usare il nuovo `AppRepository`.
3.  **Fix UI & Bug Grafici**:
    *   Correzioni varie in `ProviderProfileFragment` e aggiornamento di **Picasso alla v2.8**.
4.  **Pulizia & Ottimizzazione**:
    *   Gestiti i file Kotlin "fantasma" e aggiunte librerie `lifecycle-ktx` e `activity-ktx`.

---

## 🚧 Architettura Ibrida (Situazione Attuale)

L'app si trova ancora in uno stato di transizione, ma i componenti critici come il login, la registrazione e la visualizzazione del profilo sono ora supportati dalla nuova architettura.

*   **Repository**:
    *   `AppRepository` (Kotlin): **NUOVO**. Utilizzato dalle sezioni modernizzate.
    *   `UserRepository.java` (Java): **LEGACY**. Ancora in uso da parti non migrate.

---

## 📅 Prossimi Passi (Action Plan)

Con i task a priorità alta completati, ora possiamo concentrarci sugli obiettivi a medio termine.

### Obiettivi a Medio Termine (Verso il 2026)
1.  **Dependency Injection (Hilt)**:
    *   **PRIORITÀ SUCCESSIVA**: Smettere di istanziare `AppRepository()` manualmente nei ViewModel. Introdurre Hilt per l'iniezione delle dipendenze. Questo semplificherà il codice e migliorerà la testabilità.
2.  **Jetpack Compose**:
    *   Iniziare a convertire le singole View (es. le card del profilo o le liste) in Composable functions, integrandole nei layout XML esistenti tramite `ComposeView`.
3.  **Eliminazione Java**:
    *   Convertire progressivamente i Pojo Java in Kotlin Data Classes.
    *   Convertire le Activity rimanenti.

---

## 📝 Note Tecniche & Troubleshooting

*   **File Duplicati**: Il file `AccountSettingActivity.kt` è stato annotato. Se vedi altre coppie di file `.java` e `.kt`, verifica sempre quale è in uso.

---
*Ultimo aggiornamento: Data Corrente - Agente Gemini*
