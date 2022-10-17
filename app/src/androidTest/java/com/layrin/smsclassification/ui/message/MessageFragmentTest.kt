package com.layrin.smsclassification.ui.message

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
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
class MessageFragmentTest {

    @get:Rule
    var instantExecutor = InstantTaskExecutorRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun pressBackButton_shouldNavigateBackToConversationFragment() {
        val bundle = bundleOf(
            "contactPhoneNumber" to "a"
        )
        val navController = mock(NavController::class.java)
        launchFragmentInHiltContainer<MessageFragment>(
            fragmentArgs = bundle
        ) {
            Navigation.setViewNavController(requireView(), navController)
        }
        pressBack()
        verify(navController).popBackStack()
    }

    @Test
    fun longClickItem_deleteMultipleItem() {
        val bundle = bundleOf(
            "contactPhoneNumber" to "a"
        )
        launchFragmentInHiltContainer<MessageFragment>(
            fragmentArgs = bundle
        ) {}
        onView(withId(R.id.rv_message)).perform(
            RecyclerViewActions.actionOnItemAtPosition<MessageAdapter.MessageSendViewHolder>(
                0,
                longClick()
            )
        )
        onView(withId(R.id.rv_message)).perform(
            RecyclerViewActions.actionOnItemAtPosition<MessageAdapter.MessageSendViewHolder>(
                1,
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
            .check(matches(withText("2 message(s) deleted")))
    }

    @Test
    fun longClickItem_selectMultipleItem_thenCancel() {
        val bundle = bundleOf(
            "contactPhoneNumber" to "a"
        )
        launchFragmentInHiltContainer<MessageFragment>(
            fragmentArgs = bundle
        ) {}
        onView(withId(R.id.rv_message)).perform(
            RecyclerViewActions.actionOnItemAtPosition<MessageAdapter.MessageSendViewHolder>(
                0,
                longClick()
            )
        )
        onView(withId(R.id.rv_message)).perform(
            RecyclerViewActions.actionOnItemAtPosition<MessageAdapter.MessageSendViewHolder>(
                1,
                click()
            )
        )
        onView(withId(R.id.rv_message)).perform(
            RecyclerViewActions.actionOnItemAtPosition<MessageAdapter.MessageSendViewHolder>(
                2,
                click()
            )
        )
        onView(withId(R.id.action_delete)).perform(
            click()
        )
        onView(withText(R.string.cancel))
            .inRoot(isDialog())
            .perform(click())
    }
}