package xing.appwidget.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import xing.appwidget.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        WidgetConfigureActivity.startTest(this)
    }
}