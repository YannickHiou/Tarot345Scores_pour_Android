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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color

@Composable
fun StatistiquesGlobalesScreen(
    historique: Historique,
    onNavigateBack: () -> Unit
) {
    val analyseur = remember { AnalyseurHistorique() }

    val nbJoueursDisponibles = remember(historique) {
        historique.parties
            .map { it.joueurs.size }
            .distinct()
            .sorted()
    }
    println("nbJoueursDisponibles.size: ${nbJoueursDisponibles.size} ")
    var nbJoueursFiltre by remember { mutableStateOf<Int?>(null) }

    val historiqueFiltré = remember(historique, nbJoueursFiltre) {
        if (nbJoueursFiltre == null) {
            historique
        } else {
            Historique(
                parties = historique.parties.filter { it.joueurs.size == nbJoueursFiltre }
                    .toMutableList()
            )
        }
    }


    val statistiquesResult = remember(historiqueFiltré) {
        analyseur.analyser(historiqueFiltré)
    }

    val (_, _, statistiquesGlobales) = statistiquesResult
    val decimalFormat = DecimalFormat("#.##")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (historique.parties.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Statistiques Globales",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucune partie",
                        fontSize = 16.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = onNavigateBack,
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Statistiques Globales",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (nbJoueursDisponibles.size > 1) {
                    InlineBox(
                        title = "Filtrer par nombre de joueurs",
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { nbJoueursFiltre = null },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (nbJoueursFiltre == null) Color(BOUTON_COULEUR) else Color.DarkGray,
                                    contentColor =  if (nbJoueursFiltre == null)  Color.Black else Color.White
                                ),
                                border = BorderStroke(1.dp, Color.White)
                            ) {
                                Text("Tous")
                            }

                            nbJoueursDisponibles.forEach { nb ->
                                Button(
                                    onClick = { nbJoueursFiltre = nb },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (nbJoueursFiltre == nb) Color(BOUTON_COULEUR) else Color.DarkGray,
                                        contentColor = if (nbJoueursFiltre == nb) Color.Black else Color.White
                                    ),
                                    border = BorderStroke(1.dp, Color.White)
                                ) {
                                    Text("$nb joueurs")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        InlineBox(
                            title = "Résumé général",
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Total parties: ${statistiquesGlobales.nbParties}",
                                color = Color.White
                            )
                            Text(
                                "Total donnes: ${statistiquesGlobales.nbDonnes}",
                                color = Color.White
                            )
                            Text(
                                "Bouts attaque: ${statistiquesGlobales.nbBoutsAttaque}",
                                color = Color.White
                            )
                            Text(
                                "Bouts défense: ${3 * statistiquesGlobales.nbDonnes - statistiquesGlobales.nbBoutsAttaque}",
                                color = Color.White
                            )
                            Text(
                                "Points: ${statistiquesGlobales.pointsGagnes}",
                                color = Color.White
                            )
                            Text("Misères: ${statistiquesGlobales.miseres}", color = Color.White)
                            Text(
                                "Petit bout gagnés: ${statistiquesGlobales.petitAuBoutGagne}",
                                color = Color.White
                            )
                            Text(
                                "Petit bout perdus: ${statistiquesGlobales.petitAuBoutPerdu}",
                                color = Color.White
                            )

                            Text(
                                "Meilleur score: ${statistiquesGlobales.meilleurScore}",
                                color = Color.White
                            )
                            Text(
                                "Pire score: ${statistiquesGlobales.pireScore}",
                                color = Color.White
                            )
                        }
                    }

                    item {
                        InlineBox(
                            title = "Contrats",
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Petite: ${statistiquesGlobales.contrats[0]}",
                                    color = Color.White
                                )
                                Text(
                                    "Garde: ${statistiquesGlobales.contrats[1]}",
                                    color = Color.White
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Garde Sans: ${statistiquesGlobales.contrats[2]}",
                                    color = Color.White
                                )
                                Text(
                                    "Garde Contre: ${statistiquesGlobales.contrats[3]}",
                                    color = Color.White
                                )
                            }
                        }
                    }

                    if (statistiquesGlobales.poignees.sum() > 0) {
                        item {
                            InlineBox(
                                title = "Poignées",
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Simple: ${statistiquesGlobales.poignees[1]}",
                                        color = Color.White
                                    )
                                    Text(
                                        "Double: ${statistiquesGlobales.poignees[2]}",
                                        color = Color.White
                                    )
                                    Text(
                                        "Triple: ${statistiquesGlobales.poignees[3]}",
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    if (statistiquesGlobales.chelems.sum() > 0) {
                        item {
                            InlineBox(
                                title = "Chelems",
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Non annoncé: ${statistiquesGlobales.chelems[0]}",
                                    color = Color.White
                                )
                                Text(
                                    "Annoncé raté: ${statistiquesGlobales.chelems[1]}",
                                    color = Color.White
                                )
                                Text(
                                    "Annoncé réussi: ${statistiquesGlobales.chelems[2]}",
                                    color = Color.White
                                )
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
}