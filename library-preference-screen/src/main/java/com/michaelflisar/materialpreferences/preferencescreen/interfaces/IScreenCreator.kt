package com.michaelflisar.materialpreferences.preferencescreen.interfaces

import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import com.michaelflisar.materialpreferences.preferencescreen.PreferenceScreen

interface IScreenCreator : Parcelable {

    fun createScreen(
        activity: AppCompatActivity,
        savedInstanceState: Bundle?
    ): PreferenceScreen

    fun onCreate(activity: AppCompatActivity, savedInstanceState: Bundle?) {

    }

    fun onDestroy(activity: AppCompatActivity) {

    }
}