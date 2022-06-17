package com.doducvuong14061990.vplayer.ui.fragment.home.song

import android.annotation.SuppressLint
import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doducvuong14061990.vplayer.NotificationReceiver
import com.doducvuong14061990.vplayer.NotificationReceiver.Companion.ACTION_NEXT
import com.doducvuong14061990.vplayer.NotificationReceiver.Companion.ACTION_PAUSE
import com.doducvuong14061990.vplayer.NotificationReceiver.Companion.ACTION_PREVIOUS
import com.doducvuong14061990.vplayer.NotificationReceiver.Companion.ACTION_RESUME
import com.doducvuong14061990.vplayer.NotificationReceiver.Companion.ACTION_SEEK
import com.doducvuong14061990.vplayer.R
import com.doducvuong14061990.vplayer.app.ApplicationClass
import com.doducvuong14061990.vplayer.base.BaseFragment
import com.doducvuong14061990.vplayer.callback.OnItemClickListener
import com.doducvuong14061990.vplayer.data.model.Song
import com.doducvuong14061990.vplayer.data.model.Songs
import com.doducvuong14061990.vplayer.data.repository.Repository
import com.doducvuong14061990.vplayer.data.response.MediaPlayerSingleton
import com.doducvuong14061990.vplayer.databinding.FragmentSongsBinding
import com.doducvuong14061990.vplayer.service.VplayerService
import com.doducvuong14061990.vplayer.ui.adapter.SongAdapter

class SongsFragment : BaseFragment<FragmentSongsBinding>() {
    companion object {
        private var instance: SongsFragment? = null
        fun getInstance() = instance ?: SongsFragment().also { instance = it }
    }

    var searchAction: ((String) -> Unit)? = null

    private val player: MediaPlayerSingleton = MediaPlayerSingleton.getInstance()

    private var isServiceConnected: Boolean = false
    private var vplayerService: VplayerService? = null

    private lateinit var songAdapter: SongAdapter
    private lateinit var viewModel: SongViewModel
    private var mSong: Song? = null
    private var isPlaying: Boolean? = false

    private lateinit var runnable: Runnable
    private var handler1: Boolean = false
    private var handler2: Boolean = false

    override val layoutId: Int
        get() = R.layout.fragment_songs

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val bundle: Bundle? = intent?.extras
            if (bundle == null) {
                binding.flMiniControlHome.visibility = View.INVISIBLE
                return
            } else {
                mSong = bundle.getParcelable("object_song")
                isPlaying = bundle.getBoolean("status_player")
                val mRepeat = bundle.getInt("repeat_pattern")

                if (mSong != null && isPlaying != null) {

                    binding.flMiniControlHome.visibility = View.VISIBLE
                    viewModel.setDataToUIControlMini(mSong!!, isPlaying!!, mRepeat)
                    initSeekBar()
                }
            }
        }
    }

    private var mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, iBinder: IBinder?) {
            val vplayerBinder: VplayerService.VplayerBinder =
                iBinder as VplayerService.VplayerBinder
            vplayerService = vplayerBinder.getVplayerService()
            isServiceConnected = true
            initUiControlFragment()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            vplayerService = null
            isServiceConnected = false
            binding.flMiniControlHome.visibility = View.INVISIBLE
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(
                broadcastReceiver,
                IntentFilter("Send_Data_Vs_Action_To_SongsFragment")
            )
        Log.d("ABC", "onCreate Songs")
    }

    override fun setLayout() {
        Log.d("ABC", "onCreateViewSongs")
        setViewModel()
        initRecycleView()
    }

    private fun startBoundService() {
        val intent: Intent = Intent(requireContext(), VplayerService::class.java)
        requireContext().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun initUiControlFragment() {
        if (isServiceConnected && vplayerService != null && vplayerService?.mSong != null) {
            binding.flMiniControlHome.visibility = View.VISIBLE
            viewModel.setDataToUIControlMini(
                vplayerService?.mSong!!,
                vplayerService?.isPlaying!!,
                vplayerService?.mRepeatPattern!!
            )
        } else {
            binding.flMiniControlHome.visibility = View.INVISIBLE
        }
    }

    private fun setViewModel() {
        viewModel = ViewModelProvider(this)[SongViewModel::class.java]
        /** Đặt viewModel cho databinding -
         * điều này cho phép layout (XML) liên kết truy cập vào tất cả dữ liệu trong ViewModel */
        binding.songViewModelx = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        handleConnectedBoundService()
        initEvents()

    }

    private fun handleConnectedBoundService() {
        if (isServiceConnected) {
            initUiControlFragment()
        } else {
            startBoundService()
        }
    }

    private fun initEvents() {
        viewModel.mRepeatPattern.observe(this) {
            if (it > -1) {
                when (it) {
                    0 -> vplayerService?.setActionRepeat(0, "SongFragment")
                    1 -> vplayerService?.setActionRepeat(1, "SongFragment")
                    2 -> vplayerService?.setActionRepeat(2, "SongFragment")
                    else -> vplayerService?.setActionRepeat(0, "SongFragment")
                }
            }
        }

        viewModel.mClickPlayOrPause.observe(this) { clicked ->
            if (clicked) {
                if (viewModel.mPlaying.value == true) {
                    sendBroadcastAction(ACTION_PAUSE)
                    viewModel.setPlaying(false)
                } else {
                    sendBroadcastAction(ACTION_RESUME)
                    viewModel.setPlaying(true)
                }
                viewModel.onClickPlayOrPause(false)
            }
        }

        viewModel.mClickPrevious.observe(this) {
            if (it) {
                sendBroadcastAction(ACTION_PREVIOUS)
                viewModel.onClickPrevious(false)
            }
        }

        viewModel.mClickNext.observe(this) {
            if (it) {
                sendBroadcastAction(ACTION_NEXT)
                viewModel.onClickNext(false)
            }
        }

        viewModel.mOnClickedPhoto.observe(this) {
            if (it) {
                viewModel.setOnClickPhoto(false)
                val bundle = bundleOf(
                    "start_point" to "Mini_Control_Songs_Fragment",
                )
                this@SongsFragment.findNavController().navigate(R.id.playerFragment, bundle)
            }
        }

    }

    @SuppressLint("ResourceAsColor")
    private fun initSeekBar() {
        player.mp?.duration?.let {
            binding.seekBarMiniControlHome.max = it
        }

        runnable = Runnable {
            player.mp?.currentPosition?.let {
                binding.seekBarMiniControlHome.progress = it
                handler1 = Handler(Looper.getMainLooper()).postDelayed(runnable, 500)
            }
        }
        handler2 = Handler(Looper.getMainLooper()).postDelayed(runnable, 500)
        binding.seekBarMiniControlHome.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    player.mp?.seekTo(progress)
                    sendBroadcastAction(ACTION_SEEK)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
    }

    private fun sendBroadcastAction(action: Int) {
        val intent: Intent = Intent(requireContext(), NotificationReceiver::class.java)
        intent.putExtra("action_music", action)
        requireContext().sendBroadcast(intent)
    }

    private fun initRecycleView() {
        /** Chỉ lần đầu tiên mở ứng dụng là nhạc sẽ load từ bộ nhớ điện thoại.
         *
         * Sau đó danh sách nhạc sẽ được lưu trữ lên Class Service... do Service tồn tại kể cả khi kill app (Service chỉ bị Destroy
         * khi người dùng bấm vào Button cho phép Destroy hoặc do hệ thống thiếu tài nguyên...), Nên những lần mở app sau này...
         * ta chỉ cần kết nối Bound Service và từ đó lấy danh sách nhạc trực tiếp từ Service...
         *
         * Như vậy sẽ giúp app load danh sách nhạc nhanh hơn...
         *
         * Trong trường hợp Service bị Destroy ... thì khi đó chúng ta mới cần load lại nhạc từ bộ nhớ thiết bị... */
        Repository.getInstance().mListSong.observe(viewLifecycleOwner) {
            binding.rcvSongsHomeFragment.setHasFixedSize(true)
            binding.rcvSongsHomeFragment.setItemViewCacheSize(13)
            binding.rcvSongsHomeFragment.layoutManager =
                LinearLayoutManager(ApplicationClass.application, RecyclerView.VERTICAL, false)
            songAdapter = SongAdapter(object : OnItemClickListener<Song> {
                override fun onItemClick(data: Song) {
                    /** Khi click vào 1 bài hát trên danh sách nhạc, ta sẽ gọi startForegroundService.
                     *
                     * Service được khởi tạo bằng cách gọi onCreate():
                     * Khi đó ta sẽ truyền danh sách nhạc và position của bài hát tương ứng lên Service và lấy
                     * ra lưu vào biến tại onStartCommand(). Sau đó bài hát sẽ được onPlay...
                     *
                     * Sau đó điều hướng sang PlayerFragment()
                     *
                     * Tại PlayerFragment() , vì Service đã được tạo. Nên ta chỉ cần liên kết lại boundService là ta có
                     * thể cập nhật UI cho PlayerFragment()... bên trong onServiceConnected().
                     * */
                    /** Sau đó điều hướng đến PlayerFragment() */

                    /** Lấy position của bài hát trong danh sách Full bài hát... */
                    (it as MutableList<Song>).forEachIndexed { index: Int, value: Song ->
                        if (value == data) {
                            /** Gửi danh sách bài hát và index của bài hát được chọn... */
                            val songs: Songs = Songs(it)
                            val bundle = bundleOf(
                                "start_point" to "Songs_Fragment",
                                "songs" to songs,
                                "position" to index
                            )
                            this@SongsFragment.findNavController()
                                .navigate(R.id.playerFragment, bundle)
                        }
                    }
                }
            })
            songAdapter.addData(it)
            binding.rcvSongsHomeFragment.adapter = songAdapter
        }

        handleSearchActionSongs()
    }

    private fun handleSearchActionSongs() {
        searchAction = { newText ->
            songAdapter.filter.filter(newText)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("ABC", "onStart Songs")
    }

    override fun onResume() {
        Log.d("ABC", "onResume Songs")
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        Log.d("ABC", "onPause Songs")
    }

    override fun onStop() {
        super.onStop()
        Log.d("ABC", "onStop Songs")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("ABC", "onDestroyView Songs")
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
        vplayerService = null
        isServiceConnected = false
        super.onDestroy()
        Log.d("ABC", "onDestroy Songs")
    }

    override fun onDetach() {
        Log.d("ABC", "onDetach Songs")
        super.onDetach()
    }
}