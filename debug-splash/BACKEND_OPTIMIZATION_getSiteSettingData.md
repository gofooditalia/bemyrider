# 📊 Backend Optimization: getSiteSettingData API - Performance Analysis

**Data:** Marzo 2026  
**Problema:** L'endpoint `/ws/other/getSiteSettingData/` impiega 1000-3000ms (peak 3029ms)  
**Impatto:** L'app impiega 4-6+ secondi per avviarsi, rifiuto utenti in paesi lenti  
**Soluzione Richiesta:** Ridurre a <200ms (preferibilmente <100ms)

---

## 🎯 Executive Summary

| Metrica | Attuale | Target | Miglioramento |
|---------|---------|--------|---------------|
| Time to Response | 1000-3000ms | <200ms | **15-30x** |
| Simultaneous Users | ? | ?+ 50% | Server scaling |
| Cache Hit Rate | 0% (no caching) | 95%+ | Redis/Memcached |
| Database Roundtrips | 3-5 (assunto) | 1 | Query optimization |
| Payload Size | ? | <50KB | Compression |

---

## 🔍 Analisi: Perché è Lento?

### Possibili Colli di Bottiglia

Senza vedere il codice backend, i soliti suspect sono:

#### 1. **Database Query Non Ottimizzata** (70% probabilità)

```sql
-- ❌ SBAGLIATO: N+1 problem, join multiple tables senza index
SELECT settings.*, 
       users.count, 
       products.count,
       logs.count  -- ← Queries nested nel loop
FROM site_settings
JOIN users ON ...
JOIN products ON ...
JOIN logs ON ...
WHERE id = 1
-- Risultato: 10-20 roundtrips al database!
```

**Soluzione:** Ottimizzare con index e query unica

```sql
-- ✅ CORRETTO: Single query con index
SELECT * FROM site_settings WHERE id = 1;
-- Aggiungere index: CREATE INDEX idx_site_settings_id ON site_settings(id);
```

#### 2. **Mancanza di Caching** (85% probabilità)

```php
// ❌ SBAGLIATO: Query al database OGNI volta
function getSiteSettingData() {
    $settings = DB::query("SELECT * FROM site_settings WHERE id = 1");
    return $settings;  // 1000-3000ms OGNI volta
}

// ✅ CORRETTO: Cache con Redis/Memcached (24 ore di TTL)
function getSiteSettingData() {
    $cache = Redis::get("site_settings:1");
    if ($cache) {
        return json_decode($cache);  // <10ms from cache!
    }
    
    $settings = DB::query("SELECT * FROM site_settings WHERE id = 1");
    Redis::setex("site_settings:1", 86400, json_encode($settings));  // 24h TTL
    return $settings;
}
```

#### 3. **Network Latency + Payload Size** (30% probabilità)

```json
// ❌ SBAGLIATO: Troppe informazioni non necessarie
{
  "minAppVersion": 1,
  "maxAppVersion": 10,
  "settings": {
    "allUserProfilesData": [...],  // ← Megabyte di dati inutili!
    "allProvidersData": [...],
    "allProductsData": [...]
  }
}
// Payload: 500KB+ → Slow transfer

// ✅ CORRETTO: Solo i dati necessari per la splash screen
{
  "minAppVersion": 1,
  "maxAppVersion": 10,
  "forceUpdate": false,
  "apiVersion": "v2"
}
// Payload: <5KB → Fast transfer
```

#### 4. **Slow Server/Database Machine** (20% probabilità)

- Server CPU throttled (AWS t2.micro instead of t3.medium)
- Database on shared hosting (slow disk I/O)
- No connection pooling (creating new DB connection per request)

#### 5. **Multiple API Calls in Sequence** (10% probabilità)

```php
// ❌ SBAGLIATO: Tre call sequenziali
function getSiteSettingData() {
    $settings = getSettings();     // 300ms
    $features = getFeatures();     // 300ms
    $messages = getMessages();     // 300ms
    
    return array_merge($settings, $features, $messages);  // 900ms total
}

// ✅ CORRETTO: Single query che farà il join
SELECT * FROM settings
JOIN features ON ...
JOIN messages ON ...
-- 300ms total (1 roundtrip instead of 3)
```

---

## 🛠️ Soluzione Rapida (Do This First)

### Priority 1: Implementare Redis Caching (10 minuti)

**PHP (Laravel):**

```php
<?php

namespace App\Http\Controllers;

use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\DB;

class SiteSettingController extends Controller
{
    public function getSiteSettingData()
    {
        // Try cache first
        $cacheKey = 'site_settings:data';
        $cacheTTL = 24 * 60;  // 24 hours in minutes
        
        $settings = Cache::remember($cacheKey, $cacheTTL, function () {
            // Only run this DB query if cache miss
            return DB::table('site_settings')
                ->where('id', 1)
                ->select('minAppVersion', 'maxAppVersion', 'forceUpdate', 'apiVersion')
                ->first();
        });
        
        // If still null, return defaults
        if (!$settings) {
            $settings = [
                'minAppVersion' => 1,
                'maxAppVersion' => 100,
                'forceUpdate' => false,
                'apiVersion' => 'v2'
            ];
        }
        
        return response()->json($settings);
    }
}
```

**Expected Performance:**
- First request: ~1000-3000ms (queries DB, caches result)
- Subsequent requests: <10ms (served from Redis cache!)

---

### Priority 2: Add Database Index (5 minuti)

**SQL (Any Database):**

```sql
-- For site_settings table
CREATE INDEX idx_site_settings_id ON site_settings(id);
CREATE INDEX idx_site_settings_active ON site_settings(is_active);

-- If using timestamps for filtering
CREATE INDEX idx_site_settings_updated ON site_settings(updated_at);

-- Run ANALYZE to update statistics
ANALYZE TABLE site_settings;
```

**Expected Improvement:** 500ms → 100-200ms

---

### Priority 3: Reduce Payload Size (10 minuti)

**Current response (❌ Too much):**

```php
// ❌ SBAGLIATO: Ritorna TUTTO
SELECT * FROM site_settings;

// ✅ CORRETTO: Ritorna solo quel che serve
SELECT 
    minAppVersion,
    maxAppVersion,
    forceUpdate,
    apiVersion,
    maintenance_mode
FROM site_settings
WHERE id = 1;
```

**Expected Improvement:** 200ms → 50ms (network transfer faster)

---

## 📋 Checklist di Ottimizzazione Backend

### Fase 1: Immediate Wins (30 minuti)

- [ ] **Caching:** Implementa Redis/Memcached per getSiteSettingData
  - Cache TTL: 24 ore
  - Invalidate on update: Via admin panel or webhook

- [ ] **Database Index:** Aggiungi index su site_settings.id
  - Command: `CREATE INDEX idx_site_settings_id ON site_settings(id);`
  - Verify: `EXPLAIN SELECT * FROM site_settings WHERE id = 1;`

- [ ] **Payload Reduction:** Seleziona solo i campi necessari
  - Remove: user profiles, product lists, logs
  - Keep: minAppVersion, maxAppVersion, forceUpdate, apiVersion

- [ ] **Test Performance:**
  ```bash
  # First request (no cache)
  time curl -X POST https://bemyrider.it/ws/other/getSiteSettingData/
  
  # Second request (from cache, should be <20ms)
  time curl -X POST https://bemyrider.it/ws/other/getSiteSettingData/
  ```

---

### Fase 2: Deeper Optimization (1-2 hours)

- [ ] **Query Optimization:**
  - Profile slow queries: `EXPLAIN ANALYZE SELECT ...`
  - Use EXPLAIN to find missing indexes
  - Remove N+1 queries

- [ ] **Connection Pooling:**
  - Use PgBouncer (PostgreSQL) or ProxySQL (MySQL)
  - Reduce connection creation overhead

- [ ] **Server Resources:**
  - Check CPU/Memory usage: `top`, `htop`
  - If maxed out: Scale up server (t3.medium → t3.large)
  - Consider read replicas for DB

- [ ] **Enable Compression:**
  ```php
  // In Apache/Nginx config
  gzip on;
  gzip_types application/json text/plain;
  gzip_min_length 1024;
  ```

---

### Fase 3: Long-Term Solutions (1+ week)

- [ ] **Separate Data Layer:** Move getSiteSettingData to separate fast endpoint
  - Current: Queries DB, processes data, returns
  - Proposed: Return pre-computed JSON from cache

- [ ] **CDN Cache:** If settings are truly global, cache at CDN level
  - Cloudflare, AWS CloudFront
  - TTL: 24 hours (or per update)

- [ ] **Database Monitoring:** Setup APM
  - New Relic, DataDog, AWS RDS Performance Insights
  - Track slow queries, CPU, I/O

---

## 📊 Performance Monitoring

### Monitor After Implementation

**Before your next release, establish baseline:**

```bash
# Load test: 100 concurrent users
ab -n 100 -c 10 https://bemyrider.it/ws/other/getSiteSettingData/

# Or with Apache Bench
ab -n 1000 -c 50 -p payload.json -T application/json \
   https://bemyrider.it/ws/other/getSiteSettingData/

# Expected after optimization:
# Time per request: 50-100ms
# Requests per second: 100-200
```

---

## 🔧 Code Examples by Stack

### Option A: PHP + Laravel + Redis

```php
<?php

use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\DB;
use Illuminate\Http\JsonResponse;

class SiteSettingController
{
    public function getSiteSettingData(): JsonResponse
    {
        $settings = Cache::remember('site_settings:main', 24 * 60, function () {
            return DB::table('site_settings')
                ->where('id', 1)
                ->select(['minAppVersion', 'maxAppVersion', 'forceUpdate', 'apiVersion'])
                ->first();
        });

        return response()->json($settings ?? [], 200);
    }
}
```

### Option B: PHP + Symfony + Redis

```php
<?php

use Symfony\Component\HttpFoundation\JsonResponse;

class SiteSettingController
{
    private $cache;
    private $doctrine;
    
    public function getSiteSettingData(): JsonResponse
    {
        // Try cache
        $settings = $this->cache->get('site_settings:main');
        
        if (!$settings) {
            // Query DB
            $settings = $this->doctrine
                ->getRepository('SiteSettings')
                ->find(1);
            
            // Cache for 24 hours
            $this->cache->set('site_settings:main', $settings, 86400);
        }
        
        return new JsonResponse($settings);
    }
}
```

### Option C: Node.js + Express + Redis

```javascript
const express = require('express');
const redis = require('redis');
const db = require('./database');

const redisClient = redis.createClient();

app.post('/ws/other/getSiteSettingData', async (req, res) => {
    try {
        // Try cache
        const cached = await redisClient.get('site_settings:main');
        if (cached) {
            return res.json(JSON.parse(cached));
        }
        
        // Query DB (only on cache miss)
        const settings = await db.query(
            'SELECT minAppVersion, maxAppVersion, forceUpdate, apiVersion FROM site_settings WHERE id = ?',
            [1]
        );
        
        // Cache for 24 hours
        await redisClient.setEx('site_settings:main', 86400, JSON.stringify(settings[0]));
        
        res.json(settings[0]);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});
```

### Option D: Python + Flask + Redis

```python
from flask import Flask, jsonify
from redis import Redis
import json
from database import get_settings

app = Flask(__name__)
redis_client = Redis(host='localhost', port=6379)

@app.route('/ws/other/getSiteSettingData', methods=['POST'])
def get_site_setting_data():
    # Try cache
    cached = redis_client.get('site_settings:main')
    if cached:
        return jsonify(json.loads(cached))
    
    # Query DB
    settings = get_settings(id=1)
    
    # Cache for 24 hours
    redis_client.setex('site_settings:main', 86400, json.dumps(settings))
    
    return jsonify(settings)
```

---

## ❓ Debugging Slow Queries

If performance is still bad after caching + indexing:

### Step 1: Find the Slow Query

**MySQL:**

```sql
-- Enable slow query log
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 0.5;  -- Log queries > 500ms

-- View slow queries
SELECT * FROM mysql.slow_log ORDER BY query_time DESC LIMIT 10;
```

**PostgreSQL:**

```sql
-- Enable query logging
SET log_min_duration_statement = 500;  -- Log queries > 500ms

-- View logs
SELECT query, calls, mean_time FROM pg_stat_statements 
ORDER BY mean_time DESC LIMIT 10;
```

### Step 2: Analyze with EXPLAIN

```sql
-- See the query plan
EXPLAIN ANALYZE 
SELECT * FROM site_settings WHERE id = 1;

-- Look for:
-- ❌ Full table scan (Sequential Scan)
-- ✅ Index scan (Index Scan on idx_site_settings_id)
```

### Step 3: Profile the Endpoint

**Add timing logs:**

```php
// PHP
$start = microtime(true);

// ... your query ...

$duration = (microtime(true) - $start) * 1000;
error_log("getSiteSettingData took {$duration}ms");
```

**Check logs:**

```bash
tail -f /var/log/php-errors.log | grep getSiteSettingData
```

---

## 📞 Next Steps

1. **Review this document** with your backend team
2. **Implement Priority 1** (Redis caching) — easiest, biggest impact
3. **Add Performance Monitoring** — track before/after
4. **Load Test** — ensure it handles peak traffic
5. **Notify Frontend Team** — new response time allows faster splash screen

---

## 📚 References

- **Redis Caching Strategies:** https://redis.io/topics/memory-optimization
- **Database Index Optimization:** https://use-the-index-luke.com/
- **MySQL Query Optimization:** https://dev.mysql.com/doc/refman/8.0/en/optimization.html
- **PostgreSQL Query Performance:** https://www.postgresql.org/docs/current/using-explain.html
- **APM Tools:** New Relic, DataDog, AWS CloudWatch

---

**Version:** 1.0  
**Date:** Marzo 2026  
**Owner:** Giorgio / Backend Team  
**Priority:** 🔴 HIGH (Impacts app startup performance)
