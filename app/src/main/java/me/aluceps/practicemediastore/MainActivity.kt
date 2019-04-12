package me.aluceps.practicemediastore

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.provider.MediaStore
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
            openAlbum()
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

    override fun onDestroy() {
        binding.imageView.setImageBitmap(null)
        super.onDestroy()
    }

    companion object {
        private const val REQUEST_GALLERY = 0x0001
    }
}
