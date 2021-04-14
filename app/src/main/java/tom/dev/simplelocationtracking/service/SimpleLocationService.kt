package tom.dev.simplelocationtracking.service

import android.Manifest.permission.*
import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleObserver
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationRequest.PRIORITY_LOW_POWER

class SimpleLocationService : Service(), LifecycleObserver {

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
            Log.d("Location Callback", "$location")
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
            Log.d("LifecycleCallbacks", "onActivityResumed")
        }

        override fun onActivityPaused(activity: Activity) {
            Log.d("LifecycleCallbacks", "onActivityPaused")
        }

        override fun onActivityStopped(activity: Activity) {
            Log.d("LifecycleCallbacks", "onActivityStopped")
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            Log.d("LifecycleCallbacks", "onActivitySaveInstanceState")
        }

        override fun onActivityDestroyed(activity: Activity) {
            Log.d("LifecycleCallbacks", "onActivityDestroyed")
        }
    }

    private inner class LocationServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            if (checkSelfPermission(ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED
                && checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
                && checkSelfPermission(ACCESS_BACKGROUND_LOCATION) == PERMISSION_GRANTED
            ) {
                fusedLocationClient.requestLocationUpdates(
                    locationRequestHighAccuracy, locationCallback, looper
                )
            }
        }
    }

    override fun onCreate() {
        // Location Client 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Lifecycle Observer Callback Register
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks)

        HandlerThread("LocationArgs", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()

            // Looper, Handler 초기화 해준다.
            serviceLooper = looper
            serviceHandler = LocationServiceHandler(looper)
        }
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

        fusedLocationClient.removeLocationUpdates(locationCallback)
        application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
    }
}