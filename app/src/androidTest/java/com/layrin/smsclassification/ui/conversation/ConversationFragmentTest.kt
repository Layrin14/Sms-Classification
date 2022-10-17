package com.layrin.smsclassification.ui.conversation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.MediumTest
import com.layrin.smsclassification.R
import com.layrin.smsclassification.app.RepositoryModule
import com.layrin.smsclassification.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@MediumTest
@UninstallModules(RepositoryModule::class)
@HiltAndroidTest
class ConversationFragmentTest {

    @get:Rule
    var instantExecutor = InstantTaskExecutorRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun clickItem_shouldNavigateToMessageFragment() {
        val navController = mock(NavController::class.java)
        launchFragmentInHiltContainer<ConversationFragment> {
            Navigation.setViewNavController(requireView(), navController)
        }

        onView(withId(R.id.rv_conversation)).perform(
            RecyclerViewActions.actionOnItemAtPosition<ConversationAdapter.ConversationViewHolder>(
                0,
                click()
            )
        )
        verify(navController).navigate(
            ConversationFragmentDirections.actionConversationFragmentToMessageFragment("a")
        )
    }

    @Test
    fun longClickItem_DeleteMultipleItem() {
        launchFragmentInHiltContainer<ConversationFragment> {}
        onView(withId(R.id.rv_conversation)).perform(
            RecyclerViewActions.actionOnItemAtPosition<ConversationAdapter.ConversationViewHolder>(
                0,
                longClick()
            )
        )
        onView(withId(R.id.rv_conversation)).perform(
            RecyclerViewActions.actionOnItemAtPosition<ConversationAdapter.ConversationViewHolder>(
                1,
                click()
            )
        )
        onView(withId(R.id.rv_conversation)).perform(
            RecyclerViewActions.actionOnItemAtPosition<ConversationAdapter.ConversationViewHolder>(
                2,
                click()
            )
        )
        onView(withId(R.id.action_delete)).perform(
            click()
        )
        onView(withText(R.string.action_delete))
            .inRoot(isDialog())
            .perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText("3 conversation(s) deleted")))
    }

    @Test
    fun longClickItem_ChangeMultipleItemLabel() {
        launchFragmentInHiltContainer<ConversationFragment> {}
        onView(withId(R.id.rv_conversation)).perform(
            RecyclerViewActions.actionOnItemAtPosition<ConversationAdapter.ConversationViewHolder>(
                0,
                longClick()
            )
        )
        onView(withId(R.id.rv_conversation)).perform(
            RecyclerViewActions.actionOnItemAtPosition<ConversationAdapter.ConversationViewHolder>(
                1,
                click()
            )
        )
        onView(withId(R.id.rv_conversation)).perform(
            RecyclerViewActions.actionOnItemAtPosition<ConversationAdapter.ConversationViewHolder>(
                2,
                click()
            )
        )
        onView(withId(R.id.action_move)).perform(
            click()
        )
        onView(withText(LabelType.Fraud.toString()))
            .inRoot(isDialog())
            .perform(click())
        onView(withText(R.string.action_change))
            .inRoot(isDialog())
            .perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText("3 conversation(s) label changed to ${LabelType.Fraud}")))
    }

    @Test
    fun longClickItem_selectMultipleItem_thenCancel() {
        launchFragmentInHiltContainer<ConversationFragment> {}
        onView(withId(R.id.rv_conversation)).perform(
            RecyclerViewActions.actionOnItemAtPosition<ConversationAdapter.ConversationViewHolder>(
                0,
                longClick()
            )
        )
        onView(withId(R.id.rv_conversation)).perform(
            RecyclerViewActions.actionOnItemAtPosition<ConversationAdapter.ConversationViewHolder>(
                1,
                click()
            )
        )
        onView(withId(R.id.rv_conversation)).perform(
            RecyclerViewActions.actionOnItemAtPosition<ConversationAdapter.ConversationViewHolder>(
                2,
                click()
            )
        )
        onView(withId(R.id.action_move)).perform(
            click()
        )
        onView(withText(LabelType.Fraud.toString()))
            .inRoot(isDialog())
            .perform(click())
        onView(withText(R.string.cancel))
            .inRoot(isDialog())
            .perform(click())
    }
}