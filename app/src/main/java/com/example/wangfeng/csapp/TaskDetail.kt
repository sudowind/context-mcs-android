package com.example.wangfeng.csapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.*

import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Created by wangfeng on 2018/3/20.
 */

class TaskDetail : AppCompatActivity(), View.OnClickListener {

    private var picture: ImageView? = null

    private var output: File? = null  // 设置拍照的图片文件
    private var photoUri: Uri? = null  // 拍摄照片的路径

    private var taskId: String = ""

    @Throws(Exception::class)
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.task_detail)
        val btnTakePhoto: Button? = findViewById(R.id.btnTakePhoto)
        val btnChoosePhoto: Button? = findViewById(R.id.btnChoosePhoto)
        val btnSubmit: Button? = findViewById(R.id.btnSubmit)
        val bundle = this.intent.extras
        if (bundle != null) {
            val title = bundle.getString("title")
            val desc = bundle.getString("desc")
            val deadline = bundle.getString("deadline")
            taskId = bundle.getString("id")
            Log.v("task detail", title)
            Log.v("task detail", desc)
            val titleView = findViewById<TextView>(R.id.taskTitle)
            val descView = findViewById<TextView>(R.id.description)
            val deadlineView = findViewById<TextView>(R.id.deadline)
            titleView.text = title
            descView.text = desc
        }

        picture = findViewById<View>(R.id.displayPhoto) as ImageView
        btnTakePhoto!!.setOnClickListener(this)
        btnChoosePhoto!!.setOnClickListener(this)
        btnSubmit!!.setOnClickListener(this)
//        addRatio()
    }

    private fun addRatio() {
        val group = findViewById<RadioGroup>(R.id.ratioGroup)
        for (i in 0..4) {
            val tempButton = RadioButton(this)
            //            tempButton.setBackgroundResource(R.drawable.xxx);   // 设置RadioButton的背景图片
            //            tempButton.setButtonDrawable(R.drawable.xxx);           // 设置按钮的样式
            tempButton.setPadding(80, 0, 0, 0)                 // 设置文字距离按钮四周的距离
            tempButton.text = "按钮 " + i
            group.addView(tempButton)
        }
        group.setOnCheckedChangeListener { group, checkedId ->
            //在这个函数里面用来改变选择的radioButton的数值，以及与其值相关的 //任何操作，详见下文 selectRadioBtn();
            Toast.makeText(applicationContext,
                    StringBuilder().append(checkedId).toString(),
                    Toast.LENGTH_SHORT).show()
        }
    }

    private fun submitTask() {
        Log.i("submit task", "hahah")
        if (photoUri != null) {
            Thread(Runnable {
                try {
                    val cookieJar: ClearableCookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(MainAppliaction.context))
                    val client = OkHttpClient.Builder().cookieJar(cookieJar).build()
                    val file = File(photoUri.toString())
//                val builder = MultipartBody.Builder()
//                        .setType(MultipartBody.FORM)
//                        .addFormDataPart("image", "response_image.jpg",
//                                RequestBody.create(MediaType.parse("image/png"), file)).build()
                    val formBody = FormBody.Builder()
                            .add("taskId", taskId)
                            .add("status", "5")
                            .build()
                    val request = Request.Builder()
                            .url("http://www.sudowind.com:8000/user/task/modify_status")
                            .post(formBody)
                            .build()
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call?, e: IOException?) {
                            println("something wrong")
                        }

                        override fun onResponse(call: Call?, response: Response?) {
                            val res = response!!.body()!!.string()
//                            runOnUiThread({
//                                Toast.makeText(applicationContext, res, Toast.LENGTH_SHORT).show()
//                            })
                            if (response.code() == 200) {
                                finish()
                            }
                        }
                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }).start()
        } else {
            Toast.makeText(applicationContext,
                    "请先拍照或选择照片",
                    Toast.LENGTH_SHORT).show()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
        // 拍摄照片
            R.id.btnTakePhoto -> checkPermissionTakePhoto(v)
        // 相册中选择
            R.id.btnChoosePhoto -> checkPermissionTakePhoto(v)
            R.id.btnSubmit -> submitTask()
        }

    }

    // 检查相机权限
    private fun checkPermissionTakePhoto(view: View) {
        when (view.id) {
            R.id.btnTakePhoto ->
                // 检查是否有读取权限,如果需要设置就让他设置;
                if (ContextCompat.checkSelfPermission(this@TaskDetail, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_TAKE_PHONE)
                } else {
                    takePhoto()
                }
            R.id.btnChoosePhoto ->
                // 检查是否有读取权限,如果需要设置就让他设置;
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CHOOSE_PICTURE)
                } else {
                    choosePhoto()
                }
        }

    }

    /**
     * 调取相机拍摄照片.
     */
    private fun takePhoto() {
        val file = File(Environment.getExternalStorageDirectory(), "takePhotoDemo")
        if (!file.exists()) {
            // 如果文件路径不存在则直接创建一个文件夹
            file.mkdir()
        }
        // 把时间作为拍摄照片的保存路径;
        output = File(file, System.currentTimeMillis().toString() + ".jpq")
        // 如果该照片已经存在就删除它,然后新创建一个
        try {
            if (output!!.exists()) {
                output!!.delete()
            }
            output!!.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // 隐式打开拍摄照片
        //        photoUri = Uri.fromFile(output);
        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        photoUri = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider", output)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        startActivityForResult(intent, REQUEST_TAKE_PHOTO)
    }

    /**
     * 从相册选择照片
     */
    private fun choosePhoto() {
        // 选择相册操作
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CHOOSE_PHOTO)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null) {
            when (requestCode) {
            // 拍摄照片的回调
                REQUEST_TAKE_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                    Log.i("img url", photoUri.toString())
                    try {
                        val bit = BitmapFactory.decodeStream(contentResolver.openInputStream(photoUri!!))
                        picture!!.setImageBitmap(bit)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                        Log.d("tag", e.message)
                        Toast.makeText(this, "程序崩溃", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Log.i("REQUEST_TAKE_PHOTO", "拍摄失败")
                }
            // 调用系统相册的回调
                REQUEST_CHOOSE_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                    val uri = data.data
                    photoUri = uri
                    Log.i("img url", uri.toString())
                    try {
                        val bit = BitmapFactory.decodeStream(contentResolver.openInputStream(uri!!))
                        picture!!.setImageBitmap(bit)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                        Log.d("tag", e.message)
                        Toast.makeText(this, "程序崩溃", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Log.i("REQUEST_TAKE_PHOTO", "拍摄失败")
                }
                else -> {
                    Log.i("detail", "未知错误")
                }
            }
            val file = File(photoUri.toString())
        }
    }

    // 动态检察权限
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        // 设置写入权限
        if (requestCode == PERMISSION_REQUEST_TAKE_PHONE) {
            // 打开了读写权限
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto()
            } else {
                Toast.makeText(this@TaskDetail, "请打开应用相机权限", Toast.LENGTH_LONG).show()
            }
        }
        if (requestCode == PERMISSION_REQUEST_CHOOSE_PICTURE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                choosePhoto()
            } else {
                Toast.makeText(this@TaskDetail, "请打开读取相册权限", Toast.LENGTH_LONG).show()
            }
        }

    }

    companion object {

        private const val REQUEST_TAKE_PHOTO = 1 // 拍照标识
        private const val REQUEST_CHOOSE_PHOTO = 2 // 选择相册标示符

        // 获取拍照权限标识
        private const val PERMISSION_REQUEST_TAKE_PHONE = 6
        private const val PERMISSION_REQUEST_CHOOSE_PICTURE = 7
    }
}
