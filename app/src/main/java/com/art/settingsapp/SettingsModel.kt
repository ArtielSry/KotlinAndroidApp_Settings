package com.art.settingsapp

data class SettingsModel(
    var volume: Int,
    var vibration: Boolean,
    var darkMode: Boolean,
    var bluetooth: Boolean
)