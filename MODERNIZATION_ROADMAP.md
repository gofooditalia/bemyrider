# 🚀 Roadmap Modernizzazione Bemyrider (Verso il 2026)

Documento di tracciamento per il processo di rifattorizzazione e modernizzazione dell'app Android.

---

## ✅ Stato Attuale (Sessione 1 - Dicembre 2024)

Abbiamo iniziato la migrazione del **Core Network Layer** e risolto problemi critici di UI.

### Interventi Completati
1.  **Nuovo Network Layer Moderno**:
    *   Creato `ApiServiceKt` (Kotlin Interface) con supporto nativo per **Coroutines** (`suspend functions`).
    *   Creato `AppRepository` (in `UserRepository.kt`) che sostituisce gradualmente il vecchio `UserRepository.java`. Gestisce parsing degli errori e thread safety.
2.  **Migrazione ViewModel**:
    *   `ProviderMenuViewModel` e `ProviderProfileViewModel` sono stati migrati per usare il nuovo `AppRepository`.
    *   Abbandonato l'uso dei Callback Retrofit a favore di `viewModelScope` e `liveData { emit(...) }`.
3.  **Fix UI & Bug Grafici**:
    *   Implementato correttamente `updateUI` in `ProviderProfileFragment`.
    *   Risolto problema caricamento foto profilo (mancava gestione Picasso).
    *   Risolto problema toggle "Disponibile ora" (mancava logica di binding).
4.  **Pulizia & Ottimizzazione**:
    *   Aggiornato **Picasso alla v2.8** (rimosso warning Jetifier e migliorata compatibilità AndroidX).
    *   Gestiti i file Kotlin "fantasma" (`AccountSettingActivity.kt` è ora un placeholder documentato).
    *   Aggiunte librerie `lifecycle-ktx` e `activity-ktx` per supporto Kotlin moderno.

---

## 🚧 Architettura Ibrida (Situazione Attuale)

L'app si trova in uno stato di transizione. È fondamentale capire come convivono i due mondi per non rompere le funzionalità.

*   **Repository**:
    *   `AppRepository` (Kotlin): **NUOVO**. Da usare per tutte le nuove feature e le migrazioni.
    *   `UserRepository.java` (Java): **LEGACY**. Ancora usato dalla maggior parte delle Activity/Fragment non migrati. **NON CANCELLARE** finché la migrazione non è completa.
*   **Modelli (Pojo)**:
    *   Alcuni sono in Kotlin (`ProfilePojo`), altri in Java (`CommonPojo`).
    *   Attenzione ai setter: Kotlin preferisce `obj.status = true`, Java richiede `obj.setStatus(true)`. In caso di dubbio, usare i setter espliciti.

---

## 📅 Prossimi Passi (Action Plan)

### Priorità Alta (Prossima Sessione)
1.  **Pulizia Manuale File**:
    *   Eliminare fisicamente `AccountSettingActivity.kt` se si decide di rifarlo da zero, o popolarlo migrando la logica dal Java.
2.  **Migrazione Login/Signup**:
    *   Migrare `LoginActivity` e `SignupActivity` (e i relativi ViewModel) per usare `AppRepository`. Queste sono aree critiche che beneficerebbero della robustezza delle Coroutines.
3.  **Standardizzazione UI**:
    *   Spostare le stringhe hardcoded (es. "Small, Medium" in `ProviderProfileFragment`) nel file `strings.xml`.

### Obiettivi Medio Termine (Verso il 2026)
1.  **Dependency Injection (Hilt)**:
    *   Smettere di istanziare `AppRepository()` manualmente nei ViewModel. Introdurre Hilt per l'iniezione delle dipendenze.
2.  **Jetpack Compose**:
    *   Iniziare a convertire le singole View (es. le card del profilo o le liste) in Composable functions, integrandole nei layout XML esistenti tramite `ComposeView`.
3.  **Eliminazione Java**:
    *   Convertire progressivamente i Pojo Java in Kotlin Data Classes.
    *   Convertire le Activity rimanenti.

---

## 📝 Note Tecniche & Troubleshooting

*   **Jetifier**: Abbiamo aggiornato Picasso per evitare l'uso di Jetifier. Se in futuro aggiungi librerie vecchie, potresti doverlo riabilitare (sconsigliato).
*   **Picasso**: Usare sempre `Picasso.get()` (versione moderna).
*   **File Duplicati**: Se vedi coppie di file `.java` e `.kt` con lo stesso nome, verifica quale è in uso nel `AndroidManifest.xml` o negli import.

---
*Ultimo aggiornamento: Dicembre 2024 - Agente Gemini*
