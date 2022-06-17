package com.doducvuong14061990.vplayer.ui.fragment.player

import android.annotation.SuppressLint
import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.doducvuong14061990.vplayer.R
import com.doducvuong14061990.vplayer.base.BaseFragment
import com.doducvuong14061990.vplayer.data.model.Song
import com.doducvuong14061990.vplayer.data.model.Songs
import com.doducvuong14061990.vplayer.databinding.FragmentPlayerBinding
import com.doducvuong14061990.vplayer.service.VplayerService
import com.doducvuong14061990.vplayer.utils.formatDuration


class PlayerFragment : BaseFragment<FragmentPlayerBinding>() {
    private var isServiceConnected: Boolean = false
    private var vplayerService: VplayerService? = null
    private var mSong: Song? = null
    private var isPlaying: Boolean? = false

    private lateinit var playerAndroidViewModel: PlayerViewModel

    private lateinit var runnable: Runnable
    private var handler1: Boolean = false
    private var handler2: Boolean = false

    override val layoutId: Int
        get() = R.layout.fragment_player

    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val bundle: Bundle? = intent?.extras
            if (bundle == null) {
                return
            } else {
                mSong = bundle.getParcelable("object_song")
                isPlaying = bundle.getBoolean("status_player")

                if (mSong != null && isPlaying != null) {
                    playerAndroidViewModel.handleLayoutPlayerFragment(mSong!!)
                    playerAndroidViewModel.setPlaying(isPlaying!!)
                }

                initSeekBar()
            }
        }
    }

    private var mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, iBinder: IBinder?) {
            val vplayerBinder: VplayerService.VplayerBinder =
                iBinder as VplayerService.VplayerBinder
            vplayerService = vplayerBinder.getVplayerService()
            isServiceConnected = true
            handleConnectedBoundService()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            vplayerService = null
            isServiceConnected = false
        }

    }

    private fun initPlayerFragment() {
        vplayerService?.let { mPlayerService ->
            isPlaying = mPlayerService.isPlaying
            mPlayerService.mSong?.let {
                mSong = it
                playerAndroidViewModel.handleLayoutPlayerFragment(it)
                playerAndroidViewModel.setPlaying(mPlayerService.isPlaying)
                playerAndroidViewModel.setRepeat(mPlayerService.mRepeatPattern)
                playerAndroidViewModel.setShuffle(mPlayerService.mShuffle)

                initSeekBar()
            }

            playerAndroidViewModel.mClickPlayOrPause.observe(this) { clicked ->
                if (clicked) {
                    if (mPlayerService.isPlaying) {
                        mPlayerService.onPauseMusic()
                    } else {
                        mPlayerService.onResumeMusic()
                    }
                    playerAndroidViewModel.setPlaying(mPlayerService.isPlaying)
                    playerAndroidViewModel.onClickPlayOrPause(false)
                }
            }

            playerAndroidViewModel.mClickPrevious.observe(this) { clicked ->
                if (clicked) {
                    mPlayerService.onPreviousNext(false)
                    playerAndroidViewModel.onClickPrevious(false)
                }
            }

            playerAndroidViewModel.mClickNext.observe(this) { clicked ->
                if (clicked) {
                    mPlayerService.onPreviousNext(true)
                    playerAndroidViewModel.onClickNext(false)
                }
            }

            playerAndroidViewModel.mShuffle.observe(this) { clicked ->
                if (clicked) {
                    mPlayerService.isShuffle(true)
                } else {
                    mPlayerService.isShuffle(false)
                }
            }

            playerAndroidViewModel.mRepeatPattern.observe(this) {
                if (it > -1) {
                    when (it) {
                        0 -> vplayerService?.setActionRepeat(0, "PlayerFragment")
                        1 -> vplayerService?.setActionRepeat(1, "PlayerFragment")
                        2 -> vplayerService?.setActionRepeat(2, "PlayerFragment")
                        else -> vplayerService?.setActionRepeat(0, "PlayerFragment")
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ABC", "onCreatePlayer")
        registerBroadcastReceiver()
    }

    private fun registerBroadcastReceiver() {
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(broadcastReceiver, IntentFilter("Send_Data_Vs_Action_To_PlayerFragment"))
    }

    private fun handleFromStartPoint() {
        val startPoint: String? = arguments?.getString("start_point")
        if (startPoint != null && startPoint == "Notification") {
            handleNavigationFromMiniControlSongsFragmentOrFromNotification()
        } else if (startPoint != null && startPoint == "Songs_Fragment") {
            handleNavigationFromSongsFragment()
        } else if (startPoint != null && startPoint == "Mini_Control_Songs_Fragment") {
            handleNavigationFromMiniControlSongsFragmentOrFromNotification()
        }
    }

    private fun handleNavigationFromMiniControlSongsFragmentOrFromNotification() {
        if (!isServiceConnected) {
            onStartBoundSevice()
        } else {
            handleConnectedBoundService()
        }
    }

    private fun handleNavigationFromSongsFragment() {
        val songs: Songs? = arguments?.getParcelable("songs")
        val position: Int? = arguments?.getInt("position", -1)
        if (songs != null && position != null && position > -1) {
            startForegroundService(songs, position)
            if (!isServiceConnected) {
                onStartBoundSevice()
            } else {
                handleConnectedBoundService()
            }
        }
    }

    private fun startForegroundService(songs: Songs, position: Int) {
        val intent: Intent = Intent(requireContext(), VplayerService::class.java)
        val bundle: Bundle = Bundle()
        bundle.putString("start_point", "Player_Fragment")
        bundle.putParcelable("object_songs", songs)
        bundle.putInt("song_position", position)
        intent.putExtras(bundle)
        requireContext().startService(intent)
    }

    override fun setLayout() {
        Log.d("ABC", "onCreateViewPlayer")
        Toast.makeText(requireContext(), "12", Toast.LENGTH_SHORT).show()

        setToolbarPlayer()
        setViewModel()
    }

    private fun setToolbarPlayer() {
        (activity as AppCompatActivity?)?.setSupportActionBar(binding.toolbarPlayer)
        setHasOptionsMenu(true)
        (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity?)?.supportActionBar?.setDisplayShowHomeEnabled(true)
        (activity as AppCompatActivity?)?.supportActionBar?.setDisplayShowTitleEnabled(false)
    }


    @SuppressLint("ResourceAsColor")
    private fun initSeekBar() {
        vplayerService?.mediaPlayerSingleton?.mp?.duration?.let {
            binding.seekBarPlayer.max = it
            binding.tvTimeEndPlayer.text = it.toLong().formatDuration()
        }

        runnable = Runnable {
            vplayerService?.mediaPlayerSingleton?.mp?.currentPosition?.let {
                binding.seekBarPlayer.progress = it
                binding.tvTimeStartPlayer.text = it.toLong().formatDuration()
                handler1 = Handler(Looper.getMainLooper()).postDelayed(runnable, 500)
            }
        }
        handler2 = Handler(Looper.getMainLooper()).postDelayed(runnable, 500)

        binding.seekBarPlayer.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    vplayerService?.mediaPlayerSingleton?.mp?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home_toolbar_player, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.favoritePlayer ->
                Toast.makeText(requireContext(), "1", Toast.LENGTH_SHORT).show()
            android.R.id.home -> {
                if (isServiceConnected && isPlaying != null && mSong != null) {
                    requireActivity().onBackPressed()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /** Truyền this tức là PlayerFragment là lớp quản lý vòng đời của viewmodel.
     *  Truyền requireParentFragment()
     *  Truyền requireActivity() tức là HomeActivity quản lý PlayerFragment sẽ quản lý vòng đời của viewmodel*/
    private fun setViewModel() {
        playerAndroidViewModel =
            ViewModelProvider(this).get(PlayerViewModel::class.java)
        /** Đặt viewModel cho databinding -
         * điều này cho phép layout (XML) liên kết truy cập vào tất cả dữ liệu trong ViewModel */
        binding.playerAndroidViewModelx = playerAndroidViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        handleFromStartPoint()
    }

    private fun handleConnectedBoundService() {
        if (isServiceConnected) {
            initPlayerFragment()
        }
    }

    private fun onStartBoundSevice() {
        val intent: Intent = Intent(requireContext(), VplayerService::class.java)
        requireContext().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        handler1 = false
        handler2 = false
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
        isServiceConnected = false
        vplayerService = null
        super.onDestroy()
        Log.d("ABC", "onDestroyPlayer")
    }

    override fun onDetach() {
        Log.d("ABC", "onDetachPlayer")
        super.onDetach()
    }
}

