package se.thune.toneweaver

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import kotlin.concurrent.thread

class SoundTone//TODO FIX
//AudioTrack(AudioAttributes.USAGE_MEDIA, AudioFormat.CHANNEL_OUT_MONO,
//mBufferSize, AudioTrack.MODE_STREAM)
(mBuffer: ShortArray, repeat : Boolean) {
  enum class SoundMaker {
    PLAY, STOP, OFF
  }
  enum class VolumeMaker {
    UP, STEADY, DOWN
  }
  private var soundMaker = SoundMaker.PLAY
  private var volumeMaker = VolumeMaker.UP
  private val volIncTime = 120.0f
  private val volDecTime = 300.0f

  fun drop() {
    if (soundMaker == SoundMaker.PLAY) soundMaker = SoundMaker.STOP
  }

  init {
    if (repeat)
      playRepeat(mBuffer)
    else {
      play(mBuffer)
    }
  }

  fun play(mBuffer: ShortArray) {
    if (!mBuffer.isEmpty()) thread(start = true) {
      val mBufferSize = AudioTrack.getMinBufferSize(44100,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_8BIT)

      //TODO FIX
      //AudioTrack(AudioAttributes.USAGE_MEDIA, AudioFormat.CHANNEL_OUT_MONO,
      //mBufferSize, AudioTrack.MODE_STREAM)
      val mAudioTrack =
        AudioTrack(AudioManager.STREAM_MUSIC, 44100,
          AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
          mBufferSize, AudioTrack.MODE_STREAM)

      mAudioTrack.play()

      var mSubBuffer = mBuffer.toList()
      while (mSubBuffer.isNotEmpty()) {
        mAudioTrack.write(mSubBuffer.take(10000).toShortArray(), 0, 2000)
        mSubBuffer = mSubBuffer.drop(10000)
      }

      mAudioTrack.stop()
      mAudioTrack.release()
    }
  }

  fun playRepeat(mBuffer: ShortArray) {
    if (!mBuffer.isEmpty()) thread(start = true) {
      val mBufferSize = AudioTrack.getMinBufferSize(44100,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_8BIT)

      //TODO FIX
      //AudioTrack(AudioAttributes.USAGE_MEDIA, AudioFormat.CHANNEL_OUT_MONO,
      //mBufferSize, AudioTrack.MODE_STREAM)
      val mAudioTrack =
        AudioTrack(AudioManager.STREAM_MUSIC, 44100,
          AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
          mBufferSize, AudioTrack.MODE_STREAM)

      mAudioTrack.play()
      var volume = 0f
      val startTime = System.currentTimeMillis()
      thread(start = true) {
        while (volume < AudioTrack.getMaxVolume()) {
          mAudioTrack.setVolume(volume)
          volume = Math.min(AudioTrack.getMaxVolume(),
            AudioTrack.getMaxVolume() * (System.currentTimeMillis() - startTime) / volIncTime)
          Thread.sleep(10)
        }
        volumeMaker = VolumeMaker.STEADY
      }
      while (soundMaker == SoundMaker.PLAY || volumeMaker == VolumeMaker.UP) {
        mAudioTrack.write(mBuffer, 0, mBuffer.size)
      }
      val stopTime = System.currentTimeMillis()
      thread(start = true) {
        volumeMaker = VolumeMaker.DOWN
        val currentMax = volume
        val fraction = currentMax / AudioTrack.getMaxVolume()
        while (volume > 0f && soundMaker == SoundMaker.STOP) {
          mAudioTrack.setVolume(volume)
          volume = Math.min(AudioTrack.getMaxVolume(), currentMax * (1 - (System.currentTimeMillis() - stopTime) / (volDecTime * fraction)))
          Thread.sleep(10)
        }
        if (soundMaker == SoundMaker.STOP) soundMaker = SoundMaker.OFF
      }
      while (soundMaker == SoundMaker.STOP) {
        mAudioTrack.write(mBuffer, 0, mBuffer.size)
        Log.d("DOWN", "" + soundMaker)
      }

      mAudioTrack.stop()
      mAudioTrack.release()
    }
  }

  companion object {
    fun getPitch(a : Int) : Float = Math.pow(2.0, a.toDouble() / 12).toFloat()
    fun pitch (p : Float, mBuffer: ShortArray) : ShortArray {
      var di: Double = 0.0
      var i: Int = 0
      val pBuffer = ShortArray(Math.floor(mBuffer.size / p.toDouble()).toInt())
      while (di < mBuffer.size - 2) {
        // L + ((R - L) * fraction)
        pBuffer[i] = (mBuffer[Math.floor(di).toInt()] + ((mBuffer[Math.ceil(di).toInt()] - (mBuffer[Math.floor(di).toInt()])) * (di - Math.floor(di)))).toInt().toShort()
        di += p
        i++
      }
      return pBuffer
    }
  }

}