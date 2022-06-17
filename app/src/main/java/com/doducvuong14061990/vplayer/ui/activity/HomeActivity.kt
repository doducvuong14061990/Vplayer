package com.doducvuong14061990.vplayer.ui.activity

import android.content.pm.PackageManager
import android.widget.Toast
import com.doducvuong14061990.vplayer.base.BaseActivity
import com.doducvuong14061990.vplayer.data.repository.Repository
import com.doducvuong14061990.vplayer.databinding.ActivityHomeBinding


class HomeActivity : BaseActivity<ActivityHomeBinding>() {


    override fun getViewBinding() = ActivityHomeBinding.inflate(layoutInflater)

    override fun setupActivity() {
        if (!checkPermissions()) requestPermission() else loadMusicFromDevice()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == mRequestCode) {
            when (PackageManager.PERMISSION_GRANTED) {
                grantResults.getOrNull(INDEX_0) -> {
                    loadMusicFromDevice()
                }
                grantResults.getOrNull(INDEX_1) -> {
                    return
                }
                else -> Toast.makeText(this, "PERMISSION_DENIED", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadMusicFromDevice() {
        lifecycle.addObserver(Repository.getInstance())
    }

    override var mListPermission: Array<String>
        get() = arrayOf(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_PHONE_STATE
        )
        set(value) {}

    companion object {
        private const val INDEX_0 = 0
        private const val INDEX_1 = 1
    }

}