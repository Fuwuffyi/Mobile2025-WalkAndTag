package com.github.walkandtag.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

class DialogBuilder(
    val title: String, val onDismiss: () -> Unit, val onConfirm: (Map<String, String>) -> Unit
) {
    val inputFields: MutableMap<String, Pair<String, Boolean>> = mutableMapOf()

    fun addInput(name: String, initialValue: String = "", multiLine: Boolean = false): DialogBuilder {
        inputFields[name] = Pair(initialValue, multiLine)
        return this
    }

    @Composable
    fun Dialog() {
        val fieldStates = remember {
            inputFields.mapValues { mutableStateOf(it.value.first) }.toMutableMap()
        }
        AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = {
            Column {
                fieldStates.forEach { (name, state) ->
                    OutlinedTextField(
                        value = state.value,
                        onValueChange = { fieldStates[name]?.value = it },
                        label = { Text(name) },
                        maxLines = if (inputFields[name]!!.second) 5 else 1,
                        singleLine = !inputFields[name]!!.second
                    )
                }
            }
        }, confirmButton = {
            TextButton(onClick = {
                val result = fieldStates.mapValues { it.value.value }
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
