package com.raye.donmoeuja.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.raye.donmoeuja.AppContainer
import com.raye.donmoeuja.DonApp
import com.raye.donmoeuja.ui.detail.DetailViewModel
import com.raye.donmoeuja.ui.edit.EditViewModel
import com.raye.donmoeuja.ui.home.HomeViewModel
import com.raye.donmoeuja.ui.settings.SettingsViewModel
import com.raye.donmoeuja.ui.stats.StatsViewModel

private fun CreationExtras.container(): AppContainer =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DonApp).container

object AppViewModels {
    val Factory = viewModelFactory {
        initializer { HomeViewModel(container().repository, container().settings) }
        initializer { EditViewModel(createSavedStateHandle(), container().repository) }
        initializer { DetailViewModel(createSavedStateHandle(), container().repository) }
        initializer { StatsViewModel(container().repository, container().settings) }
        initializer { SettingsViewModel(container().repository) }
    }
}
