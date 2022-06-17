package com.doducvuong14061990.vplayer.ui.fragment.home.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


/** Chúng ta tạo đối tượng ViewModel bằng cách sử dụng ScoreViewModelFactory, và truyền vào nó 1 dữ liệu.
 * Đối tượng ViewModel được khởi tạo và trả về.
 * Dữ liệu truyền vào, sẽ được ViewModel lưu trữ (trong trường hợp thay đổi cấu hình.)
 */
@Suppress("UNCHECKED_CAST")
class HomeFragmentViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeFragmentViewModel::class.java)) {
            return HomeFragmentViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}