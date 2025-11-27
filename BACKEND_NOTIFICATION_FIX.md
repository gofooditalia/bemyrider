# Fix Notifiche Push Acustiche - Modifiche Backend

## Problema Identificato

Quando il backend invia notifiche Firebase con il campo `"notification"` (oltre a `"data"`), se l'app Android è in background, Firebase gestisce automaticamente la notifica **SENZA** chiamare `onMessageReceived()`. Questo causa:

- ❌ Nessun suono di notifica
- ❌ Nessun controllo da parte dell'app sulla notifica
- ❌ Il canale di notifica configurato non viene utilizzato correttamente

## Soluzione

Il backend deve inviare **SOLO payload "data"** (senza campo "notification") per le notifiche push. In questo modo:

- ✅ `onMessageReceived()` viene **sempre** chiamato (foreground e background)
- ✅ L'app può controllare completamente la notifica e il suono
- ✅ Il suono viene riprodotto correttamente

## Modifiche Richieste al Backend

### File da Modificare

**File:** `includes-nct/functions-nct.php`  
**Funzione:** `push_notification()` (circa riga 2800-2850)  
**Sezione:** Codice per dispositivi Android (quando `$device_type != 'i' && $device_type != 'w'`)

### Prima (❌ SBAGLIATO - causa il problema)

```json
{
  "notification": {
    "title": "Nuova richiesta di prenotazione",
    "body": "Hai ricevuto una nuova richiesta"
  },
  "data": {
    "notification_type": "s",
    "user_type": "p",
    "service_request_id": "123",
    "title": "Nuova richiesta di prenotazione",
    "body": "Hai ricevuto una nuova richiesta"
  }
}
```

### Dopo (✅ CORRETTO - risolve il problema)

```json
{
  "data": {
    "notification_type": "s",
    "user_type": "p",
    "service_request_id": "123",
    "title": "Nuova richiesta di prenotazione",
    "body": "Hai ricevuto una nuova richiesta",
    "sound": "default"
  }
}
```

## Dettagli Tecnici

### Payload "data" Richiesto

Per le notifiche di richiesta di prenotazione (rider/provider), il payload deve contenere:

```json
{
  "data": {
    "notification_type": "s",           // "s" = service request
    "user_type": "p",                    // "p" = provider (rider), "c" = customer
    "service_request_id": "123",         // ID della richiesta
    "title": "Titolo della notifica",    // Titolo da mostrare
    "body": "Corpo della notifica",      // Corpo da mostrare
    "notification_constant": "...",      // Costante opzionale
    "provider_service_id": "...",        // Opzionale per customer
    "customer_id": "...",                // Opzionale per provider
    "provider_id": "...",                // Opzionale per customer
    "service_id": "...",                 // Opzionale per messaggi
    "sound": "default"                   // Opzionale, suggerito
  }
}
```

### Modifiche Specifiche per functions-nct.php

**File:** `includes-nct/functions-nct.php`  
**Funzione:** `push_notification()` (circa riga 2800-2850)  
**Sezione:** Codice per dispositivi Android (quando `$device_type != 'i' && $device_type != 'w'`)

**Cerca questa sezione:**
```php
} else{
    $fields = array(
        'to' => $device_token,
        'data'=> $data_array,
    );
    // ... resto del codice curl ...
}
```

**Sostituisci con:**
```php
} else{
    // IMPORTANTE: Converti tutti i valori in $data_array a stringhe
    // Firebase richiede che tutti i valori nel payload "data" siano stringhe
    $data_payload = array();
    foreach ($data_array as $key => $value) {
        // Converti tutti i valori a stringhe (inclusi numeri, booleani, null)
        if ($value === null) {
            $data_payload[$key] = '';
        } else if (is_bool($value)) {
            $data_payload[$key] = $value ? '1' : '0';
        } else {
            $data_payload[$key] = (string)$value;
        }
    }
    
    // Assicurati che ci sia un campo "sound" nel payload data
    if (!isset($data_payload['sound'])) {
        $data_payload['sound'] = 'default';
    }
    
    // IMPORTANTE: Invia SOLO "data", NON includere "notification"
    // Se includi "notification", Firebase gestirà automaticamente la notifica
    // quando l'app è in background, bypassando onMessageReceived()
    $fields = array(
        'to' => $device_token,
        'data' => $data_payload,
        // NON aggiungere 'notification' qui!
    );
    // ... resto del codice curl rimane uguale ...
}
```

**Spiegazione:**
1. **Conversione a stringhe:** Tutti i valori devono essere stringhe (numeri, booleani, null)
2. **Nessun campo "notification":** NON aggiungere mai il campo "notification" nel payload
3. **Campo "sound":** Aggiunto per garantire che il suono venga riprodotto

### Esempio Codice Backend (PHP con Firebase Admin SDK o cURL)

**Prima:**
```php
$message = [
    'notification' => [
        'title' => $title,
        'body' => $body,
    ],
    'data' => [
        'notification_type' => 's',
        'user_type' => 'p',
        'service_request_id' => $requestId,
        'title' => $title,
        'body' => $body,
    ],
    'token' => $deviceToken
];
```

**Dopo:**
```php
$message = [
    // RIMUOVI completamente il campo "notification"
    'data' => [
        'notification_type' => 's',
        'user_type' => 'p',
        'service_request_id' => (string)$requestId,  // Converti a stringa
        'title' => $title,
        'body' => $body,
        'sound' => 'default'
    ],
    'token' => $deviceToken
];

// Se usi cURL direttamente:
$fields = [
    'to' => $deviceToken,
    'data' => [
        'notification_type' => 's',
        'user_type' => 'p',
        'service_request_id' => (string)$requestId,
        'title' => $title,
        'body' => $body,
        'sound' => 'default'
    ]
    // NON includere 'notification' qui!
];
```

### Esempio Codice Backend (Node.js con Firebase Admin SDK)

**Prima (❌ SBAGLIATO):**
```javascript
const admin = require('firebase-admin');
const message = {
    notification: {
        title: title,
        body: body,
    },
    data: {
        notification_type: 's',
        user_type: 'p',
        service_request_id: requestId,
        title: title,
        body: body,
    },
    token: deviceToken
};

await admin.messaging().send(message);
```

**Dopo (✅ CORRETTO):**
```javascript
const admin = require('firebase-admin');
const message = {
    // RIMUOVI completamente il campo "notification"
    data: {
        notification_type: 's',
        user_type: 'p',
        service_request_id: requestId,
        title: title,
        body: body,
        sound: 'default'
    },
    token: deviceToken
};

await admin.messaging().send(message);
```

**IMPORTANTE:** Tutti i valori in `data` devono essere stringhe! Se hai numeri, convertili:
```javascript
const message = {
    data: {
        notification_type: 's',
        user_type: 'p',
        service_request_id: String(requestId),  // Converti a stringa
        title: title,
        body: body,
        sound: 'default'
    },
    token: deviceToken
};
```

## Verifica

Dopo la modifica, quando invii una notifica:

1. L'app Android riceverà sempre `onMessageReceived()` (anche in background)
2. Nei log vedrai: `========== onMessageReceived CALLED ==========`
3. Il suono verrà riprodotto correttamente
4. La notifica avrà `sound` configurato nel dump delle notifiche

## Note Importanti

- **NON** rimuovere il campo `"data"` - è essenziale
- **RIMUOVI** completamente il campo `"notification"` 
- Il titolo e il corpo devono essere nel payload `"data"`, non in `"notification"`
- L'app Android gestirà la visualizzazione della notifica usando i dati dal payload `"data"`

## Test

Per testare:

1. Invia una notifica di richiesta di prenotazione
2. Metti l'app in background
3. Verifica che:
   - La notifica appaia
   - Il suono venga riprodotto
   - Nei log appaia `onMessageReceived CALLED`

## Riferimenti

- [Firebase Cloud Messaging - Data Messages](https://firebase.google.com/docs/cloud-messaging/concept-options#data-messages)
- [Android - onMessageReceived behavior](https://firebase.google.com/docs/cloud-messaging/android/receive#handling-messages)

