package tn.esprit.Troc.ui.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import tn.esprit.Troc.R
import tn.esprit.Troc.ui.fragments.ChatFragment
import tn.esprit.Troc.ui.fragments.PostsFragment
import tn.esprit.Troc.ui.fragments.SearchFragment
import tn.esprit.Troc.ui.fragments.SocialFragment

class MainActivity : AppCompatActivity() {

    private var bottomNavigation: BottomNavigationView? = null
    private var fab: FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.my_toolbar))

        fab = findViewById(R.id.fab)
        bottomNavigation = findViewById(R.id.bottom_navigation)


        fab!!.setOnClickListener {
            val intent = Intent(this@MainActivity, CreateActivity::class.java)
            startActivity(
                intent,
                ActivityOptions.makeCustomAnimation(baseContext, R.anim.slide_in_down, R.anim.slide_out_up).toBundle()
            )
        }

        bottomNavigation!!.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_page -> {
                    supportActionBar?.title = "Posts for you"
                    setFragment(PostsFragment())
                    true
                }
                R.id.search_page -> {
                    supportActionBar?.title = "Search anything"
                    setFragment(SearchFragment())
                    true
                }
                R.id.chat_page -> {
                    supportActionBar?.title = "Talk to friends"
                    setFragment(ChatFragment())
                    true
                }
                R.id.social_page -> {
                    supportActionBar?.title = "Find friends"
                    setFragment(SocialFragment())
                    true
                }
                else -> false
            }
        }

        setFragment(PostsFragment())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_profile -> {
            val intent = Intent(this@MainActivity, ProfileActivity::class.java)
            startActivity(
                intent,
                ActivityOptions.makeCustomAnimation(baseContext, R.anim.slide_in_up, R.anim.slide_out_up).toBundle()
            )
            true
        }
        R.id.action_settings -> {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun setFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainer, fragment)
                .setCustomAnimations(
                    R.anim.enter_from_left,
                    R.anim.exit_to_right
                ).commit()

        }
    }
}