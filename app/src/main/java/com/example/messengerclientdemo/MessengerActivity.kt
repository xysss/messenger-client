package com.example.messengerclientdemo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.messengerclientdemo.databinding.ActivityMessengerBinding
import model.Person


/**
 * 作者 : xys
 * 时间 : 2022-06-06 10:57
 * 描述 : 描述
 */
class MessengerActivity : AppCompatActivity(){

    companion object {
        const val WHAT1 = 1
        const val WHAT2 = 2
        const val WHAT3 = 3
    }

    private lateinit var viewBinding: ActivityMessengerBinding

    /** 与服务端进行沟通的Messenger */
    private var mService: Messenger? = null

    /** 客户端这边的Messenger */
    private var mClientMessenger = Messenger(IncomingHandler())

    /** 是否已bindService */
    private var bound: Boolean = false

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityMessengerBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.btnConnect.setOnClickListener {
            connectService()
        }
        viewBinding.btnSayHello.setOnClickListener {
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
        val user= User(25,"小明")
        val person= Person("小美",24)

//        message.data=Bundle().apply {
//            putString("data","12334")
//        }

        message.data = Bundle().apply {
            //putParcelable("user", user)
            putParcelable("person", person)
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