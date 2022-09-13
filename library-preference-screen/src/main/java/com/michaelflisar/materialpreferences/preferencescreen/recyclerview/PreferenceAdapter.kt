package com.michaelflisar.materialpreferences.preferencescreen.recyclerview

import android.content.Context
import android.os.Parcelable
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.michaelflisar.materialpreferences.preferencescreen.ScreenChangedListener
import com.michaelflisar.materialpreferences.preferencescreen.ScreenUtil
import com.michaelflisar.materialpreferences.preferencescreen.ViewHolderFactory
import com.michaelflisar.materialpreferences.preferencescreen.interfaces.PreferenceItem
import com.michaelflisar.materialpreferences.preferencescreen.preferences.SubScreen
import com.michaelflisar.materialpreferences.preferencescreen.recyclerview.viewholders.base.BaseViewHolder
import kotlinx.parcelize.Parcelize
import java.util.*

class PreferenceAdapter(
    context: Context,
    private val preferences: List<PreferenceItem>,
    private val onScreenChanged: ScreenChangedListener?
) : ListAdapter<PreferenceItem, BaseViewHolder<ViewBinding, PreferenceItem>>(PreferenceDiff) {

    //private var currentFilteredPrefs: List<PreferenceItem> = preferences
    private var currentUnfilteredPrefs: List<PreferenceItem> = preferences
    private var hiddenPrefs: MutableSet<PreferenceItem> = HashSet()

    private var stack: Stack<StackEntry> = Stack()
    var dialogInfo: DialogInfo? = null
    var recyclerView: RecyclerView? = null

    protected val scope = (context as LifecycleOwner).lifecycleScope

    init {

        //submitList(currentUnfilteredPrefs)

        val allPreferences = ScreenUtil.flatten(preferences)
        allPreferences
            .filterIsInstance<PreferenceItem.Preference>()
            .forEach { p ->
                p.visibilityDependsOn?.let {
                    it.observe(scope) {
                        if (it) {
                            hiddenPrefs.remove(p)
                            updateCurrentFilteredItems(true)
                        } else {
                            hiddenPrefs.add(p)
                            updateCurrentFilteredItems(true)
                        }
                    }
                }
            }
    }

    private fun updateCurrentFilteredItems(submit: Boolean): List<PreferenceItem> {
        val currentFilteredPrefs = currentUnfilteredPrefs.filter { !hiddenPrefs.contains(it) }
        if (submit)
            submitList(currentFilteredPrefs)
        return currentFilteredPrefs
    }

    fun notifyItemChanged(item: PreferenceItem) {
        val index = currentList.indexOf(item)
        if (index >= 0)
            notifyItemChanged(index)
    }

    override fun getItemViewType(position: Int) = currentList[position].type

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<ViewBinding, PreferenceItem> {
        return ViewHolderFactory.create(
            this,
            parent,
            viewType
        ) as BaseViewHolder<ViewBinding, PreferenceItem>
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<ViewBinding, PreferenceItem>,
        position: Int
    ) {
        val pref = currentList[position]
        holder.bind(pref, false)
    }

    override fun onViewRecycled(holder: BaseViewHolder<ViewBinding, PreferenceItem>) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    fun onSubScreenClicked(subScreen: SubScreen) {
        val index = currentList.indexOf(subScreen)
        stack.push(StackEntry.create(index, recyclerView))
        currentUnfilteredPrefs = subScreen.preferences
        val filtered = updateCurrentFilteredItems(false)
        onScreenChanged(stack.peek(), filtered, true)
    }

    internal fun onBackPressed(): Boolean {
        return if (stack.size > 0) {
            val stackEntry = stack.pop()
            currentUnfilteredPrefs = getCurrentSubScreenPreferencesUnfiltered()
            val filtered = updateCurrentFilteredItems(false)
            onScreenChanged(stackEntry, filtered, false)
            true
        } else false
    }

    private fun onScreenChanged(stackEntry: StackEntry, filtered: List<PreferenceItem>, forward: Boolean) {
        // rerun view animation
        recyclerView?.scheduleLayoutAnimation()
        submitList(filtered) {
            if (!forward)
                restoreView(stackEntry)
            notifyScreenChangedListener(false)
        }
    }

    private fun notifyScreenChangedListener(stateRestored: Boolean) {
        val subScreens = getCurrentSubScreens()
        onScreenChanged?.invoke(subScreens, stateRestored)
    }

    private fun getCurrentSubScreens(): List<SubScreen> {
        val screens = ArrayList<SubScreen>()
        if (stack.size > 0) {
            var p = preferences[stack[0].index] as SubScreen
            screens.add(p)
            stack.asSequence().drop(1).forEach {
                p = p.preferences[it.index] as SubScreen
                screens.add(p)
            }
        }
        return screens
    }

    private fun getCurrentSubScreenPreferencesUnfiltered(): List<PreferenceItem> {
        if (stack.size == 0) {
            return preferences
        } else {
            var p = preferences[stack[0].index] as SubScreen
            stack.asSequence().drop(1).forEach {
                p = p.preferences[it.index] as SubScreen
            }
            return p.preferences
        }
    }

    fun restoreStack(state: SavedState) {
        if (state.stack != stack) {
            stack = Stack()
            state.stack.forEach {
                stack.push(it)
            }
            currentUnfilteredPrefs = getCurrentSubScreenPreferencesUnfiltered()
            updateCurrentFilteredItems(true)
        }
        notifyScreenChangedListener(true)
        dialogInfo = state.dialogShown?.let { DialogInfo(it, null) }
    }

    fun restoreView(state: StackEntry) {
        dialogInfo?.let {
            it.preference = currentList[it.index]
            // the rv will scroll to this item and it will check and reset the showDialog variable itself, nothing more to do here
        }
        if (state.firstVisibleItem != 0 || state.firstVisibleItemOffset != 0) {
            recyclerView?.post {
                (recyclerView?.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
                    state.firstVisibleItem,
                    state.firstVisibleItemOffset
                )
            }
        }
    }

    fun getSavedState(): SavedState {
        val fullStack = stack.clone() as Stack<StackEntry>
        fullStack.push(StackEntry.create(-1, recyclerView))
        return SavedState(ArrayList(fullStack.toList()), dialogInfo?.index)
    }

    @Parcelize
    data class SavedState(
        val stack: ArrayList<StackEntry>,
        val dialogShown: Int?
    ) : Parcelable

    @Parcelize
    data class StackEntry(
        val index: Int,
        val firstVisibleItem: Int,
        val firstVisibleItemOffset: Int
    ) : Parcelable {

        companion object {
            fun create(index: Int, recyclerView: RecyclerView?): StackEntry {
                var position = 0
                var offset = 0
                (recyclerView?.layoutManager as? LinearLayoutManager)?.let {
                    position = it.findFirstCompletelyVisibleItemPosition()
                    offset = recyclerView.findViewHolderForAdapterPosition(position)
                        ?.run { itemView.top }
                        ?: 0
                }
                return StackEntry(index, position, offset)
            }
        }
    }

    class DialogInfo(
        val index: Int,
        var preference: PreferenceItem? = null
    )
}