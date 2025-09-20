import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import morgan from 'morgan';
import dotenv from 'dotenv';
import rateLimit from 'express-rate-limit';

// Importar rutas
import walletRoutes from './routes/walletRoutes.js';
import paymentRoutes from './routes/paymentRoutes.js';
import webhookRoutes from './routes/webhookRoutes.js';
import healthRoutes from './routes/healthRoutes.js';

// Importar middleware
import { errorHandler } from './middleware/errorHandler.js';
import { logger } from './utils/logger.js';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 3000;

// Configuración de Rate Limiting
const limiter = rateLimit({
  windowMs: parseInt(process.env.RATE_LIMIT_WINDOW_MS) || 15 * 60 * 1000, // 15 minutos
  max: parseInt(process.env.RATE_LIMIT_MAX_REQUESTS) || 100, // limite de 100 requests por ventana
  message: {
    error: 'Demasiadas solicitudes desde esta IP, por favor inténtalo más tarde.'
  },
  standardHeaders: true,
  legacyHeaders: false,
});

// Middleware de seguridad
app.use(helmet({
  crossOriginResourcePolicy: { policy: "cross-origin" }
}));

// Configuración CORS
app.use(cors({
  origin: [
    process.env.FRONTEND_URL,
    'http://localhost:3000',
    'http://localhost:8080',
    'https://*.railway.app'
  ],
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With']
}));

// Middleware básico
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));
app.use(morgan('combined'));
app.use(limiter);

// Rutas principales
app.use('/api/health', healthRoutes);
app.use('/api/wallets', walletRoutes);
app.use('/api/payments', paymentRoutes);
app.use('/api/webhooks', webhookRoutes);

// Ruta raíz
app.get('/', (req, res) => {
  res.json({
    message: 'NestPay Backend API - Open Payments Integration',
    version: '1.0.0',
    timestamp: new Date().toISOString(),
    endpoints: {
      health: '/api/health',
      wallets: '/api/wallets',
      payments: '/api/payments',
      webhooks: '/api/webhooks'
    }
  });
});

// Middleware de manejo de errores
app.use(errorHandler);

// Manejo de rutas no encontradas
app.use('*', (req, res) => {
  res.status(404).json({
    error: 'Endpoint no encontrado',
    path: req.originalUrl,
    method: req.method
  });
});

// Iniciar servidor
app.listen(PORT, '0.0.0.0', () => {
  logger.info(`🚀 Servidor NestPay Backend iniciado en puerto ${PORT}`);
  logger.info(`🌐 URL base: ${process.env.BASE_URL || `http://localhost:${PORT}`}`);
  logger.info(`💰 Open Payments habilitado con wallet: ${process.env.WALLET_ADDRESS_URL}`);
});

// Manejo graceful de cierre
process.on('SIGTERM', () => {
  logger.info('🛑 Cerrando servidor gracefully...');
  process.exit(0);
});

process.on('SIGINT', () => {
  logger.info('🛑 Cerrando servidor gracefully...');
  process.exit(0);
});

export default app;