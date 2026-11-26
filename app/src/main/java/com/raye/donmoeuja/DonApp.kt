package com.raye.donmoeuja

import android.app.Application
import android.content.Context
import com.raye.donmoeuja.data.AppDatabase
import com.raye.donmoeuja.data.AttachmentStore
import com.raye.donmoeuja.data.MoneyRepository
import com.raye.donmoeuja.data.SettingsRepository

class DonApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(context: Context) {
    private val db = AppDatabase.create(context)
    val attachments = AttachmentStore(context)
    val repository = MoneyRepository(db, attachments)
    val settings = SettingsRepository(context)
}
