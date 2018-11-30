package jp.ac.asojuku.st.myrollingball

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import jp.ac.asojuku.st.myrollingball.R.drawable.*
import jp.ac.asojuku.st.myrollingball.R.id.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : Activity(),SensorEventListener,SurfaceHolder.Callback{

    //プロパティ
    private var surfaceWidth:Int = 0//サーフェスの幅
    private var surfaceHeight:Int = 0//サーフェスの高さ
    private val radius = 50.0f//ボールの半径
    private val coef = 1000.0f//ボールの移動量を計算するための係数
    private var ballX:Float = 0.0f//ボールの現在のX座標
    private var ballY:Float = 0.0f//ボールの現在のY座標
    private var vx:Float = 0f//ボールのX方向の加速度
    private var vy:Float = 0f//ボールのY方向の加速度
    private var time:Long = 0L//前回の取得時間
    private var switch:Boolean = true//障害物生成カウント
    private var x:FloatArray = floatArrayOf(90.0f,100.0f,110.0f,120.0f,130.0f,140.0f)
    private val refuse:Int = Random().nextInt(6) + 6//障害物個数
    private val array = Array(refuse){FloatArray(4)}
    private var tmp1:Float = 0.0f
    private var tmp2:Float = 0.0f
    private var tmp3:Float = 0.0f
    private var tmp4:Float = 0.0f
    private var count:Int = 0

    //誕生時のライフサイクルメソッド
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val holder = surfaceView.holder//サーフェスホルダー取得
        //サーフェスフホルダーにコールバックに自クラスを追加
        holder.addCallback(this)
        //画面の縦横指定をアプリから指定してロック
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    //画面表示・再表示のライフサイクルイベント
    override fun onResume() {
        //親クラスのonResume()処理
        super.onResume()
        //自クラスのonResume()処理
        //センサーマネージャをOSから取得
        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        //加速度センサー(Accelerometer)を指定してセンサーマネージャからセンサーを取得
        val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        //リスナー登録して加速度センサーの監視を開始
        //1.イベントリスナー機能をもつインスタンス 2.加速度センサー 3.センサーの更新頻度
        sensorManager.registerListener(this,accSensor,SensorManager.SENSOR_DELAY_GAME)
        resetbtn.setOnClickListener{
            finish()
            startActivity(getIntent())
        }
    }

    override fun onPause() {
        super.onPause()
        //センサーマネージャを取得
        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        //センサーマネージャに登録したリスナーを解除(自分自身を解除)
        sensorManager.unregisterListener(this)
    }

    //精度が変わった時のイベントコールバック
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
    //センサーの値が変わった時のイベントコールバック
    override fun onSensorChanged(event: SensorEvent?) {
        //イベントが何もなかったらそのままリターン
        if(event == null){return;}
        //センサーの値が変わったらログに出力
//        if(event.sensor.type == Sensor.TYPE_ACCELEROMETER){
//            val str:String = "x = ${event.values[0].toString()}" + "y = ${event.values[1].toString()}" + "z = ${event.values[2].toString()}"
//            //デバッグログに出力
//            //Log.d("加速度センサー",str)
//            //テキストビューに表示
//            //txvMain.text= str;
//        }
        //ボールの描画計算
        if(time == 0L){
            //最初のタイミングでは現在時刻を保有
            time = System.currentTimeMillis()
        }
        //イベントのセンサー識別の情報がアクセラメーター(加速度センサー)の時だけ以下の処理を実行
        if(event.sensor.type == Sensor.TYPE_ACCELEROMETER){
            //センサーのx(左右),y(縦)値を取得
            val x = event.values[0]*-1
            val y = event.values[1]

            //経過時間を計算(今の計算ー前の時間 = 経過時間)
            var t = (System.currentTimeMillis() -time).toFloat()
            time = System.currentTimeMillis()
            t /= 1000.0f

            //移動距離を計算(ボールをどれくらい動かすか)
            val dx = (vx*t) + (x * t * t)/2.0f//xの移動距離(メートル)
            val dy = (vy*t) + (y * t * t)/2.0f//yの移動距離(メートル)
            //メートルをピクセルのcmに
            ballX += (dx * coef)
            ballY += (dy * coef)
            //今の加速度を更新
            vx +=(x * t)
            vy +=(y * t)
            //画面の端にきたら跳ね返る処理
            //左右について
            if(ballX - radius < 0 && vx < 0){
                //左にぶつかった時
                vx = -vx / 1.5f
                ballX = radius
            }else if(ballX + radius > surfaceWidth && vx > 0){
                //右にぶつかった時
                vx = -vx /1.5f
                ballX = surfaceWidth - radius
            }
            //じょうげについて　
            if(ballY - radius < 0 && vy < 0){
                //下にぶつかった時
                vy = -vy / 1.5f
                ballY = radius
            }else if(ballY + radius > surfaceHeight && vy > 0){
                //上にぶつかった時
                vy = -vy /1.5f
                ballY = surfaceHeight - radius
            }
            if(50.0f < (ballX +radius) && (ballX - radius) < 150.0f){
                if(50.0f < (ballY+radius) && (ballY - radius) < 150.0f){
                    val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
                    sensorManager.unregisterListener(this)
                    img.setImageResource(success)
                }
            }
            for(i in 0..(refuse-1)){
                if(array[i][0] < (ballX +radius) && (ballX - radius) < array[i][2]){
                    if(array[i][1] < (ballY+radius) && (ballY - radius) < array[i][3]){
                        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
                        sensorManager.unregisterListener(this)
                        img.setImageResource(miss)
                    }
                }
            }
            //サーフェスのキャンバスに描画
            drawCanvas()
        }
    }
    //サーフェスが更新された時のイベント
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        //サーフェスの幅と高さをプロパティに保存しておく
        surfaceHeight = height
        surfaceWidth = width
        //ボールの初期位置を保存しておく
        ballX = (width / 2).toFloat()
        ballY = (height / 2).toFloat()
    }
    //サーフェスが破棄された時のイベント
    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        //加速度センサーの登録を解除する流れ
        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        //センサーマネージャを通じてOSからリスナー(自分自身)を登録解除
        sensorManager.unregisterListener(this)
    }
    //サーフェスが作成された時のイベント
    override fun surfaceCreated(holder: SurfaceHolder?) {
        //加速度センサーのリスナーを登録する流れ
        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        //センサーマネージャーから加速度センサーを取得
        val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        //加速度センサーのリスナーをOSに登録
        sensorManager.registerListener(
                this,//リスナー(自クラス)
                accSensor,//加速度センサー
                SensorManager.SENSOR_DELAY_GAME//センシングの適用
        )
    }

    //サーフェスのキャンバスに描画するメソッド
    private fun drawCanvas(){
        //キャンバスをロックして取得
        val canvas = surfaceView.holder.lockCanvas()
        //キャンバスの背景色を設定
        canvas.drawColor(Color.GREEN)
        if(switch){
            drawRefuse()
            ballX = 950.0f
            ballY = 1300.0f

        }else{
            for(i in 0..(refuse-1)){
                canvas.drawRect(array[i][0],array[i][1],array[i][2],array[i][3],Paint().apply {
                    color = Color.RED
                })
//              99  canvas.drawLine(array[i][0],array[i][1],array[i][2],array[i][3],Paint().apply {
//                    strokeWidth = array[i][4]
//                    color = Color.RED
//                })
            }
            canvas.drawRect(50.0f,50.0f,150.0f,150.0f,Paint().apply {
                color = Color.BLUE
            })
            canvas.drawCircle(
                    ballX,
                    ballY,
                    radius,
                    Paint().apply{
                        color = Color.BLACK
                    }
            )
        }
        //キャンバスに縁を描いてボールにする

        //キャンバスをアンロック(ロック解除)
        surfaceView.holder.unlockCanvasAndPost(canvas)
    }
    private fun drawRefuse(){
        for(i in 0..(refuse-1)){
            if(i == 0){
                array[i][0] = (Math.random() * 800 + 25).toFloat()
                array[i][1] = (Math.random() * 1000 + 150).toFloat()
                array[i][2] = array[i][0] + x[Random().nextInt(6)]
                array[i][3] = array[i][1] + x[Random().nextInt(6)]
            }else{
                tmp1 = (Math.random() * 800 + 25).toFloat()//x開始
                tmp2 = (Math.random() * 1000 + 150).toFloat()//y開始
                tmp3 = tmp1 + x[Random().nextInt(6)]//x終わり
                tmp4 = tmp2 + x[Random().nextInt(6)]//y終わり
                while(count < i ) {
                    if((array[count][0] <= tmp1 && array[count][2] >= tmp1 ) || (tmp3 >= array[count][0] && tmp3 <= array[count][2])){
                        tmp1 = (Math.random() * 800 + 25).toFloat()
                        tmp2 = (Math.random() * 950 + 150).toFloat()
                        tmp3 = tmp1 + x[Random().nextInt(6)]
                        tmp4 = tmp2 + x[Random().nextInt(6)]
                        count = 0
                    }else if((array[count][1] <= tmp2  && array[count][3] >= tmp2 ) || (tmp4 >= array[count][1] && tmp4 <= array[count][3])){
                        tmp1 = (Math.random() * 800 + 25).toFloat()
                        tmp2 = (Math.random() * 1000 + 150).toFloat()
                        tmp3 = tmp1 + x[Random().nextInt(6)]
                        tmp4 = tmp2 + x[Random().nextInt(6)]
                        count = 0
                    }else{
                        count++
                    }
                }
                array[i][0] = tmp1
                array[i][1] = tmp2
                array[i][2] = tmp3
                array[i][3] = tmp4
                count = 0
            }
        }
        switch = false
    }
}

