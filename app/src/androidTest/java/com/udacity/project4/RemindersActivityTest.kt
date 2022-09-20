package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest :
    AutoCloseKoinTest() {

    private lateinit var reminderDataSource: ReminderDataSource
    private lateinit var appContextTest: Application
    private val binding = DataBindingIdlingResource()

    @Before
    fun init() {
        stopKoin()
        appContextTest = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContextTest,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContextTest,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContextTest) }
        }
        startKoin {
            modules(listOf(myModule))
        }
        reminderDataSource = get()
        runBlocking {
            reminderDataSource.deleteAllReminders()
        }
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(binding)
    }
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(binding)
    }


    @Test
    fun saveReminder_showBarMessageTitleError() {

        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        binding.monitorActivity(scenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())

        val barMessage = appContextTest.getString(R.string.err_enter_title)
        onView(withText(barMessage)).check(matches(isDisplayed()))

        scenario.close()
    }

    @Test
    fun saveReminder_showBarMessageLocationError() {

        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        binding.monitorActivity(scenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("Title"))
        closeSoftKeyboard()
        onView(withId(R.id.saveReminder)).perform(click())

        val barMessage = appContextTest.getString(R.string.err_select_location)
        onView(withText(barMessage)).check(matches(isDisplayed()))

        scenario.close()
    }

    @Test
    fun saveReminder_showToastMessage() {

        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        binding.monitorActivity(scenario)
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("Title"))
        closeSoftKeyboard()
        onView(withId(R.id.reminderDescription)).perform(typeText("Description"))
        closeSoftKeyboard()
        onView(withId(R.id.selectedLocation)).perform(click())
        onView(withId(R.id.mapLocation)).perform(longClick())
        onView(withId(R.id.btn_save)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withText(R.string.reminder_saved)).inRoot(withDecorView(not(`is`(getActivity(scenario).window.decorView))))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    private fun getActivity(scenario: ActivityScenario<RemindersActivity>): Activity {
        lateinit var activity: Activity
        scenario.onActivity {
            activity = it
        }
        return activity
    }


}
