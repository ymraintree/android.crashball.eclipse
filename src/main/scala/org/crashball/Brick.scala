package org.crashball

import android.graphics.{Color, Canvas, Paint, Rect}

object Brick {
  var height: Int = 20
  var width: Int = 32

  def setWidth(w: Int) {
    width = w / 10
  }

  def setHeight(h: Int) {
    height = h * 6 / 8 / 2 / 10
  }
}

class Brick extends ActiveObject {
  var x = 0
  var y = 0
  var lx = 0
  var ly = 0

  def this(x: Int, y: Int) {
    this ()
    this.x = x * Brick.width
    this.y = y * Brick.height + CrashBallView.STATE
    this.ly = this.y + Brick.height
    this.lx = this.x + Brick.width
  }

  def getRect: Rect = new Rect(x, y, lx, ly)

  def update {}

  def draw(canvas: Canvas, paint: Paint) {
    paint.setColor(Color.RED)
    canvas.drawRect(x, y, lx - 1, ly - 1, paint)
  }

  def crash(ball: Ball, crashPoint: Int): Boolean = true
}
