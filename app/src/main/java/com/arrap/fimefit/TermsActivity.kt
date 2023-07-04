package com.arrap.fimefit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class TermsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_terms)
        setSupportActionBar(toolbar)
        toolbar.title = getString(R.string.p1)
    }
}