package com.doducvuong14061990.vplayer.ui.fragment.home

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.*
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.doducvuong14061990.vplayer.R
import com.doducvuong14061990.vplayer.app.ApplicationClass
import com.doducvuong14061990.vplayer.base.BaseFragment
import com.doducvuong14061990.vplayer.databinding.FragmentHomeBinding
import com.doducvuong14061990.vplayer.service.VplayerService
import com.doducvuong14061990.vplayer.ui.fragment.home.favourites.FavouritesFragment
import com.doducvuong14061990.vplayer.ui.fragment.home.playlists.PlaylistsFragment
import com.doducvuong14061990.vplayer.ui.fragment.home.song.SongsFragment
import com.doducvuong14061990.vplayer.ui.fragment.home.viewmodel.HomeFragmentViewModel
import com.doducvuong14061990.vplayer.ui.fragment.home.viewmodel.HomeFragmentViewModelFactory

class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private lateinit var pagerAdapter : ScreenSlidePagerAdapter
    override val layoutId: Int
        get() = R.layout.fragment_home

    private lateinit var homeFragmentViewModel: HomeFragmentViewModel
    private lateinit var homeFragmentViewModelFactory: HomeFragmentViewModelFactory

    private lateinit var searchView: androidx.appcompat.widget.SearchView

    /** Callback này được gọi khi Fragment bắt đầu khởi tạo từ các dữ liệu đầu vào.
     * Khác với onCreate() của Activity, rằng bạn có thể tạo giao diện cho màn hình ở callback này,
     * thì với Fragment chúng ta còn phải đợi qua callback tiếp theo mới có thể tạo giao diện được.
     *
     * Callback này cũng được gọi một lần trong đời sống Fragment.
     * Nên thường tận dụng để lấy dữ liệu từ bên ngoài truyền vào như ở SecondFragment
     * chúng ta có làm quen. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        Log.d("ABC", "onCreate Home")
    }

    /** Nằm trong onCreateView */
    override fun setLayout() {
        Log.d("ABC", "onCreateView Home")
        (activity as AppCompatActivity?)?.setSupportActionBar(binding.toolbarHome)
        setHasOptionsMenu(true)
        (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity?)?.supportActionBar?.setDisplayShowHomeEnabled(true)
        (activity as AppCompatActivity?)?.supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.toolbarTitleHome.text = resources.getString(R.string.app_name)

        homeFragmentViewModelFactory = HomeFragmentViewModelFactory(ApplicationClass.application)
        homeFragmentViewModel =
            ViewModelProvider(
                requireParentFragment(),
                homeFragmentViewModelFactory
            )[HomeFragmentViewModel::class.java]
        /** Đặt viewModel cho databinding -
         * điều này cho phép layout (XML) liên kết truy cập vào tất cả dữ liệu trong ViewModel */
        binding.homeFragmentViewModelx = homeFragmentViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupViewPager()
        viewPagerOnChange()
    }


    private fun viewPagerOnChange() {
        binding.viewPagerHome.registerOnPageChangeCallback(homeFragmentViewModel.onPagerChange)
    }

    private fun setupViewPager() {
        pagerAdapter = ScreenSlidePagerAdapter(requireActivity())
        binding.viewPagerHome.adapter = pagerAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setNavigation(view)
        eventOnClick()
    }

    private fun setNavigation(view: View) {
        val navController: NavController = Navigation.findNavController(view)

        val config: AppBarConfiguration = AppBarConfiguration.Builder(
            navController.graph
        ).setOpenableLayout(
            binding.drawerLayoutHome
        ).build()

        NavigationUI.setupWithNavController(binding.navViewHome, navController)
        NavigationUI.setupWithNavController(binding.toolbarHome, navController, config)
    }

    private fun eventOnClick() {
        homeFragmentViewModel.mTab.observe(viewLifecycleOwner) {
            if (!it.swipe && it.pagerSongs) {
                binding.viewPagerHome.setCurrentItem(ONE_PAGE, true)
            }
            if (!it.swipe && it.pagerFavourites) {
                binding.viewPagerHome.setCurrentItem(TWO_PAGE, true)
            }
            if (!it.swipe && it.pagerPlaylists) {
                binding.viewPagerHome.setCurrentItem(THIRD_PAGE, true)
            }
        }
    }

    /** Nhập vào tìm kiêm bài hát theo tên bài... */

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home_toolbar_home, menu)
        val searchManager: SearchManager =
            requireContext().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.searchHome)?.actionView as? androidx.appcompat.widget.SearchView)?.apply {
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
            maxWidth = Int.MAX_VALUE
            setOnQueryTextListener(object :
                androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    /** Truyền các ký tự người dùng nhập vào sang SongsFragment() */
//                musicAdapter.filter.filter(query)
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    /** Truyền các ký tự người dùng nhập vào sang SongsFragment() */
//                musicAdapter.filter.filter(newText)
                    pagerAdapter.songFragment.searchAction?.invoke(newText.toString())
                    return false
                }
            })
        }
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        val songFragment by lazy { SongsFragment.getInstance() }
        override fun getItemCount(): Int = NUM_PAGES

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                ONE_PAGE -> songFragment
                TWO_PAGE -> FavouritesFragment()
                THIRD_PAGE -> PlaylistsFragment()
                else -> SongsFragment()
            }
        }
    }

    companion object {
        private const val NUM_PAGES = 3
        private const val ONE_PAGE = 0
        private const val TWO_PAGE = 1
        private const val THIRD_PAGE = 2
    }

    override fun onStart() {
        super.onStart()
        Log.d("ABC", "onStart Home")
    }

    override fun onResume() {
        Log.d("ABC", "onResume Home")
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        Log.d("ABC", "onPause Home")
    }

    override fun onStop() {
        super.onStop()
        Log.d("ABC", "onStop Home")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("ABC", "onDestroyView Home")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ABC", "onDestroy Home")
    }

    override fun onDetach() {
        Log.d("ABC", "onDetach Home")
        super.onDetach()
    }
}
