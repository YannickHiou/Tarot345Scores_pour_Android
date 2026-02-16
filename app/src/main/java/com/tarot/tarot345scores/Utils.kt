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

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale
import java.util.UUID

private const val HISTO_FILENAME = "historique.json"
private const val JOUEURS_FILENAME = "joueurs.json"
const val JEUX_COULEUR = 0xff000000 //0xFF00BCD4
const val BOUTON_COULEUR = 0xffb6caff // 0xffffff00
const val JOUEUR_SELECTIONNE =  0xff2e7d32

private val json = Json { var prettyPrint = true; encodeDefaults = true }

fun loadHistorique(context: Context): Historique {
    val file = File(context.filesDir, HISTO_FILENAME)
    if (!file.exists()) return Historique()
    return try {
        val content = file.readText()
        json.decodeFromString<Historique>(content)
    } catch (e: Exception) {
        e.printStackTrace()
        Historique()
    }
}

fun saveHistorique(context: Context, historique: Historique) {
    val file = File(context.filesDir, HISTO_FILENAME)
    try {
        val text = json.encodeToString(historique)
        file.writeText(text)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun createPartie(context: Context, joueurs: List<Joueur>): Partie {
    val hist = loadHistorique(context)
    val partie = Partie(
        id = UUID.randomUUID().toString(),
        createdAt = System.currentTimeMillis(),
        joueurs = joueurs,
        donnes = emptyList()
    )
    hist.parties.add(partie)
    saveHistorique(context, hist)
    return partie
}


fun deletePartie(context: Context, partieId: String) {
    val hist = loadHistorique(context)
    val removed = hist.parties.removeAll { it.id == partieId }
    if (removed) saveHistorique(context, hist)
}

suspend fun deleteDonneInHistorique(
    context: Context,
    partieId: String,
    donneId: String
): Historique {
    val hist = loadHistorique(context)
    val updatedParties = hist.parties.mapNotNull { p ->
        if (p.id == partieId) {
            val donnesRestantes = p.donnes.filterNot { it.id == donneId }
            if (donnesRestantes.isEmpty()) {
                null
            } else {
                p.copy(donnes = donnesRestantes as MutableList<Donne>)
            }
        } else p
    }
    val newHist = hist.copy(parties = updatedParties as MutableList<Partie>)
    saveHistorique(context, newHist)
    return newHist
}


suspend fun editDonneInHistorique(
    context: Context,
    partieId: String,
    updatedDonne: Donne
): Historique {
    val hist = loadHistorique(context)
    val updatedParties = hist.parties.map { p ->
        if (p.id == partieId) {
            val newDonnes = p.donnes.map { if (it.id == updatedDonne.id) updatedDonne else it }
            p.copy(donnes = newDonnes as MutableList<Donne>)
        } else p
    }
    val newHist = hist.copy(parties = updatedParties as MutableList<Partie>)
    saveHistorique(context, newHist)
    return newHist
}

suspend fun addDonneInHistorique(context: Context, partieId: String, newDonne: Donne): Historique {
    val hist = loadHistorique(context)
    val updatedParties = hist.parties.map { p ->
        if (p.id == partieId) {
            p.copy(donnes = (p.donnes + newDonne) as MutableList<Donne>)
        } else p
    }
    val newHist = hist.copy(parties = updatedParties as MutableList<Partie>)
    saveHistorique(context, newHist)
    return newHist
}

fun saveJoueurs(context: Context, joueurs: List<Joueur>) {
    val file = File(context.filesDir, "joueurs.json")
    try {
        val jsonArray = JSONArray()
        joueurs.forEach { joueur ->
            val obj = JSONObject()
            obj.put("nomUI", joueur.nomUI)
            obj.put("id", joueur.id)
            jsonArray.put(obj)
        }
        file.writeText(jsonArray.toString(2))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun formatNomJoueur(raw: String): String {
    if (raw.isBlank()) return raw
    val cleaned = raw.replace(Regex(" +"), " ")
    val lower = cleaned.lowercase(Locale.getDefault())
    val sb = StringBuilder(lower.length)
    var capitalizeNext = true
    for (ch in lower) {
        if (ch == ' ' || ch == '-' || ch == '_') {
            sb.append(ch)
            capitalizeNext = true
        } else if (capitalizeNext) {
            sb.append(ch.titlecase(Locale.getDefault()))
            capitalizeNext = false
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}

fun getDefaultJoueurs(load:Boolean=true): List<Joueur> {
    val joueurs = if (load) {
        emptyList<Joueur>()
    } else {
        listOf(
            Joueur("Yannick", "f47ac10b-58cc-4372-fsde-0e02b2c3d479"),
            Joueur("Christine", "9a1b2c3d-4e5f-6789-abcd-ef0123456789"),
            Joueur("Aurélie", "d290f1ee-6c54-4b01-90e6-d701748f0851"),
            Joueur("Alexis", "3fa85f64-5717-4562-b3fc-2c963f66afa6"),
            Joueur("Camille", "6fa459ea-ee8a-3ca4-894e-db77e160355e"),
            Joueur("Laïla", "f47ac10b-58cc-5729-a567-0e02b2c3d479"),
            Joueur("Christophe", "3d6f4e2a-9b1c-4f8e-a3d5-7c8b9e0f1a2b"),
            Joueur("Florence", "8e7d6c5b-4a3f-2e1d-0c9b-8a7f6e5d4c3b"),
            Joueur("Clémence", "a1b2c3d4-e5f6-4789-a012-b3c4d5e6f7a8"),
            Joueur("Arthur", "6f8e9d0c-1b2a-4536-8794-a5b6c7d8e9f0"),
            Joueur("Eloïse", "2c4d6e8f-0a1b-4c3d-9e5f-7a8b9c0d1e2f"),
            Joueur("Martin", "9b8a7f6e-5d4c-4321-b0a9-f8e7d6c5b4a3"),
            Joueur("Valérie", "5e6f7a8b-9c0d-4e1f-a2b3-c4d5e6f7a8b9"),
            Joueur("Benjamin", "d4c3b2a1-f0e9-4d8c-b7a6-5f4e3d2c1b0a"),
            Joueur("Armand", "7a8b9c0d-1e2f-4536-9748-a5b6c7d8e9f0"),
        )
    }
    return joueurs
}

fun loadConstantesFromAssets(
    context: Context,
    filename: String = "constantes.json"
): ConstantesConfig {
    val jsonText = context.assets.open(filename).bufferedReader().use { it.readText() }
    return Json.decodeFromString(ConstantesConfig.serializer(), jsonText)
}

fun toGlobalJoueurs(
    joueursGlobal: List<Joueur>,
    joueur: List<Joueur>
): List<Joueur> {
    val mapGlobal = joueursGlobal.associateBy { it.id }
    return joueur.mapNotNull { mapGlobal[it.id] ?: it } // si absent, on retourne l'objet local
}