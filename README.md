# 🛵 Bemyrider - Android App

Applicazione nativa Android per la gestione di servizi on-demand, dedicata a Rider e Provider.
Il progetto è attualmente in fase di **modernizzazione attiva** per aggiornare lo stack tecnologico agli standard del 2026.

## 🛠 Tech Stack

Il progetto utilizza un'architettura ibrida Java/Kotlin basata su pattern MVVM.

*   **Linguaggi**: Java (Legacy), Kotlin (Nuove feature & Migrazione).
*   **Architettura**: MVVM (Model-View-ViewModel).
*   **Networking**: Retrofit 2, OkHttp, Gson.
    *   *Legacy*: Callbacks e LiveData.
    *   *Moderno*: **Coroutines** e Suspend Functions (`AppRepository`).
*   **UI**: XML Layouts, DataBinding, ViewBinding.
*   **Immagini**: Picasso 2.8.
*   **Pagamenti**: Stripe SDK.
*   **Mappe**: Google Maps SDK, Places API.

## 🚀 Setup del Progetto

1.  **Clona il repository**:
    ```bash
    git clone https://github.com/gofooditalia/bemyrider.git
    ```
2.  **Apri in Android Studio**:
    *   Si consiglia l'ultima versione stabile (Ladybug o successiva).
    *   Attendi il sync di Gradle.
3.  **Configurazione**:
    *   Assicurati di avere il file `google-services.json` nella cartella `app/`.
    *   Verifica che `local.properties` contenga le chiavi API necessarie (es. `GOOGLE_MAPS_API_KEY`).

## 📈 Roadmap Modernizzazione

Stiamo lavorando per portare l'app verso uno stack puramente Kotlin e Jetpack Compose.
Vedi il file `MODERNIZATION_ROADMAP.md` per i dettagli sui progressi e i prossimi passi.

### Obiettivi Recenti Raggiunti
*   ✅ Introduzione Coroutines per chiamate di rete.
*   ✅ Aggiornamento dipendenze critiche (Picasso, AndroidX).
*   ✅ Fix interfaccia Profilo Provider.

## 🤝 Contribuire

Il ramo principale è `main`.
Per le nuove funzionalità, si prega di seguire lo standard Kotlin e utilizzare il nuovo `AppRepository`.

---
*Progetto gestito da GoFood Italia*
