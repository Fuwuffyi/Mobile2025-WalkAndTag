package com.github.walkandtag.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

class NavbarBuilder {
    private val buttonItems: ArrayList<Pair<String, ImageVector>> = ArrayList()

    fun addButton(endpoint: String, icon: ImageVector): NavbarBuilder {
        buttonItems.add(Pair(endpoint, icon))
        return this
    }

    @Composable
    fun Navbar(currentPage: String, changePageCallback: (String) -> Unit) {
        NavigationBar {
            buttonItems.forEach {
                val isCurrent: Boolean = currentPage == it.first
                NavigationBarItem(
                    selected = isCurrent,
                    onClick = { changePageCallback(it.first) },
                    icon = { Icon(it.second, it.first) },
                    label = { Text(it.first) }
                )
            }
        }
    }
}