import express from 'express';
import { body, validationResult } from 'express-validator';
import { logger } from '../utils/logger.js';

const router = express.Router();

// Middleware para validar errores
const handleValidationErrors = (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({
      error: 'Datos de validación incorrectos',
      details: errors.array()
    });
  }
  next();
};

/**
 * POST /api/webhooks/payment-events
 * Recibir eventos de webhooks relacionados con pagos
 */
router.post('/payment-events', [
  body('type').notEmpty().withMessage('Event type es requerido'),
  body('data').isObject().withMessage('Event data debe ser un objeto')
], handleValidationErrors, async (req, res) => {
  try {
    const { type, data } = req.body;
    
    logger.info(`📨 Webhook recibido - Tipo: ${type}`, { data });

    // Procesar diferentes tipos de eventos
    switch (type) {
      case 'incoming_payment.created':
        await handleIncomingPaymentCreated(data);
        break;
      
      case 'incoming_payment.completed':
        await handleIncomingPaymentCompleted(data);
        break;
      
      case 'incoming_payment.expired':
        await handleIncomingPaymentExpired(data);
        break;
      
      case 'outgoing_payment.created':
        await handleOutgoingPaymentCreated(data);
        break;
      
      case 'outgoing_payment.completed':
        await handleOutgoingPaymentCompleted(data);
        break;
      
      case 'outgoing_payment.failed':
        await handleOutgoingPaymentFailed(data);
        break;
      
      case 'quote.created':
        await handleQuoteCreated(data);
        break;
      
      case 'quote.expired':
        await handleQuoteExpired(data);
        break;
      
      default:
        logger.warn(`⚠️ Tipo de evento no manejado: ${type}`);
        break;
    }

    // Responder exitosamente al webhook
    res.status(200).json({
      success: true,
      message: 'Webhook procesado exitosamente',
      eventType: type,
      timestamp: new Date().toISOString()
    });

  } catch (error) {
    logger.error('❌ Error procesando webhook:', error);
    res.status(500).json({
      error: 'Error procesando webhook',
      message: error.message
    });
  }
});

/**
 * POST /api/webhooks/grant-events
 * Recibir eventos de webhooks relacionados con grants/autorizaciones
 */
router.post('/grant-events', [
  body('type').notEmpty().withMessage('Event type es requerido'),
  body('data').isObject().withMessage('Event data debe ser un objeto')
], handleValidationErrors, async (req, res) => {
  try {
    const { type, data } = req.body;
    
    logger.info(`🔐 Grant webhook recibido - Tipo: ${type}`, { data });

    switch (type) {
      case 'grant.approved':
        await handleGrantApproved(data);
        break;
      
      case 'grant.denied':
        await handleGrantDenied(data);
        break;
      
      case 'grant.revoked':
        await handleGrantRevoked(data);
        break;
      
      default:
        logger.warn(`⚠️ Tipo de evento de grant no manejado: ${type}`);
        break;
    }

    res.status(200).json({
      success: true,
      message: 'Grant webhook procesado exitosamente',
      eventType: type,
      timestamp: new Date().toISOString()
    });

  } catch (error) {
    logger.error('❌ Error procesando grant webhook:', error);
    res.status(500).json({
      error: 'Error procesando grant webhook',
      message: error.message
    });
  }
});

/**
 * GET /api/webhooks/test
 * Endpoint de prueba para verificar que los webhooks funcionen
 */
router.get('/test', (req, res) => {
  logger.info('🧪 Webhook test endpoint called');
  
  res.json({
    success: true,
    message: 'Webhook endpoint funcionando correctamente',
    timestamp: new Date().toISOString(),
    endpoints: {
      paymentEvents: '/api/webhooks/payment-events',
      grantEvents: '/api/webhooks/grant-events'
    }
  });
});

// =================== HANDLERS DE EVENTOS ===================

async function handleIncomingPaymentCreated(data) {
  logger.info('✅ Incoming payment creado:', {
    id: data.id,
    walletAddress: data.walletAddress,
    amount: data.incomingAmount
  });
  
  // Aquí puedes agregar lógica específica:
  // - Notificar al usuario receptor
  // - Actualizar base de datos
  // - Enviar notificación push
}

async function handleIncomingPaymentCompleted(data) {
  logger.info('💰 Incoming payment completado:', {
    id: data.id,
    receivedAmount: data.receivedAmount
  });
  
  // Lógica para manejar pago completado:
  // - Confirmar recepción de fondos
  // - Actualizar balance del usuario
  // - Enviar confirmación
}

async function handleIncomingPaymentExpired(data) {
  logger.warn('⏰ Incoming payment expirado:', {
    id: data.id,
    walletAddress: data.walletAddress
  });
  
  // Lógica para manejar expiración:
  // - Notificar al usuario
  // - Limpiar datos temporales
}

async function handleOutgoingPaymentCreated(data) {
  logger.info('🚀 Outgoing payment creado:', {
    id: data.id,
    walletAddress: data.walletAddress,
    quoteId: data.quoteId
  });
  
  // Lógica para pago saliente creado:
  // - Actualizar estado en la app
  // - Notificar al usuario emisor
}

async function handleOutgoingPaymentCompleted(data) {
  logger.info('✅ Outgoing payment completado:', {
    id: data.id,
    sentAmount: data.sentAmount,
    state: data.state
  });
  
  // Lógica para pago completado:
  // - Confirmar envío de fondos
  // - Actualizar balance del usuario
  // - Enviar confirmación de éxito
}

async function handleOutgoingPaymentFailed(data) {
  logger.error('❌ Outgoing payment falló:', {
    id: data.id,
    state: data.state,
    reason: data.reason
  });
  
  // Lógica para pago fallido:
  // - Notificar al usuario del error
  // - Revertir cambios si es necesario
  // - Ofrecer opciones de reintento
}

async function handleQuoteCreated(data) {
  logger.info('💲 Quote creado:', {
    id: data.id,
    sendAmount: data.sendAmount,
    receiveAmount: data.receiveAmount
  });
  
  // Lógica para quote creado:
  // - Mostrar estimación al usuario
  // - Guardar quote temporalmente
}

async function handleQuoteExpired(data) {
  logger.warn('⏰ Quote expirado:', {
    id: data.id
  });
  
  // Lógica para quote expirado:
  // - Invalidar quote en la UI
  // - Solicitar nuevo quote si es necesario
}

async function handleGrantApproved(data) {
  logger.info('✅ Grant aprobado:', {
    grantId: data.grantId,
    accessToken: data.accessToken ? '***' : 'none'
  });
  
  // Lógica para grant aprobado:
  // - Continuar con el flujo de pago
  // - Almacenar token de acceso de forma segura
}

async function handleGrantDenied(data) {
  logger.warn('❌ Grant denegado:', {
    grantId: data.grantId,
    reason: data.reason
  });
  
  // Lógica para grant denegado:
  // - Notificar al usuario
  // - Cancelar operación pendiente
}

async function handleGrantRevoked(data) {
  logger.warn('🚫 Grant revocado:', {
    grantId: data.grantId
  });
  
  // Lógica para grant revocado:
  // - Invalidar tokens relacionados
  // - Detener operaciones en curso
}

export default router;