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

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import java.io.File
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _joueurs = MutableStateFlow<List<Joueur>>(emptyList())
    val joueurs: StateFlow<List<Joueur>> = _joueurs.asStateFlow()

    private val _historique = MutableStateFlow<Historique?>(null)
    val historique: StateFlow<Historique?> = _historique.asStateFlow()

    private val _allDonnes = MutableStateFlow<List<Donne>>(emptyList())
    val allDonnes: StateFlow<List<Donne>> = _allDonnes.asStateFlow()

    private val _currentPartieId = MutableStateFlow<String?>(null)
    val currentPartieId: StateFlow<String?> = _currentPartieId.asStateFlow()

    private val _isPartieEnCours = MutableStateFlow<Boolean>(false)
    val isPartieEnCours: StateFlow<Boolean> = _isPartieEnCours.asStateFlow()

    private val _joueursSelectionnes = MutableStateFlow<Set<Joueur>>(emptySet())
    val joueursSelectionnes: StateFlow<Set<Joueur>> = _joueursSelectionnes.asStateFlow()


    private val _loadErrorJoueurs = MutableStateFlow<String?>(null)
    val loadErrorJoueurs: StateFlow<String?> = _loadErrorJoueurs.asStateFlow()

    private val _isLoadingJoueurs = MutableStateFlow(false)
    val isLoadingJoueurs = _isLoadingJoueurs.asStateFlow()


    private val _calculJoueurs = MutableStateFlow<List<Joueur>>(emptyList())
    val calculJoueurs: StateFlow<List<Joueur>> = _calculJoueurs.asStateFlow()

    private val _calculDonne = MutableStateFlow<Donne?>(null)
    val calculDonne: StateFlow<Donne?> = _calculDonne.asStateFlow()


    init {
        viewModelScope.launch {
            _isLoadingJoueurs.value = true
            try {
                val ctx = getApplication<Application>().applicationContext
                val joueurs = loadOrCreateJoueurs(ctx)
                val historique = withContext(Dispatchers.IO) { loadHistorique(ctx) }
                _joueurs.value = joueurs
                _historique.value = historique
            } catch (e: Exception) {
                _loadErrorJoueurs.value = e.message ?: "Erreur chargement"
            } finally {
                _isLoadingJoueurs.value = false
            }
        }
    }
    // Fonction pour sauvegarder une donne
    fun saveDonne(newDonne: Donne, joueursSelectionnes: List<Joueur>) {
        viewModelScope.launch {
            val ctx = getApplication<Application>().applicationContext
            val pid = _currentPartieId.value  // CHANGÉ

            if (pid != null) {
                val newHist = withContext(Dispatchers.IO) {
                    addDonneInHistorique(ctx, pid, newDonne)
                }
                _historique.value = newHist
                val partie = newHist.parties.firstOrNull { it.id == pid }
                _allDonnes.value = partie?.donnes?.toList() ?: emptyList()
            } else {
                val partie = withContext(Dispatchers.IO) {
                    createPartie(ctx, joueursSelectionnes)
                }
                _currentPartieId.value = partie.id  // CHANGÉ
                val newHist = withContext(Dispatchers.IO) {
                    addDonneInHistorique(ctx, partie.id, newDonne)
                }
                _historique.value = newHist
                val partieUpdated = newHist.parties.firstOrNull { it.id == partie.id }
                _allDonnes.value = partieUpdated?.donnes?.toList() ?: emptyList()
            }
        }
    }

    // Fonction pour définir la partie courante
    fun setCurrentPartie(partieId: String?) {
        _currentPartieId.value = partieId  // CHANGÉ
        if (partieId != null) {
            val partie = _historique.value?.parties?.firstOrNull { it.id == partieId }
            _allDonnes.value = partie?.donnes?.toList() ?: emptyList()
        } else {
            _allDonnes.value = emptyList()
        }
    }

    // Fonction pour obtenir la partie courante
    fun getCurrentPartieId(): String? = _currentPartieId.value  // CHANGÉ

    fun saveHistorique(newHistorique: Historique) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val ctx = getApplication<Application>().applicationContext
                saveHistorique(ctx, newHistorique)
                _historique.value = newHistorique
            }
        }
    }

    fun updateJoueurs(newJoueurs: List<Joueur>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val ctx = getApplication<Application>().applicationContext
                saveJoueurs(ctx, newJoueurs)
                _joueurs.value = newJoueurs
            }
        }
    }

    fun deletePartie(partieId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val ctx = getApplication<Application>().applicationContext
                deletePartie(ctx, partieId)
            }
            _historique.value = withContext(Dispatchers.IO) {
                val ctx = getApplication<Application>().applicationContext
                loadHistorique(ctx)
            }
        }
    }

    fun reloadHistorique() {
        viewModelScope.launch {
            _historique.value = withContext(Dispatchers.IO) {
                val ctx = getApplication<Application>().applicationContext
                loadHistorique(ctx)
            }
        }
    }

    fun clearDonnes() {
        _allDonnes.value = emptyList()
        _currentPartieId.value = null
    }

    fun editDonne(partieId: String, donne: Donne) {
        viewModelScope.launch {
            val newHist = withContext(Dispatchers.IO) {
                val ctx = getApplication<Application>().applicationContext
                editDonneInHistorique(ctx, partieId, donne)
            }
            _historique.value = newHist
            val partie = newHist.parties.firstOrNull { it.id == partieId }
            _allDonnes.value = partie?.donnes?.toList() ?: emptyList()
        }
    }

    fun editDonneLocally(targetId: String, newDonne: Donne) {
        _allDonnes.value = _allDonnes.value.map {
            if (it.id == targetId) newDonne else it
        }
    }

    fun deleteDonne(partieId: String, donneId: String) {
        viewModelScope.launch {
            val newHist = withContext(Dispatchers.IO) {
                val ctx = getApplication<Application>().applicationContext
                deleteDonneInHistorique(ctx, partieId, donneId)
            }
            _historique.value = newHist
            val partie = newHist.parties.firstOrNull { it.id == partieId }
            _allDonnes.value = partie?.donnes?.toList() ?: emptyList()
        }
    }

    fun deleteDonneLocally(donneId: String) {
        _allDonnes.value = _allDonnes.value.filterNot { it.id == donneId }
    }

    fun clearPartieState() {
        _currentPartieId.value = null
        _allDonnes.value = emptyList()
        _isPartieEnCours.value = false
        _joueursSelectionnes.value = emptySet()
    }

    suspend fun loadOrCreateJoueurs(context: Context): List<Joueur> {
        return withContext(Dispatchers.IO) {
            val file = File(context.filesDir, "joueurs.json")
            if (file.exists()) {
                try {
                    val jsonArray = JSONArray(file.readText(Charsets.UTF_8))
                    if (jsonArray.length() < 1) {
                        val defaultJoueurs = getDefaultJoueurs()
                        saveJoueurs(context, defaultJoueurs)
                        defaultJoueurs
                    } else {
                        List(jsonArray.length()) { index ->
                            val obj = jsonArray.getJSONObject(index)
                            val nomUI = obj.optString("nomUI", obj.optString("nom", "zombi"))
                            val id = obj.optString("id", UUID.randomUUID().toString())
                            Joueur(nomUI = nomUI, id = id)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    emptyList()
                }
            } else {
                val defaultJoueurs = getDefaultJoueurs()
                saveJoueurs(context, defaultJoueurs)
                defaultJoueurs
            }
        }
    }

    fun setCalculData(joueurs: List<Joueur>, donne: Donne) {
        _calculJoueurs.value = joueurs
        _calculDonne.value = donne
    }

    fun clearCalculData() {
        _calculJoueurs.value = emptyList()
        _calculDonne.value = null
    }
}