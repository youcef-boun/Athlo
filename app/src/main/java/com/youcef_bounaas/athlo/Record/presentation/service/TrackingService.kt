package com.youcef_bounaas.athlo.Record.presentation.service


import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.mapbox.geojson.Point
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.youcef_bounaas.athlo.Record.data.LocationBroadcaster
import com.youcef_bounaas.athlo.Record.utils.calculateTotalDistanceInKm
import com.youcef_bounaas.athlo.Record.utils.haversineDistance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.*

class TrackingService : Service() {

    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val pathPoints = mutableListOf<Point>()
    private var totalDistance = 0f
    private var elapsedSeconds = 0
    private var timer: Job? = null
    private var isPaused = false
    private var isReceiverRegistered = false

    companion object {
        const val NOTIF_ID = 1001
        const val ACTION_PAUSE = "com.Athlo.ACTION_PAUSE"
        const val ACTION_RESUME = "com.Athlo.ACTION_RESUME"
    }

    private val commandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_PAUSE -> {
                    isPaused = true
                    Log.d("TrackingService", "⏸️ Timer paused")
                }
                ACTION_RESUME -> {
                    isPaused = false
                    Log.d("TrackingService", "▶️ Timer resumed")
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location: Location = result.lastLocation ?: return
                if (location.accuracy > 20f) return

                val point = Point.fromLngLat(location.longitude, location.latitude)
                val last = pathPoints.lastOrNull()
                if (last == null || haversineDistance(last, point) > 5.0) {
                    pathPoints.add(point)
                    if (pathPoints.size >= 2) {
                        totalDistance = calculateTotalDistanceInKm(listOf(pathPoints))
                    }
                    LocationBroadcaster.broadcast(point)
                }
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, createNotification(0f, 0))
        startTimer()

        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(ACTION_PAUSE)
                addAction(ACTION_RESUME)
            }
            registerReceiver(commandReceiver, filter)
            isReceiverRegistered = true
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 2000L
            fastestInterval = 1000L
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        locationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        return START_STICKY
    }

    private fun startTimer() {
        timer = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                delay(1000)
                if (!isPaused) {
                    elapsedSeconds++
                    updateNotification()
                }
            }
        }
    }

    private fun stopTimer() {
        timer?.cancel()
    }

    override fun onDestroy() {
        stopTimer()
        locationClient.removeLocationUpdates(locationCallback)

        if (isReceiverRegistered) {
            unregisterReceiver(commandReceiver)
            isReceiverRegistered = false
        }

        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(distanceKm: Float, seconds: Int): Notification {
        val channelId = "tracking_channel"
        val channelName = "Tracking"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            manager.createNotificationChannel(channel)
        }

        val timeStr = String.format("%02d:%02d", seconds / 60, seconds % 60)
        val distanceStr = String.format("%.2f km", distanceKm)
        val contentText = "Time: $timeStr • Distance: $distanceStr"

        val intent = Intent(this, Class.forName("com.youcef_bounaas.athlo.MainActivity")).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Athlo is tracking your run")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateNotification() {
        val notification = createNotification(totalDistance, elapsedSeconds)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIF_ID, notification)
    }
}
