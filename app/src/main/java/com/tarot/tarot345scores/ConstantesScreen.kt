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

import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit

@Composable
fun ConstantesScreen(
    constantes: ConstantesConfig,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Titre centré en haut
        Text(
            text = "Constantes",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp)
        )

        // Zone scrollable pour le contenu
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            InlineBox(title = "Seuils par nombre de bouts", modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    constantes.seuils_bouts.forEachIndexed { index, seuil ->
                        Text(
                            text = "${index} bout(s) : $seuil points",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            InlineBox(title = "Multiplicateurs des contrats", modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    constantes.multiplicateurs.forEach { (contrat, mult) ->
                        Text(
                            text = "$contrat : x$mult",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            InlineBox(title = "Valeurs de base", modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Base de calcul : ${constantes.base_const}",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Petit au bout : ${constantes.petit_au_bout} points",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Pénalité de misère : ${constantes.misere_penalite} points",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            InlineBox(title = "Chelem", modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Chelem annoncé réussi : ${constantes.chelem.annonce_reussi} points",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Chelem non annoncé réussi : ${constantes.chelem.non_annonce_reussi} points",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Chelem annoncé raté : ${constantes.chelem.annonce_rate} points",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            InlineBox(title = "Poignées (valeur en points)", modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    constantes.poignee_values.forEach { (type, value) ->
                        if (value > 0) {
                            Text(
                                text = "$type : $value points",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            InlineBox(title = "Poignées (nombre d'atouts)", modifier = Modifier.fillMaxWidth()) {
                PoigneeTableDirect((constantes))
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Bouton Retour en bas, hors de la zone scrollable
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = onBack) {
                Text("Retour")
            }
        }
    }
}
@Composable
fun PoigneeTableDirect(constantes: ConstantesConfig) {
    val cols = constantes.poignee_atouts.keys.sortedBy { it.toInt() }
    val types = listOf("SIMPLE", "DOUBLE", "TRIPLE")
    val cellFontSize = 12.sp

    Column(Modifier.fillMaxWidth().background(Color.Black).padding(8.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Cell("Joueurs", cellFontSize, weight = 2f) // ← Première colonne plus large
            cols.forEach { Cell(it, cellFontSize, weight = 1f) }
        }
        types.forEach { type ->
            Row(Modifier.fillMaxWidth()) {
                Cell(type, cellFontSize, weight = 2f) // ← Première colonne plus large
                cols.forEach { col ->
                    val value = constantes.poignee_atouts[col]?.get(type)?.toString() ?: "-"
                    Cell(value, cellFontSize, weight = 1f)
                }
            }
        }
    }
}

@Composable
private fun RowScope.Cell(
    text: String,
    fontSize: TextUnit = 14.sp,
    weight: Float = 1f // ← Nouveau paramètre
) {
    Box(
        modifier = Modifier
            .weight(weight) // ← Utiliser le weight personnalisé
            .border(1.dp, Color.White)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = fontSize,
            textAlign = TextAlign.Center
        )
    }
}