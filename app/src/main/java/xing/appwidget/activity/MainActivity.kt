package xing.appwidget.activity

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import xing.appwidget.R
import xing.appwidget.fragment.LabelDetailFragment
import xing.appwidget.fragment.LabelManagerFragment
import xing.appwidget.storage.LabelStorageHelper
import java.io.File
import java.io.OutputStream


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermission()

        tv_apps.setOnClickListener { AppListWidgetConfigureActivity.startTest(this) }
        tv_labels.setOnClickListener { LabelManagerFragment.start(this, true) }
        tv_create_label.setOnClickListener { LabelDetailFragment.start(this, null, true) }

        tv_export.setOnClickListener {
            if (!checkPermission()) {
                Toast.makeText(this, "no permission", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
//            var file = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            try {
                if (file != null && file.isDirectory) {
                    file = File(file.path + "/appFreezeWidget.bak.txt")
                    file.delete()
                    file.createNewFile()
                }
                if (file != null && file.isFile) {
                    LabelStorageHelper.init(this)
                    val labels = LabelStorageHelper.labelSetLd.value ?: ArrayList<String>(0)
                    for (label in labels) {
                        if (label.isNullOrBlank()) {
                            continue
                        }
                        file.appendText("$label\n")
                        val sb = StringBuilder()
                        LabelStorageHelper.getPackageNameListByLabel(label)
                                .filter { !it.isNullOrBlank() }
                                .forEach { sb.append(it).append(",") }
                        file.appendText("$sb\n")
                    }
                    Toast.makeText(this, "已导出到 ${file.path}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("liujiaxing", "tv_export", e)
            } finally {
            }
        }
        tv_import.setOnClickListener {
            if (!checkPermission()) {
                Toast.makeText(this, "no permission", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
//            var file = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            try {
                if (file != null && file!!.isDirectory) {
                    file = File(file!!.path + "/appFreezeWidget.bak.txt")
                }
                if (file != null && file!!.isFile) {
                    LabelStorageHelper.init(this)
                    val lines = file!!.readLines()
                    var labelName = ""
                    var labelSet = HashSet<String>()
                    for (i in lines.indices) {
                        val line = lines[i]
                        if (i % 2 == 0) {
                            labelName = line
                        } else {
                            if (labelName.isNullOrBlank()) {
                                continue
                            }
                            labelSet.clear()
                            line.split(",")
                                    .filter { !it.isNullOrBlank() }
                                    .let { labelSet.addAll(it) }
                            if (!labelSet.isNullOrEmpty()) {
                                LabelStorageHelper.saveLabelSetting(labelName, labelSet)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("liujiaxing", "tv_import", e)
            } finally {
            }
        }
    }

    fun saveToFile(context: Context, str: String, fileName: String?): Boolean {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
//        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
//        contentValues.put(MediaStore.Images.Media.DESCRIPTION, fileName)
//        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
//        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val uri: Uri? = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        uri?.let {
            var outputStream: OutputStream? = null
            try {
                outputStream = contentResolver.openOutputStream(uri)
//                OutputStreamWriter(outputStream).write(file)
//                FileOutputStream(file).bufferedWriter().
                outputStream?.close()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                return false
            }
        }
        return true
    }

    private fun checkPermission() =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission() {
        if (!checkPermission()) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 10086)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != 10086) {
            return
        }

        if (grantResults.isNotEmpty()) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Log.d("liujiaxing", "onRequestPermissionsResult -> PERMISSION_DENIED")
//                requestPermission()
            }
        }
    }
}
