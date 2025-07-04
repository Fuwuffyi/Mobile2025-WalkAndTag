package com.github.walkandtag.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.walkandtag.R

sealed class InputType {
    data class TextField(val initialValue: String = "", val multiLine: Boolean = false) :
        InputType()

    data class RadioGroup(val options: Collection<String>, val initialSelection: String? = null) :
        InputType()
}

class DialogBuilder(
    val title: String, val onDismiss: () -> Unit, val onConfirm: (Map<String, String>) -> Unit
) {
    private val inputFields: MutableMap<String, Pair<String, InputType>> = mutableMapOf()

    fun addInput(
        id: String, label: String, initialValue: String = "", multiLine: Boolean = false
    ): DialogBuilder {
        inputFields[id] = label to InputType.TextField(initialValue, multiLine)
        return this
    }

    fun addRadioGroup(
        id: String, label: String, options: Collection<String>, initialSelection: String? = null
    ): DialogBuilder {
        require(options.isNotEmpty()) { "RadioGroup must have at least one option." }
        inputFields[id] =
            label to InputType.RadioGroup(options, initialSelection ?: options.first())
        return this
    }

    @Composable
    fun Dialog() {
        val textFieldStates = remember {
            inputFields.filterValues { it.second is InputType.TextField }
                .mapValues { mutableStateOf((it.value.second as InputType.TextField).initialValue) }
                .toMutableMap()
        }
        val radioGroupStates = remember {
            inputFields.filterValues { it.second is InputType.RadioGroup }.mapValues {
                mutableStateOf(
                    (it.value.second as InputType.RadioGroup).initialSelection ?: ""
                )
            }.toMutableMap()
        }
        AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = {
            Column {
                inputFields.forEach { (id, pair) ->
                    val (label, inputType) = pair
                    when (inputType) {
                        is InputType.TextField -> {
                            OutlinedTextField(
                                value = textFieldStates[id]?.value ?: "",
                                onValueChange = { textFieldStates[id]?.value = it },
                                label = { Text(label) },
                                maxLines = if (inputType.multiLine) 5 else 1,
                                singleLine = !inputType.multiLine
                            )
                        }

                        is InputType.RadioGroup -> {
                            Text(label)
                            inputType.options.forEach { option ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        radioGroupStates[id]?.value = option
                                    }) {
                                    RadioButton(
                                        selected = radioGroupStates[id]?.value == option,
                                        onClick = {
                                            radioGroupStates[id]?.value = option
                                        })
                                    Text(option)
                                }
                            }
                        }
                    }
                }
            }
        }, confirmButton = {
            TextButton(onClick = {
                val result = mutableMapOf<String, String>()
                result.putAll(textFieldStates.mapValues { it.value.value })
                result.putAll(radioGroupStates.mapValues { it.value.value })
                onConfirm(result)
            }) {
                Text(stringResource(R.string.confirm))
            }
        }, dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        })
    }
}
