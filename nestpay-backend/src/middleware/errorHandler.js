import { logger } from '../utils/logger.js';

export const errorHandler = (err, req, res, next) => {
  // Log del error
  logger.error('Error no manejado:', {
    message: err.message,
    stack: err.stack,
    url: req.originalUrl,
    method: req.method,
    ip: req.ip,
    userAgent: req.get('User-Agent')
  });

  // Errores de validación
  if (err.name === 'ValidationError') {
    return res.status(400).json({
      error: 'Error de validación',
      message: err.message,
      details: err.errors
    });
  }

  // Errores de sintaxis JSON
  if (err instanceof SyntaxError && err.status === 400 && 'body' in err) {
    return res.status(400).json({
      error: 'JSON malformado',
      message: 'El cuerpo de la solicitud contiene JSON inválido'
    });
  }

  // Errores de tipo (TypeError)
  if (err.name === 'TypeError') {
    return res.status(400).json({
      error: 'Error de tipo',
      message: 'Parámetros o datos inválidos'
    });
  }

  // Errores de referencia (ReferenceError)
  if (err.name === 'ReferenceError') {
    return res.status(500).json({
      error: 'Error interno del servidor',
      message: 'Referencia no definida'
    });
  }

  // Errores de conexión de red
  if (err.code === 'ECONNREFUSED' || err.code === 'ENOTFOUND' || err.code === 'ETIMEDOUT') {
    return res.status(503).json({
      error: 'Servicio no disponible',
      message: 'Error de conectividad con servicios externos'
    });
  }

  // Errores HTTP personalizados
  if (err.status || err.statusCode) {
    const statusCode = err.status || err.statusCode;
    return res.status(statusCode).json({
      error: err.name || 'Error HTTP',
      message: err.message || 'Error en la solicitud'
    });
  }

  // Error por defecto
  const statusCode = err.statusCode || 500;
  const message = process.env.NODE_ENV === 'production' 
    ? 'Error interno del servidor' 
    : err.message;

  res.status(statusCode).json({
    error: 'Error interno del servidor',
    message,
    ...(process.env.NODE_ENV !== 'production' && { stack: err.stack })
  });
};