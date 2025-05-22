package com.example.gravitfit  // match your actual package

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class JumpsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jumps)

        val athleteId = intent.getStringExtra("ATHLETE_ID") ?: "p01"

        findViewById<Button>(R.id.btnSquatJump).setOnClickListener {
            val intent = Intent(this, AnalysisActivity::class.java)
            intent.putExtra("ATHLETE_ID", athleteId)
            intent.putExtra("JUMP_TYPE", "squat")
            startActivity(intent)
        }
        findViewById<Button>(R.id.btnCMJ).setOnClickListener {
            val intent = Intent(this, AnalysisActivity::class.java)
            intent.putExtra("ATHLETE_ID", athleteId)
            intent.putExtra("JUMP_TYPE", "counter")
            startActivity(intent)
        }
        findViewById<Button>(R.id.btnCWHJ).setOnClickListener {
            val intent = Intent(this, AnalysisActivity::class.java)
            intent.putExtra("ATHLETE_ID", athleteId)
            intent.putExtra("JUMP_TYPE", "counter_hands")
            startActivity(intent)
        }
        findViewById<Button>(R.id.btnDJ).setOnClickListener {
            val intent = Intent(this, AnalysisActivity::class.java)
            intent.putExtra("ATHLETE_ID", athleteId)
            intent.putExtra("JUMP_TYPE", "depth")
            startActivity(intent)
        }
        findViewById<Button>(R.id.btnRCJ).setOnClickListener {
            val intent = Intent(this, AnalysisActivity::class.java)
            intent.putExtra("ATHLETE_ID", athleteId)
            intent.putExtra("JUMP_TYPE", "rebound")
            startActivity(intent)
        }
    }
}
