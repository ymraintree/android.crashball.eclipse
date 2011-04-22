package org.crashball
import android.app.Activity
import android.view.View

case class TypedResource[T](id: Int)
object TR {
  val ball = TypedResource[org.crashball.CrashBallView](R.id.ball)
  val stock_balls = TypedResource[android.widget.TextView](R.id.stock_balls)
  val message = TypedResource[android.widget.TextView](R.id.message)
}
trait TypedViewHolder {
  def view: View
  def findView[T](tr: TypedResource[T]) = view.findViewById(tr.id).asInstanceOf[T]  
}
trait TypedView extends View with TypedViewHolder { def view = this }
trait TypedActivityHolder {
  def activity: Activity
  def findView[T](tr: TypedResource[T]) = activity.findViewById(tr.id).asInstanceOf[T]
}
trait TypedActivity extends Activity with TypedActivityHolder { def activity = this }
object TypedResource {
  implicit def view2typed(v: View) = new TypedViewHolder { def view = v }
  implicit def activity2typed(act: Activity) = new TypedActivityHolder { def activity = act }
}
