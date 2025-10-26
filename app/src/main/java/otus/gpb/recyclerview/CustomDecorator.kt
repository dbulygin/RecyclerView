package otus.gpb.recyclerview

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView

class CustomDecorator(private val context: Context) : DividerItemDecoration(context, VERTICAL) {

    private val bounds = Rect()
    private val paint = Paint()
    private var offset = 0
    private var color = 0xFF000000.toInt()

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        paint.color = color

        val childCount = parent.childCount
        for (index: Int in 0 until childCount) {
            val child = parent.getChildAt(index)
            parent.getDecoratedBoundsWithMargins(child, bounds)
            bounds.left += offset

            val positionCurrent = parent.getChildAdapterPosition(child)
            if (positionCurrent != RecyclerView.NO_POSITION) {
                val lastElementPosition = parent.adapter?.itemCount?.minus(1)
                if (positionCurrent != lastElementPosition) {
                    c.drawLine(
                        (bounds.left).toFloat(),
                        bounds.bottom.toFloat(),
                        bounds.right.toFloat(),
                        bounds.bottom.toFloat(),
                        paint
                    )
                }
            }
        }
    }

    fun setColor(id: Int) {
        color = context.getColor(id)
    }

    fun setOffset(id: Int) { //73
        offset =
            (context.resources.getInteger(id) * Resources.getSystem().displayMetrics.density).toInt()
    }
}