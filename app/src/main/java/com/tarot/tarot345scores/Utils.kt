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
    val updatedParties = hist.parties.map { p ->
        if (p.id == partieId) {
            p.copy(donnes = p.donnes.filterNot { it.id == donneId } as MutableList<Donne>)
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

fun getDefaultJoueurs(): List<Joueur> {
    if (true) {
        return listOf()
    } else {
        return listOf(
            Joueur("A", "f47ac10b-58cc-4372-a567-0e02b2c3d479"),
            Joueur("B", "9a1b2c3d-4e5f-6789-abcd-ef0123456789"),
            Joueur("C", "d290f1ee-6c54-4b01-90e6-d701748f0851"),
            Joueur("D", "3fa85f64-5717-4562-b3fc-2c963f66afa6"),
            Joueur("E", "6fa459ea-ee8a-3ca4-894e-db77e160355e"),
        )

            /*return listOf(
                Joueur("Yannick"),
                Joueur("Christine"),
                Joueur("Aurélie"),
                Joueur("Alexis"),
                Joueur("Camille"),
                Joueur("Laïla"),
                Joueur("Christophe"),
                Joueur("Florence"),
                Joueur("Clémence"),
                Joueur("Arthur"),
                Joueur("Eloïse"),
                Joueur("Martin"),
                Joueur("Valérie"),
                Joueur("Benjamin"),
                Joueur("Armand"),
                Joueur("Alexis G")
            )
             */
    }
}

/*
550e8400-e29b-41d4-a716-446655440000
123e4567-e89b-12d3-a456-426614174000
c56a4180-65aa-42ec-a945-5fd21dec0538
1b4e28ba-2fa1-11d2-883f-0016d3cca427
7c9e6679-7425-40de-944b-e07fc1f90ae7
3b241101-e2bb-4255-8caf-4136c566a962
2c1f3b8e-9d4a-4f2b-8e5c-0a1b2c3d4e5f
8f14e45f-ceea-4d9b-9b3a-6b7c8d9e0f12
5a8d2f7c-3b4e-4c5d-9f0a-1b2c3d4e5f60
9f8b7c6d-5e4f-3a2b-1c0d-9e8f7a6b5c4d
0f8fad5b-d9cb-469f-a165-70867728950e
4d3c2b1a-0f9e-8d7c-6b5a-4c3b2a1f0e9d
ab12cd34-ef56-7890-ab12-cd34ef567890
ffeeddcc-bbaa-9988-7766-554433221100
01234567-89ab-cdef-0123-456789abcdef
*/

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