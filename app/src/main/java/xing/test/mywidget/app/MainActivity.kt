package xing.test.mywidget.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import xing.test.mywidget.R
import xing.test.mywidget.configure.WidgetConfigureActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        WidgetConfigureActivity.startTest(this)
    }
}