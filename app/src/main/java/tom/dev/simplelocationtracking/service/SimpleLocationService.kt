package tom.dev.simplelocationtracking.service

import android.app.Service
import android.content.Intent
import android.os.*
import android.widget.Toast

class SimpleLocationService : Service() {

    private var serviceLooper: Looper? = null
    private var serviceHandler: LocationServiceHandler? = null

    private inner class LocationServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {

        }
    }

    override fun onCreate() {
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