package com.example.healthia

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    //firebase auth
    var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setLogo(R.drawable.ic_baseline_healing_24)
        supportActionBar?.setDisplayUseLogoEnabled(true)

        setContentView(R.layout.activity_main)

        //init
        firebaseAuth = FirebaseAuth.getInstance()

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_timeline, R.id.navigation_users,
                R.id.navigation_panicBtn, R.id.navigation_notifications, R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        val inflater = menuInflater
//        inflater.inflate(R.menu.option_menu, menu)
//        return true
//    }
//
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_logout){
            firebaseAuth?.signOut()
            checkUserStatus()
        }

        return item.onNavDestinationSelected(findNavController(R.id.nav_host_fragment))
    }
    
    override fun onStart() {
        //check on start of app
        checkUserStatus()
        super.onStart()
    }

    private fun checkUserStatus() {
        //get current user
        val user = firebaseAuth?.currentUser
        if (user != null) {
            //Go to Timeline
        } else {
            //user not signed in, go LoginOrRegisterActivity
            startActivity(Intent(this@MainActivity, LoginOrRegisterActivity::class.java))
            finish()
        }
    }

}