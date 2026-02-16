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

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID

@Composable
fun JoueursScreen(
    joueurs: List<Joueur>,
    historique: Historique?,
    modeTriActuel: Int = 0, // AJOUTER CE PARAMÈTRE
    onUpdateModeTriActuel: (Int) -> Unit = {}, // AJOUTER CE CALLBACK
    onUpdateJoueurs: (List<Joueur>) -> Unit,
    onRetour: () -> Unit,
    onStatistiquesPartieJoueur: (Joueur) -> Unit,
    onContinuer: () -> Unit
) {
    val joueursAvecHistorique = remember(historique) {
        val ids = mutableSetOf<String>()
        historique?.parties?.forEach { partie ->
            ids.addAll(partie.joueurs.map { it.id })
        }
        ids
    }

    // Calculer les statistiques une seule fois
    val statistiques = remember(historique) {
        if (historique != null) {
            AnalyseurHistorique().analyser(historique).first
        } else {
            emptyMap()
        }
    }

    // SUPPRIMER CETTE LIGNE : var modeTriActuel by remember { mutableStateOf(0) }
    // modeTriActuel vient maintenant des paramètres

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedJoueur: Joueur? by remember { mutableStateOf(null) }
    var editNom by remember { mutableStateOf(TextFieldValue("")) }
    var newNom by remember { mutableStateOf(TextFieldValue("")) }

    // Fonction de tri
    val joueursTries = remember(joueurs, modeTriActuel, statistiques) {
        when (modeTriActuel) {
            0 -> joueurs // Chronologique
            1 -> joueurs.sortedBy { it.nomUI.lowercase() } // A-Z
            2 -> joueurs.sortedByDescending { it.nomUI.lowercase() } // Z-A
            3 -> { // Gain
                // Trier TOUS les joueurs, ceux avec stats en premier, ceux sans à la fin
                joueurs.sortedByDescending { joueur ->
                    val stats = statistiques[joueur.id]
                    if (stats != null && joueursAvecHistorique.contains(joueur.id)) {
                        // Somme des gainNet de toutes les configurations (3, 4, 5 joueurs)
                        stats.parties.values.sumOf { it.pointsGagnes + it.pointsPerdus}
                    } else {
                        Int.MIN_VALUE // Joueurs sans stats à la fin
                    }
                }
            }
            else -> joueurs
        }
    }

    // Libellé du tri actuel
    val libelleTriActuel = when (modeTriActuel) {
        0 -> "Chronologique"
        1 -> "A-Z"
        2 -> "Z-A"
        3 -> "Gain"
        else -> "Chronologique"
    }

    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(top = 24.dp)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Titre
            Text(
                text = "Gestion des joueurs",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Indicateur de tri
            Text(
                text = "Tri : $libelleTriActuel",
                fontSize = 16.sp,
                color = Color(0xFFBBBBBB),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(joueursTries) { joueur ->
                    val aHistorique = joueursAvecHistorique.contains(joueur.id)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFF333333),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = joueur.nomUI.take(20),
                            fontSize = 18.sp,
                            color = if (aHistorique) Color.White else Color.Gray,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedJoueur = joueur
                                    editNom = TextFieldValue(joueur.nomUI)
                                    showEditDialog = true
                                }
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onStatistiquesPartieJoueur(joueur) },
                                enabled = aHistorique,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = "Statistiques",
                                    tint = if (aHistorique) MaterialTheme.colorScheme.primary else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    selectedJoueur = joueur
                                    editNom = TextFieldValue(joueur.nomUI)
                                    showEditDialog = true
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = "Editer",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    selectedJoueur = joueur
                                    showDeleteDialog = true
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Supprimer",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Boutons du bas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRetour,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(contentColor = Color.White)
                ) {
                    Text("Retour", color = Color.Black)
                }

                Button(
                    onClick = {
                        onUpdateModeTriActuel((modeTriActuel + 1) % 4) // MODIFIER ICI
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("Trier", color = Color.Black)
                }

                Button(
                    onClick = {
                        newNom = TextFieldValue("")
                        showAddDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Green,
                        contentColor = Color.White
                    )
                ) {
                    Text("Ajouter", color = Color.Black)
                }
            }
        }
    }

    // ... (les dialogs restent identiques)

    if (showEditDialog && selectedJoueur != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                TextField(
                    value = editNom,
                    onValueChange = { editNom = it },
                    singleLine = true,
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                    label = { Text("Nom du joueur") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Annuler")
                    }

                    TextButton(
                        onClick = {
                            val raw = editNom.text.trim()
                            if (raw.isNotEmpty() && selectedJoueur != null) {
                                val formatted = formatNomJoueur(raw)
                                val updated =
                                    joueurs.map { j -> if (j == selectedJoueur) j.copy(nomUI = formatted) else j }
                                onUpdateJoueurs(updated)
                            }
                            showEditDialog = false
                        },
                        enabled = editNom.text.trim().isNotEmpty()
                    ) {
                        Text("Valider")
                    }
                }
            },
            dismissButton = {}
        )
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nouveau joueur") },
            text = {
                TextField(
                    value = newNom,
                    onValueChange = { newNom = it },
                    singleLine = true,
                    label = { Text("Nom du joueur") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Annuler")
                    }

                    TextButton(
                        onClick = {
                            val raw = newNom.text.trim()
                            if (raw.isNotEmpty()) {
                                val formatted = formatNomJoueur(raw)
                                val nouveau =
                                    Joueur(nomUI = formatted, id = UUID.randomUUID().toString())
                                val updated = joueurs + nouveau
                                onUpdateJoueurs(updated)
                            }
                            showAddDialog = false
                        },
                        enabled = newNom.text.trim().isNotEmpty()
                    ) {
                        Text("Valider")
                    }
                }
            },
            dismissButton = {}
        )
    }

    if (showDeleteDialog && selectedJoueur != null) {
        val joueurASupprimer = joueurs.find { it == selectedJoueur }

        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmer la suppression") },
            text = { Text("\"${joueurASupprimer?.nomUI}\" sera supprimé") },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Annuler")
                    }

                    TextButton(
                        onClick = {
                            selectedJoueur?.let { joueur ->
                                val updated = joueurs.filter { it != joueur }
                                onUpdateJoueurs(updated)
                            }
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Supprimer")
                    }
                }
            },
            dismissButton = {}
        )
    }
}