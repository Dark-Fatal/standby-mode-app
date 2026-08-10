package com.example.standby

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lancer le service en premier plan
        startForegroundService(Intent(this, StandbyService::class.java))
        Toast.makeText(this, "Mode Veille activé", Toast.LENGTH_SHORT).show()

        // Fermer l'activité immédiatement
        finish()
    }
}