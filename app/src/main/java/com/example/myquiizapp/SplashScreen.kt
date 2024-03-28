package com.example.myquiizapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth

class SplashScreen : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val splashTimeOut: Long = 3000
        auth = FirebaseAuth.getInstance()

        // Vérifier si l'utilisateur est déjà connecté
        val currentUser = auth.currentUser
        if (currentUser != null) {
//            // Utilisateur connecté, passer à MainActivity
//            startActivity(Intent(this, MainActivity::class.java))
            // Utilisation d'un Handler pour retarder le passage à l'activité suivante
            Handler().postDelayed({
                // Création d'une intention pour passer à l'activité suivante
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

                // Fermer cette activité pour éviter de retourner à l'écran de splash
                finish()
            }, splashTimeOut)
        } else {
            // Utilisateur non connecté, passer à SignInActivity
            // Utilisation d'un Handler pour retarder le passage à l'activité suivante
            Handler().postDelayed({
                // Création d'une intention pour passer à l'activité suivante
                val intent = Intent(this, SignIn::class.java)
                startActivity(intent)

                // Fermer cette activité pour éviter de retourner à l'écran de splash
                finish()
            }, splashTimeOut)
        }




    }
}