package xing.appwidget.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import xing.appwidget.R
import xing.appwidget.fragment.LabelDetailFragment
import xing.appwidget.fragment.LabelManagerFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv_apps.setOnClickListener { WidgetConfigureActivity.startTest(this) }
        tv_labels.setOnClickListener { LabelManagerFragment.start(this, true) }
        tv_create_label.setOnClickListener { LabelDetailFragment.start(this, null, true) }

    }
}