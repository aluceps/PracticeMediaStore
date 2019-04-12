package me.aluceps.practicemediastore

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ImageView
import me.aluceps.practicemediastore.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.album.setOnClickListener {
            Log.d("###", "action: OnClick")
            if (checkPermission()) {
                openAlbum()
            }
        }
    }

    private fun openAlbum() {
        Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also { album ->
            album.addCategory(Intent.CATEGORY_OPENABLE)
            album.type = "*/*"
            album.putExtra(Intent.EXTRA_MIME_TYPES, arrayListOf("image/*", "video/*"))
            startActivityForResult(album, REQUEST_GALLERY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) return
        if (requestCode == REQUEST_GALLERY) {
            val uri = data.data
            Log.d("###", "result: uri=$uri")
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                binding.imageView.apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageBitmap(bitmap)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_READ_EXTERNAL_STRAGE -> {
                if (grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                    openAlbum()
                } else {
                    // denied
                }
            }
            else ->
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onDestroy() {
        binding.imageView.setImageBitmap(null)
        super.onDestroy()
    }

    private fun Context.checkPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val activity = this as Activity
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                } else {
                    ActivityCompat.requestPermissions(
                        activity,
                        listOf(Manifest.permission.READ_EXTERNAL_STORAGE).toTypedArray(),
                        REQUEST_READ_EXTERNAL_STRAGE
                    )
                }
                false
            } else {
                true
            }
        } else {
            true
        }

    companion object {
        private const val REQUEST_READ_EXTERNAL_STRAGE = 0x0001
        private const val REQUEST_GALLERY = 0x0002
    }
}
