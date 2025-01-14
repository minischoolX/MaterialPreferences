# Screen Module

| | | | |
| :---: | :---: | :---: | :---: |
| ![Demo](../screenshots/preference-screen-1.jpg?raw=true "Demo") | ![Demo](../screenshots/preference-screen-2.jpg?raw=true "Demo") | ![Demo](../screenshots/preference-screen-3.jpg?raw=true "Demo") | ![Demo](../screenshots/preference-screen-4.jpg?raw=true "Demo") |
| ![Demo](../screenshots/preference-screen-5.jpg?raw=true "Demo") | ![Demo](../screenshots/preference-screen-6.jpg?raw=true "Demo") |  |  |

This modules are placed inside the `screen-*` artifacts.

# Usage with build in settings activity (PREFFERED)

This is an activity with a toolbar and a back button and can be shown as following:

```kotlin
fun showDefaultSettingsActivity(activity: AppCompatActivity) {
    SettingsActivity.start(activity, ScreenCreator)
}

@Parcelize
object ScreenCreator : SettingsActivity.IScreenCreator {
    override fun createScreen(activity: AppCompatActivity, savedInstanceState: Bundle?, updateTitle: (title: String) -> Unit): PreferenceScreen {
        return screen {
             // ... set up your preference screen here
        }
    }
}
```

This uses a "trick" to provide a small and efficient parcelable setup via an `object` to avoid any problems (either memory nor speed wise) with the parcel size limit and still provides a convenient and simple way to use this library without having to write your own settings activity.

# Usage with a custom activity (ALTERNATIVE)

Alternatively you can simple extend `BaseSettingsActivity` and implement the single abstract `createScreen` function there s shown inside the [CustomSettingsActivity](../demo/src/main/java/com/michaelflisar/materialpreferences/demo/activities/CustomSettingsActivity.kt) and with this method you can of course also embed the settings screen inside any bigger layout.

Generally the manual approach works as simple as following:

* create the screen
* bind it to a `RecyclerView`
* forward the back press event to the screen so that it can handle its internal backstack

Here's an example:

https://github.com/MFlisar/MaterialPreferences/blob/bd25854fff6e23a826998013fdf5356fdc9b3bb0/demo/src/main/java/com/michaelflisar/materialpreferences/demo/activities/CustomSettingsActivity.kt#L15-L105

# Example - Screen

Here's an example:

```kotlin
val screen = screen {
  state = savedInstanceState
  category {
    title = "Test App Style".asText()
  }
  input(UserSettingsModel.name) {
    title = "Name".asText()
  }
  switch(UserSettingsModel.alive) {
    title = "Alive".asText()
  }
  subScreen {
    title = "More".asText()
    color(UserSettingsModel.hairColor) {
      title = "Hair Color".asText()
    }
    input(UserSettingsModel.age) {
      title = "Age".asText()
    }
  }
}
```

Check out the [demo app](../demo/src/main/java/com/michaelflisar/materialpreferences/demo) code for more details and especially the screen definitions in the demo here:

https://github.com/MFlisar/MaterialPreferences/blob/dad0db4565464905da10d4dd17f0ba6a3d92ee0e/demo/src/main/java/com/michaelflisar/materialpreferences/demo/DemoSettings.kt#L83-L555

# Supported Settings

Following settings are supported already:

* Category
* Sub Screens (supports nesting)
* Checkbox
* Switch
* Input (text, number, password, ...)
* Buttons
* Color (w/o alpha)
* Slider (Seekbar)

And following features are supported:

* callback to check if a value is allowed to be changed (e.g. to only allow a change in the pro version)
* dependency on other preference (even with custom dependency evaluator)
* badges to display a badge next to a settings title
* restores list state automatically even in nested preferences

# Default Settings

Some values can be defined globally and will be used by all preferences - those default values are stored in the `PreferenceScreenConfig` object. You can change dafault values like following:

```kotlin
PreferenceScreenConfig.apply {
    bottomSheet = true  // default: false
    maxLinesTitle = 1   // default: 1
    maxLinesSummary = 3 // default: 3
}
```

# Credits

Special thanks goes to [ModernAndroidPreferences](https://github.com/Maxr1998/ModernAndroidPreferences) because I copied a few things from there, namely following:
* the root layout xml
* the `RecyclerView` animations
* the badge idea and the badge drawable
* the basic idea for the DSL
