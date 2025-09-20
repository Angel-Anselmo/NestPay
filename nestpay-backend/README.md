# NestPay Backend - Open Payments Integration

Backend completo para NestPay con integración total de Open Payments para pagos entre wallets
Interledger.

## 🚀 Características

- ✅ Integración completa con Open Payments API
- ✅ Soporte para wallets de prueba de Interledger Test Network
- ✅ Flujo completo de pagos (incoming/outgoing payments, quotes)
- ✅ Webhooks para eventos de pagos
- ✅ Health checks y monitoreo
- ✅ Rate limiting y seguridad
- ✅ Listo para deployer en Railway
- ✅ Documentación de API completa

## 🏗️ Arquitectura

```
src/
├── index.js              # Punto de entrada del servidor
├── services/
│   └── openPaymentsService.js  # Servicio principal de Open Payments
├── routes/
│   ├── paymentRoutes.js   # Rutas para pagos
│   ├── walletRoutes.js    # Rutas para wallets
│   ├── webhookRoutes.js   # Rutas para webhooks
│   └── healthRoutes.js    # Health checks
├── middleware/
│   └── errorHandler.js    # Manejo de errores
└── utils/
    └── logger.js          # Sistema de logging
```

## 📋 Requisitos Previos

1. **Node.js 20+**
2. **Una wallet de prueba en Interledger Test Network**
    - Crear en: https://wallet.interledger-test.dev
3. **Claves de desarrollador de tu wallet**
    - Private key (archivo .key)
    - Key ID
    - Wallet Address URL

## 🛠️ Configuración

### 1. Instalar dependencias

```bash
cd nestpay-backend
npm install
```

### 2. Configurar variables de entorno

Copia `.env.example` a `.env` y configura:

```bash
cp .env.example .env
```

Edita `.env`:

```env
# Puerto del servidor
PORT=3000

# URLs base
BASE_URL=https://tu-backend-railway.up.railway.app
FRONTEND_URL=https://tu-frontend.com

# Configuración Open Payments (REQUERIDO)
WALLET_ADDRESS_URL=https://ilp.interledger-test.dev/tu-wallet
PRIVATE_KEY_PATH=./keys/private.key
KEY_ID=tu-key-id

# URLs de Open Payments
AUTH_SERVER_URL=https://auth.interledger-test.dev
RESOURCE_SERVER_URL=https://ilp.interledger-test.dev

NODE_ENV=production
LOG_LEVEL=info
```

### 3. Obtener claves de desarrollador

1. Ve a https://wallet.interledger-test.dev
2. Crea una cuenta y wallet
3. Ve a **Settings** > **Developer Keys**
4. Genera un nuevo par de claves
5. Descarga el archivo `.key` y colócalo en `./keys/private.key`
6. Copia el Key ID
7. Tu Wallet Address URL será algo como: `https://ilp.interledger-test.dev/tu-usuario`

### 4. Crear directorio de claves

```bash
mkdir keys
# Coloca tu archivo private.key aquí
```

## 🚀 Ejecutar localmente

```bash
# Modo desarrollo
npm run dev

# Modo producción
npm start
```

El servidor estará disponible en `http://localhost:3000`

## 🌐 Deployment en Railway

### Opción 1: Deploy directo desde GitHub

1. Conecta tu repositorio a Railway
2. Configura las variables de entorno en Railway Dashboard
3. Railway detectará automáticamente el `package.json` y `railway.json`

### Opción 2: Deploy con Railway CLI

```bash
# Instalar Railway CLI
npm install -g @railway/cli

# Login
railway login

# Deploy
railway up
```

### Variables de entorno en Railway

Configura estas variables en tu proyecto de Railway:

```
WALLET_ADDRESS_URL=https://ilp.interledger-test.dev/tu-wallet
KEY_ID=tu-key-id
BASE_URL=https://tu-proyecto.up.railway.app
NODE_ENV=production
LOG_LEVEL=info
```

**⚠️ IMPORTANTE:** Para la private key en Railway, tienes dos opciones:

1. **Subir el archivo:** Sube `private.key` a tu repo y configura
   `PRIVATE_KEY_PATH=./keys/private.key`
2. **Variable de entorno:** Copia el contenido del archivo y crea una variable `PRIVATE_KEY_CONTENT`

## 📚 API Endpoints

### Health Checks

```
GET /api/health              # Health check básico
GET /api/health/detailed     # Health check detallado
GET /api/health/openpayments # Verificar Open Payments
GET /api/health/ready        # Readiness probe
GET /api/health/live         # Liveness probe
```

### Wallets

```
GET /api/wallets/validate/{wallet-url}        # Validar wallet
GET /api/wallets/info/{wallet-url}            # Info de wallet
POST /api/wallets/check-compatibility         # Verificar compatibilidad
GET /api/wallets/test-wallets                 # Wallets de prueba
POST /api/wallets/estimate-fees               # Estimar tarifas
```

### Pagos

```
POST /api/payments/incoming                   # Crear incoming payment
GET /api/payments/incoming/{wallet}/{id}      # Obtener incoming payment
POST /api/payments/quotes                     # Crear quote
GET /api/payments/quotes/{wallet}/{id}        # Obtener quote
POST /api/payments/outgoing                   # Crear outgoing payment
GET /api/payments/outgoing/{wallet}/{id}      # Obtener outgoing payment
POST /api/payments/initiate                   # Iniciar flujo completo
POST /api/payments/finalize                   # Finalizar pago
GET /api/payments/grant-callback              # Callback de autorización
GET /api/payments/status/{id}                 # Estado del pago
```

### Webhooks

```
POST /api/webhooks/payment-events             # Eventos de pagos
POST /api/webhooks/grant-events               # Eventos de grants
GET /api/webhooks/test                        # Test webhook
```

## 🔄 Flujo de Pago Completo

### 1. Iniciar pago

```javascript
POST /api/payments/initiate
{
  "senderWallet": "https://ilp.interledger-test.dev/alice",
  "receiverWallet": "https://ilp.interledger-test.dev/bob",
  "amount": {
    "value": "1000",
    "assetCode": "USD",
    "assetScale": 2
  },
  "description": "Pago de prueba"
}
```

**Respuesta:**

```javascript
{
  "success": true,
  "message": "Flujo de pago iniciado. Autorización del usuario requerida.",
  "data": {
    "paymentId": "uuid-incoming-payment",
    "quoteId": "uuid-quote",
    "authorizationUrl": "https://auth.interledger-test.dev/...",
    "estimatedFees": {
      "sendAmount": { "value": "1000", "assetCode": "USD", "assetScale": 2 },
      "receiveAmount": { "value": "995", "assetCode": "USD", "assetScale": 2 }
    },
    "continueData": {
      "continueUri": "https://auth.interledger-test.dev/continue/...",
      "continueAccessToken": "...",
      "state": "uuid-state"
    }
  }
}
```

### 2. Usuario autoriza el pago

El usuario visita `authorizationUrl` y autoriza el pago.

### 3. Finalizar pago

```javascript
POST /api/payments/finalize
{
  "continueUri": "...",
  "continueAccessToken": "...",
  "interactRef": "...",
  "walletAddress": "https://ilp.interledger-test.dev/alice",
  "quoteId": "uuid-quote"
}
```

## 🔒 Seguridad

- ✅ Rate limiting configurado
- ✅ CORS configurado
- ✅ Helmet para headers de seguridad
- ✅ Validación de entrada con express-validator
- ✅ Manejo seguro de claves privadas
- ✅ Logging de errores sin exponer datos sensibles

## 🧪 Testing

### Wallets de prueba disponibles

```
Alice: https://ilp.interledger-test.dev/alice
Bob: https://ilp.interledger-test.dev/bob
Carol: https://ilp.interledger-test.dev/carol
```

### Ejemplos de prueba

```bash
# Validar una wallet
curl https://tu-backend.railway.app/api/wallets/validate/https://ilp.interledger-test.dev/alice

# Verificar compatibilidad
curl -X POST https://tu-backend.railway.app/api/wallets/check-compatibility \
  -H "Content-Type: application/json" \
  -d '{
    "senderWallet": "https://ilp.interledger-test.dev/alice",
    "receiverWallet": "https://ilp.interledger-test.dev/bob"
  }'

# Health check
curl https://tu-backend.railway.app/api/health/detailed
```

## 📱 Integración con App Android

En tu app Android, actualiza el `OpenPaymentsApiService` para usar tu backend:

```kotlin
// En lugar de llamar directamente a Open Payments API
interface NestPayBackendService {
    @POST("payments/initiate")
    suspend fun initiatePayment(@Body request: InitiatePaymentRequest): Response<PaymentFlowResponse>
    
    @POST("payments/finalize")
    suspend fun finalizePayment(@Body request: FinalizePaymentRequest): Response<PaymentResult>
    
    @GET("wallets/validate/{walletUrl}")
    suspend fun validateWallet(@Path("walletUrl") walletUrl: String): Response<WalletValidationResponse>
}
```

## 🐛 Troubleshooting

### Error: "Faltan variables de entorno requeridas"

Asegúrate de configurar:

- `WALLET_ADDRESS_URL`
- `PRIVATE_KEY_PATH` (o `PRIVATE_KEY_CONTENT`)
- `KEY_ID`

### Error: "Error conectando con Open Payments"

1. Verifica que tu wallet address sea correcta
2. Verifica que el archivo de clave privada esté en la ruta correcta
3. Verifica que el Key ID coincida con el de tu wallet

### Error: "Wallet address inválida"

1. La URL debe comenzar con `https://`
2. Debe ser una wallet válida de Interledger Test Network
3. Prueba con: `https://ilp.interledger-test.dev/alice`

## 📞 Soporte

Para problemas específicos de integración:

1. Revisa los logs del servidor
2. Verifica el health check: `/api/health/detailed`
3. Prueba con wallets de test primero
4. Consulta la documentación de Open Payments: https://openpayments.dev

## 🎯 Próximos Pasos

1. **Implementar cache:** Redis para mejorar performance
2. **Base de datos:** PostgreSQL para persistir transacciones
3. **Autenticación:** JWT tokens para usuarios
4. **Monitoreo:** Métricas y alertas
5. **Tests:** Unit tests y integration tests

---

## 🚀 ¡Listo para producción!

Este backend está completamente configurado para trabajar con Open Payments y puede manejear el
flujo completo de pagos entre wallets Interledger. Solo necesitas configurar tus credenciales de
desarrollador y hacer deploy en Railway.