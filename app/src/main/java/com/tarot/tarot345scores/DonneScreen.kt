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

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.UUID
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.*

@Composable
fun DonneScreen(
    joueurs: List<Joueur>,
    constantes: ConstantesConfig,
    onScoresSubmit: (List<Int>) -> Unit,
    onCancel: () -> Unit,
    donneToEdit: Donne? = null,
    onDonneSubmit: ((Donne) -> Unit)? = null,
    isReadOnly: Boolean = false
) {
    val nbJoueurs = joueurs.size

    // -- Indices pour preneur / appele (Int? pour nullable)
    var preneurIndex by rememberSaveable { mutableStateOf(donneToEdit?.preneurIndex) }
    var appeleIndex by rememberSaveable { mutableStateOf(donneToEdit?.appeleIndex) }

    LaunchedEffect(nbJoueurs) {
        if (nbJoueurs != 5) appeleIndex = null
    }

    // Chelem UI mapped to enum as before
    var chelemUIType by rememberSaveable {
        mutableStateOf(
            donneToEdit?.chelem?.let { c ->
                when {
                    c.annonce && c.succes -> ChelemUIType.ANNONCE_GAGNE
                    c.annonce && !c.succes -> ChelemUIType.ANNONCE_PERDU
                    !c.annonce && c.succes -> ChelemUIType.NON_ANNONCE
                    else -> ChelemUIType.AUCUN
                }
            } ?: ChelemUIType.AUCUN
        )
    }

    // Points attaque / défense (sauvegardés)
    var pointsAtq by rememberSaveable { mutableStateOf(donneToEdit?.pointsAtq ?: 41) }
    var pointsDef by rememberSaveable { mutableStateOf(91 - pointsAtq) }

    // Bouts attaque / défense
    var nbBoutsAttaque by rememberSaveable { mutableStateOf(donneToEdit?.nbBoutsAttaque ?: 2) }
    var defenceBoutsCount by rememberSaveable { mutableStateOf(3 - nbBoutsAttaque) }

    // Misères : set of indices
    val misereSelections = remember(joueurs, donneToEdit) {
        mutableStateMapOf<Int, Boolean>().apply {
            joueurs.indices.forEach { idx ->
                put(idx, donneToEdit?.miseres?.contains(idx) == true)
            }
        }
    }

    val poignees = remember(donneToEdit, joueurs) {
        mutableStateListOf<Poignee>().apply {
            addAll(
                joueurs.indices.map { index ->
                    val existing = donneToEdit?.poignees?.find { it.index == index }
                    val poigneeType = existing?.type ?: PoigneeType.NONE
                    Poignee(index = index, type = poigneeType)
                }
            )
        }
    }
    // Contrat
    var contratBase by rememberSaveable {
        mutableStateOf(donneToEdit?.contratIndex?.let { ContratType.entries.getOrNull(it) }
            ?: ContratType.PETITE)
    }

    // Dialog visibility
    var showPreneurDialog by remember { mutableStateOf(false) }
    var showAppelleDialog by remember { mutableStateOf(false) }
    var showContratDialog by remember { mutableStateOf(false) }
    var showChelemDialog by remember { mutableStateOf(false) }
    var showMisereDialog by remember { mutableStateOf(false) }
    var showPetitAuBoutDialog by remember { mutableStateOf(false) }
    var showPoigneesDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // UI helpers: convert index -> name for display

    val preneurDisplay: String =
        preneurIndex?.let { joueurs.getOrNull(it)?.nomUI } ?: "Non sélectionné"
    val appeleDisplay: String =
        appeleIndex?.let { joueurs.getOrNull(it)?.nomUI } ?: "Non sélectionné"

    val misereSelectedIndices by remember { derivedStateOf { misereSelections.filterValues { it }.keys.toList() } }
    val misereDisplay =
        if (misereSelectedIndices.isNotEmpty())
            misereSelectedIndices
                .mapNotNull { joueurs.getOrNull(it)?.nomUI }
                .joinToString(", ")
                .ifEmpty { "Non sélectionné" }
        else "Non sélectionné"

    val poigneesDisplay = poignees.filter { it.type != PoigneeType.NONE }
        .joinToString { "${joueurs.getOrNull(it.index)?.nomUI ?: "?"}: ${it.type.displayName}" }
        .ifBlank { "Non déclarées" }

    var petitAuBoutSelection by remember { mutableStateOf<PetitAuBout?>(donneToEdit?.petitAuBout) }
    var petitAuBoutIndex by rememberSaveable { mutableStateOf(donneToEdit?.petitAuBout?.index) }
    val petitAuBoutDisplay =
        petitAuBoutIndex?.let { joueurs.getOrNull(it)?.nomUI } ?: "Non sélectionné"

    // Layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {


        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            ButtonWithResponse("Choisir un Preneur", preneurDisplay) { showPreneurDialog = true }

            if (nbJoueurs == 5) {
                ButtonWithResponse("Choisir un Appelé", appeleDisplay) { showAppelleDialog = true }
            }

            ButtonWithResponse("Choisir un contrat", contratBase.displayName) {
                showContratDialog = true
            }

            ButtonWithResponse("Petit au bout", petitAuBoutDisplay) { showPetitAuBoutDialog = true }

            ButtonWithResponse("Poignées", poigneesDisplay) { showPoigneesDialog = true }

            ButtonWithResponse("Chelem", chelemUIType.displayName) { showChelemDialog = true }

            ButtonWithResponse("Misère", misereDisplay) { showMisereDialog = true }


            NbBoutSelection(
                nbBoutsAttaque = nbBoutsAttaque,
                defenceBoutsCount = defenceBoutsCount,
                onAtqBoutChange = { newAtq ->
                    nbBoutsAttaque = newAtq
                    defenceBoutsCount = 3 - newAtq
                },
                onDefBoutChange = { newDef ->
                    defenceBoutsCount = newDef
                    nbBoutsAttaque = 3 - newDef
                }
            )

            PointsNumberPickers(
                pointsAtq = pointsAtq,
                pointsDef = pointsDef,
                nbBoutsAttaque = nbBoutsAttaque,
                constantes = constantes,
                onPointsAtqChange = { newAtq ->
                    pointsAtq = newAtq
                    pointsDef = 91 - newAtq
                },
                onPointsDefChange = { newDef ->
                    pointsDef = newDef
                    pointsAtq = 91 - newDef
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = if (isReadOnly) 0.dp else 8.dp)
                ) { Text("Retour") }

                if (!isReadOnly) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    ) { Text("Soumettre") }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // -- Dialogs (assume dialog components can work with indices; adapt if they return names)
    if (showPreneurDialog) {
        // joueurIndexSelectionDialog : should return Int (index) or null on dismiss
        joueurIndexSelectionDialog(
            title = "Choisir un Preneur",
            joueurs = joueurs,
            currentIndex = preneurIndex,
            onIndexSelected = { idx ->
                preneurIndex = idx
                showPreneurDialog = false
            },
            onDismiss = { showPreneurDialog = false }
        )
    }

    if (showAppelleDialog) {
        joueurIndexSelectionDialog(
            title = "Choisir un Appelé",
            joueurs = joueurs,
            currentIndex = appeleIndex,
            onIndexSelected = { idx ->
                appeleIndex = idx
                showAppelleDialog = false
            },
            onDismiss = { showAppelleDialog = false }
        )
    }

    if (showContratDialog) {
        ContratSelectionDialog(
            current = contratBase,
            onSelected = { newValue ->
                contratBase = newValue
                showContratDialog = false
            },
            onDismiss = { showContratDialog = false }
        )
    }

    if (showChelemDialog) {
        ChelemSelectionDialog(
            current = chelemUIType,
            onSelected = { newValue ->
                chelemUIType = newValue
                showChelemDialog = false
            },
            onDismiss = { showChelemDialog = false }
        )
    }

    if (showMisereDialog) {
        MisereSelectionDialogIndices(
            joueurs = joueurs,
            initialSelections = misereSelections.mapValues { it.value },
            onFinish = { selectedIndices ->
                // reset all then apply
                joueurs.indices.forEach { misereSelections[it] = false }
                selectedIndices.forEach { misereSelections[it] = true }
                showMisereDialog = false
            },
            onDismiss = { showMisereDialog = false }
        )
    }

    if (showPetitAuBoutDialog) {
        PetitAuBoutDialogIndex(
            joueurs = joueurs,
            currentIndex = petitAuBoutIndex,
            onIndexSelected = { idx ->
                petitAuBoutIndex = idx
                petitAuBoutSelection = idx?.let { PetitAuBout(index = it) }
                showPetitAuBoutDialog = false
            },
            onDismiss = { showPetitAuBoutDialog = false }
        )
    }

    if (showPoigneesDialog) {
        PoigneesDialog(
            joueurs = joueurs,
            constantes = constantes,
            initial = poignees.toList(),
            onFinish = { list ->
                poignees.clear()
                poignees.addAll(list)
                showPoigneesDialog = false
            },
            onDismiss = { showPoigneesDialog = false }
        )
    }

    if (showConfirmDialog) {
        // Build Donne or scores for submit. Convert UI state (indices, pairs) into Donne model.
        ConfirmDialogIndex(
            joueurs = joueurs,
            nbJoueurs = nbJoueurs,
            preneurIndex = preneurIndex,
            appeleIndex = appeleIndex,
            contratBase = contratBase,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            chelemUIType = chelemUIType,
            misereSelectedIndices = misereSelectedIndices,
            poignees = poignees,
            petitAuBoutSelection = petitAuBoutSelection,
            constantes = constantes,
            donneToEdit = donneToEdit,
            onDonneSubmit = onDonneSubmit,
            onScoresSubmit = onScoresSubmit,
            onDismiss = { showConfirmDialog = false }
        )
    }
}
/* ============================================================
   Composants réutilisables
   =========================================================== */

@Composable
fun InlineBox(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .border(BorderStroke(1.dp, Color.White), shape = RoundedCornerShape(6.dp))
            .padding(8.dp)
            .background(Color.Transparent)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            content()
        }
    }
}

@Composable
fun ButtonWithResponse(buttonText: String, responseText: String?, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .border(BorderStroke(1.dp, Color.White), shape = RoundedCornerShape(4.dp))
            .padding(12.dp)
            .fillMaxWidth()
    ) {
        Button(onClick = onClick) { Text(buttonText) }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = responseText ?: "Non sélectionné", color = Color.White, fontSize = 18.sp)
    }
}

@Composable
fun <T> RadioRow(
    label: String,
    value: T,
    selected: T,
    onSelect: (T) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = (value == selected),
                onClick = { onSelect(value) },
                role = Role.RadioButton
            )
            .padding(vertical = 4.dp)
    ) {
        RadioButton(
            selected = (value == selected),
            onClick = null
        )
        Spacer(Modifier.width(8.dp))
        Text(label, color = Color.White)
    }
}

@Composable
fun ContratRadio(
    label: String,
    value: ContratType,
    selected: ContratType,
    onSelect: (ContratType) -> Unit
) {
    RadioRow(
        label = label,
        value = value,
        selected = selected,
        onSelect = onSelect
    )
}

@Composable
fun ChelemRadio(
    label: String,
    value: ChelemUIType,
    selected: ChelemUIType,
    onSelect: (ChelemUIType) -> Unit
) {
    RadioRow(
        label = label,
        value = value,
        selected = selected,
        onSelect = onSelect
    )
}

@Composable
fun joueurIndexSelectionDialog(
    title: String,
    joueurs: List<Joueur>,
    currentIndex: Int?,
    onIndexSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(12.dp),
            color = Color.Black, // conservé
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 48.dp)
                .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = title, fontSize = 18.sp, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))

                joueurs.forEachIndexed { index, joueur ->
                    val selected = index == currentIndex
                    Button(
                        onClick = { onIndexSelected(index) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) Color.DarkGray else Color.Black,
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color.White)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = joueur.nomUI,
                                modifier = Modifier.weight(1f),
                                color = Color.White
                            )
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Green
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Fermer", color = Color.White)

                    }
                }
            }
        }
    }
}

@Composable
fun ContratSelectionDialog(
    current: ContratType,
    onSelected: (ContratType) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(12.dp),
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 48.dp)
                .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Choisir un contrat",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 18.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))

                val options = listOf(
                    ContratType.PETITE to "Petite",
                    ContratType.GARDE to "Garde",
                    ContratType.GARDE_SANS to "Garde Sans",
                    ContratType.GARDE_CONTRE to "Garde Contre"
                )

                options.forEach { (value, label) ->
                    val isSelected = current == value
                    Button(
                        onClick = { onSelected(value) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color.DarkGray else Color.Black,
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color.White)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.weight(1f),
                                color = Color.White
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Green
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Fermer", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PetitAuBoutDialogIndex(
    joueurs: List<Joueur>,
    currentIndex: Int?,
    onIndexSelected: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    var localIndex by remember { mutableStateOf(currentIndex) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(12.dp),
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 48.dp)
                .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Qui a fait le pli avec le petit au bout ?",
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))

                joueurs.forEachIndexed { index, joueur ->
                    val isSelected = localIndex == index
                    Button(
                        onClick = {
                            val newIndex = if (isSelected) null else index
                            localIndex = newIndex
                            onIndexSelected(newIndex)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color.DarkGray else Color.Black,
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color.White)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = joueur.nomUI,
                                modifier = Modifier.weight(1f),
                                color = Color.White
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Green
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Fermer", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PoigneesDialog(
    joueurs: List<Joueur>,
    constantes: ConstantesConfig,
    initial: List<Poignee> = emptyList(),
    onFinish: (List<Poignee>) -> Unit,
    onDismiss: () -> Unit
) {
    val nbJoueurs = joueurs.size
    val selectionsPoignees = remember {
        mutableStateListOf<Poignee>().apply {
            addAll(
                joueurs.indices.map { index ->
                    initial.find { it.index == index } ?: Poignee(
                        index = index,
                        type = PoigneeType.NONE
                    )
                }
            )
        }
    }

    val states = PoigneeType.entries
    val poigneeAtouts: Map<String, Int> =
        (constantes.poignee_atouts[nbJoueurs.toString()] as? Map<String, Int>) ?: emptyMap()

    val total = remember(selectionsPoignees.toList()) {
        selectionsPoignees.sumOf { poignee ->
            if (poignee.type == PoigneeType.NONE) 0
            else poigneeAtouts[poignee.type.name] ?: 0
        }
    }

    val maxAllowed = 22
    val overLimit = total > maxAllowed

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(12.dp),
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 48.dp)
                .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Déclarer les poignées",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 18.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))

                joueurs.forEachIndexed { index, joueur ->
                    val currentType =
                        selectionsPoignees.find { it.index == index }?.type ?: PoigneeType.NONE
                    val currentIndex = states.indexOf(currentType).coerceAtLeast(0)
                    val nextIndex = (currentIndex + 1) % states.size
                    val isSelected = currentType != PoigneeType.NONE

                    Button(
                        onClick = {
                            val poigneeIndex = selectionsPoignees.indexOfFirst { it.index == index }
                            if (poigneeIndex != -1) {
                                selectionsPoignees[poigneeIndex] =
                                    Poignee(index = index, type = states[nextIndex])
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color.DarkGray else Color.Black,
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color.White)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = joueur.nomUI.take(20),
                                modifier = Modifier.weight(1f),
                                color = Color.White
                            )
                            Text(
                                text = if (currentType == PoigneeType.NONE) "—" else currentType.displayName,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                if (overLimit) {
                    Text(
                        text = "Total: $total / $maxAllowed atouts",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { onFinish(selectionsPoignees.toList()) },
                        enabled = total <= maxAllowed
                    ) {
                        Text("Fermer", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ChelemSelectionDialog(
    current: ChelemUIType,
    onSelected: (ChelemUIType) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(12.dp),
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 48.dp)
                .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Type de chelem",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 18.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))

                val options = listOf(
                    ChelemUIType.AUCUN to "Aucun chelem",
                    ChelemUIType.ANNONCE_GAGNE to "Chelem annoncé gagné",
                    ChelemUIType.ANNONCE_PERDU to "Chelem annoncé perdu",
                    ChelemUIType.NON_ANNONCE to "Chelem non annoncé"
                )

                options.forEach { (value, label) ->
                    val isSelected = current == value
                    Button(
                        onClick = { onSelected(value) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color.DarkGray else Color.Black,
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color.White)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.weight(1f),
                                color = Color.White
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Green
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Fermer", color = Color.White)
                    }
                }
            }
        }
    }
}


@Composable
fun NbBoutSelection(
    nbBoutsAttaque: Int,
    defenceBoutsCount: Int,
    onAtqBoutChange: (Int) -> Unit,
    onDefBoutChange: (Int) -> Unit,
) {
    InlineBox(title = "Nombre de bouts à l'attaque", modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            (0..3).forEach { value ->
                val isSelected = value == nbBoutsAttaque
                Button(
                    onClick = { onAtqBoutChange(value) },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .size(64.dp)
                        .padding(horizontal = 12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = value.toString(),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp,           // number fills the button
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun PointsNumberPickers(
    pointsAtq: Int,
    pointsDef: Int,
    onPointsAtqChange: (Int) -> Unit,
    onPointsDefChange: (Int) -> Unit,
    nbBoutsAttaque: Int,
    constantes: ConstantesConfig
) {
    val totalMax = 91

    var atq by remember { mutableStateOf(pointsAtq.coerceIn(0, totalMax)) }
    var def by remember { mutableStateOf(pointsDef.coerceIn(0, totalMax)) }

    LaunchedEffect(pointsAtq) {
        if (pointsAtq != atq) {
            atq = pointsAtq.coerceIn(0, totalMax)
            def = totalMax - atq
        }
    }
    LaunchedEffect(pointsDef) {
        if (pointsDef != def) {
            def = pointsDef.coerceIn(0, totalMax)
            atq = totalMax - def
        }
    }
    LaunchedEffect(atq) {
        onPointsAtqChange(atq)
        onPointsDefChange(totalMax - atq)
    }

    val seuil = constantes.seuils_bouts.getOrNull(nbBoutsAttaque)
        ?: constantes.seuils_bouts.firstOrNull()
        ?: 0
    val value = atq - seuil
    val absValue = kotlin.math.abs(value)

    Surface(
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(12.dp),
        color = Color.Black,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 4.dp)
            .border(1.dp, Color.White, RoundedCornerShape(6.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Attaque column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Attaque", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (atq > 0) atq -= 1 }, enabled = atq > 0) {
                            Text("-", color = if (atq > 0) Color.White else Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = atq.toString(), color = Color.White, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { if (atq < totalMax) atq += 1 },
                            enabled = atq < totalMax
                        ) {
                            Text("+", color = if (atq < totalMax) Color.White else Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Défense column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Défense", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (atq > 0) atq -= 1 }, // défense + => attaque -
                            enabled = atq > 0
                        ) {
                            Text("+", color = if (atq > 0) Color.White else Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = (totalMax - atq).toString(),
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { if (atq < totalMax) atq += 1 }, // défense - => attaque +
                            enabled = atq < totalMax
                        ) {
                            Text("-", color = if (atq < totalMax) Color.White else Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Slider(
                value = atq.toFloat(),
                onValueChange = { newValue ->
                    val intVal = newValue.toInt()
                    if (intVal != atq) atq = intVal.coerceIn(0, totalMax)
                },
                valueRange = 0f..totalMax.toFloat(),
                steps = totalMax - 1,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (value >= 0) {
                    Text(
                        text = "La partie est faite de $absValue points",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        text = "La partie est chutée de $absValue points",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}


@Composable
fun MisereSelectionDialogIndices(
    joueurs: List<Joueur>,
    initialSelections: Map<Int, Boolean>,
    onFinish: (List<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    // copie locale pour l'UI
    val selections = remember {
        mutableStateMapOf<Int, Boolean>().apply {
            joueurs.indices.forEach { put(it, initialSelections[it] == true) }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(12.dp),
            color = Color.Black, // conservé
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 48.dp)
                .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Sélectionner les joueurs pour la Misère",
                    fontSize = 18.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))

                joueurs.forEachIndexed { index, joueur ->
                    val selected = selections[index] == true

                    Surface(
                        color = if (selected) Color.DarkGray else Color.Black,
                        shape = RoundedCornerShape(percent = 50), // <- arrondi 50%
                        border = BorderStroke(1.dp, Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selections[index] = !selected }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = joueur.nomUI,
                                modifier = Modifier.weight(1f),
                                color = Color.White
                            )

                            if (selected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Green
                                )
                            } else {
                                Spacer(modifier = Modifier.size(24.dp)) // garde l'alignement
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Annuler", color = Color.White) }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        val selectedIndices = selections.filterValues { it }.keys.sorted()
                        onFinish(selectedIndices.toList())
                    }) { Text("Valider", color = Color.White) }
                }
            }
        }
    }
}

@Composable
fun ConfirmDialogIndex(
    joueurs: List<Joueur>,
    nbJoueurs: Int,
    preneurIndex: Int?,
    appeleIndex: Int?,
    contratBase: ContratType,
    pointsAtq: Int,
    nbBoutsAttaque: Int,
    chelemUIType: ChelemUIType,
    misereSelectedIndices: List<Int>,
    poignees: List<Poignee>,
    petitAuBoutSelection: PetitAuBout?,
    constantes: ConstantesConfig,
    donneToEdit: Donne?,
    onDonneSubmit: ((Donne) -> Unit)?,
    onScoresSubmit: (List<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Confirmer la donne", color = Color.White)
            }
        },
        text = {
            val preneurName = preneurIndex?.let { joueurs.getOrNull(it)?.nomUI } ?: "—"
            val appeleName = appeleIndex?.let { joueurs.getOrNull(it)?.nomUI } ?: "—"
            val contratStr = contratBase.displayName
            val pts = pointsAtq
            val nbBouts = nbBoutsAttaque

            val seuil =
                constantes.seuils_bouts.getOrElse(nbBouts) { constantes.seuils_bouts.first() }

            val delta = pointsAtq - seuil
            val attaqueGagne = delta >= 0

            val resultatTexte = if (attaqueGagne) {
                "La partie est faite de $delta point(s)."
            } else {
                "La partie est chutée de $delta point(s)."
            }

            val petitAuBoutText = petitAuBoutSelection?.let { result ->
                val index = result.index
                if (index !in joueurs.indices) {
                    "Index invalide"
                } else {
                    val message = if (nbJoueurs == 5) {
                        if (index == preneurIndex || index == appeleIndex) {
                            "Le petit au bout est à l'attaque"
                        } else {
                            "Le petit au bout est à la défense"
                        }
                    } else {
                        if (index == preneurIndex) {
                            "Le petit au bout est à l'attaque"
                        } else {
                            "Le petit au bout est à la défense"
                        }
                    }
                    message
                }
            } ?: "Personne n'a mené le petit au bout"


            val nonEmpty = poignees.filter { it.type != PoigneeType.NONE }
            val poigneesText: String = when {
                nonEmpty.isEmpty() -> "Poignée: Aucune"
                nonEmpty.size == 1 -> {
                    val poignee = nonEmpty.first()
                    val nom = joueurs.getOrNull(poignee.index)?.nomUI ?: "?"
                    "Poignée: $nom:${poignee.type}"
                }
                else -> {
                    val items = nonEmpty.joinToString(", ") {
                        "${joueurs.getOrNull(it.index)?.nomUI ?: "?"}:${it.type}"
                    }
                    "Poignées: $items"
                }
            }

            val chelemTexte = when (chelemUIType) {
                ChelemUIType.AUCUN -> "Aucun chelem"
                ChelemUIType.ANNONCE_GAGNE -> "Chelem annoncé réussi"
                ChelemUIType.ANNONCE_PERDU -> "Chelem annoncé raté"
                ChelemUIType.NON_ANNONCE -> "Chelem non annoncé réussi"
            }

            val misereNames = misereSelectedIndices.mapNotNull { joueurs.getOrNull(it)?.nomUI }
            val miseresText =
                if (misereNames.isEmpty()) "Misère: Personne"
                else if (misereNames.size == 1) "Misère: ${misereNames.first()}"
                else "Misères: ${misereNames.joinToString(", ")}"

            Column {
                if (nbJoueurs == 5) {
                    Text(
                        "$preneurName a appelé $appeleName pour réaliser une $contratStr avec $pts points et $nbBouts bout(s).",
                        color = Color.White
                    )
                } else {
                    Text(
                        "$preneurName réalise une $contratStr avec $pts points et $nbBouts bout(s).",
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(resultatTexte, color = Color.White)

                Spacer(modifier = Modifier.height(8.dp))
                Text(petitAuBoutText, color = Color.White)

                Spacer(modifier = Modifier.height(8.dp))
                Text(poigneesText, color = Color.White)


                Spacer(modifier = Modifier.height(8.dp))
                Text("Chelem : $chelemTexte", color = Color.White)

                Spacer(modifier = Modifier.height(8.dp))
                Text(miseresText, color = Color.White)
            }
        },
        confirmButton = {
            val isButtonEnabled = if (nbJoueurs == 5) {
                preneurIndex != null && appeleIndex != null
            } else {
                preneurIndex != null
            }

            Button(
                onClick = {
                    val contratNom = contratBase.displayName
                    val contratIndex = constantes.multiplicateurs.keys.indexOf(contratNom)

                    val chelemArg: Chelem? = when (chelemUIType) {
                        ChelemUIType.AUCUN -> null
                        ChelemUIType.ANNONCE_GAGNE -> Chelem(annonce = true, succes = true)
                        ChelemUIType.ANNONCE_PERDU -> Chelem(annonce = true, succes = false)
                        ChelemUIType.NON_ANNONCE -> Chelem(annonce = false, succes = true)
                    }

                    val miseresIndex = misereSelectedIndices
                    val donneId = donneToEdit?.id ?: UUID.randomUUID().toString()
                    val createdAt = donneToEdit?.createdAt ?: System.currentTimeMillis()

                    val prIdx = preneurIndex!!
                    val apIdx = appeleIndex

                    val scores = calculerScores(
                        joueurs = joueurs,
                        preneurIndex = prIdx,
                        appeleIndex = apIdx,
                        contratNom = contratNom,
                        pointsAtq = pointsAtq,
                        nbBoutsAttaque = nbBoutsAttaque,
                        miseresIndex = miseresIndex,
                        petitAuBout = petitAuBoutSelection,
                        poignees = poignees,
                        chelem = chelemArg,
                        constantes = constantes,
                    )

                    val newDonne = Donne(
                        id = donneId,
                        createdAt = createdAt,
                        preneurIndex = prIdx,
                        appeleIndex = apIdx,
                        contratIndex = contratIndex,
                        pointsAtq = pointsAtq,
                        nbBoutsAttaque = nbBoutsAttaque,
                        petitAuBout = petitAuBoutSelection,
                        poignees = poignees,
                        miseres = miseresIndex,
                        chelem = chelemArg,
                        scores = scores
                    )

                    if (onDonneSubmit != null) {
                        onDonneSubmit(newDonne)
                    } else {
                        onScoresSubmit(scores)
                    }
                    onDismiss()
                },
                enabled = isButtonEnabled,
                colors = ButtonDefaults.buttonColors(
                    disabledContentColor = Color.LightGray,
                    //containerColor = Color.Black,
                    //contentColor = Color.White,
                ),
                border = BorderStroke(1.dp, Color.White)
            ) {
                Text("Valider", color = Color.Black)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    //containerColor = Color.Black,
                    contentColor = Color.Black
                ),
                border = BorderStroke(1.dp, Color.White)
            ) {
                Text("Annuler", color = Color.Black)
            }
        },
        containerColor = Color.Black,
        modifier = Modifier.border(1.dp, Color.White, RoundedCornerShape(12.dp))
    )
}