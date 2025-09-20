// Vercel Serverless Function - Main Handler
import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import dotenv from 'dotenv';
import rateLimit from 'express-rate-limit';

// Import routes
import walletRoutes from './routes/wallets.js';
import paymentRoutes from './routes/payments.js';
import webhookRoutes from './routes/webhooks.js';
import healthRoutes from './routes/health.js';

// Import middleware
import { errorHandler } from './middleware/errorHandler.js';

dotenv.config();

const app = express();

// Rate limiting for serverless
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 50, // más restrictivo para serverless
  message: {
    error: 'Demasiadas solicitudes, por favor inténtalo más tarde.'
  },
  standardHeaders: true,
  legacyHeaders: false,
});

// Security middleware
app.use(helmet({
  crossOriginResourcePolicy: { policy: "cross-origin" }
}));

// CORS configuration
app.use(cors({
  origin: [
    process.env.FRONTEND_URL,
    'http://localhost:3000',
    'http://localhost:8080',
    /\.vercel\.app$/,
    /\.railway\.app$/
  ],
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With']
}));

// Basic middleware
app.use(express.json({ limit: '1mb' })); // Más restrictivo para serverless
app.use(express.urlencoded({ extended: true }));
app.use(limiter);

// Routes
app.use('/api/health', healthRoutes);
app.use('/api/wallets', walletRoutes);
app.use('/api/payments', paymentRoutes);
app.use('/api/webhooks', webhookRoutes);

// Root route
app.get('/', (req, res) => {
  res.json({
    message: 'NestPay Backend API - Serverless (Vercel)',
    version: '1.0.0',
    mode: 'serverless',
    timestamp: new Date().toISOString(),
    endpoints: {
      health: '/api/health',
      wallets: '/api/wallets',
      payments: '/api/payments',
      webhooks: '/api/webhooks'
    }
  });
});

// Error handling
app.use(errorHandler);

// 404 handler
app.use('*', (req, res) => {
  res.status(404).json({
    error: 'Endpoint no encontrado',
    path: req.originalUrl,
    method: req.method
  });
});

// Export for Vercel
export default app;