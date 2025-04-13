package com.example.tripsync


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class IntroActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        viewPager = findViewById(R.id.viewPager)

        val items = listOf(
            IntroItem(R.drawable.logo, "Tripsync"),

        )

        val adapter = IntroAdapter(items)
        viewPager.adapter = adapter

        // Opcional: quando chegar ao último slide, ir para MainActivity
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == items.size - 1) {
                    // Espera 1.5 segundos e vai para o login
                    viewPager.postDelayed({
                        startActivity(Intent(this@IntroActivity, MainActivity::class.java))
                        finish()
                    }, 1500)
                }
            }
        })
    }
}
