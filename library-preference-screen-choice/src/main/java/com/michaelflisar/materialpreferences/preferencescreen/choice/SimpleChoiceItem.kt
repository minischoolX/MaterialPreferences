package com.michaelflisar.materialpreferences.preferencescreen.choice

import com.michaelflisar.text.Text
import com.michaelflisar.text.asText

class SimpleChoiceItem(
        override val label: Text,
        override val id: Int
) : ChoiceItem

fun List<Int>.asChoiceListRes() = this.mapIndexed { index, item -> SimpleChoiceItem(item.asText(), index) }
fun List<String>.asChoiceListString() = this.mapIndexed { index, item -> SimpleChoiceItem(item.asText(), index) }