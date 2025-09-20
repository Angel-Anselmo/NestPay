import { createAuthenticatedClient } from '@interledger/open-payments';
import { logger } from '../utils/logger.js';
import { v4 as uuidv4 } from 'uuid';

class OpenPaymentsService {
  constructor() {
    this.adminClient = null;
    this.userClient = null;
    this.isAdminInitialized = false;
    this.isUserInitialized = false;
  }

  async initializeAdmin() {
    try {
      if (!process.env.ADMIN_WALLET_ADDRESS_URL || !process.env.ADMIN_PRIVATE_KEY_PATH || !process.env.ADMIN_KEY_ID) {
        throw new Error('Faltan variables de entorno requeridas para Open Payments ADMIN');
      }

      this.adminClient = await createAuthenticatedClient({
        walletAddressUrl: process.env.ADMIN_WALLET_ADDRESS_URL,
        privateKey: process.env.ADMIN_PRIVATE_KEY_PATH,
        keyId: process.env.ADMIN_KEY_ID,
      });

      this.isAdminInitialized = true;
      logger.info('✅ Cliente Open Payments ADMIN inicializado correctamente');
      return this.adminClient;
    } catch (error) {
      logger.error('❌ Error inicializando cliente Open Payments ADMIN:', error);
      throw error;
    }
  }

  async initializeUser() {
    try {
      if (!process.env.USER_WALLET_ADDRESS_URL || !process.env.USER_PRIVATE_KEY_PATH || !process.env.USER_KEY_ID) {
        throw new Error('Faltan variables de entorno requeridas para Open Payments USER');
      }

      this.userClient = await createAuthenticatedClient({
        walletAddressUrl: process.env.USER_WALLET_ADDRESS_URL,
        privateKey: process.env.USER_PRIVATE_KEY_PATH,
        keyId: process.env.USER_KEY_ID,
      });

      this.isUserInitialized = true;
      logger.info('✅ Cliente Open Payments USER inicializado correctamente');
      return this.userClient;
    } catch (error) {
      logger.error('❌ Error inicializando cliente Open Payments USER:', error);
      throw error;
    }
  }

  async ensureAdminInitialized() {
    if (!this.isAdminInitialized) {
      await this.initializeAdmin();
    }
    return this.adminClient;
  }

  async ensureUserInitialized() {
    if (!this.isUserInitialized) {
      await this.initializeUser();
    }
    return this.userClient;
  }

  // =================== WALLET ADDRESSES ===================

  async getWalletAddress(walletAddressUrl) {
    try {
      // Usar admin client para operaciones generales de wallet
      const client = await this.ensureAdminInitialized();
      const walletAddress = await client.walletAddress.get({
        url: walletAddressUrl
      });

      logger.info(`✅ Wallet address obtenida: ${walletAddressUrl}`);
      return walletAddress;
    } catch (error) {
      logger.error(`❌ Error obteniendo wallet address ${walletAddressUrl}:`, error);
      throw error;
    }
  }

  async validateWalletAddress(walletAddressUrl) {
    try {
      const walletAddress = await this.getWalletAddress(walletAddressUrl);
      return {
        isValid: true,
        walletAddress,
        authServer: walletAddress.authServer,
        resourceServer: walletAddress.resourceServer || walletAddressUrl
      };
    } catch (error) {
      return {
        isValid: false,
        error: error.message
      };
    }
  }

  // =================== ADMIN OPERATIONS (Crear comunidades/conceptos) ===================

  async createCommunityIncomingPayment(amount, description = 'Aporte a comunidad NestPay') {
    try {
      const client = await this.ensureAdminInitialized();
      const adminWalletUrl = process.env.ADMIN_WALLET_ADDRESS_URL;
      logger.info(`🔍 Obteniendo wallet address para: ${adminWalletUrl}`);
      
      const walletAddress = await this.getWalletAddress(adminWalletUrl);
      logger.info(`🔍 Wallet obtenida:`, {
        id: walletAddress.id,
        assetCode: walletAddress.assetCode,
        assetScale: walletAddress.assetScale,
        authServer: walletAddress.authServer
      });
      
      if (!walletAddress.authServer) {
        throw new Error('Admin wallet no tiene servidor de autorización configurado');
      }

      logger.info(`🔍 Solicitando grant a: ${walletAddress.authServer}`);
      
      // Solicitar grant para el admin
      const grant = await client.grant.request(
        { url: walletAddress.authServer },
        {
          access_token: {
            access: [
              {
                type: 'incoming-payment',
                actions: ['list', 'read', 'read-all', 'complete', 'create'],
                identifier: adminWalletUrl
              }
            ]
          }
        }
      );

      logger.info(`🔍 Grant obtenido:`, {
        hasAccessToken: !!grant.access_token?.value,
        tokenLength: grant.access_token?.value?.length
      });

      if (!grant.access_token?.value) {
        throw new Error('No se pudo obtener el token de acceso para admin incoming payment');
      }

      logger.info(`🔍 Creando incoming payment con:`, {
        url: adminWalletUrl,
        amount: {
          value: amount.value.toString(),
          assetCode: amount.assetCode || walletAddress.assetCode,
          assetScale: amount.assetScale || walletAddress.assetScale
        }
      });

      logger.info(`🔍 Parámetros completos para incomingPayment.create:`, {
        requestUrl: {
          url: adminWalletUrl,
          accessToken: '***TOKEN***'
        },
        requestBody: {
          walletAddress: adminWalletUrl,
          incomingAmount: {
            value: amount.value.toString(),
            assetCode: amount.assetCode || walletAddress.assetCode,
            assetScale: amount.assetScale || walletAddress.assetScale
          },
          metadata: {
            description,
            createdBy: 'NestPay-Admin',
            type: 'community-contribution',
            timestamp: new Date().toISOString()
          }
        }
      });

      logger.info(`🔍 Intentando crear incoming payment...`);
      logger.info(`🔍 URL exacta que usará el SDK: ${adminWalletUrl}/incoming-payments`);
      logger.info(`🔍 Método: POST`);
      
      try {
        // Crear incoming payment en la wallet del admin
        const incomingPayment = await client.incomingPayment.create(
          {
            url: adminWalletUrl,
            accessToken: grant.access_token.value
          },
          {
            walletAddress: adminWalletUrl,
            incomingAmount: {
              value: amount.value.toString(),
              assetCode: amount.assetCode || walletAddress.assetCode,
              assetScale: amount.assetScale || walletAddress.assetScale
            },
            metadata: {
              description,
              createdBy: 'NestPay-Admin',
              type: 'community-contribution',
              timestamp: new Date().toISOString()
            }
          }
        );
        
        logger.info(`✅ Incoming payment creado exitosamente: ${incomingPayment.id}`);
        return {
          incomingPayment,
          grant,
          walletAddress,
          adminWallet: adminWalletUrl
        };
        
      } catch (createError) {
        logger.error(`❌ Error específico al crear incoming payment:`, {
          error: createError,
          message: createError.message,
          status: createError.status,
          description: createError.description,
          stack: createError.stack,
          requestUrl: adminWalletUrl,
          hasToken: !!grant.access_token?.value
        });
        throw createError;
      }

    } catch (error) {
      logger.error('❌ Error creando incoming payment para comunidad:', error);
      throw error;
    }
  }

  // =================== USER OPERATIONS (Pagar aportes) ===================

  async createUserPaymentQuote(receiverPaymentUrl, amount) {
    try {
      const client = await this.ensureUserInitialized();
      const userWalletUrl = process.env.USER_WALLET_ADDRESS_URL;
      const walletAddress = await this.getWalletAddress(userWalletUrl);
      
      if (!walletAddress.authServer) {
        throw new Error('User wallet no tiene servidor de autorización configurado');
      }

      // Solicitar grant de quote para el usuario
      const grant = await client.grant.request(
        { url: walletAddress.authServer },
        {
          access_token: {
            access: [
              {
                type: 'quote',
                actions: ['create', 'read', 'read-all'],
                identifier: userWalletUrl
              }
            ]
          }
        }
      );

      if (!grant.access_token?.value) {
        throw new Error('No se pudo obtener el token de acceso para user quote');
      }

      // Crear quote desde la wallet del usuario
      const quote = await client.quote.create(
        {
          url: userWalletUrl,
          accessToken: grant.access_token.value
        },
        {
          walletAddress: userWalletUrl,
          receiver: receiverPaymentUrl,
          method: 'ilp',
          sendAmount: {
            value: amount.value.toString(),
            assetCode: amount.assetCode || walletAddress.assetCode,
            assetScale: amount.assetScale || walletAddress.assetScale
          }
        }
      );

      logger.info(`✅ Quote de usuario creado: ${quote.id}`);
      return {
        quote,
        grant,
        walletAddress,
        userWallet: userWalletUrl
      };
    } catch (error) {
      logger.error('❌ Error creando quote de usuario:', error);
      throw error;
    }
  }

  async requestUserOutgoingPaymentGrant(receiverPaymentUrl, amount) {
    try {
      const client = await this.ensureUserInitialized();
      const userWalletUrl = process.env.USER_WALLET_ADDRESS_URL;
      const walletAddress = await this.getWalletAddress(userWalletUrl);
      
      const grant = await client.grant.request(
        { url: walletAddress.authServer },
        {
          access_token: {
            access: [
              {
                type: 'outgoing-payment',
                actions: ['create', 'read', 'read-all', 'list', 'list-all'],
                identifier: userWalletUrl,
                limits: {
                  receiver: receiverPaymentUrl,
                  debitAmount: {
                    value: amount.value.toString(),
                    assetCode: amount.assetCode,
                    assetScale: amount.assetScale
                  }
                }
              }
            ]
          },
          interact: {
            start: ['redirect']
          }
        }
      );

      logger.info(`✅ Grant interactivo de outgoing payment para usuario solicitado`);
      return grant;
    } catch (error) {
      logger.error('❌ Error solicitando grant interactivo de outgoing payment para usuario:', error);
      throw error;
    }
  }

  async createUserOutgoingPayment(quoteId, accessToken, metadata = {}) {
    try {
      const client = await this.ensureUserInitialized();
      const userWalletUrl = process.env.USER_WALLET_ADDRESS_URL;
      
      const outgoingPayment = await client.outgoingPayment.create(
        {
          url: userWalletUrl,
          accessToken
        },
        {
          walletAddress: userWalletUrl,
          quoteId,
          metadata: {
            ...metadata,
            createdBy: 'NestPay-User',
            type: 'community-contribution-payment',
            timestamp: new Date().toISOString()
          }
        }
      );

      logger.info(`✅ Outgoing payment de usuario creado: ${outgoingPayment.id}`);
      return outgoingPayment;
    } catch (error) {
      logger.error('❌ Error creando outgoing payment de usuario:', error);
      throw error;
    }
  }

  // =================== FLUJO COMPLETO NESTPAY ===================

  async createCommunityContributionFlow(amount, description = 'Aporte a comunidad NestPay') {
    try {
      logger.info(`🏢 Iniciando flujo de aporte a comunidad - Monto: ${amount.value}`);
      
      // 1. ADMIN: Crear incoming payment en la wallet del admin para recibir aportes
      const adminIncomingPayment = await this.createCommunityIncomingPayment(amount, description);

      // 2. USER: Crear quote desde la wallet del usuario hacia el incoming payment del admin
      const userQuote = await this.createUserPaymentQuote(
        adminIncomingPayment.incomingPayment.id,
        amount
      );

      // 3. USER: Solicitar grant interactivo para que el usuario autorice el pago
      const userOutgoingGrant = await this.requestUserOutgoingPaymentGrant(
        adminIncomingPayment.incomingPayment.id,
        {
          value: userQuote.quote.sendAmount.value,
          assetCode: userQuote.quote.sendAmount.assetCode,
          assetScale: userQuote.quote.sendAmount.assetScale
        }
      );

      const result = {
        // Datos del admin (receptor)
        adminWallet: process.env.ADMIN_WALLET_ADDRESS_URL,
        incomingPayment: adminIncomingPayment.incomingPayment,
        
        // Datos del usuario (pagador)
        userWallet: process.env.USER_WALLET_ADDRESS_URL,
        quote: userQuote.quote,
        
        // Flujo de autorización
        authorizationUrl: userOutgoingGrant.interact?.redirect,
        
        // Datos para continuar el flujo
        continueData: {
          continueUri: userOutgoingGrant.continue?.uri,
          continueAccessToken: userOutgoingGrant.continue?.access_token?.value,
          finishInteractionUrl: `${process.env.BASE_URL}/api/payments/grant-callback`,
          state: uuidv4()
        },
        
        // Estimación de costos
        estimatedFees: {
          sendAmount: userQuote.quote.sendAmount,
          receiveAmount: userQuote.quote.receiveAmount
        }
      };

      logger.info(`✅ Flujo de aporte a comunidad preparado. URL de autorización: ${result.authorizationUrl}`);
      return result;

    } catch (error) {
      logger.error('❌ Error en flujo de aporte a comunidad:', error);
      throw error;
    }
  }

  async finalizeCommunityPayment(continueUri, continueAccessToken, interactRef, quoteId) {
    try {
      logger.info('🔄 Finalizando pago de aporte a comunidad...');

      const userClient = await this.ensureUserInitialized();

      // 1. Continuar el grant con la referencia de interacción
      const finalGrant = await userClient.grant.continue({
        accessToken: continueAccessToken,
        url: continueUri
      }, {
        interact_ref: interactRef
      });

      if (!finalGrant.access_token?.value) {
        throw new Error('No se pudo obtener el token de acceso final para el usuario');
      }

      // 2. Crear el outgoing payment desde el usuario hacia el admin
      const outgoingPayment = await this.createUserOutgoingPayment(
        quoteId,
        finalGrant.access_token.value,
        { 
          communityContribution: true,
          paymentType: 'community-support'
        }
      );

      logger.info(`✅ Pago de aporte a comunidad finalizado exitosamente: ${outgoingPayment.id}`);
      return {
        outgoingPayment,
        grant: finalGrant,
        userWallet: process.env.USER_WALLET_ADDRESS_URL,
        adminWallet: process.env.ADMIN_WALLET_ADDRESS_URL
      };

    } catch (error) {
      logger.error('❌ Error finalizando pago de aporte a comunidad:', error);
      throw error;
    }
  }

  // =================== UTILIDADES HEREDADAS (compatibilidad) ===================

  async getIncomingPayment(walletAddressUrl, paymentId, accessToken) {
    try {
      // Determinar qué cliente usar basado en la wallet
      const isAdminWallet = walletAddressUrl === process.env.ADMIN_WALLET_ADDRESS_URL;
      const client = isAdminWallet ? await this.ensureAdminInitialized() : await this.ensureUserInitialized();
      
      const incomingPayment = await client.incomingPayment.get({
        url: `${walletAddressUrl}/incoming-payments/${paymentId}`,
        accessToken
      });

      return incomingPayment;
    } catch (error) {
      logger.error(`❌ Error obteniendo incoming payment ${paymentId}:`, error);
      throw error;
    }
  }

  async getOutgoingPayment(walletAddressUrl, paymentId, accessToken) {
    try {
      // Determinar qué cliente usar basado en la wallet
      const isUserWallet = walletAddressUrl === process.env.USER_WALLET_ADDRESS_URL;
      const client = isUserWallet ? await this.ensureUserInitialized() : await this.ensureAdminInitialized();
      
      const outgoingPayment = await client.outgoingPayment.get({
        url: `${walletAddressUrl}/outgoing-payments/${paymentId}`,
        accessToken
      });

      return outgoingPayment;
    } catch (error) {
      logger.error(`❌ Error obteniendo outgoing payment ${paymentId}:`, error);
      throw error;
    }
  }

  // Métodos de información del sistema
  getSystemInfo() {
    return {
      adminWallet: {
        address: process.env.ADMIN_WALLET_ADDRESS_URL,
        keyId: process.env.ADMIN_KEY_ID,
        initialized: this.isAdminInitialized,
        role: 'Crear comunidades y recibir aportes'
      },
      userWallet: {
        address: process.env.USER_WALLET_ADDRESS_URL,
        keyId: process.env.USER_KEY_ID,
        initialized: this.isUserInitialized,
        role: 'Pagar aportes a comunidades'
      }
    };
  }
}

// Instancia singleton
const openPaymentsService = new OpenPaymentsService();

export default openPaymentsService;