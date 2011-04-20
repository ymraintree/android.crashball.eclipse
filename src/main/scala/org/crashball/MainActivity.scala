package org.crashball

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.view.{View, MotionEvent, Window}
import android.util.Log

object MainActivity {
  private val ICICLE_KEY = "CRASH_BALL"
}

class MainActivity extends Activity with TypedActivity {
  private var mView: CrashBallView = null

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    requestWindowFeature(Window.FEATURE_NO_TITLE)
    setContentView(R.layout.main)
    mView = findView(TR.ball)
    val display = getWindowManager.getDefaultDisplay
    Log.d(this.getClass.getSimpleName, "onCreate width=" + display.getWidth() + " height=" + display.getHeight())
    mView.w = display.getWidth
    mView.h = display.getHeight
    Brick.setWidth(display.getWidth)
    Brick.setHeight(display.getHeight)
    mView.setTextView(findView(TR.message))
    mView.setPointTextView(findView(TR.stock_balls))
    if (icicle == null) {
      mView.setMode(CrashBallView.READY)
    } else {
      val map: Bundle = icicle.getBundle(MainActivity.ICICLE_KEY)
      if (map != null) mView.restoreState(map)
      else mView.setMode(CrashBallView.READY)
    }
  }

  protected override def onPause {
    super.onPause
    mView.setMode(CrashBallView.PAUSE)
  }

  override def onTouchEvent(event: MotionEvent): Boolean = {
    super.onTouchEvent(event)
    mView.onTouchEvent(event)
  }

  override def onSaveInstanceState(outState: Bundle) {
    val icicle: Bundle = new Bundle
    outState.putBundle(MainActivity.ICICLE_KEY, mView.saveState(icicle))
  }
}
