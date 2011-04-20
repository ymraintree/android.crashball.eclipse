package org.crashball

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

trait ActiveObject {
  def update

  def draw(canvas: Canvas, paint: Paint)

  def getRect: Rect
}