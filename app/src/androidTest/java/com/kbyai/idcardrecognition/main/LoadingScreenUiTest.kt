package com.kbyai.idcardrecognition.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.kbyai.idcardrecognition.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test

class LoadingScreenUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loadingScreenShowsBrandAndStatus() {
        composeRule.setContent {
            MyApplicationTheme {
                LoadingScreen()
            }
        }

        composeRule.onNodeWithTag("loading-brand").assertIsDisplayed()
        composeRule.onNodeWithText("Verifying your account...").assertIsDisplayed()
    }
}
