package se.thune.toneweaver

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import kotlinx.android.synthetic.main.draw_fragment.*
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import java.util.*
import kotlin.concurrent.thread


class DrawFragment : Fragment() {
  private val numbers: MutableList<Pair<Float, Long>> = mutableListOf()
  private val samples: MutableList<Short> = mutableListOf()
  private val lines: MutableList<Float> = mutableListOf()
  private val TAG: String = "DrawFragment"
  private var screenHeight = 0
  private var screenWidth = 0
  //todo change
  private val center: Float = 1400f
  private val updateDelaySampler = 33L
  private val updateDelayDraw = 33L
  private val numberOfLines = 500
  private val scale = 10


  private class BufferHolder {
    var mBuffer : ShortArray

    constructor(mBuffer: ShortArray) {
      this.mBuffer = mBuffer
    }
  }

  private var playField = false
  private var pianoField = false
  private var currentSound : SoundTone = SoundTone(shortArrayOf())
  private var sampling = false

  private var x: Float = 0.0f

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.draw_fragment, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    view.afterMeasured {
      screenHeight = view.height
      screenWidth = view.width
    }


    view.setOnTouchListener({ _, event ->
      handleViewTouch(event)
      true
    })
    sound_button.setOnTouchListener({ _, event ->
      handlePlayButton(event)
      true
    })
    play_field.setOnTouchListener({ _, event ->
      when (event.action) {
        MotionEvent.ACTION_UP -> {
          playField = !playField
          pianoField = false

          piano.visibility = if (pianoField) VISIBLE else GONE
          play_field.setText(if (playField) R.string.playfield_on else R.string.playfield_off)
          piano_field.setText(if (pianoField) R.string.piano_on else R.string.piano_off)
        }
      }
      true
    })
    piano_field.setOnTouchListener({ _, event ->
      when (event.action) {
        MotionEvent.ACTION_UP -> {
          pianoField = !pianoField
          playField = false

          piano.visibility = if (pianoField) VISIBLE else GONE
          play_field.setText(if (playField) R.string.playfield_on else R.string.playfield_off)
          piano_field.setText(if (pianoField) R.string.piano_on else R.string.piano_off)
        }
      }
      true
    })
  }

  private fun handlePlayButton(event: MotionEvent) {
    when (event.action) {
      MotionEvent.ACTION_DOWN -> {
        Log.d(TAG, "Btn Down")
        currentSound.stop()
        currentSound = SoundTone(samples.toShortArray())
      }
        MotionEvent.ACTION_UP -> currentSound.stop()
    }
  }


  private fun handleViewTouch(event: MotionEvent) {
    if (playField) when (event.action) {
      MotionEvent.ACTION_DOWN -> {
        currentSound.stop()
        currentSound = SoundTone(scale(event.y/screenHeight*16,samples.toShortArray()))
      }
      MotionEvent.ACTION_UP -> {
        currentSound.stop()
      }
      MotionEvent.ACTION_MOVE -> {
      }
    } else when (event.action) {
      MotionEvent.ACTION_DOWN -> {
        Log.d(TAG, "Down")
        numbers.clear()
        samples.clear()
        lines.clear()
        x = event.x
        startSampler()
      }
      MotionEvent.ACTION_UP -> {
        Log.d(TAG, "Up")
        stopSampling()
      }
      MotionEvent.ACTION_MOVE -> {
        x = event.x
      }
    }
  }

  private fun scale(mult : Float, arr : ShortArray) : ShortArray {
    Log.d(TAG, "Scale: " + mult)
    return arr.foldIndexed(shortArrayOf(), { i , a, s ->
      if (i%(mult.toInt()+1)==0) a.plus(s) else a
    })
  }

  private fun startSampler() {
    sampling = true
    var startTimeSample = System.currentTimeMillis()
    lines.add(500f)
    lines.add(timeSinceToY(System.currentTimeMillis()))
    thread(start = true) {
      while (sampling) {
        samples.add(((x / screenWidth - 0.5) * java.lang.Short.MAX_VALUE).toShort())


        lines.add(0,timeSinceToY(startTimeSample))
        lines.add(0,x)
        lines.add(0,timeSinceToY(startTimeSample))
        lines.add(0,x)

        Thread.sleep(updateDelaySampler)
      }
    }
    thread(start = true) {
      var lastTimeDraw: Long
      while (sampling) {
        lastTimeDraw = System.currentTimeMillis()
        val timeSinceStartY = timeSinceToY(startTimeSample)


        try {
          val drawLines = lines.toFloatArray()
            .drop(2)
            .take(numberOfLines * 2)
            .mapIndexed({ i, v -> if (i % 2 == 0) v else screenHeight + v - timeSinceStartY })
            .toFloatArray()
          draw_view.setDrawPoints(drawLines)
        } catch (e : ConcurrentModificationException) {
          Log.d(TAG, "NOT GOOD concurrent") // TODO FIX
        } catch (e : IndexOutOfBoundsException) {
          Log.d(TAG, "NOT GOOD index") // TODO FIX
        }
        Thread.sleep(Math.max(0, updateDelayDraw - (System.currentTimeMillis() - lastTimeDraw)))
      }
    }
  }

  fun stopSampling() {
    sampling = false
  }

  fun getDrawPoints(list: Array<Pair<Float, Long>>): FloatArray {
    return list
      .dropLast(1)
      .takeLast(numberOfLines)
      .foldRight(floatArrayOf(list.last().first, timeSinceToY(list.last().second)),
        { pair, arr ->
          arr
            .plus(pair.first)
            .plus(timeSinceToY(pair.second))
            .plus(pair.first)
            .plus(timeSinceToY(pair.second))
        })
  }


  private fun timeSinceToY(time: Long): Float {
    return ((System.currentTimeMillis() - time) / scale).toFloat()
  }

  private inline fun <T : View> T.afterMeasured(crossinline f: T.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
      override fun onGlobalLayout() {
        if (measuredWidth > 0 && measuredHeight > 0) {
          viewTreeObserver.removeOnGlobalLayoutListener(this)
          f()
        }
      }
    })
  }
}

