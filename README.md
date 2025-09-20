# NestPay

Aplicación móvil para pagos compartidos en comunidades digitales.

## ¿Qué es NestPay?

NestPay es una aplicación móvil que digitaliza y organiza los pagos colectivos en comunidades,
resolviendo los problemas comunes que surgen al organizar actividades que implican pagos compartidos
como viajes, fiestas o tandas.

La aplicación facilita la interoperabilidad en la configuración y finalización de pagos para
diferentes casos de uso incluyendo:

- Pagos de viajes grupales
- Contribuciones para fiestas y eventos
- Tandas y rifas comunitarias
- Pagos de subscripciones grupales
- Aportaciones para proyectos comunitarios
- Transferencias P2P entre miembros

### El Problema

Al organizar actividades que implican pagos colectivos se presentan problemas comunes:

- Falta de organización y claridad en las responsabilidades
- Pagos incompletos, olvidados o mal gestionados
- Riesgos asociados al manejo de efectivo: pérdidas, estafas o robos
- Escasa inclusión financiera digital en comunidades no bancarizadas

En México, estos problemas son más graves porque gran parte de las dinámicas comunitarias siguen
realizándose en efectivo, sin registros claros ni seguridad.

### La Solución

NestPay organiza y digitaliza los pagos colectivos mediante:

- Los administradores crean comunidades con metas y sub-conceptos de pago
- Los miembros realizan aportaciones seguras mediante tecnologías basadas en Open Payments (
  Interledger)
- Todos los usuarios tienen visibilidad en tiempo real de: pagos completados, pendientes, vencidos y
  avance global
- El administrador mantiene control centralizado, pero la transparencia es total para todos los
  participantes

## Arquitectura NestPay

NestPay está compuesto por tres subsistemas principales:

1. **App Android (Kotlin + Jetpack Compose)** que proporciona la interfaz de usuario nativa y maneja
   la autenticación local
2. **Backend Node.js + Express** que expone APIs para realizar funciones contra las cuentas y pagos
   subyacentes
3. **Firebase** que proporciona autenticación segura, base de datos en tiempo real y notificaciones
   push

La integración con **Open Payments (Interledger)** permite transacciones seguras, abiertas y
escalables entre diferentes wallets.

## Stack Tecnológico

**Frontend:**

- Android Studio (Kotlin + Jetpack Compose): desarrollo nativo para Android
- Firebase Auth: autenticación segura
- Jetpack Navigation: navegación entre pantallas
- Material Design 3: UI/UX moderna

**Backend:**

- Node.js + Express: API REST
- Open Payments SDK: integración con Interledger
- Firebase Firestore: base de datos en tiempo real
- Railway: deployment y hosting

**Interoperabilidad:**

- Open Payments (Interledger): estándar abierto para transacciones
- Interledger Test Network: wallets de prueba
- GNAP: autorización estándar para acceso a APIs

## Funcionalidades Principales

- Creación de comunidades y metas de pago
- Gestión de conceptos de pago
- Pagos digitales seguros mediante Open Payments
- Depósito automático a wallet del administrador de comunidad
- Visibilidad en tiempo real de pagos y progreso
- Historial de transacciones por usuario y comunidad
- Control centralizado con transparencia total

## ¿Nuevo en Interledger?

¿Nunca has escuchado de Interledger antes? ¿O te gustaría aprender más? Aquí tienes algunos
excelentes lugares para comenzar:

- [Sitio Web de Interledger](https://interledger.org/)
- [Especificación de Interledger](https://interledger.org/specs/)
- [Video Explicativo de Interledger](https://www.youtube.com/watch?v=vxJ8tTOJdSE)
- [Open Payments](https://openpayments.dev/)
- [Interledger Test Network](https://wallet.interledger-test.dev/)

## Contribuir

Por favor lee las pautas de contribución antes de enviar contribuciones. Todas las contribuciones
deben adherirse a nuestro código de conducta.

## Equipo y Responsabilidades

**Frontend (Android - Kotlin + Jetpack Compose):**

- Responsable: Equipo de desarrollo móvil
- Diseño de interfaz y flujo de usuario

**Backend (Node.js + Express):**

- Responsable: Equipo backend/cloud
- API REST, integración con Open Payments, webhooks

**Base de datos (Firebase):**

- Responsable: Equipo backend/cloud
- Autenticación, base de datos en tiempo real, notificaciones

**Integración Financiera (Open Payments):**

- Responsable: Equipo de integración financiera
- Estándares abiertos, seguridad en transacciones, interoperabilidad

## Impacto y Beneficios

### Sociales

- Inclusión financiera digital en comunidades no bancarizadas
- Herramienta accesible y confiable para administración colectiva

### Económicos

- Reducción de riesgos por manejo de efectivo
- Registros históricos de pagos y transacciones
- Generación de confianza y reputación financiera

### Tecnológicos

- Interoperabilidad gracias a estándares abiertos
- Aplicación ligera para dispositivos de gama media/baja
- Escalabilidad con backend en la nube

---

## Ambiente de Desarrollo Local

### Prerrequisitos

- **Node.js 20+** (recomendado usar NVM)
- **Android Studio** (última versión estable)
- **JDK 11** o superior
- **Git**
- **Wallet de Interledger Test Network**

### Configuración del Ambiente

```bash
# Clonar el repositorio
git clone <repository-url>
cd NestPay

# Instalar Node.js desde `./.nvmrc` (si usas NVM)
nvm install
nvm use

# Configurar backend
cd nestpay-backend
npm install

# Copiar y configurar variables de entorno
cp .env.example .env
# Editar .env con tus credenciales de Interledger
```

### Desarrollo Local

#### Iniciar el backend

```bash
cd nestpay-backend

# Modo desarrollo con recarga automática
npm run dev

# Modo producción
npm start
```

El backend estará disponible en `http://localhost:3000`

#### Ejecutar la app Android

```bash
# Abrir Android Studio e importar el proyecto
# Seleccionar "Run" → "Run 'app'" o presionar Shift+F10
# Elegir tu dispositivo/emulador
```

### Comandos Útiles

```bash
# Backend - instalar dependencias
npm install

# Backend - iniciar servidor de desarrollo
npm run dev

# Backend - verificar salud del servidor
curl http://localhost:3000/api/health

# Android - limpiar proyecto
./gradlew clean

# Android - compilar APK de debug
./gradlew assembleDebug

# Android - ejecutar pruebas
./gradlew test

# Android - verificar lint
./gradlew lint

# Firebase - desplegar reglas
firebase deploy --only firestore:rules

# Firebase - probar localmente
firebase emulators:start --only firestore
```

### Estructura del Proyecto

```
NestPay/
├── app/                          # Aplicación Android
│   ├── src/main/java/           # Código fuente Kotlin
│   ├── src/main/res/            # Recursos de Android
│   ├── build.gradle.kts         # Configuración Gradle de la app
│   └── google-services.json     # Configuración Firebase
├── nestpay-backend/             # Backend Node.js
│   ├── src/                     # Código fuente del backend
│   ├── keys/                    # Claves privadas (no en repo)
│   ├── package.json            # Dependencias Node.js
│   └── .env                    # Variables de entorno (no en repo)
├── gradle/                      # Gradle wrapper
├── build.gradle.kts            # Configuración Gradle del proyecto
└── firestore.rules             # Reglas de seguridad de Firestore
```

### Configuración Detallada

#### 1. Configuración Android

```bash
# Asegurar versiones requeridas del SDK:
# - Compile SDK: 36
# - Min SDK: 24
# - Target SDK: 36

# Descargar google-services.json desde Firebase Console
# Colocarlo en app/ directory
```

#### 2. Configuración Backend

```bash
# Configurar archivo .env con:
# WALLET_ADDRESS_URL=https://ilp.interledger-test.dev/tu-wallet
# PRIVATE_KEY_PATH=./keys/private.key
# KEY_ID=tu-key-id
# BASE_URL=http://localhost:3000
```

#### 3. Configuración Open Payments

```bash
# 1. Crear wallet de prueba en https://wallet.interledger-test.dev
# 2. Generar claves de desarrollador en configuración de wallet
# 3. Descargar archivo private.key y colocarlo en nestpay-backend/keys/
# 4. Actualizar .env con credenciales de tu wallet
```

### Pruebas

#### Flujo Completo de Pagos

1. Iniciar el backend: `npm run dev`
2. Ejecutar la app Android en modo debug
3. Crear una comunidad de prueba
4. Iniciar un pago usando wallets de prueba:
  - Alice: `https://ilp.interledger-test.dev/alice`
  - Bob: `https://ilp.interledger-test.dev/bob`

#### Pruebas de API

```bash
# Verificación de salud del backend
curl http://localhost:3000/api/health/detailed

# Validar una wallet
curl http://localhost:3000/api/wallets/validate/https://ilp.interledger-test.dev/alice

# Verificar compatibilidad de wallets
curl -X POST http://localhost:3000/api/wallets/check-compatibility \
  -H "Content-Type: application/json" \
  -d '{
    "senderWallet": "https://ilp.interledger-test.dev/alice",
    "receiverWallet": "https://ilp.interledger-test.dev/bob"
  }'
```

### Solución de Problemas

#### Problemas Comunes de Android
- **Fallo en sincronización de Gradle**: Verificar conexión a internet e intentar `./gradlew clean`
- **Problemas con Firebase**: Verificar que `google-services.json` esté en la ubicación correcta
- **Errores de compilación**: Asegurar que JDK 11+ esté configurado en Android Studio

#### Problemas Comunes del Backend

- **No se puede iniciar el servidor**: Verificar si el puerto 3000 está disponible o cambiar PORT en
  `.env`
- **Errores de Open Payments**: Verificar credenciales de wallet y conexión a internet
- **Variables de entorno**: Asegurar que el archivo `.env` exista y esté configurado correctamente

#### Consejos de Desarrollo
- Usar el administrador de dispositivos integrado de Android Studio para pruebas
- Habilitar depuración USB para pruebas en dispositivo físico
- Monitorear logs del backend con `npm run dev` para depuración en tiempo real
- Usar Firebase Console para monitorear operaciones de Firestore
- Probar pagos con wallets de prueba de Interledger antes de usar wallets reales