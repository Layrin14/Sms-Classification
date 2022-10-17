package com.layrin.smsclassification.ui.contact

import android.content.Context
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.layrin.smsclassification.R
import com.layrin.smsclassification.app.RepositoryModule
import com.layrin.smsclassification.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.endsWith
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@MediumTest
@HiltAndroidTest
@UninstallModules(RepositoryModule::class)
class ContactFragmentTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var context: Context

    @Before
    fun setUp() {
        hiltRule.inject()
        context = InstrumentationRegistry.getInstrumentation().context
    }

    @Test
    fun pressBackButton_shouldNavigateToConversationFragment() {
        val navController = mock(NavController::class.java)
        launchFragmentInHiltContainer<ContactFragment> {
            Navigation.setViewNavController(requireView(), navController)
        }
        pressBack()
        verify(navController).popBackStack()
    }

    @Test
    fun selectOneContact_shouldNavigateToMessageFragment() {
        val navController = mock(NavController::class.java)
        launchFragmentInHiltContainer<ContactFragment> {
            Navigation.setViewNavController(requireView(), navController)
        }
        onView(withId(R.id.rv_contact)).perform(
            RecyclerViewActions.actionOnItemAtPosition<ContactAdapter.ContactViewHolder>(
                0,
                click()
            )
        )
        verify(navController).navigate(
            ContactFragmentDirections.actionContactFragmentToMessageFragment("a")
        )
    }

    @Test
    fun searchContact_shouldDisplayFoundItem() {
        launchFragmentInHiltContainer<ContactFragment> { }
        onView(
            allOf(
                isDescendantOfA(withId(R.id.et_search_contact)),
                withClassName(endsWith("EditText"))
            )
        ).perform(
            typeText("2"),
        )
        onView(withId(R.id.rv_contact))
            .check(
                itemHasCorrectText(0, R.id.tv_contact_name, "2")
            )
    }

    private fun itemHasCorrectText(
        position: Int,
        @IdRes id: Int,
        text: String,
    ): ViewAssertion {
        return ViewAssertion { view, _ ->

            val itemView: TextView =
                (view as RecyclerView).findViewHolderForAdapterPosition(position)!!.itemView.findViewById(
                    id)

            Assert.assertTrue(
                "Wrong background text $position, the text was ${itemView.text}",
                itemView.text.contains(text)
            )
        }
    }

}