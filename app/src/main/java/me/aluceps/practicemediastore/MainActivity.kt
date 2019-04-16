package me.aluceps.practicemediastore

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.Toast
import me.aluceps.practicemediastore.databinding.ActivityMainBinding
import java.io.IOException

sealed class Permission {
    object ReadExternalStorage : Permission() {
        override val manifest: String = Manifest.permission.READ_EXTERNAL_STORAGE
    }

    abstract val manifest: String
}

sealed class Request {
    object ReadExternalStorage : Request() {
        override val code: Int = 0x001
    }

    object Gallery : Request() {
        override val code: Int = 0x002
    }

    abstract val code: Int
}

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.album.setOnClickListener {
            if (checkPermission()) {
                openAlbum()
            }
        }
    }

    private fun openAlbum() {
        Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also { album ->
            album.addCategory(Intent.CATEGORY_OPENABLE)
            album.type = "*/*"
            album.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
            startActivityForResult(album, Request.Gallery.code)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) return
        if (requestCode == Request.Gallery.code) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, data.data)
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
            Request.ReadExternalStorage.code -> {
                if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                    openAlbum()
                } else {
                    Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    override fun onDestroy() {
        binding.imageView.setImageBitmap(null)
        super.onDestroy()
    }

    private fun checkPermission(): Boolean =
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (selfPermissionDenied) {
                if (shouldShowRequestPermission) {
                    Toast.makeText(this, "Requested permission is already disabled...", Toast.LENGTH_SHORT).show()
                } else {
                    requestPermissionReadExternalStorage()
                }
                false
            } else {
                true
            }
        } else {
            true
        }

    private val selfPermissionDenied
        get() = ContextCompat.checkSelfPermission(
            this,
            Permission.ReadExternalStorage.manifest
        ) != PackageManager.PERMISSION_GRANTED

    private val shouldShowRequestPermission
        get() = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Permission.ReadExternalStorage.manifest
        )

    private fun requestPermissionReadExternalStorage() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Permission.ReadExternalStorage.manifest),
            Request.ReadExternalStorage.code
        )
    }
}
