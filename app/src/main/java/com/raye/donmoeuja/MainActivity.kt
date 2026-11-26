package com.raye.donmoeuja

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.raye.donmoeuja.ui.DonMoeujaApp
import com.raye.donmoeuja.ui.theme.DonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DonTheme {
                DonMoeujaApp()
            }
        }
    }
}
