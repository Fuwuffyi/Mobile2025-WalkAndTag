package com.github.walkandtag.util

enum class PermissionStatus {
    Unknown, Granted, Denied, PermanentlyDenied;

    val isGranted
        get() = this == Granted
    val isDenied
        get() = this == Denied || this == PermanentlyDenied
}