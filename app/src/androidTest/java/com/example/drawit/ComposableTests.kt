package com.example.drawit
import android.content.Intent
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Rule
import org.junit.Test

class ComposableTests {


    @get:Rule
    val composeTestRule = createAndroidComposeRule<PaintingActivity>()

    @Test
    fun toolButtonsWork() {

        val tools = listOf(
            "brushButton",
            "penButton",
            "eraserButton",
            "fillButton"
        )


        tools.forEach { tag ->

            // Click on tool
            composeTestRule.onNodeWithTag(tag).performClick()
            composeTestRule.waitForIdle()

            // This is selected
            composeTestRule.onNodeWithTag(tag).assertIsSelected()

            // Others aint selected
            tools.filter { it != tag }.forEach { otherTag ->
                composeTestRule.onNodeWithTag(otherTag).assertIsNotSelected()
            }
        }
    }


}