package com.doducvuong14061990.vplayer.base

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.viewbinding.ViewBinding
import com.doducvuong14061990.vplayer.R
import com.doducvuong14061990.vplayer.utils.hideStatusBarAndNavigationBar

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    abstract var mListPermission: Array<String>
    var mListPermissionFail: ArrayList<String> = arrayListOf()

    private lateinit var _binding: VB
    protected val binding get() = _binding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Vplayer)

        _binding = getViewBinding()
        setContentView(binding.root)

        hideStatusBarAndNavigationBar()
        setupActivity()
    }

    abstract fun getViewBinding(): VB

    abstract fun setupActivity()

    fun checkPermissions(): Boolean {
        var bool = true
        mListPermissionFail.clear()
        mListPermission.forEach {
            if (ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
                mListPermissionFail.add(it)
                bool = false
            }
        }
        return bool
    }

    fun requestPermission() {

        ActivityCompat.requestPermissions(this, mListPermissionFail.toTypedArray(), mRequestCode)
    }


    protected open fun showToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    protected open fun hideKeyboard() {
        try {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            if (currentFocus != null) imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        } catch (e: Exception) {
            Log.e("MultiBackStack", "Failed to add fragment to back stack", e)
        }
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }


    companion object {
        private val TAG: String = this::class.java.simpleName
        const val mRequestCode = 2022
    }
}