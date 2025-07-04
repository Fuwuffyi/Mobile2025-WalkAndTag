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

sealed class InputType {
    data class TextField(val initialValue: String = "", val multiLine: Boolean = false) :
        InputType()

    data class RadioGroup(val options: Collection<String>, val initialSelection: String? = null) :
        InputType()
}

class DialogBuilder(
    val title: String, val onDismiss: () -> Unit, val onConfirm: (Map<String, String>) -> Unit
) {
    private val inputFields: MutableMap<String, InputType> = mutableMapOf()

    fun addInput(
        name: String, initialValue: String = "", multiLine: Boolean = false
    ): DialogBuilder {
        inputFields[name] = InputType.TextField(initialValue, multiLine)
        return this
    }

    fun addRadioGroup(
        name: String, options: Collection<String>, initialSelection: String? = null
    ): DialogBuilder {
        require(options.isNotEmpty()) { "RadioGroup must have at least one option." }
        inputFields[name] = InputType.RadioGroup(options, initialSelection ?: options.first())
        return this
    }

    @Composable
    fun Dialog() {
        val textFieldStates = remember {
            inputFields.filterValues { it is InputType.TextField }
                .mapValues { mutableStateOf((it.value as InputType.TextField).initialValue) }
                .toMutableMap()
        }

        val radioGroupStates = remember {
            inputFields.filterValues { it is InputType.RadioGroup }.mapValues {
                mutableStateOf(
                    (it.value as InputType.RadioGroup).initialSelection ?: ""
                )
            }.toMutableMap()
        }

        AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = {
            Column {
                inputFields.forEach { (name, inputType) ->
                    when (inputType) {
                        is InputType.TextField -> {
                            OutlinedTextField(
                                value = textFieldStates[name]?.value ?: "",
                                onValueChange = { textFieldStates[name]?.value = it },
                                label = { Text(name) },
                                maxLines = if (inputType.multiLine) 5 else 1,
                                singleLine = !inputType.multiLine
                            )
                        }

                        is InputType.RadioGroup -> {
                            Text(name)
                            inputType.options.forEach { option ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        radioGroupStates[name]?.value = option
                                    }) {
                                    RadioButton(
                                        selected = radioGroupStates[name]?.value == option,
                                        onClick = { radioGroupStates[name]?.value = option })
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
                Text("Confirm")
            }
        }, dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        })
    }
}
