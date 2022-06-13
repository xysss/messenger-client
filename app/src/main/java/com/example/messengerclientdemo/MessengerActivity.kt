package com.example.messengerclientdemo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MotionEventCompat
import com.bm.library.PhotoView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.request.RequestOptions
import com.example.model.Person
import com.example.model.User
import com.example.model.UserS
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.GrayscaleTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation


/**
 * 作者 : xys
 * 时间 : 2022-06-06 10:57
 * 描述 : 描述
 */
class MessengerActivity : AppCompatActivity(){

    private val url = "https://i.bmp.ovh/imgs/2022/06/09/295d6dc338bc0b0d.png"

    private lateinit var mPhotoView: com.github.chrisbanes.photoview.PhotoView
    private lateinit var btnConnect: Button
    private lateinit var btnSayHello: Button
    private lateinit var mPhotoView2: com.bm.library.PhotoView



    companion object {
        const val WHAT1 = 1
        const val WHAT2 = 2
        const val WHAT3 = 3
    }

    //private lateinit var viewBinding: ActivityMessengerBinding

    /** 与服务端进行沟通的Messenger */
    private var mService: Messenger? = null

    /** 客户端这边的Messenger */
    private var mClientMessenger = Messenger(IncomingHandler())

    /** 是否已bindService */
    private var bound: Boolean = false

    private val DEBUG_TAG="DEBUG_TAG"

    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mService = Messenger(service)
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null
            bound = false
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val action: Int = MotionEventCompat.getActionMasked(event)

        return when (action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(DEBUG_TAG, "Action was DOWN")
                true
            }
            MotionEvent.ACTION_MOVE -> {
                Log.d(DEBUG_TAG, "Action was MOVE")
                true
            }
            MotionEvent.ACTION_UP -> {
                Log.d(DEBUG_TAG, "Action was UP")
                true
            }
            MotionEvent.ACTION_CANCEL -> {
                Log.d(DEBUG_TAG, "Action was CANCEL")
                true
            }
            MotionEvent.ACTION_OUTSIDE -> {
                Log.d(DEBUG_TAG, "Movement occurred outside bounds of current screen element")
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_messenger)

//        viewBinding = ActivityMessengerBinding.inflate(layoutInflater)
//        setContentView(viewBinding.root)

        mPhotoView = findViewById(R.id.photo_view)
        mPhotoView2 = findViewById(R.id.photo_view2)

        btnConnect = findViewById(R.id.btnConnect)
        btnSayHello = findViewById(R.id.btnSayHello)



        btnConnect.setOnClickListener {
            connectService()
        }
        btnSayHello.setOnClickListener {
            sayHello()
        }


        //模糊处理 BlurTransformation
        //灰度处理 GrayscaleTransformation
        //圆角处理 RoundedCornersTransformation
        val multi = MultiTransformation(
            BlurTransformation(1),
            GrayscaleTransformation(),
            RoundedCornersTransformation(50, 0, RoundedCornersTransformation.CornerType.ALL)
        )

        //设置Glide的apply的参数
        val option= RequestOptions()
            .transform(multi)

        Glide.with(this)
            .load(R.drawable.a)
            .apply(option)
            .placeholder(R.drawable.ic_launcher_background)
            .into(mPhotoView)



        val options2=RequestOptions()
            //.transform(BlurTransformation(25))
        // 启用图片缩放功能
        mPhotoView2.enable()
        Glide.with(this)
            .load(R.drawable.a)
            //.load(url)
            .apply(options2)
            .placeholder(R.drawable.ic_launcher_background)
            .into(mPhotoView2)

    }

    private fun sayHello() {
        if (!bound) {
            return
        }
        //创建,并且发送一个message给服务端   Message中what指定为MSG_SAY_HELLO
        val message = Message.obtain(null, WHAT1, 0, 0)
        //注意 这里是新增的
        message.replyTo = mClientMessenger
        val person= Person("小美",24)

        val user= User("小美",24)

        val userS= UserS("小美",24)

//        message.data=Bundle().apply {
//            putString("data","12334")
//        }

        message.data = Bundle().apply {
            putSerializable("person", userS)
            //putParcelable("person", person)
        }

        try {
            mService?.send(message)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun connectService() {
        Intent().apply {
            action = "com.example.messenger.Server.Action"
            setPackage("com.example.messengerservicedemo")
        }.also { intent ->
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (bound) {
            unbindService(mServiceConnection)
            bound = false
        }
    }

    class IncomingHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                WHAT1 -> {
                    Log.e("TAG", "Received from service: ${msg.data?.getString("reply")}")
                }
                else -> super.handleMessage(msg)
            }
        }
    }


}