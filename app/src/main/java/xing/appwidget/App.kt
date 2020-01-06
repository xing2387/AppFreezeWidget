package xing.appwidget

import android.app.Application
import android.util.Log

class App : Application() {

    companion object {
        var app: Application? = null
    }

    override fun onCreate() {
        app = this
        Log.d("Application", "app $app")
        super.onCreate()
    }

}