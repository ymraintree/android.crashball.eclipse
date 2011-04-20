package org.crashball

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView;

import java.lang.Math
import android.graphics.{Color, Canvas, Paint}
import android.view.{KeyEvent, MotionEvent, View}
import android.os.{Bundle, Handler, Message}
import collection.mutable.ListBuffer

object CrashBallView {
  val PAUSE = 0
  val READY = 1
  val RUNNING = 2
  val LOSE = 3
  val CLEAR = 4
  private val BRICK_ROW = 10
  private val BRICK_COL = 10
  val STATE = 50
}

class CrashBallView(var context: Context, var attrs: AttributeSet) extends View(context, attrs) {

  var w: Int = 320
  var h: Int = 480
  private final val DELAY_MILLIS: Long = 1000 / 60

  private var mMode: Int = CrashBallView.READY
  private var mMessage: TextView = null
  // private var mMessage: TextView = new TextView(context)
  private var mPoint: TextView = null
  // private var mPoint: TextView = new TextView(context)
  private var mFieldHandler = new RefreshHandler
  private var mPaint: Paint = new Paint
  private var mPad: Pad = null
  private var mBalls = new ListBuffer[Ball]
  private var mBricks: Array[Array[Brick]] = new Array[Array[Brick]](CrashBallView.BRICK_COL, CrashBallView.BRICK_ROW)
  private var mBallsCount = 0
  private var mStockBallCount = 0
  private var mBricksCount = 0
  private var mTouchEvent: MotionEvent = null

  initialProcess()

  private def initialProcess() {
    Log.d(this.getClass.getSimpleName, "initialProcess called")
    setFocusable(true)
    Log.d(this.getClass.getSimpleName, "w=" + w + " h=" + h + " " + getMeasuredWidth + " " + getMeasuredHeight)
  }

  private def newGame {
    mBalls.clear
    mBricksCount = 0
    mStockBallCount = 5
    mBallsCount = 0
    for (i <- 0 to CrashBallView.BRICK_COL - 1) {
      for (j <- 0 to CrashBallView.BRICK_ROW - 1) {
        mBricksCount += 1;
        mBricks(i)(j) = new Brick(i, j)
      }
    }
    CrashBallView.this.invalidate
  }

  override def onTouchEvent(event: MotionEvent): Boolean = {
    mTouchEvent = event
    if (event.getAction == MotionEvent.ACTION_DOWN) {
      mMode match {
        case CrashBallView.READY =>
          setMode(CrashBallView.RUNNING)
          addBall
        case CrashBallView.LOSE =>
          setMode(CrashBallView.READY)
        case CrashBallView.CLEAR =>
          setMode(CrashBallView.READY)
        case CrashBallView.PAUSE =>
          setMode(CrashBallView.RUNNING)
          addBall
        case _ =>
      }
    }
    true
  }

  def setMode(newMode: Int) {
    val oldMode = mMode
    mMode = newMode
    if (newMode == CrashBallView.RUNNING) {
      if (oldMode == CrashBallView.READY) {
        newGame
        mMessage.setText(getContext.getResources.getText(R.string.new_ball_help))
        mMessage.setVisibility(View.VISIBLE)
      }
      else if (oldMode == CrashBallView.PAUSE) {
        if (mBallsCount == 0) {
          mMessage.setText(getContext.getResources.getText(R.string.new_ball_help))
        }
        else {
          mMessage.setVisibility(View.INVISIBLE)
          addBall
        }
      }
      if (oldMode != CrashBallView.RUNNING) update
    }

    var newMessage: CharSequence = null
    val resource: Resources = getContext.getResources
    newMode match {
      case CrashBallView.PAUSE =>
        newMessage = resource.getText(R.string.pause_message)
      case CrashBallView.READY =>
        newMessage = resource.getText(R.string.ready_message)
      case CrashBallView.LOSE =>
        newMessage = resource.getText(R.string.game_over_message)
      case CrashBallView.CLEAR =>
        newMessage = resource.getText(R.string.game_clear_message)
      case _ =>
    }
    Log.d(this.getClass.getSimpleName, "newMessage=" + newMessage)
    mMessage.setText(newMessage)
    mMessage.setVisibility(View.VISIBLE)
  }

  protected override def onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    Log.d(this.getClass.getSimpleName, "onSizeChanged called w=" + w + " h=" + h)
    this.w = w
    this.h = h
    mPad = new Pad(w, h)
    setMode(CrashBallView.READY)
  }

  private def isBricksCrash(xIndex: Int, yIndex: Int): Boolean = {
    if (CrashBallView.BRICK_ROW <= yIndex || CrashBallView.BRICK_COL <= xIndex) return false
    if (mBricks(xIndex)(yIndex) != null) return true
    false
  }

  private def crashBrick(xIndex: Int, yIndex: Int) {
    if (CrashBallView.BRICK_ROW <= yIndex || CrashBallView.BRICK_COL <= xIndex || mBricks(xIndex)(yIndex) == null) return
    CrashBallView.this.invalidate(mBricks(xIndex)(yIndex).getRect)
    mBricks(xIndex)(yIndex) = null
    mBricksCount -= 1;
    if (mBricksCount <= 0) {
      setMode(CrashBallView.CLEAR)
    }
  }

  def update {
    if (mMode == CrashBallView.RUNNING) {
      if (mTouchEvent != null) {
        CrashBallView.this.invalidate(mPad.getRect)
        mPad.onTouchEvent(mTouchEvent)
        mPad.update
        CrashBallView.this.invalidate(mPad.getRect)
      }
      var xCrash = 0
      var yCrash = 0
      mBalls.foreach ( (ball) => {
        xCrash = 0
        yCrash = 0
        CrashBallView.this.invalidate(ball.getRect)
        ball.update
        CrashBallView.this.invalidate(ball.getRect)
        val xIndex = (ball.x / Brick.width).asInstanceOf[Int]
        val yIndex = ((ball.y - CrashBallView.STATE) / Brick.height).asInstanceOf[Int]
        val lxIndex = (ball.lx / Brick.width).asInstanceOf[Int]
        val lyIndex = ((ball.ly - CrashBallView.STATE) / Brick.height).asInstanceOf[Int]
        if (isBricksCrash(xIndex, yIndex)) {
          xCrash += 1
          yCrash += 1
        }
        if (isBricksCrash(lxIndex, yIndex)) {
          xCrash -= 1
          yCrash += 1
        }
        if (isBricksCrash(xIndex, lyIndex)) {
          xCrash += 1
          yCrash -= 1
        }
        if (isBricksCrash(lxIndex, lyIndex)) {
          xCrash -= 1
          yCrash -= 1
        }
        crashBrick(xIndex, yIndex)
        crashBrick(xIndex, lyIndex)
        crashBrick(lxIndex, yIndex)
        crashBrick(lxIndex, lyIndex)
        if (0 < yCrash) {
          ball.topCrash(yIndex)
        } else if (yCrash < 0) {
          ball.downCrash(lyIndex)
        }
        if (0 < xCrash) {
          ball.leftCrash(xIndex)
        } else if (xCrash < 0) {
          ball.rightCrash(lxIndex)
        }
        if (mPad.y <= ball.ly && ball.y <= mPad.ly && mPad.x <= ball.lx && ball.x <= mPad.lx) {
          var newXSpeed = ball.xSpeed + (ball.cx - mPad.lx) / 5
          var newYSpeed = -(ball.ySpeed - Math.abs(ball.cx - mPad.lx) * 1.2f)
          if (newYSpeed > -10) newYSpeed = -10
          ball.setXSpeed(newXSpeed)
          ball.setYSpeed(newYSpeed)
          if (ball.maxYSpeed < 15) ball.maxYSpeed += 0.1f
        } else if (h < ball.y) {
          mBalls -= ball
          mBallsCount -= 1
          if (mBallsCount == 0) {
            if (mStockBallCount > 0) {
              setMode(CrashBallView.PAUSE)
              mMessage.setText(getContext.getResources.getText(R.string.new_ball_help))
              mMessage.setVisibility(View.VISIBLE)
            } else {
              setMode(CrashBallView.LOSE)
              return
            }
          }
        }
      })
      mFieldHandler.sleep(DELAY_MILLIS)
    } else {
      CrashBallView.this.invalidate
    }
  }

  override def onDraw(canvas: Canvas) {
    canvas.drawColor(Color.rgb(120, 140, 160))
    mPaint.setColor(Color.BLACK)
    canvas.drawRect(0, CrashBallView.STATE, w, h, mPaint)
    mPad.draw(canvas, mPaint)
    mBalls.foreach( (ball) => ball.draw(canvas, mPaint) )
    for (i <- 0 to CrashBallView.BRICK_COL - 1) {
      for (j <- 0 to CrashBallView.BRICK_ROW - 1) {
        if (mBricks(i)(j) != null) mBricks(i)(j).draw(canvas, mPaint)
      }
    }
  }

  def setTextView(message: TextView) {
    mMessage = message
  }

  def setPointTextView(point: TextView) {
    mPoint = point
  }

  override def onKeyDown(keyCode: Int, event: KeyEvent): Boolean = {
    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
      mMode match {
        case CrashBallView.READY =>
          setMode(CrashBallView.RUNNING)
          update
          return true
        case CrashBallView.RUNNING =>
          setMode(CrashBallView.PAUSE)
          update
          return true
        case CrashBallView.PAUSE =>
          setMode(CrashBallView.RUNNING)
          update
          return true
      }
      return true
    }
    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_SPACE) addBall
    super.onKeyDown(keyCode, event)
  }

  private def addBall {
    Log.d(this.getClass.getSimpleName, "addBall called w=" + w + " h=" + h)
    if (mMode == CrashBallView.RUNNING && 0 < mStockBallCount) {
      if (mBallsCount == 0) mMessage.setVisibility(View.INVISIBLE)
      mBalls += new Ball(w / 2, h / 2, -0.2f, -5, w, h)
      mBallsCount += 1;
      mStockBallCount -= 1;
      mPoint.setText(getContext.getResources.getText(R.string.stock_ball_count) + Integer.toString(mStockBallCount))
    }
  }

  def restoreState(icicle: Bundle) {
    setMode(CrashBallView.PAUSE)
    mMode = icicle.getInt("mode")
    mBalls = flaotsToBalls(icicle.getFloatArray("balls"))
  }

  private def flaotsToBalls(rawArray: Array[Float]): ListBuffer[Ball] = {
    var balls = new ListBuffer[Ball]
    val coordCount = rawArray.length
    var index = 0
    while (index < coordCount) {
      balls += new Ball(rawArray(index), rawArray(index + 1), rawArray(index + 2), rawArray(index + 3), w, h)
      index += 4
    }
    balls
  }

  def saveState(icicle: Bundle): Bundle = {
    icicle.putInt("mode", mMode)
    icicle.putFloatArray("balls", ballsToFloats(mBalls))
    return icicle
  }

  private def ballsToFloats(balls: ListBuffer[Ball]): Array[Float] = {
    val rawArray: Array[Float] = new Array[Float](balls.size * 4)
    for (index <- 0 to balls.size - 1) {
      rawArray(4 * index) = balls(index).x
      rawArray(4 * index + 1) = balls(index).y
      rawArray(4 * index + 2) = balls(index).xSpeed
      rawArray(4 * index + 3) = balls(index).ySpeed
    }
    return rawArray
  }

  private class RefreshHandler extends Handler {
    def sleep(delayMillis: Long) {
      removeMessages(0)
      sendMessageDelayed(obtainMessage(0), delayMillis)
    }

    override def handleMessage(msg: Message) {
      CrashBallView.this.update
    }
  }

}
