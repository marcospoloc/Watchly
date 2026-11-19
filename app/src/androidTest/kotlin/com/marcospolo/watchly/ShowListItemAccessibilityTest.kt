package com.marcospolo.watchly

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.marcospolo.watchly.domain.model.Show
import com.marcospolo.watchly.ui.search.SearchScreen
import com.marcospolo.watchly.ui.search.SearchUiState
import com.marcospolo.watchly.ui.theme.WatchlyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ShowListItemAccessibilityTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val lost = Show(1, "Lost", null, 2004, listOf("Drama"), 8.3)

    @Test
    fun resultIsASingleFocusableButtonWithAClickLabel() {
        var clicked: Show? = null
        composeRule.setContent {
            WatchlyTheme {
                SearchScreen("lost", SearchUiState.Success(listOf(lost)), {}, {}, {}, { clicked = it })
            }
        }

        // The merged node contains the title, year and rating and exposes a labelled click action.
        composeRule.onNodeWithText("Lost")
            .assertHasClickAction()
            .assert(SemanticsMatcher("has an onClick label") { it.config[SemanticsActions.OnClick].label != null })
            .performClick()

        assertEquals(lost, clicked)
    }

    @Test
    fun appTitleIsAHeading() {
        composeRule.setContent {
            WatchlyTheme { SearchScreen("", SearchUiState.Idle, {}, {}, {}, {}) }
        }

        composeRule.onNodeWithText("Watchly")
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
    }
}
