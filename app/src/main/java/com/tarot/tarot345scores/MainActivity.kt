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

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import java.util.UUID
import androidx.compose.foundation.background
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.rememberUpdatedState
import com.tarot.tarot345scores.ui.theme.Tarot345ScoresTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.font.FontWeight

sealed class Screen {
    object Accueil : Screen()
    object Jouer : Screen()
    object Joueurs : Screen()
    object Jeux : Screen()
    object Donne : Screen()
    object Historique : Screen()
    object Apropos : Screen()
    object StatistiquesGlobales : Screen()
    object StatistiquesJoueurs : Screen()
    object StatistiquesPartie : Screen()
    object Constantes : Screen()
    object Calculs : Screen()
}

val ScreenSaver = Saver<Screen, String>(
    save = { screen ->
        when (screen) {
            is Screen.Accueil -> "Accueil"
            is Screen.Jouer -> "Jouer"
            is Screen.Joueurs -> "Joueurs"
            is Screen.Jeux -> "Jeux"
            is Screen.Donne -> "Donne"
            is Screen.Historique -> "Historique"
            is Screen.Apropos -> "A propos"
            is Screen.StatistiquesGlobales -> "StatistiquesGlobales"
            is Screen.StatistiquesJoueurs -> "StatistiquesJoueurs"
            is Screen.StatistiquesPartie -> "StatistiquesPartie"
            is Screen.Constantes -> "Constantes"
            is Screen.Calculs -> "Calculs"
        }
    },
    restore = { name ->
        when (name) {
            "Accueil" -> Screen.Accueil
            "Jouer" -> Screen.Jouer
            "Joueurs" -> Screen.Joueurs
            "Jeux" -> Screen.Jeux
            "Donne" -> Screen.Donne
            "Historique" -> Screen.Historique
            "A propos" -> Screen.Apropos
            "StatistiquesGlobales" -> Screen.StatistiquesGlobales
            "StatistiquesJoueurs" -> Screen.StatistiquesJoueurs
            "StatistiquesPartie" -> Screen.StatistiquesPartie
            "Constantes" -> Screen.Constantes
            "Calculs" -> Screen.Calculs
            else -> throw IllegalArgumentException("Unknown screen")
        }
    }
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Masquer les barres système
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.apply {
            // Masquer la barre de navigation
            hide(WindowInsetsCompat.Type.navigationBars())
            // Comportement immersif : les barres réapparaissent au swipe et se cachent automatiquement
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val constantes = loadConstantesFromAssets(this, "constantes.json")

        setContent {
            val viewModel: MainViewModel = viewModel()
            val joueurs by viewModel.joueurs.collectAsState()
            val historique by viewModel.historique.collectAsState()

            // Pour générer aléatoirement un fichier Historique.json pour les tests
            fakeHistorique(constantes, viewModel, filesDir, 1000)

            Tarot345ScoresTheme(dynamicColor = true) {
                Tarot345ScoresApp(constantes, joueurs = joueurs, historique = historique, viewModel)
            }
        }
    }

    /**
     * Ouvre un PDF situé dans les assets (ex: "R-RO201206.pdf")
     * en le copiant dans le cache puis en lançant un Intent ACTION_VIEW.
     */
    fun openPdfFromAssets(fileName: String) {
        val context = this

        // 1. Copier le PDF des assets vers un fichier temporaire dans le cache
        val inputStream = context.assets.open(fileName)
        val tempFile = File.createTempFile("regles_fft_", ".pdf", context.cacheDir)
        tempFile.outputStream().use { output ->
            inputStream.copyTo(output)
        }

        // 2. Obtenir un Uri via FileProvider
        val pdfUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )

        // 3. Créer l'Intent pour ouvrir le PDF
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(pdfUri, "application/pdf")
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        // 4. Démarrer l'Activity (choix de l'app PDF)
        context.startActivity(
            Intent.createChooser(intent, "Ouvrir les règles FFT avec")
        )
    }
}

@Composable
fun Tarot345ScoresApp(
    constantes: ConstantesConfig,
    joueurs: List<Joueur>,
    historique: Historique?,
    viewModel: MainViewModel,
) {
    val context = LocalContext.current
    //val scope = rememberCoroutineScope()

    val historiqueState = rememberUpdatedState(historique)

    val isLoading by viewModel.isLoadingJoueurs.collectAsState()

    var isPartieEnCours by rememberSaveable { mutableStateOf(false) }

    var nbJoueurs by rememberSaveable { mutableStateOf(3) }

    var pendingDonneSubmit by remember { mutableStateOf<((Donne) -> Unit)?>(null) }
    var pendingDonneToEdit by rememberSaveable { mutableStateOf<Donne?>(null) }
    var pendingDonneReadOnly by rememberSaveable { mutableStateOf(false) }

    val joueursSelectionnes = rememberSaveable { mutableStateOf<List<Joueur>>(emptyList()) }

    var showDonneActionDialog by remember { mutableStateOf(false) }
    var selectedDonneForAction by remember { mutableStateOf<Donne?>(null) }

    var historiqueContext by remember { mutableStateOf<HistoriqueContext?>(null) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    var currentScreen by rememberSaveable(stateSaver = ScreenSaver) { mutableStateOf<Screen>(Screen.Accueil) }
    var showExitDialog by remember { mutableStateOf(false) }

    val historique by viewModel.historique.collectAsState()
    val allDonnes by viewModel.allDonnes.collectAsState()

    val lastJoueursForCalculs by viewModel.calculJoueurs.collectAsState()
    val lastDonneForCalculs by viewModel.calculDonne.collectAsState()

    var selectedPartieId by rememberSaveable { mutableStateOf<String?>(null) }

    var selectedJoueur by rememberSaveable { mutableStateOf<Joueur?>(null) }

    var modeTriJoueurs by remember { mutableStateOf(0) }

    when (currentScreen) {

        is Screen.Accueil -> AccueilScreen(
            joueurs = joueurs,
            isLoading = isLoading,
            onJouerClick = {
                if (joueurs.size < nbJoueurs) {
                    currentScreen = Screen.Joueurs
                } else {
                    isPartieEnCours = true
                    currentScreen = Screen.Jouer
                    viewModel.setCurrentPartie(null)
                }
            },
            onAproposClick = { currentScreen = Screen.Apropos },
            onJoueursClick = { currentScreen = Screen.Joueurs },
            onHistoriqueClick = { currentScreen = Screen.Historique },
            onStatistiquesClick = { currentScreen = Screen.StatistiquesGlobales },
            onConstantesClick = { currentScreen = Screen.Constantes },
            onReglesClick = { (context as? MainActivity)?.openPdfFromAssets("R-RO201206.pdf") },
            requiredNbJoueurs = nbJoueurs
        )

        is Screen.Jouer -> ChoixJoueursScreen(
            joueurs = joueurs,
            defaultNbJoueurs = nbJoueurs,
            onRetour = { currentScreen = Screen.Accueil },
            onNavigateToGame = { selectedPlayers, choixNbJoueurs ->
                nbJoueurs = choixNbJoueurs
                joueursSelectionnes.value = selectedPlayers
                viewModel.setCurrentPartie(null)
                currentScreen = Screen.Jeux
            }
        )

        is Screen.Apropos -> AproposScreen(
            onBack = {
                currentScreen = Screen.Accueil
            }
        )

        is Screen.Joueurs -> JoueursScreen(
            joueurs = joueurs,
            historique = historique,
            modeTriActuel = modeTriJoueurs,
            onUpdateModeTriActuel = { modeTriJoueurs = it },
            onUpdateJoueurs = viewModel::updateJoueurs,
            onRetour = {
                selectedPartieId = null
                currentScreen = Screen.Accueil
            },
            onStatistiquesJoueur = { joueur ->
                selectedJoueur = joueur
                selectedPartieId = null
                currentScreen = Screen.StatistiquesJoueurs
            },
            onContinuer = {
                isPartieEnCours = true
                currentScreen = Screen.Jouer
                viewModel.clearDonnes()
            }
        )

        is Screen.Jeux -> {
            val totals: List<Int> =
                remember(allDonnes, joueursSelectionnes) {
                    val n = joueursSelectionnes.value.size
                    val acc = MutableList(n) { 0 }
                    allDonnes.forEach { donne ->
                        donne.scores.let { scores ->
                            for (i in 0 until n) {
                                acc[i] += scores[i]
                            }
                        }
                    }
                    acc.toList()
                }


            JeuxScreen(
                joueurs = joueursSelectionnes.value,
                donnes = allDonnes,
                totals = totals,
                constantes = constantes,
                isReadOnly = !isPartieEnCours,
                modifier = Modifier.fillMaxSize(),
                onRetour = { showExitDialog = true },
                onRetourLectureSeule = {
                    currentScreen = Screen.StatistiquesPartie
                },
                onNouvelleDonne = {
                    if (isPartieEnCours) {
                        pendingDonneToEdit = null
                        pendingDonneReadOnly = false
                        pendingDonneSubmit = { newDonne ->
                            viewModel.saveDonne(
                                newDonne,
                                joueursSelectionnes.value.toList()
                            )
                        }
                        currentScreen = Screen.Donne
                    }
                },
                onEditDonne = { donneToEdit ->
                    if (isPartieEnCours) {
                        selectedDonneForAction = donneToEdit
                        showDonneActionDialog = true
                    }
                },
                onViewDonne = { donneToView ->
                    pendingDonneToEdit = donneToView
                    pendingDonneSubmit = null
                    pendingDonneReadOnly = true
                    currentScreen = Screen.Donne
                },
                onLongPressDonne = { donne ->
                    viewModel.setCalculData(joueurs = joueursSelectionnes.value, donne = donne)
                    currentScreen = Screen.Calculs
                },
                viewModel = viewModel
            )
        }

        is Screen.Donne -> {
            DonneScreen(
                joueurs = joueursSelectionnes.value,
                constantes = constantes,
                onScoresSubmit = { newScores ->
                    val donne = Donne(
                        id = UUID.randomUUID().toString(),
                        createdAt = System.currentTimeMillis(),
                        preneurIndex = 0,
                        appeleIndex = 0,
                        contratIndex = 0,
                        pointsAtq = 0,
                        nbBoutsAttaque = 0,
                        petitAuBout = null,
                        poignees = emptyList(),
                        miseres = emptyList(),
                        chelem = null,
                        scores = newScores,
                    )
                    viewModel.saveDonne(donne, joueursSelectionnes.value.toList())
                    pendingDonneToEdit = null
                    pendingDonneSubmit = null
                    pendingDonneReadOnly = false
                    currentScreen = Screen.Jeux
                },
                onCancel = {
                    pendingDonneReadOnly = false
                    pendingDonneToEdit = null
                    pendingDonneSubmit = null
                    currentScreen = Screen.Jeux
                },
                donneToEdit = pendingDonneToEdit,
                onDonneSubmit = { newDonne ->
                    val delegate = pendingDonneSubmit
                    if (delegate != null) {
                        delegate(newDonne)
                    } else {
                        viewModel.saveDonne(
                            newDonne,
                            joueursSelectionnes.value.toList()
                        )
                    }
                    pendingDonneToEdit = null
                    pendingDonneSubmit = null
                    pendingDonneReadOnly = false
                    currentScreen = Screen.Jeux
                },
                isReadOnly = pendingDonneReadOnly
            )
        }

        Screen.Historique -> {
            val joueursGlobal by viewModel.joueurs.collectAsState(initial = emptyList())
            HistoriqueScreen(
                historiqueState = historiqueState,
                onBack = {
                    historiqueContext = null
                    currentScreen = Screen.Accueil
                },
                onReprendrePartie = { partie ->
                    isPartieEnCours = true
                    historiqueContext = null
                    viewModel.setCurrentPartie(partie.id)
                    selectedPartieId = partie.id
                    joueursSelectionnes.value =
                        toGlobalJoueurs(joueursGlobal, partie.joueurs.toList())
                    currentScreen = Screen.Jeux
                },
                onStatistiquesPartie = { partie, ctx ->
                    selectedPartieId = partie.id
                    historiqueContext = ctx
                    currentScreen = Screen.StatistiquesPartie
                },
                onSupprimerPartie = { partie, ctx ->
                    selectedPartieId = partie.id
                    historiqueContext = ctx
                    viewModel.deletePartie(partie.id)
                    currentScreen = Screen.Historique
                },
                initialContext = historiqueContext
            )
        }

        is Screen.StatistiquesGlobales -> {
            if (historique == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                val historiqueValue = historique
                if (historiqueValue != null) {
                    StatistiquesGlobalesScreen(
                        historique = historiqueValue,
                        onNavigateBack = {
                            currentScreen = Screen.Accueil
                        }
                    )
                }
            }
        }

        is Screen.StatistiquesPartie -> {
            val joueursGlobal by viewModel.joueurs.collectAsState(initial = emptyList())
            StatistiquesPartieScreen(
                historique = historique ?: Historique(mutableListOf()),
                joueursGlobal = joueursGlobal,
                partieId = selectedPartieId,
                onNavigateBack = {
                    currentScreen = Screen.Historique
                },
                onNavigateToJoueur = { joueur ->
                    selectedJoueur = joueur
                    currentScreen = Screen.StatistiquesJoueurs
                },
                onConsulterPartie = { id ->
                    val navigateWithPartie: (Partie) -> Unit = { partie ->
                        viewModel.setCurrentPartie(partie.id)
                        selectedPartieId = partie.id
                        isPartieEnCours = false
                        joueursSelectionnes.value =
                            toGlobalJoueurs(joueursGlobal, partie.joueurs.toList())
                        currentScreen = Screen.Jeux
                    }

                    val partie = historique?.parties?.find { it.id == id }
                    if (partie != null) {
                        navigateWithPartie(partie)
                    } else {
                        viewModel.reloadHistorique()
                    }
                },
                onRecreateJoueur = { joueur ->
                    val newJoueurs = joueursGlobal + joueur
                    viewModel.updateJoueurs(newJoueurs)
                }
            )
        }

        is Screen.StatistiquesJoueurs -> {
            if (historique == null || selectedJoueur == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                val historiqueValue = historique
                val joueurValue = selectedJoueur

                if (historiqueValue != null && joueurValue != null) {
                    StatistiquesJoueursScreen(
                        historique = historiqueValue,
                        joueur = joueurValue,
                        onNavigateBack = {
                            selectedJoueur = null
                            if (selectedPartieId != null) {
                                currentScreen = Screen.StatistiquesPartie
                            } else {
                                currentScreen = Screen.Joueurs
                            }
                        },
                        fromPartie = selectedPartieId != null
                    )
                }
            }
        }

        is Screen.Constantes -> ConstantesScreen(
            constantes,
            onBack = { currentScreen = Screen.Accueil }
        )

        is Screen.Calculs -> {
            CalculsScreen(
                joueurs = lastJoueursForCalculs,
                donne = lastDonneForCalculs,
                constantes = constantes,
                onDismiss = {
                    viewModel.clearCalculData()
                    currentScreen = Screen.Jeux
                }
            )
        }

    }

    if (showDonneActionDialog && selectedDonneForAction != null) {
        val target = selectedDonneForAction!!
        Dialog(onDismissRequest = {
            showDonneActionDialog = false
            selectedDonneForAction = null
        }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black,
                modifier = Modifier
                    .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "Action sur la donne",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    Button(onClick = {
                        // Éditer
                        pendingDonneToEdit = target
                        pendingDonneReadOnly = false
                        pendingDonneSubmit = { newDonne ->
                            val pid = viewModel.currentPartieId.value
                            if (pid != null) viewModel.editDonne(pid, newDonne)
                            else viewModel.editDonneLocally(target.id, newDonne)
                        }
                        showDonneActionDialog = false
                        selectedDonneForAction = null
                        currentScreen = Screen.Donne
                    }) {
                        Text("Éditer", color = Color.Black)
                    }

                    Button(onClick = {
                        showDonneActionDialog = false
                        viewModel.setCalculData(joueurs = joueursSelectionnes.value, donne = target)
                        currentScreen = Screen.Calculs
                        selectedDonneForAction = null
                    }) {
                        Text("Calculer", color = Color.Black)
                    }

                    Button(onClick = {
                        // Supprimer
                        showDonneActionDialog = false
                        showDeleteConfirmDialog = true
                    }) {
                        Text("Supprimer", color = Color.Black)
                    }

                    TextButton(onClick = {
                        // Annuler
                        showDonneActionDialog = false
                        selectedDonneForAction = null
                    }) {
                        Text("Annuler", color = Color.White)
                    }
                }
            }
        }
    }


    if (showDeleteConfirmDialog && selectedDonneForAction != null) {
        val target = selectedDonneForAction!!
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                selectedDonneForAction = null
                currentScreen = Screen.Jeux
            },
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Supprimer cette donne", color = Color.White)
                }
            },
            confirmButton = {},
            dismissButton = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row {
                        TextButton(onClick = {
                            showDeleteConfirmDialog = false
                            selectedDonneForAction = null
                            currentScreen = Screen.Jeux
                        }) {
                            Text("Retour", color = Color.White)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        TextButton(onClick = {
                            val pid = viewModel.currentPartieId.value
                            if (pid != null) {
                                viewModel.deleteDonne(pid, target.id)
                            } else {
                                viewModel.deleteDonneLocally(target.id)
                            }

                            showDeleteConfirmDialog = false
                            selectedDonneForAction = null
                            currentScreen = Screen.Jeux
                        }) {
                            Text("Confirmer", color = Color.White)
                        }
                    }
                }
            },
            containerColor = Color.Black,
            modifier = Modifier.border(1.dp, Color.White, RoundedCornerShape(12.dp))
        )
    }

    if (showExitDialog) {
        CloturePartiDialog(
            onDismiss = { showExitDialog = false },
            onConfirm = {
                showExitDialog = false
                viewModel.clearPartieState()
                currentScreen = Screen.Accueil
            }
        )
    }
}

@Composable
fun CloturePartiDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Terminer la partie") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Oui")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Non")
            }
        }
    )
}