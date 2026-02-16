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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.String
import kotlin.math.absoluteValue

@Composable
fun CalculsScreen(
    joueurs: List<Joueur>,
    donne: Donne?,
    constantes: ConstantesConfig,
    onDismiss: () -> Unit
) {
    // Si la donne est nulle, on revient automatiquement
    if (donne == null) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }

    // Récupération des valeurs depuis la Donne
    val nbJoueurs = joueurs.size
    val preneurIndex = donne.preneurIndex
    val appeleIndex = donne.appeleIndex
    val pointsRealises = donne.pointsAtq
    val nbBoutsAttaque = donne.nbBoutsAttaque
    val petitAuBout = donne.petitAuBout

    val contratIndex = donne.contratIndex.coerceIn(0, ContratType.entries.lastIndex)
    val contratAffichable = ContratType.entries[contratIndex].displayName

    val nbBoutsIndex = nbBoutsAttaque?.coerceIn(0, constantes.seuils_bouts.lastIndex) ?: 0
    val seuil = constantes.seuils_bouts[nbBoutsIndex]
    val delta = pointsRealises - seuil

    val multiplicateur = constantes.multiplicateurs[contratAffichable] ?: 1

    val petitAuBoutAttaque = petitAuBout?.let {
        if (joueurs.size == 5) {
            preneurIndex == it.index || appeleIndex == it.index
        } else {
            preneurIndex == it.index
        }
    } ?: false

    val scores = MutableList(nbJoueurs) { 0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Titre centré en haut
        Text(
            text = "Calculs",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            val LIGNE1: TextUnit = 16.sp
            val LIGNE2: TextUnit = 12.sp
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val boutsText = if (nbBoutsIndex < 2) {
                    "$nbBoutsIndex bout"
                } else {
                    "$nbBoutsIndex bouts"
                }

                val preneurNom = joueurs[donne.preneurIndex].nomUI


                Text(
                    "Le preneur est $preneurNom ",
                    color = Color.White,
                    style = TextStyle(fontSize = LIGNE2)
                )

                if (donne.appeleIndex != null && donne.appeleIndex != -1) {
                    val appeleNom = joueurs[donne.appeleIndex].nomUI
                    Text(
                        "L'appélé est $appeleNom ",
                        color = Color.White,
                        style = TextStyle(fontSize = LIGNE2)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    "Calcul des points de base",
                    color = Color.White,
                    style = TextStyle(fontSize = LIGNE1)
                )
                Text(
                    text = "Seuil : $seuil pour  $boutsText",
                    color = Color.White,
                    style = TextStyle(fontSize = LIGNE2)
                )
                Text(
                    text = "Points réalisés : $pointsRealises",
                    color = Color.White,
                    style = TextStyle(fontSize = LIGNE2)
                )
                Text(
                    text = "Delta = $pointsRealises - $seuil = $delta",
                    color = Color.White,
                    style = TextStyle(fontSize = LIGNE2)
                )

                Text(
                    text = "Le contrat est une $contratAffichable",
                    color = Color.White,
                    style = TextStyle(fontSize = LIGNE2)
                )
                Text(
                    text = "Les points de base sont multipliés par $multiplicateur",
                    color = Color.White,
                    style = TextStyle(fontSize = LIGNE2)
                )

                val baseConst = constantes.base_const
                val absDelta = delta.absoluteValue
                val attaqueGagne = delta >= 0

                var baseAjusteAvecPetitAuBout = 0
                val petitUubotBonus = constantes.petit_au_bout

                val signe = if (attaqueGagne) 1 else -1

                if (petitAuBoutAttaque) {
                    baseAjusteAvecPetitAuBout = (baseConst + absDelta) * multiplicateur
                    Text(
                        text = "Points de base: ($baseConst + |$delta|) x $multiplicateur = $baseAjusteAvecPetitAuBout",
                        color = Color.White,
                        style = TextStyle(fontSize = LIGNE2)
                    )
                } else {
                    baseAjusteAvecPetitAuBout = (baseConst + absDelta) * multiplicateur
                    Text(
                        text = "Points de base: ($baseConst + |$delta|) x $multiplicateur = $baseAjusteAvecPetitAuBout",
                        color = Color.White,
                        style = TextStyle(fontSize = LIGNE2)
                    )
                }

                ajouterPoints(
                    scores,
                    nbJoueurs,
                    preneurIndex,
                    appeleIndex,
                    baseAjusteAvecPetitAuBout * signe
                )

                afficherScoresTableLigneEntete(joueurs)
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    Text(
                        "Sous total",
                        color = Color.White,
                        style = TextStyle(fontSize = LIGNE2)
                    )
                    afficherScoresTableLigneScores(scores, true)
                }
                Spacer(modifier = Modifier.height(8.dp))

                var pointsPetitAuBout = 0
                if (petitAuBout != null) {
                    if (petitAuBoutAttaque) {
                        pointsPetitAuBout = petitUubotBonus * multiplicateur
                        Text(
                            "Petit au bout est à l'attaque",
                            color = Color.White,
                            style = TextStyle(fontSize = LIGNE2)
                        )
                    } else {
                        pointsPetitAuBout = -petitUubotBonus * multiplicateur
                        Text(
                            "Petit au bout est à la défense",
                            color = Color.White,
                            style = TextStyle(fontSize = LIGNE2)
                        )
                    }
                    val tmpScores = MutableList(scores.size) { 0 }
                    ajouterPoints(
                        tmpScores,
                        nbJoueurs,
                        preneurIndex,
                        appeleIndex,
                        pointsPetitAuBout
                    )
                    ajouterPoints(scores, nbJoueurs, preneurIndex, appeleIndex, pointsPetitAuBout)

                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        afficherScoresTableLigneScores(tmpScores)
                        Text(
                            "Sous total",
                            color = Color.White,
                            style = TextStyle(fontSize = LIGNE2)
                        )
                        afficherScoresTableLigneScores(scores, true)

                    }
                    Spacer(modifier = Modifier.height(8.dp))

                }

                val poignees = donne.poignees

                val nonEmpty = poignees.filter { it.type != PoigneeType.NONE }

                val nbPoignees = nonEmpty.size
                if (nbPoignees > 0) {
                    if (nbPoignees < 2) {
                        Text(
                            "Calcul de la poignée",
                            color = Color.White,
                            style = TextStyle(fontSize = LIGNE1)
                        )
                        val poigneeIndex = nonEmpty[0].index
                        val poigneeType = nonEmpty[0].type
                        val poigneeNom = poigneeType.displayName
                        val joueurNom = joueurs[poigneeIndex].nomUI
                        val poigneeValue = constantes.poignee_values[poigneeType.name] ?: 0

                        Text(
                            "Poignée $poigneeNom de $joueurNom",
                            color = Color.White,
                            style = TextStyle(fontSize = LIGNE2)
                        )
                        val tmpScores = MutableList(scores.size) { 0 }
                        ajouterPoints(
                            tmpScores,
                            nbJoueurs,
                            preneurIndex,
                            appeleIndex,
                            poigneeValue * signe
                        )
                        ajouterPoints(
                            scores,
                            nbJoueurs,
                            preneurIndex,
                            appeleIndex,
                            poigneeValue * signe
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            afficherScoresTableLigneScores(tmpScores)
                            Text(
                                "Sous total",
                                color = Color.White,
                                style = TextStyle(fontSize = LIGNE2)
                            )
                            afficherScoresTableLigneScores(scores, true)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        Text(
                            "Calcul des poignées",
                            color = Color.White,
                            style = TextStyle(fontSize = LIGNE1)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            nonEmpty.forEach { poignee ->
                                val poigneeIndex = poignee.index
                                val poigneeType = poignee.type
                                val poigneeNom = poigneeType.displayName
                                val joueurNom = joueurs[poigneeIndex].nomUI
                                val poigneeValue = constantes.poignee_values[poigneeType.name] ?: 0

                                Text(
                                    "Poignée $poigneeNom de $joueurNom",
                                    color = Color.White,
                                    style = TextStyle(fontSize = LIGNE2)
                                )
                                val tmpScores = MutableList(scores.size) { 0 }
                                ajouterPoints(
                                    tmpScores,
                                    nbJoueurs,
                                    preneurIndex,
                                    appeleIndex,
                                    poigneeValue * signe
                                )
                                ajouterPoints(
                                    scores,
                                    nbJoueurs,
                                    preneurIndex,
                                    appeleIndex,
                                    poigneeValue * signe
                                )
                                afficherScoresTableLigneScores(tmpScores)
                            }
                            Text(
                                "Sous total",
                                color = Color.White,
                                style = TextStyle(fontSize = LIGNE2)
                            )
                            afficherScoresTableLigneScores(scores, true)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                val chelem = donne.chelem
                if (chelem != null) {
                    val tmpScores = MutableList(scores.size) { 0 }

                    Text(
                        "Calcul du Chelem",
                        color = Color.White,
                        style = TextStyle(fontSize = LIGNE1)
                    )
                    chelem?.let {
                        val bonusBase = when {
                            it.annonce && it.succes -> constantes.chelem.annonce_reussi
                            !it.annonce && it.succes -> constantes.chelem.non_annonce_reussi
                            it.annonce && !it.succes -> constantes.chelem.annonce_rate
                            else -> 0
                        }
                        if (bonusBase != 0) {
                            ajouterPoints(
                                tmpScores,
                                nbJoueurs,
                                preneurIndex,
                                appeleIndex,
                                bonusBase
                            )
                            ajouterPoints(scores, nbJoueurs, preneurIndex, appeleIndex, bonusBase)
                        }
                    }

                    if (chelem.annonce) {
                        if (chelem.succes) {
                            Text(
                                "Chelem annoncé et gagné",
                                color = Color.White,
                                style = TextStyle(fontSize = LIGNE2)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                afficherScoresTableLigneScores(tmpScores)
                                Text(
                                    "Sous total",
                                    color = Color.White,
                                    style = TextStyle(fontSize = LIGNE2)
                                )
                                afficherScoresTableLigneScores(scores, true)
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                        } else {
                            Text(
                                "Chelem annoncé et perdu",
                                color = Color.White,
                                style = TextStyle(fontSize = LIGNE2)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                afficherScoresTableLigneScores(tmpScores)
                                Text(
                                    "Sous total",
                                    color = Color.White,
                                    style = TextStyle(fontSize = LIGNE2)
                                )
                                afficherScoresTableLigneScores(scores, true)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    } else {
                        Text(
                            "Chelem non annoncé",
                            color = Color.White,
                            style = TextStyle(fontSize = LIGNE2)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            afficherScoresTableLigneScores(tmpScores)
                            Text(
                                "Sous total",
                                color = Color.White,
                                style = TextStyle(fontSize = LIGNE2)
                            )
                            afficherScoresTableLigneScores(scores, true)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                val miseres = donne.miseres
                val nbMiseres = miseres.size
                if (nbMiseres > 0) {
                    if (nbMiseres < 2) {
                        Text(
                            "Calcul de la misère",
                            color = Color.White,
                            style = TextStyle(fontSize = LIGNE1)
                        )
                    } else {
                        Text(
                            "Calcul des misères",
                            color = Color.White,
                            style = TextStyle(fontSize = LIGNE1)
                        )
                    }
                    miseres?.forEach { idx ->
                        val tmpScores = MutableList(scores.size) { 0 }
                        val joueurNom = joueurs[idx].nomUI
                        Text(
                            "$joueurNom a une misère",
                            color = Color.White,
                            style = TextStyle(fontSize = LIGNE2)
                        )
                        scores[idx] += 10 * (nbJoueurs - 1)
                        tmpScores[idx] = 10 * (nbJoueurs - 1)
                        for (i in 0 until nbJoueurs) {
                            if (i != idx) {
                                scores[i] -= 10
                                tmpScores[i] = -10
                            }
                        }
                        afficherScoresTableLigneScores(tmpScores)
                    }
                    Text(
                        "Sous total",
                        color = Color.White,
                        style = TextStyle(fontSize = LIGNE2)
                    )
                    afficherScoresTableLigneScores(scores, true)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(8.dp)
            ) {
                Text("Total", color = Color.White, fontSize = LIGNE2)
                afficherScoresTableLigneScores(scores, false, true)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(BOUTON_COULEUR),
                            contentColor = Color.Black
                        )
                    ) { Text("Retour") }
                }
            }
        }
        /*
    if( donne.scores != scores) {
        Text(
            "Scores différents",
            color = Color.White,
            style = TextStyle(fontSize = 80.sp)
        )
    }
    */
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RowScope.Cell(
    text: String,
    fontSize: TextUnit,
    weight: Float = 1f,
    cellHeight: Dp = 30.dp,
    backgroundColor: Color = Color.DarkGray,
    textColor: Color = Color.Black,
    horizontalPadding: Dp = 4.dp
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .height(cellHeight)
            .border(1.dp, Color.White)
            .background(backgroundColor)
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .basicMarquee(
                    iterations = Int.MAX_VALUE,
                    animationMode = MarqueeAnimationMode.Immediately,
                    repeatDelayMillis = 1200,
                    velocity = 40.dp
                ),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun afficherScoresTableLigneEntete(
    joueurs: List<Joueur>,
) {
    val nbJoueurs = joueurs.size
    val cellFontSize = 12.sp
    val rowHeight = 34.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight)
        ) {
            joueurs.take(nbJoueurs).forEach { joueur ->
                Cell(
                    text = joueur.nomUI,
                    fontSize = cellFontSize,
                    weight = 1f,
                    cellHeight = rowHeight,
                    backgroundColor = Color(BOUTON_COULEUR),
                    textColor = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
fun afficherScoresTableLigneScores(
    scores: List<Int>,
    trait: Boolean = false,
    total: Boolean = false,
) {
    val nbJoueurs = scores.size
    val cellFontSize = 12.sp
    val rowHeight = 34.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        if (trait) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color.Red)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight)
        ) {
            scores.take(nbJoueurs).forEach { score ->
                Cell(
                    text = score.toString(),
                    fontSize = cellFontSize,
                    weight = 1f,
                    cellHeight = rowHeight,
                    backgroundColor = if (total) {
                        Color(0xFFFFB6C1)
                    } else {
                        Color.Black
                    },
                    textColor = if (total) Color.Black else Color.White,
                )
            }
        }
    }
}
