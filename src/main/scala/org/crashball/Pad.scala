package org.crashball

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent
import android.util.Log

object Pad {
  val WIDTH: Int = 80
  val HEIGHT: Int = 10
  val WIDTH_BLOCK: Int = WIDTH / 5
  val HALF_WIDTH: Int = WIDTH / 2
}

class Pad extends ActiveObject {

  private var mScreenWide = 100
  var x = .0f
  var y = .0f
  private var mTouchX: Float = 100

  def this(w: Int, h: Int) {
    this ()
    mScreenWide = w
    y = h * 5 / 6
    Log.d(this.getClass.getSimpleName, "Pad constructor mScreenWide=" + mScreenWide + " y=" + y)
  }

  def lx: Float = x + Pad.WIDTH

  def ly: Float = y + Pad.HEIGHT

  def colArea(pointX: Float): Int = {
    if (pointX < x) 0
    else (pointX - x).asInstanceOf[Int] / Pad.WIDTH_BLOCK + 1
  }

  def onTouchEvent(event: MotionEvent) {
    if (event.getAction == MotionEvent.ACTION_MOVE) mTouchX = event.getX
  }

  def update {
    x = mTouchX - Pad.HALF_WIDTH
    if (x < 0) x = 0
    else if (lx > mScreenWide) x = mScreenWide - Pad.WIDTH
  }

  def setSize(w: Int, h: Int) {
    mScreenWide = w
  }

  def draw(canvas: Canvas, paint: Paint) {
    paint.setColor(Color.YELLOW)
    canvas.drawRect(x, y, x + Pad.WIDTH - 1, y + Pad.HEIGHT, paint)
  }

  def getRect: Rect = {
    new Rect(x.asInstanceOf[Int], y.asInstanceOf[Int], x.asInstanceOf[Int] + Pad.WIDTH, y.asInstanceOf[Int] + Pad.HEIGHT)
  }
}
