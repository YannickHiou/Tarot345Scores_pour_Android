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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect

@Composable
fun JeuxScreen(
    joueurs: List<Joueur>,
    donnes: List<Donne>,
    totals: List<Int>,
    constantes: ConstantesConfig,
    isReadOnly: Boolean = false,
    modifier: Modifier = Modifier,
    onRetour: () -> Unit = {},
    onRetourLectureSeule: () -> Unit = {},
    onNouvelleDonne: () -> Unit = {},
    onEditDonne: (Donne) -> Unit = {},
    onViewDonne: (Donne) -> Unit = {},
    onLongPressDonne: (Donne) -> Unit,
    viewModel: MainViewModel
) {
    val separatorColor = Color.White
    val separatorStrokeDp = 2.dp
    var longPressDonne by remember { mutableStateOf<Donne?>(null) }
    val highlightColor = Color(0xFF000000) // Color(0xFFFFB6C1)

    val nbCols = remember(donnes, joueurs) {
        val maxScoresSize = donnes.maxOfOrNull { it.scores.size } ?: 0
        maxOf(joueurs.size, maxScoresSize)
    }.coerceAtLeast(0)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(JEUX_COULEUR)) // bleu cyan
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .drawBehind {
                        // Draw vertical separators across the entire table area
                        if (nbCols > 0) {
                            val columnW = size.width / nbCols
                            val stroke = separatorStrokeDp.toPx()
                            for (i in 1 until nbCols) {
                                val x = columnW * i
                                drawLine(
                                    color = separatorColor,
                                    start = Offset(x, 0f),
                                    end = Offset(x, size.height),
                                    strokeWidth = stroke
                                )
                            }
                        }
                    }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    HeaderRow(joueurs)

                    // Ligne noire entre header et scores
                    HorizontalDivider(color = separatorColor, thickness = separatorStrokeDp)

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        itemsIndexed(donnes) { _, donne ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .combinedClickable(
                                        onClick = {
                                            if (!isReadOnly) onEditDonne(donne) else onViewDonne(
                                                donne
                                            )
                                        },
                                        onLongClick = {
                                            longPressDonne = donne
                                        }
                                    )
                            ) {
                                val scores = donne.scores
                                val maxScore = scores.maxOrNull()
                                val uniqueMax =
                                    maxScore != null && scores.count { it == maxScore } == 1

                                for (i in 0 until nbCols) {
                                    val s = scores.getOrNull(i) ?: 0
                                    val isMax =
                                        maxScore != null && (if (uniqueMax) s == maxScore else s == maxScore)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f),
                                        //.then(if (isMax) Modifier.background(highlightColor) else Modifier),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = s.toString(),
                                            color = if (isMax) Color.Green else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = separatorColor, thickness = separatorStrokeDp)

                    // Ligne des totaux
                    val maxTotal = totals.maxOrNull()
                    val uniqueTotalMax = maxTotal != null && totals.count { it == maxTotal } == 1

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until nbCols) {
                            val total = totals.getOrNull(i) ?: 0
                            val isMax =
                                maxTotal != null && (if (uniqueTotalMax) total == maxTotal else total == maxTotal)
                            Box(
                                modifier = Modifier
                                    .weight(1f),
                                //.then(if (isMax) Modifier.background(highlightColor) else Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = total.toString(),
                                    fontSize = 14.sp,
                                    color = if (isMax) Color.Green else Color.Yellow
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (isReadOnly) {
                            onRetourLectureSeule()
                        } else {
                            onRetour()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = if (isReadOnly) "Retour" else "Terminer la partie")
                }

                // Afficher "Nouvelle Donne" seulement si pas en lecture seule
                if (!isReadOnly) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onNouvelleDonne,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(text = "Nouvelle Donne")
                    }
                }
            }
        }

        LaunchedEffect(longPressDonne) {
            longPressDonne?.let { targetDonne ->
                onLongPressDonne(targetDonne)
                longPressDonne = null
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeaderRow(joueurs: List<Joueur>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .background(Color(JEUX_COULEUR))
    ) {
        joueurs.forEach { joueur ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(1.dp, Color.White)
                    .padding(end = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = joueur.nomUI,
                    fontSize = 14.sp,
                    color = Color.White,
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
    }
}
