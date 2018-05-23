package se.thune.toneweaver

import android.content.Context
import android.media.AudioFormat
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.draw_fragment.*
import android.media.AudioFormat.ENCODING_PCM_8BIT
import android.media.AudioFormat.CHANNEL_OUT_MONO
import android.media.AudioTrack
import android.media.AudioManager
import java.util.Collections.frequency
import android.R.attr.duration
import kotlin.concurrent.thread
import kotlin.math.log2


class DrawFragment : Fragment() {
  private val numbers: MutableList<Pair<Float, Long>> = mutableListOf()
  private val samples : MutableList<Float> = mutableListOf()
  private val lines : MutableList<Float> = mutableListOf()
  private val TAG: String = "DrawFragment"
  //todo change
  private val center: Float = 1400f
  private val updateDelaySampler = 33L
  private val updateDelayDraw = 33L
  private val numberOfLines = 500
  private val scale = 15

  enum class SoundMaker {
    PLAY, STOP, OFF
  }

  private var soundMaker: SoundMaker = SoundMaker.OFF
  private var sampling = false

  private var x: Float = 0.0f
  private var screenWidth = 0

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.draw_fragment, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    view.setOnTouchListener({ _, event ->
      when (event.action) {
        MotionEvent.ACTION_DOWN -> {
          Log.d(TAG, "Down")
          numbers.clear()
          samples.clear()
          lines.clear()
          screenWidth = view.width
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
      true
    })
    sound_button.setOnTouchListener({ _, event ->
      when (event.action) {
        MotionEvent.ACTION_DOWN -> {
          Log.d(TAG, "Btn Down")
          soundMaker = SoundMaker.PLAY
          makeSoftSound()
        }
        MotionEvent.ACTION_UP -> soundMaker = SoundMaker.STOP
      }
      true
    })
  }

  private fun startSampler() {
    sampling = true
    thread(start = true) {
      while (sampling) {
        samples.add(x)
        Thread.sleep(updateDelaySampler)
      }
    }
    thread(start = true) {
      var lastTimeDraw : Long
      var startTimeSample = System.currentTimeMillis()
      lines.add(500f)
      lines.add(timeSinceToY(System.currentTimeMillis()))
      while (sampling) {
        lastTimeDraw = System.currentTimeMillis()
        val timeSinceStartY = timeSinceToY(startTimeSample)

        lines.add(x)
        lines.add(timeSinceToY(startTimeSample))
        lines.add(x)
        lines.add(timeSinceToY(startTimeSample))

        val drawLines = lines
          .dropLast(2)
          .takeLast(numberOfLines * 2)
          .mapIndexed({ i, v -> if (i % 2 == 0) v else  center + v - timeSinceStartY})
          .toFloatArray()
        draw_view.setDrawPoints(drawLines)
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

  private fun makeSoftSound() {
    thread(start = true) {
      val mBufferSize = AudioTrack.getMinBufferSize(44100,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_8BIT)

      val mAudioTrack = AudioTrack(AudioManager.STREAM_MUSIC, 44100,
        AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
        mBufferSize, AudioTrack.MODE_STREAM)

      // wave
      var wave = samples.map { v: Float ->
        ((v / 1000.0 - 0.5) * java.lang.Short.MAX_VALUE).toShort()
      }

      val mBuffer = wave.toShortArray()
      mAudioTrack.play()
      var volume = 0f
      val startTime = System.currentTimeMillis()
      thread(start = true) {
        while (volume < 1f && soundMaker == SoundMaker.PLAY) {
          mAudioTrack.setVolume(volume)
          volume = Math.min(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume() * (System.currentTimeMillis() - startTime) / 300.0f)
          Thread.sleep(10)
        }
      }
      while (soundMaker == SoundMaker.PLAY) {
        mAudioTrack.write(mBuffer, 0, mBuffer.size)
      }
      val stopTime = System.currentTimeMillis()
      thread(start = true) {
        val currentMax = volume
        while (volume > 0f && soundMaker == SoundMaker.STOP) {
          mAudioTrack.setVolume(volume)
          volume = Math.min(AudioTrack.getMaxVolume(), currentMax * (1 - (System.currentTimeMillis() - stopTime) / 300.0f))
          Thread.sleep(10)
        }
        soundMaker = SoundMaker.OFF
      }
      while (soundMaker == SoundMaker.STOP) {
        mAudioTrack.write(mBuffer, 0, mBuffer.size)
        Log.d(TAG, ""+soundMaker)
      }
      mAudioTrack.stop()
      mAudioTrack.release()
    }
  }

  //TODO write fragment
}

