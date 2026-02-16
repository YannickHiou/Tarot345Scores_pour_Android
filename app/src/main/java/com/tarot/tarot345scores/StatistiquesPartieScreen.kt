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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatistiquesPartieScreen(
    historique: Historique,
    joueursGlobal: List<Joueur>,
    partieId: String?,
    onNavigateBack: () -> Unit,
    onNavigateToJoueur: (Joueur) -> Unit,
    onConsulterPartie: (String) -> Unit,
    onRecreateJoueur: (Joueur) -> Unit
) {
    val partie = remember(historique, partieId) {
        historique.parties.find { it.id == partieId }
    }

    val analyseur = remember { AnalyseurHistorique() }
    val statistiques = remember(partie, historique) {
        if (partie != null) {
            val (_, partiesStats, _) = analyseur.analyser(historique)
            // Trouver les statistiques de la partie spécifique
            val indexPartie = historique.parties.indexOf(partie)
            if (indexPartie >= 0 && indexPartie < partiesStats.size) {
                partiesStats[indexPartie]
            } else null
        } else null
    }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {

            Text(
                text = if (partie != null) {
                    "${dateFormat.format(Date(partie.createdAt))}"
                } else {
                    "Partie non trouvée"
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (partie == null || statistiques == null) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        "Aucune partie",
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier.padding(bottom = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(BOUTON_COULEUR),
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Retour")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        InlineBox(
                            title = "Informations générales",
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Nombre de donnes : ${statistiques.nbDonnes}",
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Classement :",
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            // Calculer les gains par joueur dans cette partie
                            val gainsParJoueur = remember(partie) {
                                val gains = mutableMapOf<Joueur, Int>()

                                partie.joueurs.forEach { joueur ->
                                    gains[joueur] = 0
                                }

                                // Additionner les scores de toutes les donnes
                                partie.donnes.forEach { donne ->
                                    donne.scores.forEachIndexed { index, score ->
                                        val joueur = partie.joueurs[index]
                                        gains[joueur] = gains.getOrDefault(joueur, 0) + (score ?: 0)
                                    }
                                }

                                // Trier par gain décroissant
                                gains.toList().sortedByDescending { it.second }
                            }

                            gainsParJoueur.forEach { (joueur, gain) ->

                                var showJoueurSupprimeDialog by remember { mutableStateOf(false) }

                                val joueurGlobal: Joueur? =
                                    joueursGlobal.find { it.id == joueur.id }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            if (joueurGlobal != null) {
                                                onNavigateToJoueur(joueurGlobal)
                                            } else {
                                                showJoueurSupprimeDialog = true
                                            }
                                        },
                                        modifier = Modifier
                                            .height(36.dp)
                                            .width(140.dp),
                                        contentPadding = PaddingValues(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(BOUTON_COULEUR),
                                            contentColor = Color.Black
                                        ),
                                        border = BorderStroke(1.dp, Color.White)
                                    ) {
                                        Text(
                                            text = joueurGlobal?.nomUI ?: joueur.nomUI,
                                            fontSize = 14.sp,
                                            color = Color.Black
                                        )
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    Text(
                                        text = if (gain >= 0) "+$gain" else "$gain",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                if (showJoueurSupprimeDialog) {
                                    Dialog(onDismissRequest = {
                                        showJoueurSupprimeDialog = false
                                    }) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.Black, // fond noir
                                            modifier = Modifier
                                                .border(
                                                    1.dp,
                                                    Color.White,
                                                    RoundedCornerShape(8.dp)
                                                ) // trait blanc collé au fond noir
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) { // padding intérieur pour le contenu seulement
                                                Text(
                                                    "Joueur supprimé",
                                                    style = MaterialTheme.typography.titleLarge,
                                                    color = Color.White
                                                )
                                                Spacer(Modifier.height(8.dp))
                                                Text(text = joueur.nomUI, color = Color.White)
                                                Text(text = "ID: ${joueur.id}", color = Color.White)
                                                Spacer(Modifier.height(12.dp))
                                                Row(
                                                    horizontalArrangement = Arrangement.End,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    TextButton(
                                                        onClick = {
                                                            showJoueurSupprimeDialog = false
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = Color(BOUTON_COULEUR),
                                                            contentColor = Color.Black
                                                        )
                                                    ) {
                                                        Text("OK", color = Color.White)
                                                    }
                                                    TextButton(
                                                        onClick = {
                                                            showJoueurSupprimeDialog = false
                                                            onRecreateJoueur(joueur)
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = Color(BOUTON_COULEUR),
                                                            contentColor = Color.Black
                                                        )
                                                    ) {
                                                        Text("Re Créer", color = Color.White)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        InlineBox(
                            title = "Scores",
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Meilleur score : ${statistiques.meilleurScore}",
                                color = Color.White
                            )
                            Text("Pire score : ${statistiques.pireScore}", color = Color.White)
                            Text("Points : ${statistiques.pointsGagnes}", color = Color.White)
                        }
                    }

                    item {
                        InlineBox(
                            title = "Contrats",
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Petite : ${statistiques.partieContrats[0]}", color = Color.White)
                            Text("Garde : ${statistiques.partieContrats[1]}", color = Color.White)
                            Text(
                                "Garde Sans : ${statistiques.partieContrats[2]}",
                                color = Color.White
                            )
                            Text(
                                "Garde Contre : ${statistiques.partieContrats[3]}",
                                color = Color.White
                            )
                        }
                    }

                    item {
                        InlineBox(
                            title = "Détails",
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Bouts de l'attaque : ${statistiques.nbBoutsAttaque}",
                                color = Color.White
                            )
                            Text(
                                "Bouts de la défense : ${statistiques.nbDonnes * 3 - statistiques.nbBoutsAttaque}",
                                color = Color.White
                            )
                            Text(
                                "Petit au bout gagnés : ${statistiques.petitAuBoutGagne}",
                                color = Color.White
                            )
                            Text(
                                "Petit au bout perdus : ${statistiques.petitAuBoutPerdus}",
                                color = Color.White
                            )
                            Text("Misères : ${statistiques.miseres}", color = Color.White)
                        }
                    }

                    if (statistiques.partiePoignees.sum() > 0) {
                        item {
                            InlineBox(
                                title = "Poignées",
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Simple : ${statistiques.partiePoignees[1]}",
                                    color = Color.White
                                )
                                Text(
                                    "Double : ${statistiques.partiePoignees[2]}",
                                    color = Color.White
                                )
                                Text(
                                    "Triple : ${statistiques.partiePoignees[3]}",
                                    color = Color.White
                                )
                            }
                        }
                    }

                    if (statistiques.partieChelems.sum() > 0) {
                        item {
                            InlineBox(
                                title = "Chélems",
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Non annoncé : ${statistiques.partieChelems[0]}",
                                    color = Color.White
                                )
                                Text(
                                    "Annoncé raté : ${statistiques.partieChelems[1]}",
                                    color = Color.White
                                )
                                Text(
                                    "Annoncé réussi : ${statistiques.partieChelems[2]}",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.padding(start = 8.dp),
                    border = BorderStroke(1.dp, Color.White),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(BOUTON_COULEUR),
                        contentColor = Color.Black
                    )
                ) {
                    Text("Retour")
                }
            }

        }
    }

}