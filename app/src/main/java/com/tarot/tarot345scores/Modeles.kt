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

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Poignee(
    val index: Int,
    val type: PoigneeType
) : Parcelable

@Parcelize
@Serializable
data class PetitAuBout(
    val index: Int
) : Parcelable

enum class PoigneeType(val displayName: String) {
    NONE("Sans"),
    SIMPLE("Simple"),
    DOUBLE("Double"),
    TRIPLE("Triple")
}

enum class ContratType(val displayName: String) {
    PETITE("Petite"),
    GARDE("Garde"),
    GARDE_SANS("Garde Sans"),
    GARDE_CONTRE("Garde Contre")
}


@Parcelize
@Serializable
data class Joueur(
    val nomUI: String,
    val id: String
) : Parcelable

@Parcelize
@Serializable
data class Partie(
    val id: String,
    val createdAt: Long,
    val joueurs: List<Joueur>,
    val donnes: List<Donne>
) : Parcelable

@Parcelize
@Serializable
data class Donne(
    val id: String,
    val createdAt: Long,
    val preneurIndex: Int,
    val appeleIndex: Int? = null,
    val contratIndex: Int,
    val pointsAtq: Int,
    val nbBoutsAttaque: Int? = null,
    val petitAuBout: PetitAuBout? = null,
    val poignees: List<Poignee> = emptyList(),
    val miseres: List<Int> = emptyList(),
    val chelem: Chelem? = null,
    val scores: List<Int>
) : Parcelable


@Parcelize
@Serializable
data class Chelem(
    val annonce: Boolean,
    val succes: Boolean
) : Parcelable

@Parcelize
@Serializable
data class Historique(
    val parties: MutableList<Partie> = mutableListOf(),
) : Parcelable

@Serializable
data class HistoriqueContext(
    val year: Int,
    val month: Int,
    val day: Int,
    val nbJoueurs: Int
)

@Parcelize
@Serializable
data class ChelemConfig(
    val annonce_reussi: Int,
    val non_annonce_reussi: Int,
    val annonce_rate: Int
) : Parcelable

@Parcelize
@Serializable
data class ConstantesConfig(
    val seuils_bouts: List<Int>,
    val multiplicateurs: Map<String, Int>,
    val petit_au_bout: Int,
    val chelem: ChelemConfig,
    val misere_penalite: Int = 10,
    val base_const: Int,
    val poignee_values: Map<String, Int>,
    val poignee_atouts: Map<String, Map<String, Int>>
) : Parcelable

enum class ChelemUIType(val displayName: String) {
    AUCUN("Aucun chelem"),
    ANNONCE_GAGNE("Chelem annoncé gagné"),
    ANNONCE_PERDU("Chelem annoncé perdu"),
    NON_ANNONCE("Chelem non annoncé")
}
