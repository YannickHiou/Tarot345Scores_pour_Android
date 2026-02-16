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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color

@Composable
fun StatistiquesPartieJoueursScreen(
    historique: Historique,
    joueur: Joueur,
    onNavigateBack: () -> Unit,
    fromPartie: Boolean = false,
    onNavigateToHistorique: (Joueur, Int?) -> Unit = { _, _ -> }
) {
    val analyseur = remember { AnalyseurHistorique() }

    val statistiquesResult = remember(historique) {
        analyseur.analyser(historique)
    }

    val (statistiquesJoueurs, _, _) = statistiquesResult

    val nomsJoueurs = remember(statistiquesJoueurs) {
        statistiquesJoueurs.keys.sorted()
    }

    var menuExpanded by remember { mutableStateOf(false) }

    var joueurSelectionne by rememberSaveable(joueur) {
        mutableStateOf(
            if (joueur.id.isNotEmpty() && statistiquesJoueurs.containsKey(joueur.id)) {
                joueur.id
            } else {
                nomsJoueurs.firstOrNull() ?: ""
            }
        )
    }

    val nbJoueursDisponibles = remember(statistiquesJoueurs, joueurSelectionne) {
        statistiquesJoueurs[joueurSelectionne]
            ?.parties
            ?.filter { (_, stats) -> (stats.totalDonnes > 0) || (stats.totalParties > 0) }
            ?.keys
            ?.sorted()
            ?: emptyList()
    }

    var nbJoueursFiltre by rememberSaveable { mutableStateOf<Int?>(null) }

    LaunchedEffect(nbJoueursDisponibles) {
        if (nbJoueursFiltre != null && !nbJoueursDisponibles.contains(nbJoueursFiltre)) {
            nbJoueursFiltre = null
        }
    }

    val decimalFormat = DecimalFormat("#.##")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (historique.parties.isEmpty() || nomsJoueurs.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        item {
                            Text(
                                text = "Statistiques Joueur",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Aucune donnée disponible",
                                    fontSize = 16.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = onNavigateBack,
                            border = BorderStroke(1.dp, Color.White)
                        ) {
                            Text("Retour")
                        }
                    }
                }
            } else {
                val statsJoueur = statistiquesJoueurs[joueurSelectionne]
                val statsAffichees = if (statsJoueur != null) {
                    if (nbJoueursFiltre == null) {
                        agregerStats(statsJoueur.parties)
                    } else {
                        statsJoueur.parties[nbJoueursFiltre]
                    }
                } else {
                    null
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .statusBarsPadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Statistiques Joueur",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    item {
                        InlineBox(
                            title = "Joueur",
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = {
                                        onNavigateToHistorique(joueur, nbJoueursFiltre)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    border = BorderStroke(1.dp, Color.White),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(joueur.nomUI)
                                }
                            }
                        }
                    }

                    if (nbJoueursDisponibles.size > 1) {
                        item {
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
                                            containerColor = if (nbJoueursFiltre == null) MaterialTheme.colorScheme.primary else Color.DarkGray,
                                            contentColor = Color.White
                                        ),
                                        border = BorderStroke(1.dp, Color.White)
                                    ) {
                                        Text("Tous")
                                    }

                                    nbJoueursDisponibles.forEach { nb ->
                                        Button(
                                            onClick = { nbJoueursFiltre = nb },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (nbJoueursFiltre == nb) MaterialTheme.colorScheme.primary else Color.DarkGray,
                                                contentColor = Color.White
                                            ),
                                            border = BorderStroke(1.dp, Color.White)
                                        ) {
                                            Text("$nb joueurs")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (statsAffichees != null) {
                        item {
                            InlineBox(
                                title = "Résumé général",
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Total parties: ${statsAffichees.totalParties}",
                                    color = Color.White
                                )
                                Text(
                                    "Total donnes: ${statsAffichees.totalDonnes}",
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = if (statsAffichees.partiesGagnees < 2) "Partie gagnée: ${statsAffichees.partiesGagnees}"
                                    else "Parties gagnées: ${statsAffichees.partiesGagnees}",
                                    color = Color.White
                                )

                                Text(
                                    text = if (statsAffichees.donnesGagnees < 2) "Donne gagnée: ${statsAffichees.donnesGagnees}"
                                    else "Donnes gagnées: ${statsAffichees.donnesGagnees}",
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "Preneur: ${statsAffichees.preneur} fois",
                                    color = Color.White
                                )

                                if (nbJoueursFiltre == 5 || (nbJoueursFiltre == null && statsAffichees.appele > 0)) {
                                    Text(
                                        "Appelé: ${statsAffichees.appele} fois ",
                                        color = Color.White
                                    )
                                }

                                Text(
                                    "Défense: ${statsAffichees.defense} fois ",
                                    color = Color.White
                                )
                            }
                        }

                        item {
                            InlineBox(
                                title = "Performance",
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Points gagnés: ${statsAffichees.pointsGagnes}",
                                    color = Color.White
                                )
                                Text(
                                    "Points perdus: ${statsAffichees.pointsPerdus}",
                                    color = Color.White
                                )

                                val gainNet =
                                    statsAffichees.pointsGagnes + statsAffichees.pointsPerdus

                                Text(
                                    "Gain net: ${gainNet}",
                                    color = Color.White,
                                )

                                val gainMoyenParDonne = if (statsAffichees.totalDonnes > 0) {
                                    gainNet.toDouble() / statsAffichees.totalDonnes
                                } else {
                                    0.0
                                }

                                val gainMoyenParPartie = if (statsAffichees.totalParties > 0) {
                                    gainNet.toDouble() / statsAffichees.totalParties
                                } else {
                                    0.0
                                }

                                Text(
                                    "Gain moyen/partie: ${
                                        decimalFormat.format(
                                            gainMoyenParPartie
                                        )
                                    }",
                                    color = Color.White
                                )

                                Text(
                                    "Gain moyen/donne: ${decimalFormat.format(gainMoyenParDonne)}",
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "Meilleur score: ${statsAffichees.meilleurScore}",
                                    color = Color.White
                                )
                                Text(
                                    "Pire score: ${statsAffichees.pireScore}",
                                    color = Color.White
                                )
                            }
                        }

                        item {
                            InlineBox(
                                title = "Bonus/Malus",
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Petit au bout gagné: ${statsAffichees.petitAuBoutGagne}",
                                    color = Color.White
                                )
                                Text(
                                    "Petit au bout perdu: ${statsAffichees.petitAuBoutPerdu}",
                                    color = Color.White
                                )
                                Text("Misères: ${statsAffichees.miseres}", color = Color.White)
                            }
                        }

                        if (statsAffichees.contrats.sum() > 0) {
                            item {
                                InlineBox(
                                    title = "Contrats",
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val totalContrats = statsAffichees.contrats.sum()

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Petite: ${statsAffichees.contrats[0]}",
                                            color = Color.White
                                        )

                                        Text(
                                            "Garde: ${statsAffichees.contrats[1]}",
                                            color = Color.White
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Garde Sans: ${statsAffichees.contrats[2]}",
                                            color = Color.White
                                        )

                                        Text(
                                            "Garde Contre: ${statsAffichees.contrats[3]}",
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Total: $totalContrats",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (statsAffichees.poignees.sum() > 0) {
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
                                            "Simple: ${statsAffichees.poignees[1]}",
                                            color = Color.White
                                        )
                                        Text(
                                            "Double: ${statsAffichees.poignees[2]}",
                                            color = Color.White
                                        )
                                        Text(
                                            "Triple: ${statsAffichees.poignees[3]}",
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        if (statsAffichees.chelems.sum() > 0) {
                            item {
                                InlineBox(
                                    title = "Chelems",
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "Non annoncé réussi: ${statsAffichees.chelems[0]}",
                                        color = Color.White
                                    )
                                    Text(
                                        "Annoncé raté: ${statsAffichees.chelems[1]}",
                                        color = Color.White
                                    )
                                    Text(
                                        "Annoncé réussi: ${statsAffichees.chelems[2]}",
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Aucune donnée pour cette configuration",
                                    fontSize = 16.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onNavigateBack,
                    border = BorderStroke(1.dp, Color.White)
                ) {
                    Text("Retour")
                }
            }
        }
    }
}

private fun agregerStats(parties: MutableMap<Int, StatistiquesJoueur>): StatistiquesJoueur? {
    if (parties.isEmpty()) return null

    val contrats = MutableList(4) { 0 }
    val poignees = MutableList(4) { 0 }
    val chelems = MutableList(3) { 0 }
    var miseres = 0
    var petitAuBoutGagne = 0
    var petitAuBoutPerdu = 0
    var preneur = 0
    var appele = 0
    var defense = 0
    var totalDonnes = 0
    var totalParties = 0
    var pointsGagnes = 0
    var pointsPerdus = 0
    var meilleurScore = Int.MIN_VALUE
    var pireScore = Int.MAX_VALUE
    var donnesGagnees: Int = 0
    var partiesGagnees: Int = 0

    parties.values.forEach { stats ->
        for (i in contrats.indices) {
            contrats[i] += stats.contrats[i]
        }
        for (i in poignees.indices) {
            poignees[i] += stats.poignees[i]
        }
        for (i in chelems.indices) {
            chelems[i] += stats.chelems[i]
        }

        miseres += stats.miseres
        petitAuBoutGagne += stats.petitAuBoutGagne
        petitAuBoutPerdu += stats.petitAuBoutPerdu
        preneur += stats.preneur
        appele += stats.appele
        defense += stats.defense
        totalDonnes += stats.totalDonnes
        totalParties += stats.totalParties
        pointsGagnes += stats.pointsGagnes
        pointsPerdus += stats.pointsPerdus
        meilleurScore = maxOf(meilleurScore, stats.meilleurScore)
        pireScore = minOf(pireScore, stats.pireScore)
        donnesGagnees += stats.donnesGagnees
        partiesGagnees += stats.partiesGagnees
    }

    return StatistiquesJoueur(
        contrats = contrats,
        poignees = poignees,
        chelems = chelems,
        miseres = miseres,
        petitAuBoutGagne = petitAuBoutGagne,
        petitAuBoutPerdu = petitAuBoutPerdu,
        preneur = preneur,
        appele = appele,
        defense = defense,
        totalDonnes = totalDonnes,
        totalParties = totalParties,
        pointsGagnes = pointsGagnes,
        pointsPerdus = pointsPerdus,
        meilleurScore = meilleurScore,
        pireScore = pireScore,
        donnesGagnees = donnesGagnees,
        partiesGagnees = partiesGagnees
    )
}
