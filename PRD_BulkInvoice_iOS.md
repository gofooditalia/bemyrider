# PRD — Bulk Invoice Download (iOS)

**Versione:** 1.1  
**Data:** 2026-05-10  
**Piattaforma:** iOS  
**Feature:** Download multiplo ricevute per periodo  
**Stato Android:** Rilasciato (v1.4.13)

---

## 1. Contesto

Il sistema BeMyRider genera ricevute PDF lato backend per ogni prenotazione completata.
Fino ad ora l'utente poteva scaricare una sola ricevuta per volta, dalla schermata di dettaglio prenotazione.

È stata implementata una nuova API backend che genera un archivio ZIP contenente tutte le ricevute di un periodo selezionato dall'utente (ultima settimana, ultimo mese o periodo personalizzato). 

---

## 2. Obiettivo

Permettere a utenti (customer) e partner (provider) di scaricare in un unico file ZIP tutte le ricevute delle prenotazioni completate nel periodo selezionato, direttamente dalla schermata della cronologia prenotazioni.

---

## 3. API Backend

### Endpoint

```
POST https://bemyrider.it/ws/bulk-invoices
```

### Parametri (body form-encoded)

| Parametro   | Tipo   | Obbligatorio | Descrizione |
|-------------|--------|:---:|---|
| `user_id`   | int    | ✓ | ID utente loggato |
| `user_type` | string | ✓ | `c` (customer) oppure `p` (provider) |
| `period`    | string | ✓ | `last_week`, `last_month`, oppure `custom` |
| `date_from` | string | Solo se `period=custom` | Data inizio nel formato `YYYY-MM-DD` |
| `date_to`   | string | Solo se `period=custom` | Data fine nel formato `YYYY-MM-DD` |

### Risposta — successo

```json
{
  "status": true,
  "type": "success",
  "message": "",
  "data": {
    "file_name": "https://bemyrider.it/upload-nct/invoice/zips/ricevute-42-last_month-20260509-114523.zip",
    "count": 5
  }
}
```

| Campo | Tipo | Descrizione |
|---|---|---|
| `data.file_name` | string | URL pubblico del file ZIP da scaricare |
| `data.count` | int | Numero di ricevute incluse nello ZIP |

### Risposta — errore

```json
{
  "status": false,
  "type": "error",
  "message": "Nessuna prenotazione completata trovata nel periodo selezionato.",
  "data": []
}
```

### Note tecniche

- Il backend genera i PDF on-demand per ogni prenotazione trovata e li aggrega in un unico ZIP.
- Limite massimo: **30 prenotazioni** per richiesta (salvaguardia timeout server).
- Lo ZIP per la stessa coppia utente+periodo viene sovrascritto ad ogni richiesta (non si accumulano).
- **Importante:** La chiamata può impiegare fino a 30-60 secondi in base al numero di ricevute. Impostare un timeout client adeguato (almeno 120s).

---

## 4. UX / UI

### Punto di accesso

Il pulsante di download bulk va posizionato nella schermata **Cronologia Prenotazioni** (tab "PAST" / prenotazioni completate), visibile per entrambi i profili utente e partner.

Su Android è stato implementato come **FloatingActionButton** (FAB) posizionato in basso a destra, con icona download (freccia verso il basso). 

### Flusso

```
[Schermata Cronologia]
        │
        ▼ tap icona download
[Dialog: "Seleziona periodo"]
   ├── Ultima settimana      → period = "last_week"
   ├── Ultimo mese           → period = "last_month"
   └── Periodo personalizzato → period = "custom" (mostra date picker)
        │
        ▼ selezione
[Indicatore di caricamento / spinner (OBBLIGATORIO)]
        │
        ├── Successo → avvia download ZIP
        │              mostra notifica: "Download avviato."
        │
        └── Errore   → mostra messaggio errore (es. "Nessuna prenotazione")
```

### Stringhe UI

| Chiave | Testo |
|---|---|
| Titolo dialog | "Seleziona periodo" |
| Opzione 1 | "Ultima settimana" |
| Opzione 2 | "Ultimo mese" |
| Opzione 3 | "Periodo personalizzato" |
| Toast successo | "Download avviato. Controlla la cartella Download." |
| Errore nessun dato | "Nessuna prenotazione completata nel periodo selezionato." |
| Label icona | "Download Ricevute" |

---

## 5. Comportamento del download

- Il file ZIP deve essere salvato in una posizione accessibile dall'utente (es. cartella Download / Files app / iCloud).
- Al completamento mostrare una notifica o un'opzione di condivisione (`UIActivityViewController`).

---

## 6. Modello dati (risposta API)

```swift
// Esempio Swift
struct BulkInvoiceResponse: Decodable {
    let status: Bool
    let type: String
    let message: String
    let data: BulkInvoiceData?
}

struct BulkInvoiceData: Decodable {
    let fileName: String
    let count: Int

    enum CodingKeys: String, CodingKey {
        case fileName = "file_name"
        case count
    }
}
```

---

## 7. Note per lo Sviluppatore iOS (Legacy Fix)

Durante l'implementazione su Android è stato riscontrato un problema di timeout e crash sul server quando venivano richieste più di 2 ricevute. Il backend è stato corretto (usando `require_once` per mPDF e aumentando i limiti di memoria).

Tuttavia, lato client iOS è fondamentale:
1.  **Aumentare il Timeout**: La richiesta POST potrebbe essere lenta (generazione di molti PDF lato server). Impostare il timeout della sessione di rete a **120 secondi**.
2.  **Gestione Errori**: Se il server restituisce una risposta vuota o un errore 500, gestire il caso con un messaggio "Errore del server durante la generazione dei PDF".

---

## 8. Riferimenti

- **Android PR di riferimento:** `ServiceHistoryActivity.java`, `bulk_invoice.php`
- **Backend:** `ws/service-nct/bulk_invoice.php`
- **Cartella ZIP sul server:** `upload-nct/invoice/zips/`
