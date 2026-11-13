package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Yusuf Electronics", appName)
  }

  @Test
  fun `mainActivity launches safely with or without firebase initialized`() {
    // Launches MainActivity without google-services.json initialized -> renders cleanly without crashing
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("YUSUF ELECTRONICS", substring = true).assertIsDisplayed()

    // Now initialize FirebaseApp and recreate/launch MainActivity -> renders AuthScreen cleanly without crashing
    val context = ApplicationProvider.getApplicationContext<Context>()
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(
        context,
        FirebaseOptions.Builder()
          .setApplicationId("1:904064121698:android:e55759bfa4eb3364750a3b")
          .setApiKey("AIzaSyDummyTestKeyForRobolectric123456789")
          .setProjectId("gen-lang-client-0060106260")
          .build()
      )
    }
    val activityController = Robolectric.buildActivity(MainActivity::class.java).setup()
    activityController.pause().stop().destroy()
  }
}
