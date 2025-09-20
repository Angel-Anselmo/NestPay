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

/**
 * GET /api/wallets/validate/:walletAddress
 * Validar una wallet address
 */
router.get('/validate/*', async (req, res) => {
  try {
    // Reconstruir la URL completa desde los parámetros
    const walletAddressUrl = req.originalUrl.replace('/api/wallets/validate/', '');
    
    if (!walletAddressUrl.startsWith('http')) {
      return res.status(400).json({
        error: 'Wallet address debe ser una URL válida que comience con http o https',
        received: walletAddressUrl
      });
    }

    const result = await openPaymentsService.validateWalletAddress(walletAddressUrl);

    if (result.isValid) {
      res.json({
        success: true,
        message: 'Wallet address válida',
        data: {
          walletAddress: result.walletAddress,
          authServer: result.authServer,
          resourceServer: result.resourceServer,
          assetInfo: {
            code: result.walletAddress.assetCode,
            scale: result.walletAddress.assetScale
          }
        }
      });
    } else {
      res.status(400).json({
        success: false,
        error: 'Wallet address inválida',
        message: result.error
      });
    }
  } catch (error) {
    logger.error('Error validando wallet address:', error);
    res.status(500).json({
      error: 'Error interno del servidor',
      message: error.message
    });
  }
});

/**
 * GET /api/wallets/info/*
 * Obtener información de una wallet address
 */
router.get('/info/*', async (req, res) => {
  try {
    // Reconstruir la URL completa desde los parámetros
    const walletAddressUrl = req.originalUrl.replace('/api/wallets/info/', '');
    
    if (!walletAddressUrl.startsWith('http')) {
      return res.status(400).json({
        error: 'Wallet address debe ser una URL válida que comience con http o https',
        received: walletAddressUrl
      });
    }

    const walletAddress = await openPaymentsService.getWalletAddress(walletAddressUrl);

    res.json({
      success: true,
      data: {
        id: walletAddress.id,
        url: walletAddress.id, // En Open Payments, el id es la URL
        publicName: walletAddress.publicName,
        assetCode: walletAddress.assetCode,
        assetScale: walletAddress.assetScale,
        authServer: walletAddress.authServer,
        resourceServer: walletAddress.resourceServer,
        // Información adicional si está disponible
        receiverEndpoint: `${walletAddress.id}/incoming-payments`,
        quotesEndpoint: `${walletAddress.id}/quotes`,
        outgoingPaymentsEndpoint: `${walletAddress.id}/outgoing-payments`
      }
    });
  } catch (error) {
    logger.error('Error obteniendo información de wallet:', error);
    res.status(500).json({
      error: 'Error interno del servidor',
      message: error.message
    });
  }
});

/**
 * POST /api/wallets/check-compatibility
 * Verificar compatibilidad entre dos wallets para realizar un pago
 */
router.post('/check-compatibility', [
  body('senderWallet').isURL().withMessage('Sender wallet debe ser una URL válida'),
  body('receiverWallet').isURL().withMessage('Receiver wallet debe ser una URL válida')
], handleValidationErrors, async (req, res) => {
  try {
    const { senderWallet, receiverWallet } = req.body;

    // Validar ambas wallets
    const [senderResult, receiverResult] = await Promise.all([
      openPaymentsService.validateWalletAddress(senderWallet),
      openPaymentsService.validateWalletAddress(receiverWallet)
    ]);

    if (!senderResult.isValid) {
      return res.status(400).json({
        success: false,
        error: 'Sender wallet inválida',
        details: senderResult.error
      });
    }

    if (!receiverResult.isValid) {
      return res.status(400).json({
        success: false,
        error: 'Receiver wallet inválida',
        details: receiverResult.error
      });
    }

    // Verificar compatibilidad de assets
    const senderAsset = senderResult.walletAddress;
    const receiverAsset = receiverResult.walletAddress;

    const isCompatible = senderAsset.assetCode === receiverAsset.assetCode ||
                        (senderAsset.assetCode && receiverAsset.assetCode); // Si ambos tienen asset codes

    res.json({
      success: true,
      data: {
        compatible: isCompatible,
        senderWallet: {
          url: senderAsset.id,
          publicName: senderAsset.publicName,
          assetCode: senderAsset.assetCode,
          assetScale: senderAsset.assetScale,
          authServer: senderAsset.authServer
        },
        receiverWallet: {
          url: receiverAsset.id,
          publicName: receiverAsset.publicName,
          assetCode: receiverAsset.assetCode,
          assetScale: receiverAsset.assetScale,
          authServer: receiverAsset.authServer
        },
        compatibilityNotes: isCompatible 
          ? 'Las wallets son compatibles para realizar pagos'
          : 'Las wallets pueden requerir conversión de moneda. Verifique las tasas de cambio.'
      }
    });
  } catch (error) {
    logger.error('Error verificando compatibilidad de wallets:', error);
    res.status(500).json({
      error: 'Error interno del servidor',
      message: error.message
    });
  }
});

/**
 * GET /api/wallets/test-wallets
 * Obtener una lista de wallets de prueba disponibles
 */
router.get('/test-wallets', (req, res) => {
  const testWallets = [
    {
      name: 'Alice Test Wallet',
      url: 'https://ilp.interledger-test.dev/alice',
      description: 'Wallet de prueba Alice',
      assetCode: 'USD',
      assetScale: 2
    },
    {
      name: 'Bob Test Wallet',
      url: 'https://ilp.interledger-test.dev/bob',
      description: 'Wallet de prueba Bob',
      assetCode: 'USD',
      assetScale: 2
    },
    {
      name: 'Carol Test Wallet',
      url: 'https://ilp.interledger-test.dev/carol',
      description: 'Wallet de prueba Carol',
      assetCode: 'USD',
      assetScale: 2
    }
  ];

  res.json({
    success: true,
    message: 'Wallets de prueba disponibles en Interledger Test Network',
    data: testWallets,
    instructions: {
      createWallet: 'Visita https://wallet.interledger-test.dev para crear tu propia wallet de prueba',
      getPrivateKey: 'En tu wallet de prueba, ve a Settings > Developer Keys para obtener las claves necesarias',
      documentation: 'https://openpayments.dev/es/overview/getting-started/'
    }
  });
});

/**
 * POST /api/wallets/estimate-fees
 * Estimar tarifas para un pago entre dos wallets
 */
router.post('/estimate-fees', [
  body('senderWallet').isURL().withMessage('Sender wallet debe ser una URL válida'),
  body('receiverWallet').isURL().withMessage('Receiver wallet debe ser una URL válida'),
  body('amount.value').isNumeric().withMessage('El valor del monto debe ser numérico'),
  body('amount.assetCode').optional().isString().withMessage('Asset code debe ser string'),
  body('amount.assetScale').optional().isInt({ min: 0 }).withMessage('Asset scale debe ser entero positivo')
], handleValidationErrors, async (req, res) => {
  try {
    const { senderWallet, receiverWallet, amount } = req.body;

    // Crear un incoming payment temporal para obtener la URL
    const incomingPaymentResult = await openPaymentsService.createIncomingPayment(
      receiverWallet,
      amount,
      'Estimación de tarifas - NestPay'
    );

    // Crear un quote para estimar los costos
    const quoteResult = await openPaymentsService.createQuote(
      senderWallet,
      incomingPaymentResult.incomingPayment.id,
      { ...amount, type: 'send' }
    );

    const quote = quoteResult.quote;
    const sendAmount = quote.sendAmount;
    const receiveAmount = quote.receiveAmount;

    // Calcular las tarifas
    const sendValue = parseFloat(sendAmount.value);
    const receiveValue = parseFloat(receiveAmount.value);
    const fees = sendValue - receiveValue;
    const feePercentage = ((fees / sendValue) * 100).toFixed(2);

    res.json({
      success: true,
      data: {
        quote: {
          id: quote.id,
          expiresAt: quote.expiresAt
        },
        estimation: {
          sendAmount: {
            value: sendAmount.value,
            assetCode: sendAmount.assetCode,
            assetScale: sendAmount.assetScale,
            displayValue: (sendValue / Math.pow(10, sendAmount.assetScale)).toFixed(sendAmount.assetScale)
          },
          receiveAmount: {
            value: receiveAmount.value,
            assetCode: receiveAmount.assetCode,
            assetScale: receiveAmount.assetScale,
            displayValue: (receiveValue / Math.pow(10, receiveAmount.assetScale)).toFixed(receiveAmount.assetScale)
          },
          fees: {
            value: fees.toString(),
            assetCode: sendAmount.assetCode,
            assetScale: sendAmount.assetScale,
            displayValue: (fees / Math.pow(10, sendAmount.assetScale)).toFixed(sendAmount.assetScale),
            percentage: feePercentage
          }
        },
        // Datos necesarios si el usuario quiere proceder con el pago
        paymentData: {
          incomingPaymentId: incomingPaymentResult.incomingPayment.id,
          quoteId: quote.id
        }
      }
    });
  } catch (error) {
    logger.error('Error estimando tarifas:', error);
    res.status(500).json({
      error: 'Error interno del servidor',
      message: error.message,
      suggestion: 'Verifica que las wallet addresses sean válidas y accesibles'
    });
  }
});

export default router;