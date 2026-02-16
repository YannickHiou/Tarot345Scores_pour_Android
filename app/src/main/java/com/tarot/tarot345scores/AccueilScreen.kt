/*
 * Tarot345Scores - Application de gestion des scores de Tarot
 * Copyright (C) 2026  Yannick Hiou <yannick.hiou@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.tarot.tarot345scores

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme

@Composable
fun AccueilScreen(
    joueurs: List<Joueur>,
    isLoading: Boolean,
    onAproposClick: () -> Unit,
    onJouerClick: () -> Unit,
    onJoueursClick: () -> Unit,
    onHistoriqueClick: () -> Unit,
    onStatistiquesClick: () -> Unit,
    onConstantesClick: () -> Unit,
    onReglesClick: () -> Unit,
    requiredNbJoueurs: Int
) {
    val requiredNbJoueurs_ = 3

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Titre + indicateur de chargement
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Tarot",
                color = Color.White,
                fontSize = 55.sp,
                textAlign = TextAlign.Center
            )
            if (isLoading) {
                Spacer(modifier = Modifier.width(12.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Calculatrice",
            color = Color.White,
            fontSize = 30.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = "de scores",
            color = Color.White,
            fontSize = 30.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(100.dp))

        // Boutons
        Button(
            onClick = onAproposClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black
            )
        )
        {
            Text("A propos")
        }
        Spacer(modifier = Modifier.height(8.dp))

        val canStart = !isLoading && joueurs.size >= requiredNbJoueurs_
        Button(
            onClick = onJouerClick,
            enabled = canStart,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (joueurs.size >= requiredNbJoueurs_) Color.Green else MaterialTheme.colorScheme.primary,
                contentColor = Color.Black
            )
        ) {
            Text("Nouvelle partie")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onJoueursClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (joueurs.size < requiredNbJoueurs_) Color.Green else MaterialTheme.colorScheme.primary,
                contentColor = Color.Black
            )

        ) { Text("Gestion des joueurs") }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onHistoriqueClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black
            )
        )
        {
            Text("Historique")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onStatistiquesClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black
            )

        ) { Text("Statistiques") }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onConstantesClick, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black
            )
        ) { Text("Constantes") }
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onReglesClick, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black
            )
        ) { Text("Règles") }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "v ${Version.VERSION}",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}