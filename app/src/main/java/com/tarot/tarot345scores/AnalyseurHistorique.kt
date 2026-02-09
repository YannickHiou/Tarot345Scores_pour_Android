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

data class StatistiquesPartieJoueur(
    val contrats: List<Int>, // [PETITE, GARDE, GARDE_SANS, GARDE_CONTRE]
    val poignees: List<Int>, // [NONE, SIMPLE, DOUBLE, TRIPLE]
    val chelems: List<Int>, // [NON_ANNONCE_REUSSI, ANNONCE_RATE, ANNONCE_REUSSI]
    val miseres: Int,
    val petitAuBoutGagne: Int,
    val petitAuBoutPerdu: Int,
    val preneur: Int,
    val appele: Int,
    val defense: Int,
    val totalDonnes: Int,
    val totalParties: Int,
    val pointsGagnes: Int,
    val pointsPerdus: Int,
    val meilleurScore: Int,
    val pireScore: Int,
)

data class StatistiquesJoueur(
    val parties: MutableMap<Int, StatistiquesPartieJoueur> = mutableMapOf()
)

data class StatistiquesPartie(
    val nbDonnes: Int,
    val nbBoutsAttaque: Int,
    val petitAuBoutGagne: Int,
    val petitAuBoutPerdus: Int,
    val miseres: Int,
    val pointsGagnes: Int,
    val pointsPerdus: Int,
    val meilleurScore: Int,
    val pireScore: Int,
    val partieContrats: List<Int>,
    val partiePoignees: List<Int>,
    val partieChelems: List<Int>
)

data class StatistiquesGlobales(
    val nbParties: Int,
    val nbDonnes: Int,
    val nbBoutsAttaque: Int,
    val petitAuBoutGagne: Int,
    val petitAuBoutPerdu: Int,
    val miseres: Int,
    val pointsGagnes: Int,
    val meilleurScore: Int,
    val pireScore: Int,
    val contrats: List<Int>,
    val poignees: List<Int>,
    val chelems: List<Int>,
    val gainMin: Int,
    val gainMax: Int,
    val mediane: Int,
    val gainMoyenMin: Double,
    val gainMoyenMax: Double,
    val medianeMoyenne: Double
)

class AnalyseurHistorique {

    companion object {
        private val CONTRATS_TYPE = ContratType.entries
        private val POIGNEES_TYPE = PoigneeType.entries
    }

    fun analyser(
        historique: Historique
    ): Triple<Map<String, StatistiquesJoueur>, List<StatistiquesPartie>, StatistiquesGlobales> {

        val joueurs = mutableMapOf<String, MutableStatistiquesJoueur>()
        val parties = mutableListOf<StatistiquesPartie>()
        val globales = MutableStatistiquesGlobales()

        for (partie in historique.parties) {

            val nbJoueurs = partie.joueurs.size

            for (joueur in partie.joueurs) {
                val joueurId = joueur.id
                if (!joueurs.containsKey(joueurId)) {
                    joueurs[joueurId] = MutableStatistiquesJoueur()
                }
                joueurs[joueurId]!!.parties[nbJoueurs]!!.totalParties += 1
                joueurs[joueurId]!!.parties[nbJoueurs]!!.totalDonnes += partie.donnes.size
            }

            val partieNbDonnes = partie.donnes.size
            var partienbBoutsAttaque = 0
            var partieMeilleurScore = Int.MIN_VALUE
            var partiePireScore = Int.MAX_VALUE
            var partiePointsGagnes = 0
            var partiePointsPerdus = 0
            val partieContrats = MutableList(4) { 0 }
            val partiePoignees = MutableList(4) { 0 }
            val partieChelems = MutableList(3) { 0 }
            var partieMiseres = 0
            var partiePetitAuBoutGagne = 0
            var partiePetitAuBoutPerdu = 0


            for (donne in partie.donnes) {
                // PRENEUR
                val preneurIndex = donne.preneurIndex
                val preneurId = partie.joueurs[preneurIndex].id
                joueurs[preneurId]!!.parties[nbJoueurs]!!.preneur++

                // APPELE
                if (nbJoueurs == 5) {
                    donne.appeleIndex?.let { appeleIndex ->
                        val appeleId = partie.joueurs[appeleIndex].id
                        joueurs[appeleId]!!.parties[nbJoueurs]!!.appele++
                    }
                }

                // POINTS A LA DEFENSE
                if (nbJoueurs == 5) {
                    donne.appeleIndex?.let { appeleIndex ->
                        for ((joueurIndex, joueur) in partie.joueurs.withIndex()) {
                            if (joueurIndex != preneurIndex && joueurIndex != appeleIndex) {
                                joueurs[joueur.id]!!.parties[nbJoueurs]!!.defense++
                            }
                        }
                    }
                } else {
                    for ((joueurIndex, joueur) in partie.joueurs.withIndex()) {
                        if (joueurIndex != preneurIndex)
                            joueurs[joueur.id]!!.parties[nbJoueurs]!!.defense++
                    }
                }

                // POINTS GAGNES/PERDUS
                for ((joueurIndex, score) in donne.scores.withIndex()) {
                    val joueur = partie.joueurs[joueurIndex]
                    val joueurStats = joueurs[joueur.id]!!.parties[nbJoueurs]!!

                    if (score >= 0) {
                        joueurStats.pointsGagnes += score
                        partiePointsGagnes += score
                    } else {
                        joueurStats.pointsPerdus += score
                        partiePointsPerdus += score
                    }

                    joueurStats.meilleurScore = maxOf(joueurStats.meilleurScore, score)
                    joueurStats.pireScore = minOf(joueurStats.pireScore, score)
                    partieMeilleurScore = maxOf(partieMeilleurScore, score)
                    partiePireScore = minOf(partiePireScore, score)
                }

                // NOMBRE DE BOUTS
                partienbBoutsAttaque += donne.nbBoutsAttaque ?: 0

                // PETIT AU BOUT
                donne.petitAuBout?.let { petitAuBout ->
                    val joueurIndex = petitAuBout.index
                    val joueur = partie.joueurs[joueurIndex]
                    if (nbJoueurs == 5) {
                        donne.appeleIndex?.let { appeleIndex ->
                            if (preneurIndex == joueurIndex || appeleIndex == joueurIndex) {
                                joueurs[joueur.id]!!.parties[nbJoueurs]!!.petitAuBoutGagne++
                                partiePetitAuBoutGagne += 1
                            } else {
                                joueurs[joueur.id]!!.parties[nbJoueurs]!!.petitAuBoutPerdu++
                                partiePetitAuBoutPerdu += 1
                            }
                        }
                    } else {
                        if (preneurIndex == joueurIndex) {
                            joueurs[joueur.id]!!.parties[nbJoueurs]!!.petitAuBoutGagne++
                            partiePetitAuBoutGagne += 1
                        } else {
                            joueurs[joueur.id]!!.parties[nbJoueurs]!!.petitAuBoutPerdu++
                            partiePetitAuBoutPerdu += 1
                        }
                    }
                }

                // MISERE
                for (miseresIndex in donne.miseres) {
                    val joueur = partie.joueurs[miseresIndex]
                    joueurs[joueur.id]!!.parties[nbJoueurs]!!.miseres++
                    partieMiseres++
                }

                //  CONTRATS_
                CONTRATS_TYPE.getOrNull(donne.contratIndex)?.let { _ ->
                    val joueur = partie.joueurs[preneurIndex]
                    joueurs[joueur.id]!!.parties[nbJoueurs]!!.contrats[donne.contratIndex]++
                    partieContrats[donne.contratIndex]++
                }

                // CHELEM
                donne.chelem?.let { chelemType ->
                    val idxChelem = when {
                        chelemType.annonce && chelemType.succes -> 2
                        chelemType.annonce && !chelemType.succes -> 1
                        !chelemType.annonce && chelemType.succes -> 0
                        else -> 0
                    }

                    // Ajouter aux statistiques du preneur
                    val preneur = partie.joueurs[preneurIndex]
                    joueurs[preneur.id]!!.parties[nbJoueurs]!!.chelems[idxChelem]++

                    // Ajouter aux statistiques de l'appele (si different du preneur)
                    if (nbJoueurs == 5) {
                        donne.appeleIndex?.let { appeleIndex ->
                            if (appeleIndex != preneurIndex) {
                                val appele = partie.joueurs[appeleIndex]
                                joueurs[appele.id]!!.parties[nbJoueurs]!!.chelems[idxChelem]++
                            }
                        }
                    }
                    partieChelems[idxChelem]++
                }


                for (poignee in donne.poignees) {
                    if (poignee.type != PoigneeType.NONE) {
                        val joueurIndex = poignee.index
                        val joueur = partie.joueurs[joueurIndex]
                        POIGNEES_TYPE.indexOf(poignee.type).takeIf { it >= 0 }?.let { idxPoignee ->
                            joueurs[joueur.id]!!.parties[nbJoueurs]!!.poignees[idxPoignee]++
                            partiePoignees[idxPoignee]++
                        }
                    }
                }
            }

            parties.add(
                StatistiquesPartie(
                    nbDonnes = partieNbDonnes,
                    nbBoutsAttaque = partienbBoutsAttaque,
                    petitAuBoutGagne = partiePetitAuBoutGagne,
                    petitAuBoutPerdus = partiePetitAuBoutPerdu,
                    miseres = partieMiseres,
                    pointsGagnes = partiePointsGagnes,
                    pointsPerdus = partiePointsPerdus,
                    meilleurScore = partieMeilleurScore,
                    pireScore = partiePireScore,
                    partieContrats = partieContrats.toList(),
                    partiePoignees = partiePoignees.toList(),
                    partieChelems = partieChelems.toList()
                )
            )

            globales.nbParties += 1
            globales.nbDonnes += partieNbDonnes
            globales.nbBoutsAttaque += partienbBoutsAttaque
            globales.petitAuBoutGagne += partiePetitAuBoutGagne
            globales.petitAuBoutPerdu += partiePetitAuBoutPerdu
            globales.miseres += partieMiseres
            globales.pointsGagnes += partiePointsGagnes

            for (i in partieContrats.indices) {
                globales.contrats[i] += partieContrats[i]
            }

            for (i in partiePoignees.indices) {
                globales.poignees[i] += partiePoignees[i]
            }

            for (i in partieChelems.indices) {
                globales.chelems[i] += partieChelems[i]
            }

            globales.meilleurScore = maxOf(globales.meilleurScore, partieMeilleurScore)
            globales.pireScore = minOf(globales.pireScore, partiePireScore)
        }

        val joueursImmutables = joueurs.mapValues { (_, dataJoueur) ->
            StatistiquesJoueur(
                parties = dataJoueur.parties.mapValues { (_, partie) ->
                    StatistiquesPartieJoueur(
                        contrats = partie.contrats.toList(),
                        poignees = partie.poignees.toList(),
                        chelems = partie.chelems.toList(),
                        miseres = partie.miseres,
                        petitAuBoutGagne = partie.petitAuBoutGagne,
                        petitAuBoutPerdu = partie.petitAuBoutPerdu,
                        preneur = partie.preneur,
                        appele = partie.appele,
                        defense = partie.defense,
                        totalDonnes = partie.totalDonnes,
                        totalParties = partie.totalParties,
                        pointsGagnes = partie.pointsGagnes,
                        pointsPerdus = partie.pointsPerdus,
                        meilleurScore = partie.meilleurScore,
                        pireScore = partie.pireScore,
                    )
                }.toMutableMap()
            )
        }

        val globalesImmutables = StatistiquesGlobales(
            nbParties = globales.nbParties,
            nbDonnes = globales.nbDonnes,
            nbBoutsAttaque = globales.nbBoutsAttaque,
            petitAuBoutGagne = globales.petitAuBoutGagne,
            petitAuBoutPerdu = globales.petitAuBoutPerdu,
            miseres = globales.miseres,
            pointsGagnes = globales.pointsGagnes,
            meilleurScore = globales.meilleurScore,
            pireScore = globales.pireScore,
            contrats = globales.contrats.toList(),
            poignees = globales.poignees.toList(),
            chelems = globales.chelems.toList(),
            gainMin = globales.gainMin,
            gainMax = globales.gainMax,
            mediane = globales.mediane,
            gainMoyenMin = globales.gainMoyenMin,
            gainMoyenMax = globales.gainMoyenMax,
            medianeMoyenne = globales.medianeMoyenne
        )

        return Triple(joueursImmutables, parties.toList(), globalesImmutables)
    }

    private data class MutableStatistiquesJoueur(
        val parties: MutableMap<Int, MutableStatistiquesPartieJoueur> = mutableMapOf(
            3 to MutableStatistiquesPartieJoueur(),
            4 to MutableStatistiquesPartieJoueur(),
            5 to MutableStatistiquesPartieJoueur()
        )
    )

    private data class MutableStatistiquesPartieJoueur(
        val contrats: MutableList<Int> = MutableList(4) { 0 },
        val poignees: MutableList<Int> = MutableList(4) { 0 },
        val chelems: MutableList<Int> = MutableList(3) { 0 },
        var miseres: Int = 0,
        var petitAuBoutGagne: Int = 0,
        var petitAuBoutPerdu: Int = 0,
        var preneur: Int = 0,
        var appele: Int = 0,
        var defense: Int = 0,
        var totalDonnes: Int = 0,
        var totalParties: Int = 0,
        var pointsGagnes: Int = 0,
        var pointsPerdus: Int = 0,
        var meilleurScore: Int = Int.MIN_VALUE,
        var pireScore: Int = Int.MAX_VALUE,
        var gainNet: Int = 0,
        var gainMoyenParDonne: Double = 0.0,
        var gainMoyenParPartie: Double = 0.0,
    )

    private data class MutableStatistiquesGlobales(
        var nbParties: Int = 0,
        var nbDonnes: Int = 0,
        var nbBoutsAttaque: Int = 0,
        var petitAuBoutGagne: Int = 0,
        var petitAuBoutPerdu: Int = 0,
        var miseres: Int = 0,
        var pointsGagnes: Int = 0,
        var meilleurScore: Int = Int.MIN_VALUE,
        var pireScore: Int = Int.MAX_VALUE,
        val contrats: MutableList<Int> = MutableList(4) { 0 },
        val poignees: MutableList<Int> = MutableList(4) { 0 },
        val chelems: MutableList<Int> = MutableList(3) { 0 },
        var gainMin: Int = Int.MAX_VALUE,
        var gainMax: Int = Int.MIN_VALUE,
        var mediane: Int = 0,
        var gainMoyenMin: Double = 0.0,
        var gainMoyenMax: Double = 0.0,
        var medianeMoyenne: Double = 0.0
    )
}