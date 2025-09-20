import express from 'express';
import { body, param, query, validationResult } from 'express-validator';
import openPaymentsService from '../services/openPaymentsService.js';
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

// =================== FLUJO NESTPAY COMUNIDADES ===================

/**
 * POST /api/payments/community/initiate
 * Iniciar un flujo de aporte a comunidad (USER paga al ADMIN)
 */
router.post('/community/initiate', [
  body('amount.value').isNumeric().withMessage('El valor del monto debe ser numérico'),
  body('amount.assetCode').optional().isString().withMessage('Asset code debe ser string'),
  body('amount.assetScale').optional().isInt({ min: 0 }).withMessage('Asset scale debe ser entero positivo'),
  body('description').optional().isString().withMessage('Description debe ser string'),
  body('communityId').optional().isString().withMessage('Community ID debe ser string'),
  body('conceptId').optional().isString().withMessage('Concept ID debe ser string')
], handleValidationErrors, async (req, res) => {
  try {
    const { amount, description, communityId, conceptId } = req.body;

    const paymentDescription = description || 
      `Aporte a comunidad${communityId ? ` ${communityId}` : ''}${conceptId ? ` - ${conceptId}` : ''}`;

    const communityFlow = await openPaymentsService.createCommunityContributionFlow(
      amount,
      paymentDescription
    );

    res.json({
      success: true,
      message: 'Flujo de aporte a comunidad iniciado. Autorización del usuario requerida.',
      data: {
        // IDs de los pagos
        incomingPaymentId: communityFlow.incomingPayment.id,
        quoteId: communityFlow.quote.id,
        
        // Wallets involucradas
        adminWallet: communityFlow.adminWallet,
        userWallet: communityFlow.userWallet,
        
        // URL para autorización
        authorizationUrl: communityFlow.authorizationUrl,
        
        // Estimación de costos
        estimatedFees: communityFlow.estimatedFees,
        
        // Datos para continuar el flujo
        continueData: communityFlow.continueData,
        
        // Metadatos adicionales
        metadata: {
          communityId,
          conceptId,
          description: paymentDescription,
          flow: 'community-contribution'
        }
      }
    });
  } catch (error) {
    logger.error('Error iniciando flujo de aporte a comunidad:', error);
    res.status(500).json({
      error: 'Error interno del servidor',
      message: error.message
    });
  }
});

/**
 * POST /api/payments/community/finalize
 * Finalizar un pago de aporte a comunidad después de la autorización del usuario
 */
router.post('/community/finalize', [
  body('continueUri').isURL().withMessage('Continue URI debe ser una URL válida'),
  body('continueAccessToken').notEmpty().withMessage('Continue access token es requerido'),
  body('interactRef').notEmpty().withMessage('Interact reference es requerido'),
  body('quoteId').isUUID().withMessage('Quote ID debe ser un UUID válido'),
  body('communityId').optional().isString().withMessage('Community ID debe ser string'),
  body('conceptId').optional().isString().withMessage('Concept ID debe ser string')
], handleValidationErrors, async (req, res) => {
  try {
    const { continueUri, continueAccessToken, interactRef, quoteId, communityId, conceptId } = req.body;

    const result = await openPaymentsService.finalizeCommunityPayment(
      continueUri,
      continueAccessToken,
      interactRef,
      quoteId
    );

    res.json({
      success: true,
      message: 'Aporte a comunidad finalizado exitosamente',
      data: {
        // Resultado del pago
        outgoingPayment: result.outgoingPayment,
        status: result.outgoingPayment.state,
        
        // Wallets involucradas
        fromWallet: result.userWallet,
        toWallet: result.adminWallet,
        
        // Metadatos
        metadata: {
          communityId,
          conceptId,
          paymentType: 'community-contribution',
          completedAt: new Date().toISOString()
        }
      }
    });
  } catch (error) {
    logger.error('Error finalizando aporte a comunidad:', error);
    res.status(500).json({
      error: 'Error interno del servidor',
      message: error.message
    });
  }
});

// =================== QUOTES ===================

/**
 * POST /api/payments/quotes
 * Crear un quote para calcular el costo de un pago
 */
router.post('/quotes', [
  body('senderWallet').isURL().withMessage('Sender wallet debe ser una URL válida'),
  body('receiverPaymentUrl').isURL().withMessage('Receiver payment URL debe ser una URL válida'),
  body('amount.value').isNumeric().withMessage('El valor del monto debe ser numérico'),
  body('amount.type').isIn(['send', 'receive']).withMessage('Tipo debe ser send o receive'),
  body('amount.assetCode').optional().isString().withMessage('Asset code debe ser string'),
  body('amount.assetScale').optional().isInt({ min: 0 }).withMessage('Asset scale debe ser entero positivo')
], handleValidationErrors, async (req, res) => {
  try {
    const { senderWallet, receiverPaymentUrl, amount } = req.body;

    const result = await openPaymentsService.createQuote(
      senderWallet,
      receiverPaymentUrl,
      amount
    );

    res.json({
      success: true,
      data: {
        quote: result.quote,
        walletAddress: result.walletAddress
      }
    });
  } catch (error) {
    logger.error('Error creando quote:', error);
    res.status(500).json({
      error: 'Error interno del servidor',
      message: error.message
    });
  }
});

/**
 * GET /api/payments/quotes/:walletAddress/:quoteId
 * Obtener un quote específico
 */
router.get('/quotes/:walletAddress/:quoteId', [
  param('walletAddress').isURL().withMessage('Wallet address debe ser una URL válida'),
  param('quoteId').isUUID().withMessage('Quote ID debe ser un UUID válido'),
  query('accessToken').notEmpty().withMessage('Access token es requerido')
], handleValidationErrors, async (req, res) => {
  try {
    const { walletAddress, quoteId } = req.params;
    const { accessToken } = req.query;

    const quote = await openPaymentsService.getQuote(
      walletAddress,
      quoteId,
      accessToken
    );

    res.json({
      success: true,
      data: quote
    });
  } catch (error) {
    logger.error('Error obteniendo quote:', error);
    res.status(500).json({
      error: 'Error interno del servidor',
      message: error.message
    });
  }
});

// =================== OUTGOING PAYMENTS ===================

/**
 * POST /api/payments/outgoing
 * Crear un outgoing payment
 */
router.post('/outgoing', [
  body('walletAddress').isURL().withMessage('Wallet address debe ser una URL válida'),
  body('quoteId').isUUID().withMessage('Quote ID debe ser un UUID válido'),
  body('accessToken').notEmpty().withMessage('Access token es requerido'),
  body('metadata').optional().isObject().withMessage('Metadata debe ser un objeto')
], handleValidationErrors, async (req, res) => {
  try {
    const { walletAddress, quoteId, accessToken, metadata = {} } = req.body;

    const outgoingPayment = await openPaymentsService.createOutgoingPayment(
      walletAddress,
      quoteId,
      accessToken,
      metadata
    );

    res.json({
      success: true,
      data: outgoingPayment
    });
  } catch (error) {
    logger.error('Error creando outgoing payment:', error);
    res.status(500).json({
      error: 'Error interno del servidor',
      message: error.message
    });
  }
});

/**
 * GET /api/payments/outgoing/:walletAddress/:paymentId
 * Obtener un outgoing payment específico
 */
router.get('/outgoing/:walletAddress/:paymentId', [
  param('walletAddress').isURL().withMessage('Wallet address debe ser una URL válida'),
  param('paymentId').isUUID().withMessage('Payment ID debe ser un UUID válido'),
  query('accessToken').notEmpty().withMessage('Access token es requerido')
], handleValidationErrors, async (req, res) => {
  try {
    const { walletAddress, paymentId } = req.params;
    const { accessToken } = req.query;

    const outgoingPayment = await openPaymentsService.getOutgoingPayment(
      walletAddress,
      paymentId,
      accessToken
    );

    res.json({
      success: true,
      data: outgoingPayment
    });
  } catch (error) {
    logger.error('Error obteniendo outgoing payment:', error);
    res.status(500).json({
      error: 'Error interno del servidor',
      message: error.message
    });
  }
});

// =================== FLUJO COMPLETO DE PAGO ===================

/**
 * POST /api/payments/initiate
 * Iniciar un flujo completo de pago (simplificado para la app)
 */
router.post('/initiate', [
  body('senderWallet').isURL().withMessage('Sender wallet debe ser una URL válida'),
  body('receiverWallet').isURL().withMessage('Receiver wallet debe ser una URL válida'),
  body('amount.value').isNumeric().withMessage('El valor del monto debe ser numérico'),
  body('amount.assetCode').optional().isString().withMessage('Asset code debe ser string'),
  body('amount.assetScale').optional().isInt({ min: 0 }).withMessage('Asset scale debe ser entero positivo'),
  body('description').optional().isString().withMessage('Description debe ser string')
], handleValidationErrors, async (req, res) => {
  try {
    const { senderWallet, receiverWallet, amount, description } = req.body;

    const paymentFlow = await openPaymentsService.createCompletePaymentFlow(
      senderWallet,
      receiverWallet,
      amount,
      description
    );

    // Guardar datos del flujo en algún lugar (Redis, base de datos, etc.)
    // Para este ejemplo, simplemente los devolvemos
    res.json({
      success: true,
      message: 'Flujo de pago iniciado. Autorización del usuario requerida.',
      data: {
        paymentId: paymentFlow.incomingPayment.id,
        quoteId: paymentFlow.quote.id,
        authorizationUrl: paymentFlow.authorizationUrl,
        estimatedFees: {
          sendAmount: paymentFlow.quote.sendAmount,
          receiveAmount: paymentFlow.quote.receiveAmount
        },
        continueData: paymentFlow.continueData
      }
    });
  } catch (error) {
    logger.error('Error iniciando flujo de pago:', error);
    res.status(500).json({
      error: 'Error interno del servidor',
      message: error.message
    });
  }
});

/**
 * POST /api/payments/finalize
 * Finalizar un pago después de la autorización del usuario
 */
router.post('/finalize', [
  body('continueUri').isURL().withMessage('Continue URI debe ser una URL válida'),
  body('continueAccessToken').notEmpty().withMessage('Continue access token es requerido'),
  body('interactRef').notEmpty().withMessage('Interact reference es requerido'),
  body('walletAddress').isURL().withMessage('Wallet address debe ser una URL válida'),
  body('quoteId').isUUID().withMessage('Quote ID debe ser un UUID válido')
], handleValidationErrors, async (req, res) => {
  try {
    const { continueUri, continueAccessToken, interactRef, walletAddress, quoteId } = req.body;

    const result = await openPaymentsService.finalizePayment(
      continueUri,
      continueAccessToken,
      interactRef,
      walletAddress,
      quoteId
    );

    res.json({
      success: true,
      message: 'Pago finalizado exitosamente',
      data: {
        outgoingPayment: result.outgoingPayment,
        status: result.outgoingPayment.state
      }
    });
  } catch (error) {
    logger.error('Error finalizando pago:', error);
    res.status(500).json({
      error: 'Error interno del servidor',
      message: error.message
    });
  }
});

/**
 * GET /api/payments/grant-callback
 * Callback para manejar la autorización del grant
 */
router.get('/grant-callback', async (req, res) => {
  try {
    const { interact_ref, hash, state } = req.query;

    if (!interact_ref) {
      return res.status(400).json({
        error: 'Missing interact_ref parameter'
      });
    }

    // En una implementación real, aquí deberías:
    // 1. Validar el state parameter
    // 2. Recuperar los datos del flujo de pago usando el state
    // 3. Continuar automáticamente el grant
    // 4. Redirigir al usuario a tu app con el resultado

    res.json({
      success: true,
      message: 'Autorización recibida para aporte a comunidad',
      data: {
        interactRef: interact_ref,
        hash,
        state,
        instructions: 'Usa estos datos en el endpoint /api/payments/community/finalize para completar el aporte'
      }
    });
  } catch (error) {
    logger.error('Error en grant callback:', error);
    res.status(500).json({
      error: 'Error interno del servidor',
      message: error.message
    });
  }
});

// =================== INFORMACIÓN DEL SISTEMA ===================

/**
 * GET /api/payments/system/info
 * Obtener información sobre las wallets del sistema (admin y user)
 */
router.get('/system/info', async (req, res) => {
  try {
    const systemInfo = openPaymentsService.getSystemInfo();
    
    res.json({
      success: true,
      data: {
        system: 'NestPay Backend - Multi-Wallet',
        wallets: systemInfo,
        flows: {
          communityContribution: {
            description: 'Usuario paga aportes al admin de la comunidad',
            endpoints: {
              initiate: 'POST /api/payments/community/initiate',
              finalize: 'POST /api/payments/community/finalize'
            }
          }
        },
        timestamp: new Date().toISOString()
      }
    });
  } catch (error) {
    logger.error('Error obteniendo información del sistema:', error);
    res.status(500).json({
      error: 'Error interno del servidor',
      message: error.message
    });
  }
});

// =================== INCOMING PAYMENTS (Compatibilidad) ===================

/**
 * POST /api/payments/incoming
 * Crear un incoming payment (mantenido para compatibilidad)
 */
router.post('/incoming', [
  body('walletAddress').isURL().withMessage('Wallet address debe ser una URL válida'),
  body('amount.value').isNumeric().withMessage('El valor del monto debe ser numérico'),
  body('amount.assetCode').optional().isString().withMessage('Asset code debe ser string'),
  body('amount.assetScale').optional().isInt({ min: 0 }).withMessage('Asset scale debe ser entero positivo'),
  body('description').optional().isString().withMessage('Description debe ser string')
], handleValidationErrors, async (req, res) => {
  try {
    const { walletAddress, amount, description } = req.body;

    // Determinar si es admin wallet para usar el método correcto
    if (walletAddress === process.env.ADMIN_WALLET_ADDRESS_URL) {
      const result = await openPaymentsService.createCommunityIncomingPayment(amount, description);
      
      res.json({
        success: true,
        data: {
          incomingPayment: result.incomingPayment,
          walletAddress: result.walletAddress,
          adminWallet: result.adminWallet
        }
      });
    } else {
      // Para otras wallets, usar método genérico
      res.status(400).json({
        error: 'Wallet no soportada',
        message: 'Solo se pueden crear incoming payments en la wallet del admin',
        supportedWallet: process.env.ADMIN_WALLET_ADDRESS_URL
      });
    }
  } catch (error) {
    logger.error('Error creando incoming payment:', error);
    res.status(500).json({
      error: 'Error interno del servidor',
      message: error.message
    });
  }
});

/**
 * GET /api/payments/incoming/:walletAddress/:paymentId
 * Obtener un incoming payment específico
 */
router.get('/incoming/:walletAddress/:paymentId', [
  param('walletAddress').isURL().withMessage('Wallet address debe ser una URL válida'),
  param('paymentId').isUUID().withMessage('Payment ID debe ser un UUID válido'),
  query('accessToken').notEmpty().withMessage('Access token es requerido')
], handleValidationErrors, async (req, res) => {
  try {
    const { walletAddress, paymentId } = req.params;
    const { accessToken } = req.query;

    const incomingPayment = await openPaymentsService.getIncomingPayment(
      walletAddress,
      paymentId,
      accessToken
    );

    res.json({
      success: true,
      data: incomingPayment
    });
  } catch (error) {
    logger.error('Error obteniendo incoming payment:', error);
    res.status(500).json({
      error: 'Error interno del servidor',
      message: error.message
    });
  }
});

// =================== OUTGOING PAYMENTS ===================

/**
 * GET /api/payments/outgoing/:walletAddress/:paymentId
 * Obtener un outgoing payment específico
 */
router.get('/outgoing/:walletAddress/:paymentId', [
  param('walletAddress').isURL().withMessage('Wallet address debe ser una URL válida'),
  param('paymentId').isUUID().withMessage('Payment ID debe ser un UUID válido'),
  query('accessToken').notEmpty().withMessage('Access token es requerido')
], handleValidationErrors, async (req, res) => {
  try {
    const { walletAddress, paymentId } = req.params;
    const { accessToken } = req.query;

    const outgoingPayment = await openPaymentsService.getOutgoingPayment(
      walletAddress,
      paymentId,
      accessToken
    );

    res.json({
      success: true,
      data: outgoingPayment
    });
  } catch (error) {
    logger.error('Error obteniendo outgoing payment:', error);
    res.status(500).json({
      error: 'Error interno del servidor',
      message: error.message
    });
  }
});

// =================== GRANT CALLBACK ===================

/**
 * GET /api/payments/grant-callback
 * Callback para manejar la autorización del grant
 */
router.get('/grant-callback', async (req, res) => {
  try {
    const { interact_ref, hash, state } = req.query;

    if (!interact_ref) {
      return res.status(400).json({
        error: 'Missing interact_ref parameter'
      });
    }

    // En una implementación real, aquí deberías:
    // 1. Validar el state parameter
    // 2. Recuperar los datos del flujo de pago usando el state
    // 3. Continuar automáticamente el grant
    // 4. Redirigir al usuario a tu app con el resultado

    res.json({
      success: true,
      message: 'Autorización recibida para aporte a comunidad',
      data: {
        interactRef: interact_ref,
        hash,
        state,
        instructions: 'Usa estos datos en el endpoint /api/payments/community/finalize para completar el aporte'
      }
    });
  } catch (error) {
    logger.error('Error en grant callback:', error);
    res.status(500).json({
      error: 'Error interno del servidor',
      message: error.message
    });
  }
});

// =================== UTILIDADES ===================

/**
 * GET /api/payments/status/:paymentId
 * Obtener el estado de un pago (tanto incoming como outgoing)
 */
router.get('/status/:paymentId', [
  param('paymentId').isUUID().withMessage('Payment ID debe ser un UUID válido'),
  query('walletAddress').isURL().withMessage('Wallet address debe ser una URL válida'),
  query('accessToken').notEmpty().withMessage('Access token es requerido'),
  query('type').isIn(['incoming', 'outgoing']).withMessage('Type debe ser incoming u outgoing')
], handleValidationErrors, async (req, res) => {
  try {
    const { paymentId } = req.params;
    const { walletAddress, accessToken, type } = req.query;

    let payment;
    if (type === 'incoming') {
      payment = await openPaymentsService.getIncomingPayment(walletAddress, paymentId, accessToken);
    } else {
      payment = await openPaymentsService.getOutgoingPayment(walletAddress, paymentId, accessToken);
    }

    res.json({
      success: true,
      data: {
        id: payment.id,
        state: payment.state || (payment.completed ? 'COMPLETED' : 'PENDING'),
        createdAt: payment.createdAt,
        updatedAt: payment.updatedAt,
        amount: type === 'incoming' ? payment.receivedAmount : payment.sentAmount,
        walletAddress: walletAddress,
        paymentType: type
      }
    });
  } catch (error) {
    logger.error('Error obteniendo estado del pago:', error);
    res.status(500).json({
      error: 'Error interno del servidor',
      message: error.message
    });
  }
});

export default router;