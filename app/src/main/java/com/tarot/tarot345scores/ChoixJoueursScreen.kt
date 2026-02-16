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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChoixJoueursScreen(
    joueurs: List<Joueur>,
    defaultNbJoueurs: Int,
    onNavigateToGame: (List<Joueur>, Int) -> Unit,
    onRetour: () -> Unit
) {
    var selected by remember { mutableStateOf(listOf<Joueur>()) }
    var sortAsc by remember { mutableStateOf(true) } // toggle de tri

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = "Sélectionner les joueurs",
            color = Color.White,
            modifier = Modifier
                .padding(bottom = 12.dp)
                .fillMaxWidth(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Trier + espace (toggle)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(
                onClick = { sortAsc = !sortAsc },
            ) {
                Text(text = if (sortAsc) "Trier A→Z" else "Trier Z→A", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.weight(1f)) {
            val joueursTries = remember(joueurs, sortAsc) {
                if (sortAsc) joueurs.sortedBy { it.nomUI } else joueurs.sortedByDescending { it.nomUI }
            }
            choixJoueurs(
                joueurs = joueursTries,
                preselected = selected,
                onSelectedChanged = { newSelection -> selected = newSelection }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onRetour,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(BOUTON_COULEUR),
                    contentColor = Color.Black
                )
            ) {
                Text(text = "Retour")
            }

            val isSelected = selected.size >= 3 && selected.size <= 5
            Button(
                onClick = {
                    if (isSelected) {
                        onNavigateToGame(selected, selected.size)
                    }
                },
                enabled = isSelected,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) Color(JOUEUR_SELECTIONNE) else Color(BOUTON_COULEUR),
                    contentColor = Color.White
                ),
            ) {
                Text(text = "Valider")
            }
        }
    }
}

@Composable
fun choixJoueurs(
    joueurs: List<Joueur>,
    preselected: List<Joueur> = emptyList(),
    onSelectedChanged: (List<Joueur>) -> Unit
) {
    var selectedJoueurs by remember { mutableStateOf<List<Joueur>>(preselected) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(joueurs) { joueur ->
            val isSelected = selectedJoueurs.any { it.id == joueur.id }

            Button(
                onClick = {
                    selectedJoueurs = if (isSelected) {
                        selectedJoueurs.filter { it.id != joueur.id }.toList()
                    } else {
                        if (selectedJoueurs.size < 5) selectedJoueurs + joueur else selectedJoueurs
                    }
                    onSelectedChanged(selectedJoueurs)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                colors = if (isSelected) {
                    ButtonDefaults.buttonColors(
                        containerColor = Color(JOUEUR_SELECTIONNE),
                        contentColor = Color.White
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = Color(BOUTON_COULEUR),
                        contentColor = Color.Black
                    )
                }
            ) {
                Text(
                    text = joueur.nomUI,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}