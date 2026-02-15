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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID
import kotlin.String
import kotlin.random.Random

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

fun fakeHistorique(
    constantes: ConstantesConfig,
    filesDir: File,
    nb_parties: Int
) {
    val joueursFile = File(filesDir, "joueurs.json")
    val historiqueFile = File(filesDir, "historique.json")
    val json = Json { prettyPrint = true }

    if (nb_parties == 0) {
        val joueurs = mutableListOf<Partie>()
        joueursFile.writeText(json.encodeToString(joueurs))

        val historique = Historique(parties = mutableListOf<Partie>())
        historiqueFile.writeText(json.encodeToString(historique))
        return
    }

    var joueurs = getDefaultJoueurs(false)
    joueursFile.writeText(json.encodeToString(joueurs))

    val contratsType = ContratType.entries.map { it.displayName }
    val poigneesType = PoigneeType.entries.map { it.displayName }

    val seuils = constantes.seuils_bouts

    val maintenant = System.currentTimeMillis()
    val cinqAnsEnMillis = 2L * 365 * 24 * 60 * 60 * 1000 // 5 ans en millisecondes
    val intervalleEntreParties = if (nb_parties > 1) {
        cinqAnsEnMillis / (nb_parties - 1)
    } else {
        0L
    }

    val parties = mutableListOf<Partie>()

    repeat(nb_parties) { partieIndex ->
        val nbJoueurs: Int = listOf(3, 4, 5).random()
        val datePartie = maintenant - cinqAnsEnMillis + (partieIndex * intervalleEntreParties)
        val joueursPartie = joueurs.shuffled().take(nbJoueurs)

        val partie = genere_parite(
            datePartie,
            joueursPartie,
            nbJoueurs,
            contratsType,
            poigneesType,
            seuils,
            constantes
        )
        parties.add(partie)
    }



    val historique = Historique(parties = parties)

    // Sauvegarder dans historique.json
    historiqueFile.writeText(json.encodeToString(historique))
}

private fun genere_parite(
    datePartie: Long,
    joueurs: List<Joueur>,
    nbJoueurs: Int,
    contratsType: List<String>,
    poigneesType: List<String>,
    seuils: List<Int>,
    constantes: ConstantesConfig
): Partie {
    val nbDonnes = Random.nextInt(10, 20)
    val donnes = mutableListOf<Donne>()

    repeat(nbDonnes) { donneIndex ->
        val preneurIndex = Random.nextInt(nbJoueurs)
        val preneurNom = joueurs[preneurIndex]

        val (appeleIndex, appeleNom) = if (nbJoueurs == 5) {
            val idx = if (Random.nextFloat() < 0.25f) {
                preneurIndex
            } else {
                generateSequence { Random.nextInt(nbJoueurs) }.first { it != preneurIndex }
            }
            idx to joueurs[idx]
        } else {
            -1 to null
        }

        val contratIndex = Random.nextInt(contratsType.size)
        val contratNom = contratsType[contratIndex]

        // Générer le nombre de bouts possédés par l'attaque (0 à 3)
        val nbBoutsAttaque = Random.nextInt(0, seuils.size)

        // Seuil selon le nombre de bouts
        val seuil = seuils[nbBoutsAttaque]

        // Points d'attaque selon le contrat
        var pointsAtq = when (contratNom) {
            contratsType[0] -> Random.nextInt(seuil - 15, seuil + 20)
            contratsType[1] -> Random.nextInt(seuil - 10, seuil + 25)
            contratsType[2] -> Random.nextInt(seuil - 5, seuil + 30)
            contratsType[3] -> Random.nextInt(seuil, seuil + 35)
            else -> Random.nextInt(seuil - 15, seuil + 20)
        }.coerceIn(20, 91)

        // Petit au bout dans 25% des cas
        val petitAuBout = if (Random.nextFloat() < 0.25f) {
            PetitAuBout(index = joueurs.indices.random())
        } else null

        val nbMiseres: Int = when (nbJoueurs) {
            5 -> listOf(1, 4).random()
            4 -> listOf(1, 3).random()
            else -> listOf(1, 2).random()
        }

        val miseresIndex: List<Int> = if (Random.nextFloat() < 0.25f) {
            val takeCount = nbMiseres.coerceAtMost(nbJoueurs)
            (0 until nbJoueurs).shuffled().take(takeCount)
        } else {
            emptyList()
        }

        val poignees: List<Poignee> =
            if (Random.nextFloat() < 0.5f && joueurs.isNotEmpty()) {
                val nbPoignees = Random.nextInt(1, (joueurs.size - 1).coerceAtLeast(1))
                joueurs.indices.shuffled()
                    .take(nbPoignees)
                    .mapNotNull { index ->
                        val rand = Random.nextFloat()
                        val type = when {
                            rand < 0.5f -> PoigneeType.NONE  // 50% NONE
                            rand < 0.85f -> PoigneeType.SIMPLE  // 35% SIMPLE
                            rand < 0.95f -> PoigneeType.DOUBLE  // 10% DOUBLE
                            else -> PoigneeType.TRIPLE  // 5% TRIPLE
                        }
                        type?.let { Poignee(index = index, type = it) }
                    }
            } else {
                emptyList()
            }

        val chelem: Chelem? = if (Random.nextFloat() < 0.05f) {
            val r = Random.nextFloat()
            when {
                r < 0.05f -> Chelem(annonce = true, succes = true)   // 5% des chelems
                r < 0.05f + 0.15f -> Chelem(annonce = true, succes = false) // 15%
                else -> Chelem(annonce = false, succes = true) // 80%
            }
        } else {
            null
        }

        val scores = calculerScores(
            joueurs = joueurs,
            preneurIndex = preneurIndex,
            appeleIndex = appeleIndex,
            contratNom = contratNom,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseresIndex = miseresIndex,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = chelem,
            constantes = constantes,
        )

        val resultat = verifierScores(
            joueurs,
            preneurIndex,
            appeleIndex,
            miseresIndex,
            pointsAtq,
            nbBoutsAttaque,
            petitAuBout,
            contratNom,
            poignees,
            chelem,
            scores,
            constantes
        )

        if (!resultat) {
            println()
            throw RuntimeException("Internal error")
        }

        val dateDonne = datePartie + Random.nextLong(
            0,
            3 * 60 * 60 * 1000
        ) // +0 à 3h après le début de partie

        val donne = Donne(
            id = UUID.randomUUID().toString(),
            createdAt = dateDonne,
            preneurIndex = preneurIndex,
            appeleIndex = appeleIndex,
            contratIndex = contratIndex,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            petitAuBout = petitAuBout,
            poignees = poignees,
            miseres = miseresIndex,
            chelem = chelem,
            scores = scores
        )
        donnes.add(donne)
    }
    val partie = Partie(
        id = UUID.randomUUID().toString(),
        createdAt = datePartie, //  MODIFIÉ : Utilise la date calculée
        joueurs = joueurs,
        donnes = donnes
    )
    return partie
}
