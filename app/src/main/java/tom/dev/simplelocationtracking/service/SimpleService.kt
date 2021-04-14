package tom.dev.simplelocationtracking.service

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleObserver

/**
 * Simple Example Service
 */
class SimpleService : Service(), LifecycleObserver {

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    // Thread 로 부터 message 받아오는 Handler
    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            // 보통 여기서 작업을 수행한다. (ex. 파일 다운로드)

            // Lifecycle Observe
            this@SimpleService.application.registerActivityLifecycleCallbacks(
                object : Application.ActivityLifecycleCallbacks {
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
            )

            try {
                Thread.sleep(100000)
            } catch (e: InterruptedException) {
                // interrupt restore
                Thread.currentThread().interrupt()
            }

            /**
             * 위의 작업 수행이 끝났으므로 startId(= msg.arg1)를 통해서 service 종료
             * @see [onStartCommand] startId 를 msg.arg1 로 전달
             */
            stopSelf(msg.arg1)
        }
    }

    override fun onCreate() {
        /**
         * service 동작을 위한 thread 를 시작하는 부분이다.
         * service 는 보통 process 의 main thread 에서 동작하기 때문에
         * 앱이 멈추지 않기 위해서는 따로 thread 를 만들어 주어야 한다.
         * 또한 CPU-intensive 작업이 UI를 방해하지 않도록
         * [Process.THREAD_PRIORITY_BACKGROUND] 설정해주어야 한다.
         */
        HandlerThread("ServiceStartArgs", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()

            // HandlerThread 의 Looper 를 가져와서 custom 한 Handler 에서 사용하도록 한다.
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()

        /**
         * 각각의 시작 요청마다, job 을 시작하라고 message 를 보내면서 start ID 를 전달한다.
         * 그로인해 job 이 종료될 때 어떤 요청을 멈춰야 하는지 알 수 있다.
         */
        serviceHandler?.obtainMessage()?.also { msg ->
            // handler 로 넘겨주는 message 내부에 argument 로 startId 넘겨준다.
            msg.arg1 = startId

            serviceHandler?.sendMessage(msg)
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Binding 사용하지 않을 것이기 때문에 null 반환
        return null
    }

    override fun onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
    }
}