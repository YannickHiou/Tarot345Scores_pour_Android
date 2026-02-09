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

import org.junit.Assert.assertEquals
import org.junit.Test



class Test_3_Joueurs {
    private val joueurs = listOf(
        Joueur("A", "f47ac10b-58cc-4372-a567-0e02b2c3d479"),
        Joueur("B", "9a1b2c3d-4e5f-6789-abcd-ef0123456789"),
        Joueur("C", "d290f1ee-6c54-4b01-90e6-d701748f0851")
    )


    @Test
    fun FFT_01() {
        val preneurIndex = 0
        val contratNom = "Garde"
        val nbBoutsAttaque = 2
        val pointsAtq = 41 + 8
        val miseresIndex = emptyList<Int>()
        val petitAuBout: PetitAuBout? = PetitAuBout( 0)
        val poignees = listOf(Poignee(0, PoigneeType.SIMPLE))
        val chelemArg = null

        val result = calculerScores(
            joueurs = joueurs,
            preneurIndex = preneurIndex,
            appeleIndex = null,
            contratNom = contratNom,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseresIndex = miseresIndex,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = chelemArg,
            constantes = constantes,
        )

        val expected = listOf(212, -106, -106)
        assertEquals(expected, result)
    }

    @Test
    fun FFT_02() {
        val preneurIndex = 0
        val contratNom = "Garde Sans"
        val nbBoutsAttaque = 2
        val pointsAtq = 41 + 4
        val miseresIndex = emptyList<Int>()
        val petitAuBout: PetitAuBout? = PetitAuBout(2)
        val poignees = emptyList<Poignee>()
        val chelemArg = null

        val result = calculerScores(
            joueurs = joueurs,
            preneurIndex = preneurIndex,
            appeleIndex = null,
            contratNom = contratNom,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseresIndex = miseresIndex,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = chelemArg,
            constantes = constantes,
        )

        val expected = listOf(152, -76, -76)
        assertEquals(expected, result)
    }

    @Test
    fun FFT_03() {
        val preneurIndex = 0
        val contratNom = "Petite"
        val nbBoutsAttaque = 2
        val pointsAtq = 41 - 7
        val miseresIndex = emptyList<Int>()
        val petitAuBout: PetitAuBout? = PetitAuBout(0)
        val poignees = listOf(Poignee(0, PoigneeType.SIMPLE))
        val chelemArg = null

        val result = calculerScores(
            joueurs = joueurs,
            preneurIndex = preneurIndex,
            appeleIndex = null,
            contratNom = contratNom,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseresIndex = miseresIndex,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = chelemArg,
            constantes = constantes,
        )

        val expected = listOf(-84, 42, 42)
        assertEquals(expected, result)
    }

    @Test
    fun FFT_04() {
        val preneurIndex = 0
        val contratNom = "Garde"
        val nbBoutsAttaque = 2
        val pointsAtq = 41 + 11
        val miseresIndex = emptyList<Int>()
        val petitAuBout: PetitAuBout? = null
        val poignees = listOf(Poignee(1, PoigneeType.SIMPLE))
        val chelemArg = null

        val result = calculerScores(
            joueurs = joueurs,
            preneurIndex = preneurIndex,
            appeleIndex = null,
            contratNom = contratNom,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseresIndex = miseresIndex,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = chelemArg,
            constantes = constantes,
        )

        val expected = listOf(184, -92, -92)
        assertEquals(expected, result)
    }


    @Test
    fun FFT_05() {
        val preneurIndex = 0
        val contratNom = "Garde"
        val nbBoutsAttaque = 2
        val pointsAtq = 87
        val miseresIndex = emptyList<Int>()
        val petitAuBout: PetitAuBout? = PetitAuBout(0)
        val poignees = listOf(Poignee(0, PoigneeType.SIMPLE))

        val chelemArg = Chelem(
            annonce = true,
            succes = true,
        )
        val result = calculerScores(
            joueurs = joueurs,
            preneurIndex = preneurIndex,
            appeleIndex = null,
            contratNom = contratNom,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseresIndex = miseresIndex,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = chelemArg,
            constantes = constantes,
        )

        val expected = listOf(1164, -582, -582)
        assertEquals(expected, result)
    }

    @Test
    fun perso_01() {
        val preneurIndex = 0
        val contratNom = "Garde Sans"
        val nbBoutsAttaque = 3
        val pointsAtq = 91
        val miseresIndex = emptyList<Int>()
        val petitAuBout: PetitAuBout? = PetitAuBout(0)
        val poignees = listOf(Poignee(0, PoigneeType.SIMPLE))

        val chelemArg = Chelem(
            annonce = false, // chelem non annoncé
            succes = true,
        )
        val result = calculerScores(
            joueurs = joueurs,
            preneurIndex = preneurIndex,
            appeleIndex = null,
            contratNom = contratNom,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseresIndex = miseresIndex,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = chelemArg,
            constantes = constantes,
        )

        val expected = listOf(1160, -580, -580) // OK
        assertEquals(expected, result)
    }

    @Test
    fun perso_02() {
        val preneurIndex = 0
        val contratNom = "Garde Sans"
        val nbBoutsAttaque = 2
        val pointsAtq = 80
        val miseresIndex = emptyList<Int>()
        val petitAuBout: PetitAuBout? = PetitAuBout(2)
        val poignees = listOf(Poignee(0, PoigneeType.SIMPLE))

        val chelemArg = Chelem(
            annonce = true, // chelem annoncé
            succes = false, // perdu
        )
        val result = calculerScores(
            joueurs = joueurs,
            preneurIndex = preneurIndex,
            appeleIndex = null,
            contratNom = contratNom,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseresIndex = miseresIndex,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = chelemArg,
            constantes = constantes,
        )

        val expected = listOf(72, -36, -36) //OK
        assertEquals(expected, result)
    }

    @Test
    fun perso_03() {
        val preneurIndex = 0
        val contratNom = "Garde"
        val nbBoutsAttaque = 2
        val pointsAtq = 41 + 17
        val miseresIndex = listOf(1, 2)
        val petitAuBout: PetitAuBout? = PetitAuBout(2)
        val poignees = listOf(Poignee(0, PoigneeType.DOUBLE))
        val chelemArg = null

        val result = calculerScores(
            joueurs = joueurs,
            preneurIndex = preneurIndex,
            appeleIndex = null,
            contratNom = contratNom,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseresIndex = miseresIndex,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = chelemArg,
            constantes = constantes,
        )

        val expected = listOf(168, -84, -84) // OK
        assertEquals(expected, result)
    }

    @Test
    fun perso_04() {
        val preneurIndex = 0
        val contratNom = "Garde"
        val nbBoutsAttaque = 2
        val pointsAtq = 41 - 5
        val miseresIndex = listOf(1, 2)
        val petitAuBout: PetitAuBout? = PetitAuBout(0)
        val poignees = listOf(Poignee(0, PoigneeType.SIMPLE))
        val chelemArg = null

        val result = calculerScores(
            joueurs = joueurs,
            preneurIndex = preneurIndex,
            appeleIndex = null,
            contratNom = contratNom,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseresIndex = miseresIndex,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = chelemArg,
            constantes = constantes,
        )

        val expected = listOf(-140, 70, 70) //OK
        assertEquals(expected, result)
    }

}
