package tom.dev.simplelocationtracking.service

import android.Manifest.permission.*
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleObserver
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationRequest.PRIORITY_LOW_POWER
import tom.dev.simplelocationtracking.MainActivity
import tom.dev.simplelocationtracking.R

class SimpleLocationService : Service(), LifecycleObserver {

    private var isServiceRunning = true

    private var serviceLooper: Looper? = null
    private var serviceHandler: LocationServiceHandler? = null

    // Location Client
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationRequestHighAccuracy = LocationRequest.create().apply {
        // 기본 인터벌 10초
        interval = 10000
        fastestInterval = 5000
        priority = PRIORITY_HIGH_ACCURACY
    }

    private val locationRequestLowPower = LocationRequest.create().apply {
        interval = 30000
        fastestInterval = 10000
        priority = PRIORITY_LOW_POWER
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(location: LocationResult) {
            Log.d("Location Callback", "$isServiceRunning: $location")
        }
    }

    private val lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            Log.d("LifecycleCallbacks", "onActivityCreated")
        }

        override fun onActivityStarted(activity: Activity) {
            Log.d("LifecycleCallbacks", "onActivityStarted")
        }

        override fun onActivityResumed(activity: Activity) {
            isServiceRunning = true
            setUpLocationUpdates()
        }

        override fun onActivityPaused(activity: Activity) {
            Log.d("LifecycleCallbacks", "onActivityPaused")
        }

        override fun onActivityStopped(activity: Activity) {
            isServiceRunning = false
            setUpLocationUpdates()
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            Log.d("LifecycleCallbacks", "onActivitySaveInstanceState")
        }

        override fun onActivityDestroyed(activity: Activity) {
            Log.d("LifecycleCallbacks", "onActivityDestroyed")
        }
    }

    /**
     * Thread Handler class
     */
    private inner class LocationServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            // 최초 서비스 핸들러 시작
            setUpLocationUpdates()
            startLifecycleObserve()
        }
    }

    override fun onCreate() {
        // Location Client 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Version Oreo 이상 Notification Channel 필수
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel()

        val locationServiceNotification = createNotificationItem()

        HandlerThread("LocationArgs", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()

            // Looper, Handler 초기화 해준다.
            serviceLooper = looper
            serviceHandler = LocationServiceHandler(looper)
        }

        startForeground(LOCATION_SERVICE_ID, locationServiceNotification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 위치 서비스 시작 토스트 띄움
        Toast.makeText(this, "Location Service Start", Toast.LENGTH_SHORT).show()

        serviceHandler?.obtainMessage()?.also {
            it.arg1 = startId

            serviceHandler?.sendMessage(it)
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        // binding 하지 않을 것이기 때문에 return null
        return null
    }

    override fun onDestroy() {
        Toast.makeText(this, "Location Service Done", Toast.LENGTH_SHORT).show()

        // 진행되고있던 location update, lifecycle observe 모두 멈추어야 한다.
        stopLocationUpdates()
        stopLifecycleObserve()
    }

    // Location Update 설정하는 함수
    private fun setUpLocationUpdates() {
        if (serviceLooper != null) {
            if (ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_BACKGROUND_LOCATION) == PERMISSION_GRANTED
            ) {
                when (isServiceRunning) {
                    true -> {
                        fusedLocationClient.requestLocationUpdates(
                            locationRequestHighAccuracy, locationCallback, serviceLooper!!
                        )
                    }
                    false -> {
                        fusedLocationClient.requestLocationUpdates(
                            locationRequestLowPower, locationCallback, serviceLooper!!
                        )
                    }
                }
            }
        }
    }

    // Location Update 멈추는 함수
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // Lifecycle Observer 등록하는 함수
    private fun startLifecycleObserve() {
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks)
    }

    // Lifecycle Observe 멈추는 함수
    private fun stopLifecycleObserve() {
        application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val locationServiceNotificationChannel = NotificationChannel(
            LOCATION_SERVICE_NOTIFICATION_CHANNEL_ID,
            LOCATION_SERVICE_NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            setShowBadge(false)
        }

        // Notification Channel Creation
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(locationServiceNotificationChannel)
    }

    private fun createNotificationItem(): Notification {
        val pendingIntent = Intent(this, MainActivity::class.java).let {
            PendingIntent.getActivity(this, PENDING_INTENT_RC_CODE, it, 0)
        }

        return NotificationCompat
            .Builder(this, LOCATION_SERVICE_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setNumber(0)
            .setContentIntent(pendingIntent)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("위치 서비스 실행중")
            .build()
    }

    companion object {
        private const val LOCATION_SERVICE_ID = 1
        private const val LOCATION_SERVICE_NOTIFICATION_CHANNEL_ID = "Location Service Notification Channel ID"
        private const val LOCATION_SERVICE_NOTIFICATION_CHANNEL_NAME = "Location Service Notification Channel Name"

        private const val PENDING_INTENT_RC_CODE = 0
    }
}