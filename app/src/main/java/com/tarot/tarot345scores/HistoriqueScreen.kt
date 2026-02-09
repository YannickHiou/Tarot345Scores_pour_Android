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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import java.text.SimpleDateFormat


private fun getMonthName(month: Int): String {
    return when (month) {
        Calendar.JANUARY -> "Janvier"
        Calendar.FEBRUARY -> "Février"
        Calendar.MARCH -> "Mars"
        Calendar.APRIL -> "Avril"
        Calendar.MAY -> "Mai"
        Calendar.JUNE -> "Juin"
        Calendar.JULY -> "Juillet"
        Calendar.AUGUST -> "Août"
        Calendar.SEPTEMBER -> "Septembre"
        Calendar.OCTOBER -> "Octobre"
        Calendar.NOVEMBER -> "Novembre"
        Calendar.DECEMBER -> "Décembre"
        else -> "Mois $month"
    }
}

// États de navigation
private sealed class HState {
    object Years : HState()
    data class Months(val year: Int) : HState()
    data class Parties(val year: Int, val month: Int) : HState()
}

// Saver pour HState
private val HStateSaver = Saver<HState, String>(
    save = { state ->
        when (state) {
            is HState.Years -> "Years"
            is HState.Months -> "Months:${state.year}"
            is HState.Parties -> "Parties:${state.year}:${state.month}"
        }
    },
    restore = { savedState ->
        val parts = savedState.split(":")
        when (parts[0]) {
            "Years" -> HState.Years
            "Months" -> HState.Months(parts[1].toInt())
            "Parties" -> HState.Parties(parts[1].toInt(), parts[2].toInt())
            else -> HState.Years
        }
    }
)

@Composable
fun HistoriqueScreen(
    historiqueState: State<Historique?>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onReprendrePartie: (Partie) -> Unit,
    onStatistiquesPartie: (Partie, HistoriqueContext) -> Unit,
    onSupprimerPartie: (Partie, HistoriqueContext) -> Unit,
    initialContext: HistoriqueContext? = null
) {
    val historique by historiqueState

    val calendar = remember { Calendar.getInstance() }
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    val validParties = remember(historique) {
        historique?.parties?.filter { it.donnes.isNotEmpty() } ?: emptyList()
    }

    // --- filtre par nombre de joueurs ---
    val nbJoueursDisponibles = remember(validParties) {
        validParties.map { it.joueurs.size }.distinct().sorted()
    }

    var navState by rememberSaveable(stateSaver = HStateSaver) {
        mutableStateOf<HState>(
            initialContext?.let { HState.Parties(it.year, it.month) } ?: HState.Years
        )
    }

    // mémoriser le filtre entre navigations / rotations
    var nbJoueursFiltre by rememberSaveable { mutableStateOf<Int?>(null) }

    val years = remember(validParties) {
        validParties.map {
            calendar.timeInMillis = it.createdAt
            calendar.get(Calendar.YEAR)
        }.distinct().sortedDescending()
    }

    val monthsForYear = { year: Int ->
        validParties.filter {
            calendar.timeInMillis = it.createdAt
            calendar.get(Calendar.YEAR) == year
        }.map {
            calendar.timeInMillis = it.createdAt
            calendar.get(Calendar.MONTH)
        }.distinct().sortedDescending()
    }

    val partiesForYearMonth = { year: Int, month: Int ->
        validParties.filter {
            calendar.timeInMillis = it.createdAt
            calendar.get(Calendar.YEAR) == year &&
                    calendar.get(Calendar.MONTH) == month &&
                    (nbJoueursFiltre == null || it.joueurs.size == nbJoueursFiltre)
        }.sortedByDescending { it.createdAt }
    }

    var showDialog by remember { mutableStateOf(false) }
    var longPressPartie by remember { mutableStateOf<Partie?>(null) }

    if (validParties.isEmpty()) {
        // Afficher le message "Historique vide"
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Text(
                text = "Historique",
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
                    onClick = onBack,
                    border = BorderStroke(1.dp, Color.White)
                ) {
                    Text("Retour")
                }
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding()
                .padding(12.dp)
        ) {
            // Titre selon état
            val title = when (val s = navState) {
                HState.Years -> "Historique"
                is HState.Months -> "Année ${s.year}"
                is HState.Parties -> "${getMonthName(s.month)} ${s.year}"
            }

            Text(
                text = title,
                color = Color.White,
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Contenu principal
            when (val s = navState) {
                is HState.Years -> {
                    if (years.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Aucune partie", color = Color.White)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(years) { year ->
                                Button(
                                    onClick = { navState = HState.Months(year) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text("Année $year")
                                }
                            }
                        }
                    }

                    // Bas : Accueil
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = onBack,
                            modifier = Modifier.padding(4.dp)
                        ) { Text("Accueil") }
                    }
                }

                is HState.Months -> {
                    val months = monthsForYear(s.year)
                    if (months.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Aucune partie pour ${s.year}", color = Color.White)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(months) { month ->
                                Button(
                                    onClick = { navState = HState.Parties(s.year, month) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(getMonthName(month))
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { navState = HState.Years },
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text("Retour")
                        }
                        Button(
                            onClick = onBack,
                            modifier = Modifier.padding(4.dp)
                        ) { Text("Accueil") }
                    }
                }

                is HState.Parties -> {
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

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    val parties = partiesForYearMonth(s.year, s.month)
                    if (parties.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Aucune partie", color = Color.White)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(parties) { partie ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable(
                                            onClick = {
                                                longPressPartie = partie
                                                showDialog = true
                                            }
                                        ),
                                    tonalElevation = 2.dp,
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    val dateStr = try {
                                        sdf.format(Date(partie.createdAt))
                                    } catch (e: Exception) {
                                        partie.createdAt.toString()
                                    }
                                    Text(
                                        text = dateStr,
                                        modifier = Modifier.padding(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        if (showDialog && longPressPartie != null) {
                            ActionOnPartieDialog(
                                partie = longPressPartie!!,
                                onRetour = {
                                    showDialog = false
                                    longPressPartie = null
                                },
                                onReprendre = { partie ->
                                    onReprendrePartie(partie)
                                    showDialog = false
                                    longPressPartie = null
                                },
                                onSupprimer = { partie ->
                                    val ctx = HistoriqueContext(
                                        year = run {
                                            calendar.timeInMillis = partie.createdAt
                                            calendar.get(Calendar.YEAR)
                                        },
                                        month = run {
                                            calendar.timeInMillis = partie.createdAt
                                            calendar.get(Calendar.MONTH)
                                        },
                                        nbJoueurs = partie.joueurs.size
                                    )
                                    onSupprimerPartie(partie, ctx)
                                    showDialog = false
                                    longPressPartie = null
                                },
                                onStatistiques = { partie ->
                                    val ctx = HistoriqueContext(
                                        year = run {
                                            calendar.timeInMillis = partie.createdAt
                                            calendar.get(Calendar.YEAR)
                                        },
                                        month = run {
                                            calendar.timeInMillis = partie.createdAt
                                            calendar.get(Calendar.MONTH)
                                        },
                                        nbJoueurs = partie.joueurs.size
                                    )
                                    onStatistiquesPartie(partie, ctx)
                                    showDialog = false
                                    longPressPartie = null
                                }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { navState = HState.Months(s.year) },
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text("Retour")
                        }
                        Button(
                            onClick = onBack,
                            modifier = Modifier.padding(4.dp)
                        ) { Text("Accueil") }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionOnPartieDialog(
    partie: Partie,
    onSupprimer: (Partie) -> Unit,
    onReprendre: (Partie) -> Unit,
    onStatistiques: (Partie) -> Unit,
    onRetour: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { onReprendre(partie) }) {
        Surface(
            color = Color.Black,
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Actions sur la partie",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                CompositionLocalProvider(LocalContentColor provides Color.White) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onReprendre(partie) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Reprendre") }

                        Button(
                            onClick = { showConfirmDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Supprimer") }

                        Button(
                            onClick = { onStatistiques(partie) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Statistiques") }

                        TextButton(
                            onClick = onRetour,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Retour") }
                    }
                }
            }
        }
    }

    if (showConfirmDialog) {
        ConfirmDeleteDialog(
            onConfirm = {
                showConfirmDialog = false
                onSupprimer(partie)
            },
            onDismiss = { showConfirmDialog = false }
        )
    }
}

@Composable
fun ConfirmDeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Color.Black,
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Supprimer la partie",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) { Text("Non") }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f)
                    ) { Text("Oui") }
                }
            }
        }
    }
}