package org.crashball

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect

import java.lang.Math

object Ball {
  private val SIZE = 16
  private val HALF_SIZE = Ball.SIZE / 2
  private val LEFT_TOP = 0
  private val RIGHT_TOP = 1
  private val LEFT_DOWN = 2
  private val RIGHT_DOWN = 3
}

class Ball extends ActiveObject {

  private var mScreenWide = 100
  private var mState = 50
  var xSpeed = .0f
  var ySpeed = .0f
  var maxYSpeed = 8f
  val maxXSpeed = 3f
  var x = .0f
  var y = .0f
  var g = 0.15f

  def lx: Float = x + Ball.SIZE

  def cx: Float = x + Ball.HALF_SIZE

  def ly: Float = y + Ball.SIZE

  def getRect: Rect = new Rect(x.asInstanceOf[Int] - 1, y.asInstanceOf[Int] - 1, lx.asInstanceOf[Int] + 1, ly.asInstanceOf[Int] + 1)

  def this(x: Float, y: Float, xSpeed: Float, ySpeed: Float, w: Int, h: Int) {
    this()
    this.x = x
    this.y = y
    this.xSpeed = xSpeed
    this.ySpeed = ySpeed
    mScreenWide = w
    mState = CrashBallView.STATE
  }

  def setXSpeed(speed: Float) {
    xSpeed = getNewSpeed(speed, maxXSpeed)
  }

  def setYSpeed(speed: Float) {
    ySpeed = getNewSpeed(speed, maxYSpeed)
  }

  private def getNewSpeed(newSpeed: Float, maxSpeed: Float) = {
    if (maxSpeed < Math.abs(newSpeed)) {
      if (0 < newSpeed) maxSpeed
      else -maxSpeed
    }
    newSpeed
  }

  private def getAfterCrashPoint(speed: Float, position: Float, wall: Int): Int = {
    var newPoint: Int = ((wall * 2) - position.asInstanceOf[Int] - speed.asInstanceOf[Int])
    // 壁のめりこみ対策
    if ((0 < speed && wall < newPoint) || (speed < 0 && newPoint < wall)) newPoint = wall.asInstanceOf[Int]
    newPoint
  }

  def update {
    ySpeed += g / 2 // ここが肝心
    if (x + xSpeed <= 0) {
      x = getAfterCrashPoint(xSpeed, x, 0)
      xSpeed *= -1.01f
      xSpeed = getNewSpeed(xSpeed, maxXSpeed)
    } else {
      if (mScreenWide <= lx + xSpeed) {
        x = getAfterCrashPoint(xSpeed, lx, mScreenWide) - Ball.SIZE
        xSpeed *= -1.01f
        xSpeed = getNewSpeed(xSpeed, maxXSpeed)
      } else {
        x += xSpeed
      }
    }
    if (y + ySpeed <= mState) {
      y = getAfterCrashPoint(ySpeed, y, mState)
      ySpeed *= -0.9f
      ySpeed = getNewSpeed(ySpeed, maxYSpeed)
    } else {
      y += ySpeed
    }
  }

  def draw(canvas: Canvas, paint: Paint) {
    paint.setColor(Color.WHITE)
    paint.setAntiAlias(true)
    canvas.drawCircle(x + Ball.HALF_SIZE, y + Ball.HALF_SIZE, Ball.HALF_SIZE, paint)
  }

  def topCrash(index: Int) {
    y += ySpeed
    ySpeed = Math.abs(ySpeed)
  }

  def downCrash(index: Int) {
    y += ySpeed
    ySpeed = -Math.abs(ySpeed)
  }

  def leftCrash(index: Int) {
    x += xSpeed
    xSpeed = Math.abs(xSpeed)
  }

  def rightCrash(index: Int) {
    x += xSpeed
    xSpeed = -Math.abs(xSpeed)
  }
}
