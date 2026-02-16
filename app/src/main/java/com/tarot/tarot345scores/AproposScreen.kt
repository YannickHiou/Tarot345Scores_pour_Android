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


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme

@Composable
fun AproposScreen(onBack: () -> Unit) {
    var showLicenseDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()) // Ajout du défilement ici
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Titre centré sur deux lignes, gras et grand
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tarot",
                color = Color.White,
                fontSize = 55.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Calculatrice de scores",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(25.dp))

            Text(
                text = "Parties de 3, 4 ou 5 joueurs",
                color = Color.White,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Avantages
        Text(
            text = "Points forts",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp)
        ) {
            Text(
                text = "• Gratuité",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )
            Text(
                text = "• Pas de publicité",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )
            Text(
                text = "• Pas de traceur",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            val url = "https://github.com/YannickHiou/Tarot345Scores_pour_Android"

            val annotatedString = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.White)) {
                    append("• Code ouvert publié sur ")
                }
                withLink(
                    LinkAnnotation.Url(
                        url = url,
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = Color.White,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    )
                ) {
                    append("github")
                }
            }

            Text(
                text = annotatedString,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Inconvénients
        Text(
            text = "Points faibles",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp)
        ) {
            Text(text = "• Pas de graphisme", color = Color.White, fontSize = 16.sp)
            Text(text = "• Maintenance épisodique", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(50.dp))

        Text(
            text = "Réalisé avec l'aide d'une IA",
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(50.dp))

        Text(
            text = "Licence GNU GPL v3 (copyleft)",
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .clickable { showLicenseDialog = true }
        )
        Spacer(modifier = Modifier.weight(1f))

        // Bouton Retour centré en bas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(BOUTON_COULEUR),
                    contentColor = Color.Black
                )
            ) {
                Text("Retour")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }

    if (showLicenseDialog) {
        AlertDialog(
            onDismissRequest = { showLicenseDialog = false },
            title = { Text("Licence GNU GPL v3") },
            text = {
                Text(
                    text =
                        "Il est distribué sous licence GNU GPL v3, une licence de logiciel libre " +
                                "à copyleft fort : toute copie ou modification du code source original " +
                                "doit être redistribuée sous la même licence GPL v3.\n\n" +
                                "Vous pouvez utiliser ce code librement, l'étudier, l'enrichir ou y " +
                                "apporter des modifications importantes, puis distribuer vos versions " +
                                "modifiées, à condition de conserver cette licence et de fournir le code source.\n\n" +
                                "Note : Le logo \"Tarot 345 Scores\" reste la propriété de Yannick Hiou " +
                                "et n'est pas couvert par la GPL v3. Son utilisation nécessite une autorisation explicite.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showLicenseDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(BOUTON_COULEUR),
                        contentColor = Color.Black
                    )
                ) {
                    Text("Fermer")
                }
            }
        )
    }
}