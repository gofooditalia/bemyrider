# Note di Rilascio - BeMyRider v1.1.4

## Versione 1.1.4 (Build 15)

### 🐛 Correzioni Bug Critiche
- 🔧 **Upload Immagini Profilo**: Risolto il problema "file upload migration pending" che impediva il caricamento delle immagini profilo
- 🔧 **Multipart Request**: Implementato correttamente il metodo di upload multipart per il caricamento dei file
- 🔧 **Gestione Errori**: Migliorata la gestione degli errori HTTP durante l'upload con lettura corretta delle risposte del server
- 🔧 **Validazione File**: Aggiunto controllo di esistenza del file prima dell'upload per prevenire errori

### ⚙️ Miglioramenti Tecnici
- 📦 Migliorata la gestione delle richieste multipart in `WebServiceCall`
- 📦 Aggiunto logging dettagliato per il debug degli upload
- 📦 Ottimizzata la gestione degli stream di errore HTTP

---

## Versione 1.1.3 (Build 14)

### 🎯 Conformità Play Store
- ✅ Aggiornamento a Target SDK 35 (Android 15) per conformità con i requisiti Google Play 2025
- ✅ Miglioramento della sicurezza di rete con configurazione HTTPS obbligatoria
- ✅ Aggiornamento di tutte le dipendenze alle versioni più recenti e stabili

### 🎨 Miglioramenti UI/UX
- ✨ **Splash Screen**: Migliorato il design del pulsante "Continua" con stile moderno e ombre
- ✨ **Onboarding**: Corretta la posizione dei pulsanti per evitare sovrapposizioni con la barra di navigazione
- ✨ **Registrazione**: Ottimizzati spaziature e dimensioni degli elementi per una migliore usabilità
- 🔧 **Layout**: Risolti i problemi di layout su tutte le pagine (customer e provider) dove il contenuto veniva oscurato dalla barra superiore
- 🔧 **Profilo Provider**: Corretta la posizione dell'immagine profilo per renderla raggiungibile
- 🔧 **Editor Foto**: Migliorata l'interfaccia di modifica foto profilo con pulsanti correttamente posizionati

### 🐛 Correzioni Bug
- 🔧 **Logout**: Risolto il problema di logout che non riportava correttamente alla schermata di login
- 🔧 **Provider**: Eliminato il messaggio "please provide valid data" che appariva alla chiusura dell'app per account provider
- 🔧 **Lingua**: Corretto il problema della lingua di default che veniva impostata in inglese all'avvio
- 🔧 **Slide Introduttive**: Ripristinate le slide di presentazione al primo avvio dell'app
- 🔧 **Permessi**: Risolti i problemi di permessi per il caricamento delle immagini profilo su Android 13+

### 📱 Notifiche Push
- 🔔 Migliorata la gestione delle notifiche push con supporto completo per Android 13+
- 🔊 Aggiunto il supporto per i suoni di notifica
- 🔔 Implementata la richiesta corretta dei permessi per le notifiche su Android 13+
- 🧪 Aggiunto pulsante di test notifiche nelle impostazioni account

### 🔒 Sicurezza
- 🔐 Migliorata la gestione dei dati sensibili con EncryptedSharedPreferences
- 🔐 Configurazione di sicurezza di rete migliorata

### ⚙️ Miglioramenti Tecnici
- 📦 Aggiornamento Material Design Components a versione 1.13.0
- 📦 Aggiornamento Firebase Messaging a versione 24.1.0
- 📦 Aggiornamento Google Play Services alle versioni più recenti
- 📦 Aggiornamento AndroidX libraries per compatibilità con API 35

---

## Release Notes - BeMyRider v1.1.4

### Version 1.1.4 (Build 15)

### 🐛 Critical Bug Fixes
- 🔧 **Profile Image Upload**: Fixed "file upload migration pending" issue that prevented profile image uploads
- 🔧 **Multipart Request**: Properly implemented multipart upload method for file uploads
- 🔧 **Error Handling**: Improved HTTP error handling during uploads with correct server response reading
- 🔧 **File Validation**: Added file existence check before upload to prevent errors

### ⚙️ Technical Improvements
- 📦 Improved multipart request handling in `WebServiceCall`
- 📦 Added detailed logging for upload debugging
- 📦 Optimized HTTP error stream handling

---

## Version 1.1.3 (Build 14)

### 🎯 Play Store Compliance
- ✅ Updated to Target SDK 35 (Android 15) for Google Play 2025 requirements compliance
- ✅ Improved network security with mandatory HTTPS configuration
- ✅ Updated all dependencies to latest stable versions

### 🎨 UI/UX Improvements
- ✨ **Splash Screen**: Enhanced "Continue" button design with modern style and shadows
- ✨ **Onboarding**: Fixed button positioning to avoid overlaps with navigation bar
- ✨ **Registration**: Optimized spacing and element sizes for better usability
- 🔧 **Layout**: Fixed layout issues on all pages (customer and provider) where content was obscured by the top bar
- 🔧 **Provider Profile**: Fixed profile image position to make it accessible
- 🔧 **Photo Editor**: Improved profile photo editing interface with correctly positioned buttons

### 🐛 Bug Fixes
- 🔧 **Logout**: Fixed logout issue that didn't correctly return to login screen
- 🔧 **Provider**: Removed "please provide valid data" message appearing on app closure for provider accounts
- 🔧 **Language**: Fixed default language issue that was set to English on startup
- 🔧 **Intro Slides**: Restored introduction slides on first app launch
- 🔧 **Permissions**: Fixed permission issues for profile image upload on Android 13+

### 📱 Push Notifications
- 🔔 Improved push notification handling with full Android 13+ support
- 🔊 Added notification sound support
- 🔔 Implemented correct permission requests for notifications on Android 13+
- 🧪 Added notification test button in account settings

### 🔒 Security
- 🔐 Improved sensitive data handling with EncryptedSharedPreferences
- 🔐 Enhanced network security configuration

### ⚙️ Technical Improvements
- 📦 Updated Material Design Components to version 1.13.0
- 📦 Updated Firebase Messaging to version 24.1.0
- 📦 Updated Google Play Services to latest versions
- 📦 Updated AndroidX libraries for API 35 compatibility

---

## Versione Breve per Play Store (Italiano)

**v1.1.4 - Correzioni Importanti**

🔧 Risolto problema critico di upload immagini profilo
🔧 Migliorata gestione errori durante l'upload
📦 Ottimizzazioni tecniche per upload multipart

**Inclusi tutti i miglioramenti della versione 1.1.3:**
🎯 Conformità Play Store 2025
✨ Miglioramenti UI/UX su splash screen, onboarding e registrazione
🔧 Correzioni layout su tutte le pagine
🔔 Miglioramenti notifiche push con supporto Android 13+
🐛 Risolti bug di logout, lingua e permessi
🔒 Miglioramenti sicurezza e aggiornamento dipendenze

---

## Short Release Notes for Play Store (English)

**v1.1.4 - Important Fixes**

🔧 Fixed critical profile image upload issue
🔧 Improved error handling during uploads
📦 Technical optimizations for multipart uploads

**Includes all improvements from version 1.1.3:**
🎯 Play Store 2025 Compliance
✨ UI/UX improvements on splash screen, onboarding, and registration
🔧 Layout fixes on all pages
🔔 Push notification improvements with Android 13+ support
🐛 Fixed logout, language, and permissions bugs
🔒 Security improvements and dependency updates

