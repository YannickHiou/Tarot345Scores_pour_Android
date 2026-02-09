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

val constantes = ConstantesConfig(
    seuils_bouts = listOf(56, 51, 41, 36),
    multiplicateurs = mapOf(
        "Petite" to 1,
        "Garde" to 2,
        "Garde Sans" to 4,
        "Garde Contre" to 6
    ),
    petit_au_bout = 10,
    chelem = ChelemConfig(
        annonce_reussi = 400,
        non_annonce_reussi = 200,
        annonce_rate = -200
    ),
    misere_penalite = 10,
    base_const = 25,
    poignee_values = mapOf(
        "NONE" to 0,
        "SIMPLE" to 20,
        "DOUBLE" to 30,
        "TRIPLE" to 40
    ),
    poignee_atouts = mapOf(
        "3" to mapOf("SIMPLE" to 13, "DOUBLE" to 15, "TRIPLE" to 18),
        "4" to mapOf("SIMPLE" to 10, "DOUBLE" to 13, "TRIPLE" to 15),
        "5" to mapOf("SIMPLE" to 8, "DOUBLE" to 10, "TRIPLE" to 13)
    )
)