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
        /** ?????t viewModel cho databinding -
         * ??i???u n??y cho ph??p layout (XML) li??n k???t truy c???p v??o t???t c??? d??? li???u trong ViewModel */
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
        /** Ch??? l???n ?????u ti??n m??? ???ng d???ng l?? nh???c s??? load t??? b??? nh??? ??i???n tho???i.
         *
         * Sau ???? danh s??ch nh???c s??? ???????c l??u tr??? l??n Class Service... do Service t???n t???i k??? c??? khi kill app (Service ch??? b??? Destroy
         * khi ng?????i d??ng b???m v??o Button cho ph??p Destroy ho???c do h??? th???ng thi???u t??i nguy??n...), N??n nh???ng l???n m??? app sau n??y...
         * ta ch??? c???n k???t n???i Bound Service v?? t??? ???? l???y danh s??ch nh???c tr???c ti???p t??? Service...
         *
         * Nh?? v???y s??? gi??p app load danh s??ch nh???c nhanh h??n...
         *
         * Trong tr?????ng h???p Service b??? Destroy ... th?? khi ???? ch??ng ta m???i c???n load l???i nh???c t??? b??? nh??? thi???t b???... */
        Repository.getInstance().mListSong.observe(viewLifecycleOwner) {
            binding.rcvSongsHomeFragment.setHasFixedSize(true)
            binding.rcvSongsHomeFragment.setItemViewCacheSize(13)
            binding.rcvSongsHomeFragment.layoutManager =
                LinearLayoutManager(ApplicationClass.application, RecyclerView.VERTICAL, false)
            songAdapter = SongAdapter(object : OnItemClickListener<Song> {
                override fun onItemClick(data: Song) {
                    /** Khi click v??o 1 b??i h??t tr??n danh s??ch nh???c, ta s??? g???i startForegroundService.
                     *
                     * Service ???????c kh???i t???o b???ng c??ch g???i onCreate():
                     * Khi ???? ta s??? truy???n danh s??ch nh???c v?? position c???a b??i h??t t????ng ???ng l??n Service v?? l???y
                     * ra l??u v??o bi???n t???i onStartCommand(). Sau ???? b??i h??t s??? ???????c onPlay...
                     *
                     * Sau ???? ??i???u h?????ng sang PlayerFragment()
                     *
                     * T???i PlayerFragment() , v?? Service ???? ???????c t???o. N??n ta ch??? c???n li??n k???t l???i boundService l?? ta c??
                     * th??? c???p nh???t UI cho PlayerFragment()... b??n trong onServiceConnected().
                     * */
                    /** Sau ???? ??i???u h?????ng ?????n PlayerFragment() */

                    /** L???y position c???a b??i h??t trong danh s??ch Full b??i h??t... */
                    (it as MutableList<Song>).forEachIndexed { index: Int, value: Song ->
                        if (value == data) {
                            /** G???i danh s??ch b??i h??t v?? index c???a b??i h??t ???????c ch???n... */
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