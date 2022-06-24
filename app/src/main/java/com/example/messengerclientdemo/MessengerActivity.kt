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


/**
 * 作者 : xys
 * 时间 : 2022-06-06 10:57
 * 描述 : 描述
 */
class MessengerActivity : AppCompatActivity(), View.OnTouchListener, View.OnGenericMotionListener {

    private val url = "https://i.bmp.ovh/imgs/2022/06/09/295d6dc338bc0b0d.png"

    private lateinit var mPhotoView: com.github.chrisbanes.photoview.PhotoView
    private lateinit var btnConnect: Button
    private lateinit var btnSayHello: Button
    private lateinit var showText: TextView
    private lateinit var imageView1: ImageView
    private var scaleXMultiple : Float =1F
    private lateinit var bitmap: Bitmap
    private lateinit var mPhotoView2: com.bm.library.PhotoView

    private var smallbig = 1.0f //缩放比例

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

    private val DEBUG_TAG = "xysss"

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

        setContentView(R.layout.activity_messenger)

//        viewBinding = ActivityMessengerBinding.inflate(layoutInflater)
//        setContentView(viewBinding.root)

        mPhotoView = findViewById(R.id.photo_view)
        mPhotoView2 = findViewById(R.id.photo_view2)
        btnConnect = findViewById(R.id.btnConnect)
        btnSayHello = findViewById(R.id.btnSayHello)
        showText = findViewById(R.id.showText)
        imageView1 = findViewById(R.id.imageView1)

        bitmap = BitmapFactory.decodeResource(resources, R.drawable.a)

        btnConnect.setOnClickListener {
            connectService()
        }
        btnSayHello.setOnClickListener {
            sayHello()
        }

        //mPhotoView.setOnTouchListener(this)

        mPhotoView2.setOnGenericMotionListener(this)
        imageView1.setOnGenericMotionListener(this)

        //模糊处理 BlurTransformation
        //灰度处理 GrayscaleTransformation
        //圆角处理 RoundedCornersTransformation
        val multi = MultiTransformation(
            BlurTransformation(1),
            GrayscaleTransformation(),
            RoundedCornersTransformation(50, 0, RoundedCornersTransformation.CornerType.ALL)
        )

        //设置Glide的apply的参数
        val option = RequestOptions()
            .transform(multi)

        Glide.with(this)
            .load(R.drawable.a)
            .apply(option)
            .placeholder(R.drawable.ic_launcher_background)
            .into(mPhotoView)

        val options2 = RequestOptions()
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

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {

        return when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                showText.text = "Action was DOWN"
                Log.d(DEBUG_TAG, "Action was DOWN")
                false
            }
            MotionEvent.ACTION_MOVE -> {
                showText.text = "Action was MOVE"
                Log.d(DEBUG_TAG, "Action was MOVE")
                false
            }
            MotionEvent.ACTION_UP -> {
                showText.text = "Action was UP"
                Log.d(DEBUG_TAG, "Action was UP")
                false
            }
            MotionEvent.ACTION_CANCEL -> {
                showText.text = "Action was CANCEL"
                Log.d(DEBUG_TAG, "Action was CANCEL")
                false
            }
            MotionEvent.ACTION_OUTSIDE -> {
                showText.text = "Movement occurred outside bounds of current screen element"
                Log.d(DEBUG_TAG, "Movement occurred outside bounds of current screen element")
                false
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                showText.text = "第一个手指按下后其他手指按下"
                Log.d(DEBUG_TAG, "第一个手指按下后其他手指按下")
                false
            }
            MotionEvent.ACTION_POINTER_UP -> {
                showText.text = "多个手指长按时抬起其中一个手指，注意松开后还有手指在屏幕上"
                Log.d(DEBUG_TAG, "多个手指长按时抬起其中一个手指，注意松开后还有手指在屏幕上")
                false
            }
            MotionEvent.ACTION_BUTTON_PRESS -> {
                showText.text = "ACTION_BUTTON_PRESS"
                Log.d(DEBUG_TAG, "ACTION_BUTTON_PRESS")
                false
            }
            MotionEvent.ACTION_BUTTON_RELEASE -> {
                showText.text = "ACTION_BUTTON_RELEASE"
                Log.d(DEBUG_TAG, "ACTION_BUTTON_RELEASE")
                false
            }
            MotionEvent.ACTION_SCROLL -> {
                showText.text = "ACTION_SCROLL"
                Log.d(DEBUG_TAG, "ACTION_SCROLL")
                false
            }

            else -> super.onTouchEvent(event)
        }
    }

    fun showThumbImage() {
        imageView1.setImageBitmap(
            thumbImageWithMatrix(imageView1.width.toFloat(), imageView1.height.toFloat())
        )
    }

    fun thumbImageWithMatrix(destWidth: Float, destHeight: Float): Bitmap? {
        val bitmapOrg = BitmapFactory.decodeResource(resources, R.drawable.a)
        val bitmapOrgW = bitmapOrg.width.toFloat()
        val bitmapOrgH = bitmapOrg.height.toFloat()
        val bitmapNewW = destWidth.toInt().toFloat()
        val bitmapNewH = destHeight.toInt().toFloat()
        val matrix = Matrix()
        matrix.postScale(bitmapNewW / bitmapOrgW, bitmapNewH / bitmapOrgH)
        bitmapOrg.recycle()
        return Bitmap.createBitmap(
            bitmapOrg,
            0,
            0,
            bitmapOrgW.toInt(),
            bitmapOrgH.toInt(),
            matrix,
            true
        )
    }

    private fun SmallPicture() {
        val matrix = Matrix()
        //缩放区间 0.5-1.0
        if (smallbig > 1.0f) smallbig -= 0.1f else smallbig = 1.0f
        //x y坐标同一时候缩放
        matrix.setScale(smallbig, smallbig, (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat())
        val createBmp = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvas = Canvas(createBmp) //画布 传入位图用于绘制
        val paint = Paint() //画刷 改变颜色 对照度等属性
        canvas.drawBitmap(bitmap, matrix, paint)
        mPhotoView2.setImageBitmap(createBmp)
    }

    private fun BigPicture() {
        val matrix = Matrix()
        //缩放区间 0.5-1.0
        smallbig = if (smallbig < 5.5f) smallbig + 0.1f else 5.5f
        //x y坐标同一时候缩放
        matrix.setScale(smallbig, smallbig, (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat())
        val createBmp = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvas = Canvas(createBmp)
        val paint = Paint()
        canvas.drawBitmap(bitmap, matrix, paint)
        mPhotoView2.setImageBitmap(createBmp)
    }


    //处理滚轮事件
//    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
//        when (event?.action) {
//            MotionEvent.ACTION_SCROLL -> {
//                if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f) {
//                    if (scaleXMultiple>0)
//                        scaleXMultiple--
//                    showText.text = "向下滚动"
//                    Log.d(DEBUG_TAG, "down")
//                    SmallPicture()
//                    //showThumbImage()
//                } else {
//                    showText.text = "向上滚动"
//                    Log.d(DEBUG_TAG, "up")
//                    if (scaleXMultiple>0)
//                        scaleXMultiple++
//
//                    BigPicture()
//                    //showThumbImage()
//                }
//            }
//            MotionEvent.ACTION_BUTTON_PRESS -> {
//                showText.text = "ACTION_BUTTON_PRESS"
//                Log.d(DEBUG_TAG, "ACTION_BUTTON_PRESS")
//                true
//            }
//            MotionEvent.ACTION_BUTTON_RELEASE -> {
//                showText.text = "ACTION_BUTTON_RELEASE"
//                Log.d(DEBUG_TAG, "ACTION_BUTTON_RELEASE")
//                true
//            }
//            MotionEvent.ACTION_SCROLL -> {
//                showText.text = "ACTION_SCROLL"
//                Log.d(DEBUG_TAG, "ACTION_SCROLL")
//                true
//            }
//        }
//        return super.onGenericMotionEvent(event)
//    }

    override fun onGenericMotion(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_SCROLL -> {
                if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f) {
                    if (scaleXMultiple>0)
                        scaleXMultiple--
                    showText.text = "向下滚动"
                    Log.d(DEBUG_TAG, "down")
                    SmallPicture()
                    //showThumbImage()
                } else {
                    showText.text = "向上滚动"
                    Log.d(DEBUG_TAG, "up")
                    if (scaleXMultiple>0)
                        scaleXMultiple++

                    BigPicture()
                    //showThumbImage()
                }
            }
            MotionEvent.ACTION_BUTTON_PRESS -> {
                showText.text = "ACTION_BUTTON_PRESS"
                Log.d(DEBUG_TAG, "ACTION_BUTTON_PRESS")
                true
            }
            MotionEvent.ACTION_BUTTON_RELEASE -> {
                showText.text = "ACTION_BUTTON_RELEASE"
                Log.d(DEBUG_TAG, "ACTION_BUTTON_RELEASE")
                true
            }

        }
        return super.onGenericMotionEvent(event)
    }


}