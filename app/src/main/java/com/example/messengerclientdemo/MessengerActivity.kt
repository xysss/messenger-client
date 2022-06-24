package com.example.messengerclientdemo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.*
import android.os.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.request.RequestOptions
import com.example.model.Person
import com.example.model.User
import com.example.model.UserS
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.GrayscaleTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import java.lang.ref.WeakReference


/**
 * 作者 : xys
 * 时间 : 2022-06-06 10:57
 * 描述 : 描述
 */
class MessengerActivity : AppCompatActivity() {

    private val url = "https://i.bmp.ovh/imgs/2022/06/09/295d6dc338bc0b0d.png"

    private lateinit var btnConnect: Button
    private lateinit var btnSayHello: Button
    private lateinit var showText: TextView

    companion object {
        const val WHAT1 = 1
        const val WHAT2 = 2
        const val WHAT3 = 3
    }

    //private lateinit var viewBinding: ActivityMessengerBinding

    /** 与服务端进行沟通的Messenger */
    private var mService: Messenger? = null

    /** 客户端这边的Messenger */
    private var mClientMessenger = Messenger(IncomingHandler(WeakReference(this)))

    /** 是否已bindService */
    private var bound: Boolean = false

    private val DEBUG_TAG = "xysss"

    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mService = Messenger(service)
            bound = true
            sayHello()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null
            bound = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_messenger)

//        viewBinding = ActivityMessengerBinding.inflate(layoutInflater)
//        setContentView(viewBinding.root)

        btnConnect = findViewById(R.id.btnConnect)
        btnSayHello = findViewById(R.id.btnSayHello)
        showText = findViewById(R.id.showText)

        btnConnect.setOnClickListener {
            connectService()
        }
        btnSayHello.setOnClickListener {
            sayHello()
        }

    }

    private fun sayHello() {
        if (!bound) {
            return
        }
        //创建,并且发送一个message给服务端   Message中what指定为MSG_SAY_HELLO
        val message = Message.obtain(null, WHAT1, 0, 0)
        //注意 这里是新增的
        message.replyTo = mClientMessenger
        val person = Person("小美", 24)
        val user = User("小美", 24)
        val userS = UserS("小美", 24)

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

    private class IncomingHandler(val wrActivity: WeakReference<MessengerActivity>) :
        Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            wrActivity.get()?.run {
                when (msg.what) {
                    WHAT1 -> {
                        val userS=msg.data?.getSerializable("person")
                        //val person : Person? = acceptBundle.getParcelable("person")
                        Log.e("来自service的",userS.toString())
                        showText.text = userS.toString()
                    }
                    else -> super.handleMessage(msg)
                }
            }

        }
    }

}