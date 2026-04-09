# Frontend inteqrasiya təlimatı (React / Vite)

Backend əsas URL (default): **`http://localhost:8085`** (`SERVER_PORT` ilə dəyişə bilər)

OpenAPI spesifikasiyası: **`http://localhost:8085/v3/api-docs`**  
Swagger UI: **`http://localhost:8085/swagger-ui.html`**

---

## 1. CORS

Backend default olaraq bu origin-lərə icazə verir:

- `http://localhost:3000` (Create React App)
- `http://localhost:5173` (Vite)

Başqa port/domain üçün serverdə `CORS_ORIGINS` və ya `application.yml` içində `app.cors.allowed-origins` siyahısına öz ünvanını əlavə et (vergüllə ayır).

`credentials: true` olduğu üçün brauzerdə **konkret origin** lazımdır (`*` işləməz). Adətən `fetch`/`axios` üçün:

- `credentials: 'include'` yalnız cookie/session üçün lazımdır; bu API **JWT header** ilə işlədiyi üçün çox vaxt **`Authorization: Bearer ...`** kifayətdir və `credentials` olmadan da CORS keçir.

---

## 2. Cavab formatı (`ApiResponse`)

Uğurlu cavab:

```json
{
  "success": true,
  "message": "…",
  "data": { }
}
```

Xəta (məsələn validasiya):

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": ["field: reason"]
}
```

Frontenddə həmişə əvvəl `success` yoxlanılmalıdır; real payload `data` içindədir.

---

## 3. Autentifikasiya (JWT)

### Qeydiyyat (ilk istifadəçi çox vaxt `ADMIN` olur — DB boşdursa)

`POST /api/auth/register`  
Content-Type: `application/json`

```json
{
  "fullName": "Analyst One",
  "email": "user@company.com",
  "password": "minimum8chars"
}
```

Cavab `data.token` (JWT string) və `data.user` verir.

### Giriş

`POST /api/auth/login`

```json
{
  "email": "user@company.com",
  "password": "…"
}
```

### Qorunan sorğular

Hər sorğuya header əlavə et:

```http
Authorization: Bearer <access_token>
```

### Profil

`GET /api/auth/me` — cari istifadəçi (`data`: `id`, `fullName`, `email`, `role`).

**Rollar:** `ADMIN` | `ANALYST` | `VIEWER`  
- `VIEWER`: əsasən **GET** (oxuma)  
- `ANALYST` / `ADMIN`: log ingest, alert dəyişikliyi, phishing analyze, report generate və s.

---

## 4. Səhifələmə (`PageData`)

Siyahı endpoint-ləri `data` içində belə qaytarır:

```json
{
  "content": [ … ],
  "page": 0,
  "size": 20,
  "totalElements": 57,
  "totalPages": 3
}
```

Query parametrləri: Spring `Pageable` — məsələn `?page=0&size=20&sort=occurredAt,desc`.

---

## 5. Əsas endpoint-lər (qısa)

| Əməliyyat | Method | Path |
|-----------|--------|------|
| Dashboard xülasə | GET | `/api/dashboard/summary` |
| Risk trend | GET | `/api/dashboard/risk-trend?days=7` |
| Son aktivlik | GET | `/api/dashboard/recent-activity` |
| Log siyahısı | GET | `/api/logs` (+ filter query) |
| Log əlavə | POST | `/api/logs` |
| Alert siyahısı | GET | `/api/alerts` |
| Phishing analiz | POST | `/api/phishing/analyze` |
| Risk cari | GET | `/api/risk/current` |
| Report siyahısı | GET | `/api/reports` |

Tam cədvəl: [README.md](README.md).

---

## 6. HTTP status və xətalar

| Status | Məna | Frontend təklifi |
|--------|------|------------------|
| **401** | Token yoxdur / etibarsızdır | Login səhifəsinə yönləndir, `localStorage` tokeni sil |
| **403** | Rol çatmır (məs. `VIEWER` yaz əməliyyatı) | UI-da düyməni gizlət və ya mesaj göstər |
| **400** | Validasiya / `BadRequest` | `errors` massivini göstər |
| **404** | `ResourceNotFound` | `message` göstər |
| **502** | İnteqrasiya xətası | `IntegrationException` — isteğe bağlı retry |

**401 / 403** indi JSON `ApiResponse` formatındadır (`success: false`, `message`), controller səviyyəsindəki xətalar da eyni struktura yaxındır.

---

## 7. Nümunə: `fetch` wrapper (TypeScript)

```typescript
const API_BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8085';

function getToken(): string | null {
  return localStorage.getItem('access_token');
}

export async function api<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...(options.headers as object),
  };
  const token = getToken();
  if (token) {
    (headers as Record<string, string>)['Authorization'] = `Bearer ${token}`;
  }

  const res = await fetch(`${API_BASE}${path}`, { ...options, headers });
  const json = await res.json().catch(() => ({}));

  if (!res.ok) {
    const msg = json?.message ?? res.statusText;
    throw new Error(msg);
  }
  if (json.success === false) {
    throw new Error(json.message ?? 'Request failed');
  }
  return json.data as T;
}

// Login
export async function login(email: string, password: string) {
  const data = await api<{ token: string; user: unknown }>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  });
  localStorage.setItem('access_token', data.token);
  return data;
}

// Dashboard
export function getDashboardSummary() {
  return api<{
    totalAlerts: number;
    activeThreats: number;
    riskScore: number;
    safeSystemsPercent: number;
    phishingDetectedCount: number;
    recentAlerts: unknown[];
    riskTrend: { label: string; score: number }[];
  }>('/api/dashboard/summary');
}
```

`.env` (Vite):

```env
VITE_API_URL=http://localhost:8085
```

---

## 8. Yoxlama siyahısı (hackathon)

- [ ] Backend işləyir: `GET /api/health` → `success: true`
- [ ] Register / Login → token saxlanılır
- [ ] Qorunan sorğuda `Authorization` header göndərilir
- [ ] CORS: frontend origin backend konfiqində var
- [ ] Pagination: `data.content` və `data.totalElements` UI-da istifadə olunur
- [ ] 401 halında logout / login redirect

---

## 9. Əlavə

- OpenAPI-dan **client generate** etmək üçün `openapi-generator` və ya `openapi-typescript` istifadə edə bilərsən (`v3/api-docs` JSON export).
- Demo rejimində hazır istifadəçi: README-dəki `admin@demo.local` (yalnız `APP_DEMO_MODE=true` olduqda).
