package com.example.myapplication;

import android.animation.ValueAnimator
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout

class FloatingBallService : Service() {

    private lateinit var ballView: View
    private lateinit var panelView: LinearLayout
    private var isExpanded = false

    private val windowManager by lazy { getSystemService(Context.WINDOW_SERVICE) as WindowManager }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private var lastX = 0f
    private var lastY = 0f
    private var isDragging = false
    private val screenWidth = Resources.getSystem().displayMetrics.widthPixels
    val touchThreshold = 10f  // 超过这个阈值将被视为拖拽操作而非点击操作

    override fun onCreate() {
        super.onCreate()

        // 创建 WindowManager.LayoutParams 实例
        val ballParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT ,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )

        // 初始化悬浮球和面板视图
        val layoutInflater = LayoutInflater.from(this)
        val floatingView = layoutInflater.inflate(R.layout.layout_floating_ball, null)

        ballView = floatingView.findViewById(R.id.ballView)
        panelView = floatingView.findViewById(R.id.panelView)

        windowManager.addView(floatingView, ballParams)

        ballParams.x = screenWidth - ballView.width
        ballParams.y = Resources.getSystem().displayMetrics.heightPixels - ballView.height

        ballView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 开始拖拽
                    lastX = event.rawX
                    lastY = event.rawY
                    isDragging = false
                }
                MotionEvent.ACTION_MOVE -> {

                    val offsetX = event.rawX - lastX
                    val offsetY = event.rawY - lastY
                    if (Math.abs(offsetX) > touchThreshold || Math.abs(offsetY) > touchThreshold) {
                        isDragging = true
                    }
                    if (isDragging) {
                        view.x += offsetX
                        view.y += offsetY
                        lastX = event.rawX
                        lastY = event.rawY
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (isDragging){
                        // 根据位置吸附到屏幕边缘
                        // 根据悬浮球的位置吸附到屏幕的左或右边缘
                        val targetX =
                            if (view.x + view.width / 2 < screenWidth / 2) 0f else (screenWidth - view.width).toFloat()

                        view.animate()
                            .x(targetX)
                            .setDuration(300)
                            .start()
                    }else{
                        view.performClick()  // 如果是点击操作，则调用 performClick()
                    }


                }
            }
            true
        }

        ballView.setOnClickListener {
            if (isExpanded) {
                collapsePanel(floatingView)
            } else {
                expandPanel(floatingView)
            }
            isExpanded = !isExpanded
        }

    }

    private fun expandPanel(floatingView: View) {
        panelView.visibility = View.VISIBLE
        panelView.alpha = 0f

        // 获取屏幕尺寸和悬浮球的位置
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels
        val ballX = ballView.x // 悬浮球当前的X坐标
        val ballY = ballView.y // 悬浮球当前的Y坐标

        // 面板最终的宽度和高度
        val finalWidth = screenWidth
        val finalHeight = screenHeight

        // 面板的起始宽度和高度是悬浮球的宽度和高度
        val initialWidth = ballView.width
        val initialHeight = ballView.height

        // 计算悬浮球中心点
        val ballCenterX = ballX + ballView.width / 2
        val ballCenterY = ballY + ballView.height / 2

        // 动画从悬浮球的大小变化到全屏
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300 // 动画持续时间
            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                // 计算当前宽度和高度
                val currentWidth = initialWidth + (finalWidth - initialWidth) * animatedValue
                val currentHeight = initialHeight + (finalHeight - initialHeight) * animatedValue

                // 更新 floatingView 的布局参数
                val layoutParams = floatingView.layoutParams as WindowManager.LayoutParams
                layoutParams.width = currentWidth.toInt()
                layoutParams.height = currentHeight.toInt()

                // 面板中心店
                val centerX = screenWidth / 2
                val centerY = screenHeight / 2

                // 设置面板居中显示
                layoutParams.x = (centerX - currentWidth / 2).toInt()
                layoutParams.y = (centerY - currentHeight / 2).toInt()

                // 更新 floatingView 的布局参数
                windowManager.updateViewLayout(floatingView, layoutParams)

                // 更新面板的透明度
                panelView.alpha = animatedValue
            }
            start()
        }
    }


    private fun collapsePanel(floatingView: View) {
        // 获取屏幕尺寸
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels

        // 设置动画，从全屏变化到悬浮球尺寸
        ValueAnimator.ofFloat(1f, 0f).apply {
            duration = 300 // 动画持续时间
            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                // 计算当前宽度和高度
                val layoutParams = floatingView.layoutParams as WindowManager.LayoutParams
                layoutParams.width = (screenWidth * animatedValue).toInt()
                layoutParams.height = (screenHeight * animatedValue).toInt()

                // 设置面板居中显示
                layoutParams.x = ((screenWidth - layoutParams.width) / 2)
                layoutParams.y = ((screenHeight - layoutParams.height) / 2)

                // 更新 floatingView 的布局参数
                windowManager.updateViewLayout(floatingView, layoutParams)

                // 更新面板的透明度
                panelView.alpha = 1 - animatedValue

                // 动画结束时隐藏面板
                if (animatedValue == 0f) {
                    panelView.visibility = View.GONE
                }
            }
            start()
        }
    }


    // TODO: 实现其他方法，例如添加和移除悬浮球，处理屏幕边缘吸附等
}




