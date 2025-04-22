package com.example.tripsync.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.tripsync.R
import com.example.tripsync.adapters.IntroSliderAdapter

class IntroActivity : AppCompatActivity() {

    private lateinit var introViewPager: ViewPager2
    private lateinit var indicatorsContainer: LinearLayout
    private lateinit var btnNext: Button
    private val PREF_NAME = "MyAppPrefs"
    private val INTRO_SHOWN_KEY = "intro_shown"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar se a introdução já foi mostrada
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean(INTRO_SHOWN_KEY, false)) {
            startMainActivity()
            return
        }

        setContentView(R.layout.activity_intro)

        introViewPager = findViewById(R.id.introViewPager)
        indicatorsContainer = findViewById(R.id.indicatorsContainer)
        btnNext = findViewById(R.id.btnNext)

        // Configurar o adaptador
        val adapter = IntroSliderAdapter(this)
        introViewPager.adapter = adapter

        setupIndicators(adapter.itemCount)
        updateIndicators(0)

        introViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicators(position)

                // Mudar texto do botão no último slide
                if (position == adapter.itemCount - 1) {
                    btnNext.text = "Começar"
                } else {
                    btnNext.text = "Próximo"
                }
            }
        })

        btnNext.setOnClickListener {
            if (introViewPager.currentItem == adapter.itemCount - 1) {
                // Último slide, marcar como visualizado e iniciar atividade principal
                sharedPreferences.edit().putBoolean(INTRO_SHOWN_KEY, true).apply()
                startMainActivity()
            } else {
                // Avançar para o próximo slide
                introViewPager.currentItem++
            }
        }
    }

    private fun setupIndicators(count: Int) {
        val indicators = arrayOfNulls<ImageView>(count)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(8, 0, 8, 0)
        }

        for (i in 0 until count) {
            indicators[i] = ImageView(this)
            indicators[i]?.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.indicator_inactive)
            )
            indicators[i]?.layoutParams = params
            indicatorsContainer.addView(indicators[i])
        }
    }

    private fun updateIndicators(position: Int) {
        for (i in 0 until indicatorsContainer.childCount) {
            val imageView = indicatorsContainer.getChildAt(i) as ImageView
            if (i == position) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.indicator_active)
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.indicator_inactive)
                )
            }
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}