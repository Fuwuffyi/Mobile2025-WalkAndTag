package com.github.walkandtag.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.walkandtag.ui.navigation.Navigation

class NavbarBuilder {
    private val buttonItems: ArrayList<Triple<Navigation, ImageVector, String>> = ArrayList()

    fun addButton(endpoint: Navigation, icon: ImageVector, text: String): NavbarBuilder {
        buttonItems.add(Triple(endpoint, icon, text))
        return this
    }

    @Composable
    fun Navbar(currentPage: Navigation, changePageCallback: (Navigation) -> Unit) {
        NavigationBar {
            buttonItems.forEach {
                val isCurrent: Boolean = currentPage == it.first
                NavigationBarItem(
                    selected = isCurrent,
                    onClick = { changePageCallback(it.first) },
                    icon = { Icon(it.second, it.third) },
                    label = { Text(it.third) }
                )
            }
        }
    }
}