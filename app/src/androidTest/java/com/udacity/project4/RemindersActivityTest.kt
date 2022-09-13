package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private lateinit var remindersActivityForTest: RemindersActivity
    private lateinit var saveReminderViewModelTest: SaveReminderViewModel

    @get:Rule
    var activityTestRuleTest: ActivityTestRule<RemindersActivity> =
        ActivityTestRule(RemindersActivity::class.java)

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        startKoin {
            modules(listOf(myModule))
        }
        remindersActivityForTest = activityTestRuleTest.activity
        repository = get()

        runBlocking {
            repository.deleteAllReminders()
        }
        saveReminderViewModelTest = GlobalContext.get().koin.get()
    }
    @Test
    fun showAndSaveReminders_showToast(){
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        Espresso.onView(withId(R.id.noDataTextView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.reminderTitle))
            .perform(ViewActions.replaceText("titleTest"))
        Espresso.onView(withId(R.id.reminderDescription))
            .perform(ViewActions.replaceText("descriptionTest"))
        Espresso.onView(withId(R.id.selectedLocation)).perform(ViewActions.click())
        Thread.sleep(1000)
        Espresso.onView(withId(R.id.mapLocation)).perform(ViewActions.longClick())
        Espresso.onView(withId(R.id.btn_save)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.reminder_saved))
            .inRoot(RootMatchers.withDecorView(CoreMatchers.not(remindersActivityForTest.window.decorView))).check(
                ViewAssertions.matches(ViewMatchers.isDisplayed())
            )
        scenario.close()
    }

    @Test
    fun showAndSaveReminders_checkSnackBar(){
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        Espresso.onView(withId(R.id.noDataTextView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.snackbar_text))
            .check(ViewAssertions.matches(ViewMatchers.withText(R.string.err_enter_title)))
        Thread.sleep(1000)
        Espresso.onView(withId(R.id.reminderTitle))
            .perform(ViewActions.replaceText("check title"))
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.snackbar_text))
            .check(ViewAssertions.matches(ViewMatchers.withText(R.string.err_select_location)))
        scenario.close()
    }

}
