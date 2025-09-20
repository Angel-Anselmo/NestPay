import express from 'express';
import openPaymentsService from '../services/openPaymentsService.js';
import { logger } from '../utils/logger.js';

const router = express.Router();

/**
 * GET /api/health
 * Health check básico
 */
router.get('/', (req, res) => {
  res.json({
    status: 'healthy',
    message: 'NestPay Backend API funcionando correctamente',
    timestamp: new Date().toISOString(),
    version: '1.0.0',
    environment: process.env.NODE_ENV || 'development'
  });
});

/**
 * GET /api/health/detailed
 * Health check detallado con verificación de servicios
 */
router.get('/detailed', async (req, res) => {
  const healthCheck = {
    status: 'healthy',
    timestamp: new Date().toISOString(),
    version: '1.0.0',
    environment: process.env.NODE_ENV || 'development',
    services: {
      openPayments: { status: 'unknown', message: 'No verificado' },
      server: { status: 'healthy', message: 'Servidor funcionando' }
    },
    configuration: {
      walletConfigured: !!process.env.WALLET_ADDRESS_URL,
      privateKeyConfigured: !!process.env.PRIVATE_KEY_PATH,
      keyIdConfigured: !!process.env.KEY_ID
    }
  };

  // Verificar conexión con Open Payments
  try {
    if (process.env.WALLET_ADDRESS_URL) {
      const walletResult = await openPaymentsService.validateWalletAddress(process.env.WALLET_ADDRESS_URL);
      
      if (walletResult.isValid) {
        healthCheck.services.openPayments = {
          status: 'healthy',
          message: 'Conexión con Open Payments establecida',
          walletAddress: process.env.WALLET_ADDRESS_URL,
          authServer: walletResult.authServer
        };
      } else {
        healthCheck.services.openPayments = {
          status: 'error',
          message: `Error conectando con Open Payments: ${walletResult.error}`
        };
        healthCheck.status = 'degraded';
      }
    } else {
      healthCheck.services.openPayments = {
        status: 'not_configured',
        message: 'Wallet address no configurada'
      };
      healthCheck.status = 'degraded';
    }
  } catch (error) {
    logger.error('Error en health check de Open Payments:', error);
    healthCheck.services.openPayments = {
      status: 'error',
      message: `Error: ${error.message}`
    };
    healthCheck.status = 'unhealthy';
  }

  // Determinar status general
  const hasErrors = Object.values(healthCheck.services).some(service => service.status === 'error');
  const hasWarnings = Object.values(healthCheck.services).some(service => service.status === 'not_configured');
  
  if (hasErrors) {
    healthCheck.status = 'unhealthy';
  } else if (hasWarnings) {
    healthCheck.status = 'degraded';
  }

  // Responder con el código de estado apropiado
  const statusCode = healthCheck.status === 'healthy' ? 200 : 
                    healthCheck.status === 'degraded' ? 200 : 503;

  res.status(statusCode).json(healthCheck);
});

/**
 * GET /api/health/openpayments
 * Health check específico para Open Payments
 */
router.get('/openpayments', async (req, res) => {
  try {
    const checks = {
      timestamp: new Date().toISOString(),
      configuration: {
        walletAddress: process.env.WALLET_ADDRESS_URL || 'No configurada',
        privateKeyPath: process.env.PRIVATE_KEY_PATH ? 'Configurada' : 'No configurada',
        keyId: process.env.KEY_ID || 'No configurado'
      },
      connectivity: {
        status: 'unknown',
        message: 'No verificado'
      }
    };

    if (!process.env.WALLET_ADDRESS_URL) {
      return res.status(503).json({
        ...checks,
        status: 'error',
        message: 'Configuración de Open Payments incompleta'
      });
    }

    // Verificar conectividad
    const walletResult = await openPaymentsService.validateWalletAddress(process.env.WALLET_ADDRESS_URL);
    
    if (walletResult.isValid) {
      checks.connectivity = {
        status: 'healthy',
        message: 'Conexión exitosa con Open Payments',
        walletInfo: {
          url: walletResult.walletAddress.id,
          publicName: walletResult.walletAddress.publicName,
          assetCode: walletResult.walletAddress.assetCode,
          assetScale: walletResult.walletAddress.assetScale,
          authServer: walletResult.authServer
        }
      };

      res.json({
        ...checks,
        status: 'healthy',
        message: 'Open Payments funcionando correctamente'
      });
    } else {
      checks.connectivity = {
        status: 'error',
        message: walletResult.error
      };

      res.status(503).json({
        ...checks,
        status: 'error',
        message: 'Error conectando con Open Payments'
      });
    }

  } catch (error) {
    logger.error('Error en health check de Open Payments:', error);
    res.status(503).json({
      status: 'error',
      message: 'Error verificando Open Payments',
      error: error.message,
      timestamp: new Date().toISOString()
    });
  }
});

/**
 * GET /api/health/ready
 * Readiness probe - verifica si el servicio está listo para recibir tráfico
 */
router.get('/ready', async (req, res) => {
  try {
    // Verificar configuración mínima
    const hasRequiredConfig = process.env.WALLET_ADDRESS_URL && 
                             process.env.PRIVATE_KEY_PATH && 
                             process.env.KEY_ID;

    if (!hasRequiredConfig) {
      return res.status(503).json({
        ready: false,
        message: 'Configuración incompleta',
        missing: {
          walletAddress: !process.env.WALLET_ADDRESS_URL,
          privateKey: !process.env.PRIVATE_KEY_PATH,
          keyId: !process.env.KEY_ID
        }
      });
    }

    // Verificar que Open Payments esté accesible
    const walletResult = await openPaymentsService.validateWalletAddress(process.env.WALLET_ADDRESS_URL);
    
    if (walletResult.isValid) {
      res.json({
        ready: true,
        message: 'Servicio listo para recibir tráfico',
        timestamp: new Date().toISOString()
      });
    } else {
      res.status(503).json({
        ready: false,
        message: 'Open Payments no accesible',
        error: walletResult.error
      });
    }

  } catch (error) {
    logger.error('Error en readiness check:', error);
    res.status(503).json({
      ready: false,
      message: 'Error verificando readiness',
      error: error.message
    });
  }
});

/**
 * GET /api/health/live
 * Liveness probe - verifica si el servicio está funcionando
 */
router.get('/live', (req, res) => {
  // Verificación básica de que el proceso está funcionando
  const memoryUsage = process.memoryUsage();
  const uptime = process.uptime();

  res.json({
    alive: true,
    message: 'Servicio funcionando',
    timestamp: new Date().toISOString(),
    uptime: `${Math.floor(uptime / 3600)}h ${Math.floor((uptime % 3600) / 60)}m ${Math.floor(uptime % 60)}s`,
    memory: {
      rss: `${Math.round(memoryUsage.rss / 1024 / 1024)}MB`,
      heapTotal: `${Math.round(memoryUsage.heapTotal / 1024 / 1024)}MB`,
      heapUsed: `${Math.round(memoryUsage.heapUsed / 1024 / 1024)}MB`
    }
  });
});

export default router;