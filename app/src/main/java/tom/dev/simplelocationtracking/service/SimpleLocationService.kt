package tom.dev.simplelocationtracking.service

import android.Manifest.permission.*
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.*
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

class SimpleLocationService : Service() {

    private var serviceLooper: Looper? = null
    private var serviceHandler: LocationServiceHandler? = null

    // Location Client
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private inner class LocationServiceHandler(looper: Looper) : Handler(looper) {
        private val locationRequest = LocationRequest.create().apply {

        }

        private val locationCallback = object : LocationCallback() {
            override fun onLocationResult(location: LocationResult) {
                Log.d("Location Callback", "$location")
            }
        }

        override fun handleMessage(msg: Message) {
            if (checkSelfPermission(ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED
                && checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
                && checkSelfPermission(ACCESS_BACKGROUND_LOCATION) == PERMISSION_GRANTED
            ) {
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest, locationCallback, looper
                )
            }
        }
    }

    override fun onCreate() {
        // Location Client 초기화
        fusedLocationProviderClient = FusedLocationProviderClient(this)

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
    }
}