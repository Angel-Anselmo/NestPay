# 🚀 Guía de Deployment - NestPay Backend Multi-Wallet

Esta guía te ayudará a desplegar el backend de NestPay con Open Payments en producción con soporte
para Admin y User wallets.

## 📋 Preparación Previa

### 1. Obtener Credenciales de Open Payments

**Para ADMIN (Creador de comunidades):**

1. Ve a: https://wallet.interledger-test.dev
2. Crea cuenta como **admin** (ej: admin-nestpay)
3. En Settings → Developer Keys → Generate Key Pair
4. Descarga: `private_key_admin.key`
5. Copia: Key ID del admin
6. Tu URL será: `https://ilp.interledger-test.dev/admin-nestpay`

**Para USER (Pagador de aportes):**

1. Ve a: https://wallet.interledger-test.dev
2. Crea cuenta como **user** (ej: user-nestpay)
3. En Settings → Developer Keys → Generate Key Pair
4. Descarga: `private_key_user.key`
5. Copia: Key ID del user
6. Tu URL será: `https://ilp.interledger-test.dev/user-nestpay`

### 2. Configurar Variables de Entorno Multi-Wallet

```bash
# Configuración ADMIN (recibe aportes de comunidad)
ADMIN_WALLET_ADDRESS_URL=https://ilp.interledger-test.dev/admin-nestpay
ADMIN_PRIVATE_KEY_PATH=./keys/private_key_admin.key
ADMIN_KEY_ID=admin-key-id-aqui

# Configuración USER (paga aportes)
USER_WALLET_ADDRESS_URL=https://ilp.interledger-test.dev/user-nestpay
USER_PRIVATE_KEY_PATH=./keys/private_key_user.key
USER_KEY_ID=user-key-id-aqui

# Configuración general
NODE_ENV=production
LOG_LEVEL=info
BASE_URL=https://tu-dominio.com
```

## 🚄 Railway Deployment

### 1. Preparar Claves Privadas

```bash
cd nestpay-backend

# Crear directorio de claves
mkdir keys

# Copiar ambas claves privadas
cp /path/to/admin.key keys/private_key_admin.key
cp /path/to/user.key keys/private_key_user.key

# Verificar estructura
ls -la keys/
# private_key_admin.key
# private_key_user.key
```

### 2. Variables de Entorno en Railway

```bash
# En Railway Dashboard → Variables:

# ADMIN WALLET
ADMIN_WALLET_ADDRESS_URL=https://ilp.interledger-test.dev/admin-nestpay
ADMIN_KEY_ID=tu-admin-key-id
ADMIN_PRIVATE_KEY_PATH=./keys/private_key_admin.key

# USER WALLET  
USER_WALLET_ADDRESS_URL=https://ilp.interledger-test.dev/user-nestpay
USER_KEY_ID=tu-user-key-id
USER_PRIVATE_KEY_PATH=./keys/private_key_user.key

# GENERAL
NODE_ENV=production
LOG_LEVEL=info
BASE_URL=https://tu-proyecto.up.railway.app
```

### 3. Configuración Railway
```json
{
  "$schema": "https://railway.app/railway.schema.json",
  "build": {
    "builder": "NIXPACKS"
  },
  "deploy": {
    "startCommand": "npm start",
    "healthcheckPath": "/api/health",
    "healthcheckTimeout": 300,
    "restartPolicyType": "ON_FAILURE",
    "restartPolicyMaxRetries": 10
  }
}
```

## 🧪 Testing Multi-Wallet

### 1. Verificar Sistema
```bash
curl https://tu-backend.railway.app/api/payments/system/info
```

**Respuesta esperada:**

```json
{
  "success": true,
  "data": {
    "system": "NestPay Backend - Multi-Wallet",
    "wallets": {
      "adminWallet": {
        "address": "https://ilp.interledger-test.dev/admin-nestpay",
        "keyId": "admin-key-id",
        "initialized": true,
        "role": "Crear comunidades y recibir aportes"
      },
      "userWallet": {
        "address": "https://ilp.interledger-test.dev/user-nestpay", 
        "keyId": "user-key-id",
        "initialized": true,
        "role": "Pagar aportes a comunidades"
      }
    }
  }
}
```

### 2. Probar Flujo de Aporte a Comunidad
```bash
# Iniciar aporte
curl -X POST https://tu-backend.railway.app/api/payments/community/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "amount": {
      "value": "500",
      "assetCode": "USD",
      "assetScale": 2
    },
    "description": "Aporte mensual comunidad",
    "communityId": "comunidad-123",
    "conceptId": "cuota-mensual"
  }'
```

**Respuesta:**

```json
{
  "success": true,
  "message": "Flujo de aporte a comunidad iniciado. Autorización del usuario requerida.",
  "data": {
    "incomingPaymentId": "uuid-incoming-payment",
    "quoteId": "uuid-quote",
    "adminWallet": "https://ilp.interledger-test.dev/admin-nestpay",
    "userWallet": "https://ilp.interledger-test.dev/user-nestpay",
    "authorizationUrl": "https://auth.interledger-test.dev/...",
    "continueData": { ... }
  }
}
```

## 📱 Integración Android Multi-Wallet

### 1. Actualizar Modelos
```kotlin
data class CommunityPaymentRequest(
    val amount: PaymentAmount,
    val description: String = "",
    val communityId: String? = null,
    val conceptId: String? = null
)

data class CommunityPaymentResponse(
    val success: Boolean,
    val message: String,
    val data: CommunityPaymentData
)

data class CommunityPaymentData(
    val incomingPaymentId: String,
    val quoteId: String,
    val adminWallet: String,
    val userWallet: String,
    val authorizationUrl: String,
    val estimatedFees: FeeEstimation,
    val continueData: ContinueData,
    val metadata: PaymentMetadata
)
```

### 2. Service Interface

```kotlin
interface NestPayBackendService {
    @POST("api/payments/community/initiate")
    suspend fun initiateCommunityPayment(
        @Body request: CommunityPaymentRequest
    ): Response<CommunityPaymentResponse>
    
    @POST("api/payments/community/finalize")
    suspend fun finalizeCommunityPayment(
        @Body request: FinalizeCommunityPaymentRequest
    ): Response<CommunityPaymentResult>
    
    @GET("api/payments/system/info")
    suspend fun getSystemInfo(): Response<SystemInfoResponse>
}
```

### 3. Repository Implementation

```kotlin
class CommunityPaymentRepository(
    private val backendService: NestPayBackendService
) {
    
    suspend fun payToommunity(
        amount: String,
        communityId: String,
        conceptId: String,
        description: String = ""
    ): Result<CommunityPaymentData> {
        return try {
            val request = CommunityPaymentRequest(
                amount = PaymentAmount(value = amount),
                description = description,
                communityId = communityId,
                conceptId = conceptId
            )
            
            val response = backendService.initiateCommunityPayment(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## 🔄 Flujo Completo NestPay

### Arquitectura del Sistema:
```
ADMIN WALLET (Comunidad)
    ↑ Recibe dinero
    |
[INCOMING PAYMENT]
    |
    | Open Payments
    |
[OUTGOING PAYMENT]  
    |
    v Envía dinero
USER WALLET (Miembro)
```

### Pasos del Flujo:

1. **User inicia:** App llama `POST /api/payments/community/initiate`
2. **Backend crea:** Incoming payment (Admin) + Quote (User)
3. **User autoriza:** Abre `authorizationUrl` y autoriza
4. **App finaliza:** Llama `POST /api/payments/community/finalize`
5. **Pago completo:** Dinero transferido de User → Admin

## 🚨 Troubleshooting Multi-Wallet

### Error: "Faltan variables de entorno ADMIN"

```bash
# Verificar en Railway:
ADMIN_WALLET_ADDRESS_URL ✓
ADMIN_KEY_ID ✓  
ADMIN_PRIVATE_KEY_PATH ✓
```

### Error: "Faltan variables de entorno USER"

```bash
# Verificar en Railway:
USER_WALLET_ADDRESS_URL ✓
USER_KEY_ID ✓
USER_PRIVATE_KEY_PATH ✓
```

### Error: "Private key not found"

```bash
# Verificar archivos en GitHub:
nestpay-backend/keys/private_key_admin.key ✓
nestpay-backend/keys/private_key_user.key ✓
```

## ✅ Checklist Multi-Wallet

- [ ] ✅ Admin wallet creada en Interledger Test Network
- [ ] ✅ User wallet creada en Interledger Test Network
- [ ] ✅ Ambas private keys descargadas y guardadas
- [ ] ✅ Ambos Key IDs copiados
- [ ] ✅ Variables de entorno ADMIN configuradas
- [ ] ✅ Variables de entorno USER configuradas
- [ ] ✅ Backend desplegado en Railway
- [ ] ✅ Health check pasa: `/api/health/detailed`
- [ ] ✅ System info pasa: `/api/payments/system/info`
- [ ] ✅ Test de flujo: `/api/payments/community/initiate`

## 🎯 Casos de Uso Específicos

### 1. **Aporte a Comunidad**

- **Usuario:** Miembro de comunidad quiere pagar cuota
- **Flujo:** User Wallet → Admin Wallet
- **Endpoint:** `POST /api/payments/community/initiate`

### 2. **Concepto Específico**

- **Usuario:** Pagar concepto específico (eventos, servicios)
- **Datos:** `communityId` + `conceptId`
- **Metadata:** Se incluye en el pago para tracking

### 3. **Múltiples Comunidades**

- **Escalabilidad:** Mismo Admin wallet recibe de múltiples comunidades
- **Identificación:** Usar `communityId` para distinguir

¡Tu sistema multi-wallet está listo para manejar aportes de comunidades con Open Payments! 🚀