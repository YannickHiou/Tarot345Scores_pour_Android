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

import kotlin.math.absoluteValue

var TRACE_ENABLED = false

private fun trace(tag: String, msg: String) {
    if (TRACE_ENABLED) {
        //Log.d(tag, msg)
        println("$tag : $msg")
    }
}

fun calculerScores(
    joueurs: List<Joueur>,
    preneurIndex: Int,
    appeleIndex: Int?,
    miseresIndex: List<Int>?,
    pointsAtq: Int,
    nbBoutsAttaque: Int,
    petitAuBout: PetitAuBout?,
    contratNom: String,
    poignees: List<Poignee>,
    chelem: Chelem?,
    constantes: ConstantesConfig,
): List<Int> {
    trace("SCORE_CALC", "=== DÉBUT CALCUL DONNE ===")

    val nbJoueurs = joueurs.size
    val scores = MutableList(nbJoueurs) { 0 }

    trace("SCORE_CALC", "Point réalisés: $pointsAtq")

    val nbBouts = nbBoutsAttaque.coerceIn(0, constantes.seuils_bouts.size - 1)
    trace("SCORE_CALC", "Bout à l'attaque: $nbBouts")

    val seuil = constantes.seuils_bouts[nbBouts]
    trace("SCORE_CALC", "Seuil: $seuil")

    val delta = pointsAtq - seuil
    val pointComptes = kotlin.math.abs(delta)

    val baseConst = constantes.base_const
    val multiplicateur = constantes.multiplicateurs[contratNom] ?: 1

    val attaqueGagne = delta >= 0
    if (attaqueGagne) {
        trace("SCORE_CALC", "L'attaque gagne de $pointComptes")
    } else {
        trace("SCORE_CALC", "La défence gagne de $pointComptes")
    }

    val basePointMultipliee = (baseConst + pointComptes) * multiplicateur
    trace(
        "SCORE_CALC",
        "Points de base: ($baseConst + |$delta| ) x $multiplicateur = $basePointMultipliee"
    )

    val signe = if (attaqueGagne) 1 else -1
    ajouterPoints(scores, nbJoueurs, preneurIndex, appeleIndex, basePointMultipliee * signe)
    trace("SCORE_CALC", "Score initial:$scores")

    if(petitAuBout != null) {
        val petitAuBoutAttaque = when {
            nbJoueurs == 5 -> (preneurIndex == petitAuBout.index || appeleIndex == petitAuBout.index)
            else -> preneurIndex == petitAuBout.index
        }

        when (petitAuBoutAttaque) {
            true -> trace("SCORE_CALC", "Petit au bout à l'attaque")
            false -> trace("SCORE_CALC", "Petit au bout à la défense")
        }

        val petitAuBoutBonus = when {
            petitAuBoutAttaque == true -> constantes.petit_au_bout * multiplicateur
            else -> -constantes.petit_au_bout * multiplicateur
        }

        if (petitAuBoutBonus != 0) {
            ajouterPoints(scores, nbJoueurs, preneurIndex, appeleIndex, petitAuBoutBonus)
            trace("SCORE_CALC", "Score après petit au bout: $scores")
        }
    } else {
        trace("SCORE_CALC", "Pas de petit au bout")
    }

    if (poignees?.isNotEmpty() == true) {
        poignees?.filter { it.type != PoigneeType.NONE }?.forEach { poignee ->
            val poigneNom = poignee.type.name
            val poigneeValue = constantes.poignee_values[poigneNom] ?: 0

            if (poigneeValue != 0) {
                val mult = if (attaqueGagne) 1 else -1
                ajouterPoints(scores, nbJoueurs, preneurIndex, appeleIndex, poigneeValue * mult)
            }
        }

        trace("SCORE_CALC", "Score après poignées:$scores")
    }

    if (chelem != null) {
        chelem?.let {
            val bonusBase = when {
                it.annonce && it.succes -> constantes.chelem.annonce_reussi
                !it.annonce && it.succes -> constantes.chelem.non_annonce_reussi
                it.annonce && !it.succes -> constantes.chelem.annonce_rate
                else -> 0
            }
            if (bonusBase != 0) {
                ajouterPoints(scores, nbJoueurs, preneurIndex, appeleIndex, bonusBase)
            }
        }
        trace("SCORE_CALC", "Score après chelem:$scores")
    }
    // Ajouter les scores pour chaque misère
    miseresIndex?.forEach { idx ->
        scores[idx] += 10 * (nbJoueurs - 1)
        for (i in 0 until nbJoueurs) {
            if (i != idx) scores[i] -= 10
        }
    }
    trace("SCORE_CALC", "Misères index:$miseresIndex")
    trace("SCORE_CALC", "Score après misères:$scores")

    trace("SCORE_CALC", "=== FIN CALCUL DONNE ===")

    return scores
}

fun ajouterPoints(
    scores: MutableList<Int>,
    nbJoueurs: Int,
    preneurIndex: Int,
    appeleIndex: Int?,
    points: Int
) {
    when {
        nbJoueurs == 5 && preneurIndex == appeleIndex -> {
            scores[preneurIndex] += 4 * points
            for (i in 0 until nbJoueurs) {
                if (i != preneurIndex) scores[i] -= points
            }
        }

        nbJoueurs == 5 -> {
            scores[preneurIndex] += 2 * points
            scores[appeleIndex!!] += points
            for (i in 0 until nbJoueurs) {
                if (i != preneurIndex && i != appeleIndex) scores[i] -= points
            }
        }

        nbJoueurs == 4 -> {
            scores[preneurIndex] += 3 * points
            for (i in 0 until nbJoueurs) {
                if (i != preneurIndex) scores[i] -= points
            }
        }

        else -> {
            scores[preneurIndex] += 2 * points
            for (i in 0 until nbJoueurs) {
                if (i != preneurIndex) scores[i] -= points
            }
        }
    }
}


fun verifierScores(
    joueurs: List<Joueur>,
    preneurIndex: Int,
    appeleIndex: Int?,
    miseresIndex: List<Int>?,
    pointsAtq: Int,
    nbBoutsAttaque: Int,
    petitAuBout: PetitAuBout?,
    contratNom: String,
    poignees: List<Poignee>?,
    chelem: Chelem?,
    scores: List<Int>,
    constantes: ConstantesConfig,
): Boolean {
    val nbJoueurs = joueurs.size
    if (scores.size != nbJoueurs) return false

    val cmpScores = scores.toMutableList()
    trace("SCORE_CALC", "=== DÉBUT VERIFICTION DONNE ===")

    trace("SCORE_CALC", "Score point en entrée: $cmpScores")

    if (miseresIndex?.isNotEmpty() == true) {
        trace("SCORE_CALC", "Misère index: $miseresIndex")

        // Annuler les misères
        miseresIndex?.forEach { idx ->
            cmpScores[idx] -= 10 * (nbJoueurs - 1)
            for (i in 0 until nbJoueurs) {
                if (i != idx) cmpScores[i] += 10
            }
        }
        if (!verifierStructureScores(cmpScores, nbJoueurs, preneurIndex, appeleIndex)) {
            return false
        }
        trace("SCORE_CALC", "Score point après suppression misères: $cmpScores")
    }

    val multiplicateur = constantes.multiplicateurs[contratNom] ?: 1
    val nbBouts = nbBoutsAttaque.coerceIn(0, constantes.seuils_bouts.size - 1)
    val seuil = constantes.seuils_bouts[nbBouts]
    val delta = pointsAtq - seuil
    val attaqueGagne = delta >= 0
    val pointComptes = kotlin.math.abs(delta)
    val baseConst = constantes.base_const

    val signe = if (attaqueGagne) 1 else -1
    val basePointMultipliee = (baseConst + pointComptes) * multiplicateur
    trace(
        "SCORE_CALC",
        "Points de base: ($baseConst + |$delta| ) x $multiplicateur = $basePointMultipliee"
    )

    annulerPoints(cmpScores, nbJoueurs, preneurIndex, appeleIndex, basePointMultipliee * signe)
    if (!verifierStructureScores(cmpScores, nbJoueurs, preneurIndex, appeleIndex)) {
        return false
    }
    trace("SCORE_CALC", "Score point de base enlevés: $cmpScores")

    if (petitAuBout != null) {
        val petitAuBoutAttaque = when {
            nbJoueurs == 5 -> (preneurIndex == petitAuBout.index || appeleIndex == petitAuBout.index)
            else -> preneurIndex == petitAuBout.index
        }

        val petitAuBoutBonus = when {
            petitAuBoutAttaque == true -> constantes.petit_au_bout * multiplicateur
            else -> -constantes.petit_au_bout * multiplicateur
        }

        if (petitAuBoutBonus != 0) {
            annulerPoints(cmpScores, nbJoueurs, preneurIndex, appeleIndex, petitAuBoutBonus)
            trace("SCORE_CALC", "Score après petit au bout: $cmpScores")
        }

        if (!verifierStructureScores(cmpScores, nbJoueurs, preneurIndex, appeleIndex)) {
            return false
        }

        trace("SCORE_CALC", "Score point après suppression Petit au bout : $cmpScores")
        trace("SCORE_CALC", "=== FIN VERIFICATION DONNE ===")
    }

    if (poignees?.isNotEmpty() == true) {
        poignees?.filter { it.type != PoigneeType.NONE }?.forEach { poignee ->
            val poigneNom = poignee.type.name
            val poigneeValue = constantes.poignee_values[poigneNom] ?: 0

            if (poigneeValue != 0) {
                val mult = if (attaqueGagne) 1 else -1
                annulerPoints(cmpScores, nbJoueurs, preneurIndex, appeleIndex, poigneeValue * mult)
            }
        }

        trace("SCORE_CALC", "Score point après suppression des poignées : $cmpScores")
        if (!verifierStructureScores(cmpScores, nbJoueurs, preneurIndex, appeleIndex)) {
            return false
        }
    }

    if (chelem != null) {
        chelem?.let {
            val bonusBase = when {
                it.annonce && it.succes -> constantes.chelem.annonce_reussi
                !it.annonce && it.succes -> constantes.chelem.non_annonce_reussi
                it.annonce && !it.succes -> constantes.chelem.annonce_rate
                else -> 0
            }
            if (bonusBase != 0) {
                annulerPoints(cmpScores, nbJoueurs, preneurIndex, appeleIndex, bonusBase)
            }

            trace("SCORE_CALC", "Score point après suppression du chelem: $cmpScores")
            if (!verifierStructureScores(cmpScores, nbJoueurs, preneurIndex, appeleIndex)) {
                return false
            }
        }
    }


    val somme = cmpScores.map { it.absoluteValue }.sum()

    if (somme != 0) {
        println()
    }
    return somme == 0
}

private fun verifierStructureScores(
    scores: List<Int>,
    nbJoueurs: Int,
    preneurIndex: Int,
    appeleIndex: Int?
): Boolean {
    val absScores = scores.map { it.absoluteValue }.sorted()
    val valeur = absScores[0]

    return when {
        nbJoueurs == 5 && preneurIndex != appeleIndex -> {
            absScores.take(4).all { it == valeur } && absScores[4] == valeur * 2
        }

        nbJoueurs == 5 && preneurIndex == appeleIndex -> {
            absScores.take(4).all { it == valeur } && absScores[4] == valeur * 4
        }

        nbJoueurs == 4 -> {
            absScores.take(3).all { it == valeur } && absScores[3] == valeur * 3
        }

        nbJoueurs == 3 -> {
            absScores.take(2).all { it == valeur } && absScores[2] == valeur * 2
        }

        else -> false
    }
}

private fun annulerPoints(
    scores: MutableList<Int>,
    nbJoueurs: Int,
    preneurIndex: Int,
    appeleIndex: Int?,
    points: Int
) {
    when {
        nbJoueurs == 5 && preneurIndex == appeleIndex -> {
            scores[preneurIndex] -= 4 * points
            for (i in 0 until nbJoueurs) {
                if (i != preneurIndex) scores[i] += points
            }
        }

        nbJoueurs == 5 -> {
            scores[preneurIndex] -= 2 * points
            scores[appeleIndex!!] -= points
            for (i in 0 until nbJoueurs) {
                if (i != preneurIndex && i != appeleIndex) scores[i] += points
            }
        }

        nbJoueurs == 4 -> {
            scores[preneurIndex] -= 3 * points
            for (i in 0 until nbJoueurs) {
                if (i != preneurIndex) scores[i] += points
            }
        }

        else -> {
            scores[preneurIndex] -= 2 * points
            for (i in 0 until nbJoueurs) {
                if (i != preneurIndex) scores[i] += points
            }
        }
    }
}