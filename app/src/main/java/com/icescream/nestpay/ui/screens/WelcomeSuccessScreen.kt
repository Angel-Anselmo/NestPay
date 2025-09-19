package com.icescream.nestpay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeSuccessScreen(
    onNavigateToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF4DB6AC)), // Color teal/verde como en la imagen
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Título principal "¡Bienvenido a" - Tamaño aumentado
        Text(
            text = "¡Bienvenido a",
            fontSize = 38.sp, // Aumentado de 32sp a 38sp
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Título "NestPay!" con color amarillo yema de huevo - Tamaño aumentado
        Text(
            text = "NestPay!",
            fontSize = 42.sp, // Aumentado de 32sp a 42sp
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD700), // Amarillo yema de huevo cocido
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(56.dp))

        // Descripción - Tamaño aumentado
        Text(
            text = "Tu espacio seguro y sencillo para crear Comunidades, compartir pagos y alcanzar metas en equipo.",
            fontSize = 18.sp, // Aumentado de 16sp a 18sp
            fontWeight = FontWeight.Normal,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp, // Aumentado el interlineado
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Pregunta - Tamaño aumentado
        Text(
            text = "¿Estás listo para comenzar?",
            fontSize = 20.sp, // Aumentado de 18sp a 20sp
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp)) // Reducido el espacio antes del botón

        // Botón "¡Comencemos!" con color amarillo yema de huevo - Más grande y más arriba
        Button(
            onClick = onNavigateToHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp) // Aumentado de 56dp a 64dp
                .padding(horizontal = 28.dp), // Reducido el padding horizontal para que sea más ancho
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700) // Amarillo yema de huevo cocido
            ),
            shape = RoundedCornerShape(32.dp) // Aumentado el radio de las esquinas
        ) {
            Text(
                text = "¡Comencemos!",
                fontSize = 20.sp, // Aumentado de 18sp a 20sp
                fontWeight = FontWeight.Bold,
                color = Color.Black // Texto negro sobre fondo amarillo
            )
        }

        Spacer(modifier = Modifier.weight(1.2f)) // Más espacio en la parte inferior
    }
}