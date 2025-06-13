package com.github.walkandtag.ui.components

import android.graphics.drawable.Icon
import android.util.Log
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable

class NavbarBuilder {
    val buttonItems: ArrayList<Pair<Any, Icon>> = ArrayList<Pair<Any, Icon>>()

    fun addButton(endpoint: Any, icon: Icon) {
        buttonItems.add(Pair<Any, Icon>(endpoint, icon))
    }

    @Composable
    fun Navbar() {
        NavigationBar {
            buttonItems.forEach {
                NavigationBarItem(
                    selected = false,
                    onClick = { Log.i("Navbar", it.second.toString()) },
                    icon = { it.first }
                )
            }
        }
    }
}