package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.architecture.blueprints.todoapp.getOrAwaitValue
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SaveReminderViewModelTest {
    @get:Rule
    var instantExecutorRuleTest = InstantTaskExecutorRule()
    private lateinit var fakeLocalDataSourceForTesting: FakeDataSource
    private lateinit var saveRemindersViewModelTest: SaveReminderViewModel
    private lateinit var reminderDTO: ReminderDataItem

    @Before
    fun setup() {
        fakeLocalDataSourceForTesting = FakeDataSource()
        saveRemindersViewModelTest =
            SaveReminderViewModel(
                ApplicationProvider.getApplicationContext(),
                fakeLocalDataSourceForTesting
            )

    }

    @Test
    fun saveRemindersLocation() {
        reminderDTO = ReminderDataItem(
            title = "saveReminders",
            description = "descriptionReminders",
            location = "locationReminders",
            latitude = 30.043457431,
            longitude = 31.2765762
        )
        saveRemindersViewModelTest.saveReminder(reminderDTO)
        assertThat(
            saveRemindersViewModelTest.showToast.getOrAwaitValue(),
            Is.`is`("Reminder Saved !")
        )

    }

    @Test
    fun saveRemindersLocation_withoutTitle() {
        val reminderDTO = ReminderDataItem(
            title = "",
            description = "descriptionReminders",
            location = "locationReminders",
            latitude = 30.043457431,
            longitude = 31.2765762
        )
        saveRemindersViewModelTest.saveReminder(reminderDTO)
        assertThat(
            saveRemindersViewModelTest.showToast.getOrAwaitValue(),
            CoreMatchers.notNullValue()
        )

    }

    @Test
    fun saveRemindersLocation_withoutDescription() {
        val reminderDTO = ReminderDataItem(
            title = "saveReminders",
            description = "",
            location = "locationReminders",
            latitude = 30.043457431,
            longitude = 31.2765762
        )
        saveRemindersViewModelTest.saveReminder(reminderDTO)
        assertThat(
            saveRemindersViewModelTest.showToast.getOrAwaitValue(),
            CoreMatchers.notNullValue()
        )

    }

    @Test
    fun saveRemindersLocation_withoutLocation() {
        val reminderDTO = ReminderDataItem(
            title = "saveReminders",
            description = "descriptionReminders",
            location = "",
            latitude = 30.043457431,
            longitude = 31.2765762
        )
        saveRemindersViewModelTest.saveReminder(reminderDTO)
        assertThat(
            saveRemindersViewModelTest.showToast.getOrAwaitValue(),
            CoreMatchers.notNullValue()
        )

    }

    @Test
    fun saveRemindersLocation_withoutTitle_and_Description_and_Location() {
        val reminderDTO = ReminderDataItem(
            title = "",
            description = "",
            location = "",
            latitude = 30.043457431,
            longitude = 31.2765762
        )
        saveRemindersViewModelTest.saveReminder(reminderDTO)
        assertThat(
            saveRemindersViewModelTest.showToast.getOrAwaitValue(),
            CoreMatchers.notNullValue()
        )

    }

    @Test
    fun loadSaveReminders_and_showLocationLoading() = runBlockingTest {
        reminderDTO = ReminderDataItem(
            title = "saveReminders",
            description = "descriptionReminders",
            location = "locationReminders",
            latitude = 30.043457431,
            longitude = 31.2765762
        )
        saveRemindersViewModelTest.validateAndSaveReminder(reminderDTO)

        assertThat(saveRemindersViewModelTest.showLoading.getOrAwaitValue(), `is`(false))



    }


}