package com.github.walkandtag.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController

class NavbarBuilder {
    private val buttonItems: ArrayList<Pair<String, ImageVector>> = ArrayList()

    fun addButton(endpoint: String, icon: ImageVector): NavbarBuilder {
        buttonItems.add(Pair(endpoint, icon))
        return this
    }

    @Composable
    fun Navbar(navController: NavController, currentPage: String, modifier: Modifier = Modifier) {
        NavigationBar(modifier = modifier) {
            buttonItems.forEach {
                val isCurrent: Boolean = currentPage == it.first
                NavigationBarItem(
                    selected = isCurrent,
                    onClick = { if (!isCurrent) navController.navigate(it.first) },
                    icon = { Icon(it.second, it.first) },
                    label = { Text(it.first) },
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}