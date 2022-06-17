package se.thune.toneweaver


import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationBarView
import se.thune.toneweaver.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private val manager = supportFragmentManager
    private val drawFragment : Fragment = DrawFragment()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("I am alive")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.navigation.setOnItemSelectedListener { item ->
            println("Press the press $item")
            when (item.itemId) {
                R.id.navigation_home -> {
                    binding.message.setText(R.string.title_draw)
                    true
                }
                R.id.navigation_dashboard -> {
                    binding.message.setText(R.string.title_dashboard)
                    showFragment(drawFragment)
                    true
                }
                R.id.navigation_notifications -> {
                    binding.message.setText(R.string.title_notifications)
                    true
                }
                else -> false
            }

        }
    }

    private fun showFragment(fragment : Fragment) {
        println("Show the frag")
        val transaction = manager.beginTransaction()
        transaction.replace(R.id.fragment_holder, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
