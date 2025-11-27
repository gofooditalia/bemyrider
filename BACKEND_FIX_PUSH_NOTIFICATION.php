<?php
/**
 * MODIFICHE DA APPLICARE AL FILE: includes-nct/functions-nct.php
 * 
 * Funzione da modificare: push_notification()
 * 
 * PROBLEMA: Le notifiche push non vengono gestite correttamente quando l'app è in background
 * perché Firebase gestisce automaticamente le notifiche con campo "notification", bypassando onMessageReceived()
 * 
 * SOLUZIONE: Inviare SOLO payload "data" (senza campo "notification") e assicurarsi che tutti i valori siano stringhe
 */

// ============================================
// CODICE ATTUALE (DA SOSTITUIRE)
// ============================================
// Cerca questa sezione nella funzione push_notification() (circa riga 2800-2850):
/*
            } else{
                $fields = array(
                    'to' => $device_token,
                    'data'=> $data_array,
                );

                $headers = array(
                    'Authorization:key=' . API_ACCESS_KEY,
                    'Content-Type:application/json',
                );

                #Send Reponse To FireBase Server
                $ch = curl_init();
                curl_setopt($ch, CURLOPT_URL, 'https://fcm.googleapis.com/fcm/send');
                curl_setopt($ch, CURLOPT_POST, true);
                curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
                curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
                curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
                curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));
                $result = curl_exec($ch);
                curl_close($ch);
            }
*/

// ============================================
// CODICE MODIFICATO (SOSTITUISCI CON QUESTO)
// ============================================
/*
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

                $headers = array(
                    'Authorization:key=' . API_ACCESS_KEY,
                    'Content-Type:application/json',
                );

                #Send Reponse To FireBase Server
                $ch = curl_init();
                curl_setopt($ch, CURLOPT_URL, 'https://fcm.googleapis.com/fcm/send');
                curl_setopt($ch, CURLOPT_POST, true);
                curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
                curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
                curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
                curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));
                $result = curl_exec($ch);
                curl_close($ch);
            }
*/

// ============================================
// SPIEGAZIONE DELLE MODIFICHE
// ============================================
/*
1. CONVERSIONE A STRINGHE:
   - Firebase richiede che tutti i valori nel payload "data" siano stringhe
   - I numeri (es. service_request_id) devono essere convertiti a stringhe
   - I booleani devono essere convertiti a '1' o '0'
   - I null devono essere convertiti a stringa vuota

2. RIMOZIONE CAMPO "notification":
   - NON aggiungere mai il campo "notification" nel payload
   - Se presente, Firebase gestisce automaticamente la notifica quando l'app è in background
   - Questo bypassa onMessageReceived() e impedisce il controllo dell'app sulla notifica

3. CAMPO "sound":
   - Aggiunto campo "sound" nel payload data per garantire che il suono venga riprodotto
   - L'app Android userà questo valore per configurare il suono della notifica

4. RISULTATO:
   - onMessageReceived() viene SEMPRE chiamato (foreground e background)
   - L'app può controllare completamente la notifica e il suono
   - Il suono viene riprodotto correttamente
*/

// ============================================
// ESEMPIO DI PAYLOAD CORRETTO
// ============================================
/*
{
    "to": "device_token_here",
    "data": {
        "notification_type": "s",
        "user_type": "p",
        "service_request_id": "123",  // Stringa, non numero!
        "title": "Nuova richiesta di prenotazione",
        "body": "Hai ricevuto una nuova richiesta",
        "notification_constant": "AC_NT_NOTIFY_ME_WHEN_SERVICE_REQUEST_RECEIVED",
        "provider_service_id": "456",
        "customer_id": "789",
        "provider_id": "101",
        "service_id": "202",
        "sound": "default"
    }
    // NOTA: NON c'è campo "notification" qui!
}
*/

// ============================================
// VERIFICA DOPO LA MODIFICA
// ============================================
/*
1. Invia una notifica di richiesta di prenotazione
2. Metti l'app Android in background
3. Verifica nei log Android:
   - Dovresti vedere: "========== onMessageReceived CALLED =========="
   - Dovresti vedere: "Notification has only 'data' field - onMessageReceived will always be called"
   - NON dovresti vedere: "WARNING: Notification has 'notification' field"
4. Verifica che:
   - La notifica appaia
   - Il suono venga riprodotto
   - La notifica abbia il suono configurato correttamente
*/

