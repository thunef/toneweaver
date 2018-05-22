package se.thune.toneweaver

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.draw_fragment.*

class DrawFragment : Fragment() {
    private val numbers : MutableList<Pair<Float,Long>> = mutableListOf()
    private val TAG : String = "DrawFragment"
    //todo change
    private val center : Float = 500f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.draw_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //val drawView: DrawView? = view.findViewById(R.id.drawView)
        view.setOnTouchListener({ _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    numbers.clear()
                }
                MotionEvent.ACTION_UP -> {}
                MotionEvent.ACTION_MOVE -> {
                    //Log.d(TAG, "move:" + event.x)
                    numbers.add(Pair(event.x, System.currentTimeMillis()))

                    //Log.d("Ehheh", "Hmm" + draw_view)
                    //drawView?.setDrawPoints(getDrawPoints(numbers))
                }
            }
            true
        })
    }


    fun getDrawPoints (list : List<Pair<Float,Long>>): FloatArray {
        return list.take(20).foldRight(floatArrayOf(), { pair , arr ->
            arr.plus(pair.first)
            arr.plus(center - (System.currentTimeMillis() - pair.second) / 100)
            arr
        })
    }

    //TODO write fragment
}

