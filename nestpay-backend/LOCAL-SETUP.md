# 🏠 NestPay Backend - Servidor Local

Guía para usar el backend NestPay en tu máquina local.

## 🚀 Inicio Rápido

### **Opción 1: Script Windows (Recomendado)**

```bash
# Doble clic en:
start-local.bat
```

### **Opción 2: Comandos manuales**

```bash
cd nestpay-backend

# Desarrollo (recarga automática)
npm run dev

# Producción local (sin recarga)
npm run local
```

## 📍 URLs del Servidor Local

- **Backend API:** http://localhost:3000
- **Health Check:** http://localhost:3000/api/health
- **System Info:** http://localhost:3000/api/payments/system/info
- **Documentación:** http://localhost:3000/

## 🏢 Configuración de Wallets

### **Admin Wallet (walltest):**

- **URL:** https://ilp.interledger-test.dev/walltest
- **Rol:** Recibe aportes de comunidades
- **Key ID:** cd768451-a054-464f-a630-11ff1808fc2f

### **User Wallet (testwall):**

- **URL:** https://ilp.interledger-test.dev/testwall
- **Rol:** Paga aportes a comunidades
- **Key ID:** da18130b-b77a-437d-95c1-41dda9a771bc

## 📱 Configuración Android App

En tu app Android, configurar:

```kotlin
// En constants o build.gradle
const val NESTPAY_BACKEND_URL = "http://10.0.2.2:3000" // Para emulador
// O
const val NESTPAY_BACKEND_URL = "http://192.168.1.XXX:3000" // Para dispositivo físico
```

### **Encontrar tu IP local:**

```bash
# Windows
ipconfig
# Buscar "IPv4 Address" de tu adaptador de red activo
```

## 🧪 Endpoints de Prueba

### **System Info:**

```bash
curl http://localhost:3000/api/payments/system/info
```

### **Iniciar Pago de Comunidad:**

```bash
curl -X POST http://localhost:3000/api/payments/community/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "amount": {"value": "500"},
    "communityId": "test-comunidad",
    "conceptId": "cuota-mensual"
  }'
```

### **Validar Wallet:**

```bash
curl http://localhost:3000/api/wallets/validate/https://ilp.interledger-test.dev/walltest
```

## 🔧 Configuración de Red

### **Para Emulador Android:**

- Usar: `http://10.0.2.2:3000`
- El emulador mapea 10.0.2.2 → localhost del PC

### **Para Dispositivo Físico:**

1. **Encontrar IP de tu PC:**
   ```bash
   ipconfig
   # Ejemplo: 192.168.1.100
   ```

2. **Configurar en Android:**
   ```kotlin
   const val BACKEND_URL = "http://192.168.1.100:3000"
   ```

3. **Asegurar que el firewall permita conexiones al puerto 3000**

## ⚠️ Troubleshooting

### **Error: "Network Error" desde Android**

1. Verificar que el servidor esté corriendo: `http://localhost:3000`
2. Para emulador: usar `http://10.0.2.2:3000`
3. Para dispositivo: verificar IP local del PC
4. Verificar firewall de Windows

### **Error: "EADDRINUSE" (Puerto ocupado)**

```bash
# Encontrar proceso usando puerto 3000
netstat -ano | findstr :3000

# Terminar proceso (reemplazar PID)
taskkill /PID XXXX /F
```

### **Error: Variables de entorno**

- Verificar que `.env` existe en `nestpay-backend/`
- Verificar credenciales de wallets

## 🎯 Ventajas del Servidor Local

✅ **Sin deploy:** Cambios inmediatos  
✅ **Sin costo:** No necesitas servicios en la nube  
✅ **Control total:** Logs y debugging fácil  
✅ **Desarrollo rápido:** Reiniciar instant  
✅ **Sin límites:** No hay rate limits estrictos

## 🚀 ¡Listo para Desarrollar!

Tu backend local está configurado y funcionando. Ahora puedes:

1. **Desarrollar tu app Android** conectándola al backend local
2. **Probar pagos** con las wallets configuradas
3. **Hacer cambios** al backend en tiempo real
4. **Debuggear** fácilmente con logs detallados

**URL del servidor:** http://localhost:3000  
**Status:** ✅ Funcionando con walltest & testwall