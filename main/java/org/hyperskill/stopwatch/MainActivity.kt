package org.hyperskill.stopwatch

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat

const val CHANNEL_ID = "org.hyperskill"

class MainActivity : AppCompatActivity() {
    private fun stringTime(seconds: Int): String {
        val min = seconds / 60
        val sec = seconds - min * 60
        return "${String.format("%02d", min)}:${String.format("%02d", sec)}"
    }
    private fun notification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification"
            val descriptionText = "Time exceeded"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            val intent = Intent(applicationContext, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)

            val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentTitle("Stopwatch")
                    .setContentText("Time Over!")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)

            val notification = notificationBuilder.build()
            notification.flags = notification.flags or Notification.FLAG_INSISTENT
            notificationManager.notify(393939, notification)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val textView = findViewById<TextView>(R.id.textView)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val settingsButton = findViewById<Button>(R.id.settingsButton)
        progressBar.visibility = View.INVISIBLE
        val handler = Handler(Looper.getMainLooper())
        var started = false
        var seconds = 0
        val colors = arrayOf(Color.RED, Color.GREEN, Color.BLUE)
        var color = colors[0]
        var limit: Int? = null

        val updateTime: Runnable = object : Runnable {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun run() {
                color = colors[(colors.indexOf(color) + 1) % colors.size]
                progressBar.indeterminateTintList = ColorStateList.valueOf(color)
                textView.text = stringTime(seconds)
                seconds++
                if (limit != null && seconds > limit!!) {
                    textView.setTextColor(Color.RED)
                    if (seconds - limit!! == 1 && limit!! > 0) notification()
                }
                if (started) handler.postDelayed(this, 1000)
            }
        }

        findViewById<Button>(R.id.startButton).setOnClickListener {
            if (!started) {
                settingsButton.isEnabled = false
                settingsButton.isClickable = false
                progressBar.visibility = View.VISIBLE
                started = true
                handler.post(updateTime)
            }
        }

        findViewById<Button>(R.id.resetButton).setOnClickListener {
            progressBar.visibility = View.INVISIBLE
            started = false
            seconds = 0
            textView.setTextColor(Color.BLACK)
            textView.text = stringTime(seconds)
            settingsButton.isEnabled = true
            settingsButton.isClickable = true
        }

        findViewById<Button>(R.id.settingsButton).setOnClickListener {
            val contentView = LayoutInflater.from(this).inflate(R.layout.alert_dialog, null, false)
            AlertDialog.Builder(this)
                    .setTitle("Set upper limit in seconds")
                    .setView(contentView)
                    .setPositiveButton("OK") { _, _ ->
                        val editText = contentView.findViewById<EditText>(R.id.upperLimitEditText)
                        limit = editText.text.toString().toInt()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
        }
    }
}