package com.github.walkandtag.util

interface MultiplePermissionHandler {
    val statuses: Map<String, PermissionStatus>
    fun launchPermissionRequest()
}
