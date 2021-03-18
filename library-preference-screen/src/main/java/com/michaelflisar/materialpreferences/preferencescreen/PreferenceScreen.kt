package com.michaelflisar.materialpreferences.preferencescreen

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.annotation.MainThread
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.michaelflisar.materialpreferences.preferencescreen.interfaces.PreferenceItem
import com.michaelflisar.materialpreferences.preferencescreen.recyclerview.PreferenceAdapter

class PreferenceScreen(
        val preferences: List<PreferenceItem>,
        savedInstanceState: Bundle?,
        onScreenChanged: ((level: Int) -> Unit)? = null
) {

    companion object {
        val KEY_ADAPTER_STATE = PreferenceScreen::class.java.name
    }

    private val adapter: PreferenceAdapter = PreferenceAdapter(preferences, onScreenChanged)

    init {
        savedInstanceState?.getParcelable<PreferenceAdapter.SavedState>(KEY_ADAPTER_STATE)?.let(::loadSavedState)
    }

    fun bind(recyclerView: RecyclerView) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(recyclerView.context, RecyclerView.VERTICAL, false)
            adapter = this@PreferenceScreen.adapter
            layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.preference_layout_fall_down)
        }
        adapter.recyclerView = recyclerView
    }

    fun onBackPressed() = adapter.onBackPressed()

    @MainThread
    fun loadSavedState(state: PreferenceAdapter.SavedState) {
        adapter.restoreState(state.stack)
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_ADAPTER_STATE, adapter.getSavedState())
    }
}