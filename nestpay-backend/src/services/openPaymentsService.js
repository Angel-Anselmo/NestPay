const { createAuthenticatedClient } = require('@interledger/open-payments');
const { v4: uuidv4 } = require('uuid');

class OpenPaymentsService {
  constructor() {
    this.baseUrl = process.env.OPEN_PAYMENTS_BASE_URL || 'https://wallet.interledger-test.dev';
    this.defaultWalletAddress = process.env.DEFAULT_WALLET_ADDRESS;
    this.clients = new Map(); // Cache for authenticated clients
  }

  /**
   * Create an authenticated client for a wallet address
   */
  async createClient(walletAddress, privateKey) {
    try {
      // Si no se proporciona una clave privada específica, intentar determinar cuál usar
      if (!privateKey) {
        privateKey = this.getPrivateKeyForWallet(walletAddress);
      }

      const client = await createAuthenticatedClient({
        walletAddressUrl: walletAddress,
        privateKey: privateKey,
      });

      this.clients.set(walletAddress, client);
      return client;
    } catch (error) {
      console.error('Error creating Open Payments client:', error);
      throw new Error(`Failed to create client for ${walletAddress}: ${error.message}`);
    }
  }

  /**
   * Determine which private key to use for a given wallet address
   */
  getPrivateKeyForWallet(walletAddress) {
    // Lógica para determinar qué clave privada usar
    // Esto podría basarse en la URL de la wallet o en una configuración
    
    if (walletAddress.includes('admin') || walletAddress === process.env.DEFAULT_WALLET_ADDRESS) {
      return process.env.ADMIN_WALLET_PRIVATE_KEY || process.env.PRIVATE_KEY;
    } else if (walletAddress.includes('user')) {
      return process.env.USER_WALLET_PRIVATE_KEY || process.env.PRIVATE_KEY;
    }
    
    // Fallback a la clave privada por defecto
    return process.env.PRIVATE_KEY;
  }

  /**
   * Get or create a client for a wallet address
   */
  async getClient(walletAddress, privateKey) {
    if (this.clients.has(walletAddress)) {
      return this.clients.get(walletAddress);
    }
    return await this.createClient(walletAddress, privateKey);
  }

  /**
   * Get wallet address information
   */
  async getWalletAddress(walletAddressUrl) {
    try {
      const client = await this.getClient(walletAddressUrl);
      const walletAddress = await client.walletAddress.get({
        url: walletAddressUrl
      });

      return {
        id: walletAddress.id,
        url: walletAddress.url,
        assetCode: walletAddress.assetCode,
        assetScale: walletAddress.assetScale,
        authServer: walletAddress.authServer,
        resourceServer: walletAddress.resourceServer
      };
    } catch (error) {
      console.error('Error getting wallet address:', error);
      throw new Error(`Failed to get wallet address: ${error.message}`);
    }
  }

  /**
   * Create an incoming payment (for receiving money)
   */
  async createIncomingPayment(receiverWalletAddress, amount, description = '') {
    try {
      const client = await this.getClient(receiverWalletAddress);
      const grantToken = await this.getGrantToken(receiverWalletAddress, 'incoming-payment');
      
      const requestOptions = {
        walletAddressUrl: receiverWalletAddress
      };

      // Solo agregar accessToken si tenemos un grant token válido
      if (grantToken) {
        requestOptions.accessToken = grantToken;
      }
      
      const incomingPayment = await client.incomingPayment.create(
        requestOptions,
        {
          incomingAmount: {
            value: amount.toString(),
            assetCode: 'USD',
            assetScale: 2
          },
          metadata: {
            description: description,
            source: 'NestPay',
            createdAt: new Date().toISOString()
          }
        }
      );

      return {
        id: incomingPayment.id,
        walletAddress: incomingPayment.walletAddress,
        completed: incomingPayment.completed,
        incomingAmount: incomingPayment.incomingAmount,
        receivedAmount: incomingPayment.receivedAmount,
        metadata: incomingPayment.metadata,
        createdAt: incomingPayment.createdAt,
        updatedAt: incomingPayment.updatedAt
      };
    } catch (error) {
      console.error('Error creating incoming payment:', error);
      throw new Error(`Failed to create incoming payment: ${error.message}`);
    }
  }

  /**
   * Create an outgoing payment (for sending money)
   */
  async createOutgoingPayment(senderWalletAddress, receiverWalletAddress, amount, description = '') {
    try {
      const client = await this.getClient(senderWalletAddress);

      // First, create an incoming payment on the receiver's wallet
      const incomingPayment = await this.createIncomingPayment(
        receiverWalletAddress,
        amount,
        description
      );

      const grantToken = await this.getGrantToken(senderWalletAddress, 'outgoing-payment');
      
      const requestOptions = {
        walletAddressUrl: senderWalletAddress
      };

      // Solo agregar accessToken si tenemos un grant token válido
      if (grantToken) {
        requestOptions.accessToken = grantToken;
      }

      // Then create the outgoing payment
      const outgoingPayment = await client.outgoingPayment.create(
        requestOptions,
        {
          walletAddress: senderWalletAddress,
          quoteId: await this.createQuote(senderWalletAddress, incomingPayment.id, amount),
          metadata: {
            description: description,
            source: 'NestPay',
            incomingPaymentId: incomingPayment.id,
            createdAt: new Date().toISOString()
          }
        }
      );

      return {
        id: outgoingPayment.id,
        walletAddress: outgoingPayment.walletAddress,
        receiver: incomingPayment.id,
        debitAmount: outgoingPayment.debitAmount,
        receiveAmount: outgoingPayment.receiveAmount,
        metadata: outgoingPayment.metadata,
        state: outgoingPayment.state,
        createdAt: outgoingPayment.createdAt,
        incomingPayment: incomingPayment
      };
    } catch (error) {
      console.error('Error creating outgoing payment:', error);
      throw new Error(`Failed to create outgoing payment: ${error.message}`);
    }
  }

  /**
   * Create a quote for payment estimation
   */
  async createQuote(walletAddress, receiver, amount) {
    try {
      const client = await this.getClient(walletAddress);
      const grantToken = await this.getGrantToken(walletAddress, 'quote');
      
      const requestOptions = {
        walletAddressUrl: walletAddress
      };

      // Solo agregar accessToken si tenemos un grant token válido
      if (grantToken) {
        requestOptions.accessToken = grantToken;
      }
      
      const quote = await client.quote.create(
        requestOptions,
        {
          walletAddress: walletAddress,
          receiver: receiver,
          method: 'ilp',
          debitAmount: {
            value: amount.toString(),
            assetCode: 'USD',
            assetScale: 2
          }
        }
      );

      return quote.id;
    } catch (error) {
      console.error('Error creating quote:', error);
      throw new Error(`Failed to create quote: ${error.message}`);
    }
  }

  /**
   * Get payment status
   */
  async getPaymentStatus(walletAddress, paymentId, type = 'outgoing') {
    try {
      const client = await this.getClient(walletAddress);
      
      let payment;
      if (type === 'outgoing') {
        payment = await client.outgoingPayment.get({
          url: paymentId
        });
      } else {
        payment = await client.incomingPayment.get({
          url: paymentId
        });
      }

      return {
        id: payment.id,
        state: payment.state || (payment.completed ? 'COMPLETED' : 'PENDING'),
        amount: payment.receiveAmount || payment.receivedAmount,
        createdAt: payment.createdAt,
        updatedAt: payment.updatedAt,
        metadata: payment.metadata
      };
    } catch (error) {
      console.error('Error getting payment status:', error);
      throw new Error(`Failed to get payment status: ${error.message}`);
    }
  }

  /**
   * Get grant token for specific operations
   */
  async getGrantToken(walletAddress, access) {
    try {
      // Intentar obtener el token de las variables de entorno
      const tokenKey = `GRANT_TOKEN_${access.toUpperCase().replace('-', '_')}`;
      const token = process.env[tokenKey];
      
      if (token && token.trim() !== '') {
        return token;
      }

      // Si no hay token configurado, intentar crear un grant dinámicamente
      // En el entorno de test de Interledger, algunos endpoints pueden funcionar sin grants explícitos
      console.warn(`No grant token found for ${access}. Attempting to use client authentication.`);
      
      // Para el entorno de test, podemos intentar usar el cliente autenticado directamente
      // sin un grant token específico
      return null;
      
    } catch (error) {
      console.error('Error getting grant token:', error);
      console.warn(`Falling back to no grant token for ${access}`);
      return null;
    }
  }

  /**
   * Validate wallet address format
   */
  isValidWalletAddress(walletAddress) {
    try {
      const url = new URL(walletAddress);
      return url.protocol === 'https:' && url.hostname.includes('interledger');
    } catch {
      return false;
    }
  }

  /**
   * Create a payment request for NestPay community payments
   */
  async createCommunityPayment(fromWallet, toWallet, amount, communityId, conceptId, description) {
    try {
      const paymentId = uuidv4();
      
      console.log(`Creating community payment ${paymentId}:`, {
        from: fromWallet,
        to: toWallet,
        amount,
        communityId,
        conceptId
      });

      // Create the payment
      const payment = await this.createOutgoingPayment(
        fromWallet,
        toWallet,
        amount,
        `NestPay - ${description} (Community: ${communityId}, Concept: ${conceptId})`
      );

      return {
        paymentId,
        openPaymentId: payment.id,
        fromWallet,
        toWallet,
        amount,
        communityId,
        conceptId,
        description,
        state: payment.state,
        createdAt: new Date().toISOString(),
        incomingPayment: payment.incomingPayment
      };
    } catch (error) {
      console.error('Error creating community payment:', error);
      throw new Error(`Failed to create community payment: ${error.message}`);
    }
  }
}

module.exports = new OpenPaymentsService();