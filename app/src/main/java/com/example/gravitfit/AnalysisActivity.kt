package com.example.gravitfit

import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.gravitfit.databinding.ActivityAnalysisBinding
import com.example.gravitfit.ui.analysis.EmgFragment
import com.google.android.material.tabs.TabLayoutMediator

class AnalysisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalysisBinding
    private lateinit var player: ExoPlayer
    private var playWhenReady = true
    private var currentPosition = 0L
    private var isPlayerInitialized = false

    private val openPoseViewModel: OpenPoseViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by viewModels() // ✅ Added

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val athleteId = intent.getStringExtra("ATHLETE_ID") ?: "p01"
        val jumpTypeFromIntent = intent.getStringExtra("JUMP_TYPE") ?: "counter_hands"
        val jumpTypeSuffix = when (jumpTypeFromIntent) {
            "squat" -> "squat"
            "counter" -> "counter"
            "counter_hands" -> "counter_hands"
            "depth" -> "depth"
            "rebound" -> "rebound"
            else -> "counter_hands"
        }

        // ✅ Convert "p03" to 3 and push to SharedViewModel
        val athleteNumber = athleteId.removePrefix("p").toIntOrNull() ?: 1
        sharedViewModel.selectedAthleteId.value = athleteNumber

        openPoseViewModel.setSelection(athleteId, jumpTypeSuffix)
        setupViewPager()
        initializePlayer(athleteId, jumpTypeSuffix)
        setupVideoControls()
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 3
            override fun createFragment(position: Int): Fragment = when (position) {
                0 -> EmgFragment()
                1 -> EmgAnalysisFragment()
                else -> OpenPoseFragment()
            }
        }

        val titles = listOf("EMG", "EMG Analysis", "OpenPose")
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, pos ->
            tab.text = titles[pos]
        }.attach()
    }

    private fun initializePlayer(athleteIdForVideo: String, jumpTypeForVideo: String) {
        try {
            player = ExoPlayer.Builder(this).build()
            binding.playerView.player = player
            player.playWhenReady = playWhenReady
            player.repeatMode = Player.REPEAT_MODE_ONE

            val videoResourceName = "${athleteIdForVideo}_${jumpTypeForVideo}_savgol_skeleton"
            val videoResourceId = resources.getIdentifier(
                videoResourceName,
                "raw",
                packageName
            )

            if (videoResourceId != 0) {
                val rawResourceUri = "android.resource://${packageName}/${videoResourceId}".toUri()
                val mediaItem = MediaItem.fromUri(rawResourceUri)
                player.setMediaItem(mediaItem)
                player.prepare()
                player.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_READY) {
                            binding.seekBar.max = player.duration.toInt()
                            isPlayerInitialized = true
                        }
                    }
                })
            } else {
                tryAlternativeVideo(athleteIdForVideo, jumpTypeForVideo)
            }
        } catch (e: Exception) {
            Log.e("AnalysisActivity", "Error initializing player: ${e.message}")
            Toast.makeText(this, "Failed to load video: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun tryAlternativeVideo(athleteId: String, jumpTypeAttempt: String) {
        // Add your fallback video logic here
    }

    private fun setupVideoControls() {
        binding.btnPlayPause.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
                binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
            } else {
                player.play()
                binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
            }
        }

        binding.btnNextFrame.setOnClickListener { seekFrame(100) }
        binding.btnPrevFrame.setOnClickListener { seekFrame(-100) }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && isPlayerInitialized) {
                    player.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {
                playWhenReady = player.playWhenReady
                player.playWhenReady = false
                binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
            }

            override fun onStopTrackingTouch(sb: SeekBar?) {
                player.playWhenReady = playWhenReady
                if (playWhenReady) {
                    binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                }
            }
        })

        Thread {
            while (!isFinishing) {
                try {
                    if (this::player.isInitialized) {
                        runOnUiThread {
                            binding.seekBar.progress = player.currentPosition.toInt()
                        }
                    }
                    Thread.sleep(50)
                } catch (_: Exception) {}
            }
        }.start()
    }

    private fun seekFrame(deltaMs: Int) {
        if (isPlayerInitialized) {
            val newPos = (player.currentPosition + deltaMs).coerceIn(0, player.duration)
            player.seekTo(newPos)
            binding.seekBar.progress = newPos.toInt()
        }
    }

    override fun onStart() {
        super.onStart()
        if (this::player.isInitialized) {
            player.playWhenReady = playWhenReady
            player.seekTo(currentPosition)
            binding.btnPlayPause.setImageResource(
                if (playWhenReady) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (this::player.isInitialized && playWhenReady) {
            player.play()
            binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
        }
    }

    override fun onPause() {
        super.onPause()
        if (this::player.isInitialized) {
            currentPosition = player.currentPosition
            playWhenReady = player.playWhenReady
            player.pause()
            binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
        }
    }

    override fun onStop() {
        super.onStop()
        if (this::player.isInitialized) {
            player.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::player.isInitialized) {
            player.release()
        }
    }
}
