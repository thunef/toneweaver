package se.thune.toneweaver

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.concurrent.thread

class SoundTone//TODO FIX
//AudioTrack(AudioAttributes.USAGE_MEDIA, AudioFormat.CHANNEL_OUT_MONO,
//mBufferSize, AudioTrack.MODE_STREAM)
(mBuffer: ShortArray) {

  enum class SoundMaker {
    PLAY, STOP, OFF
  }
  private var soundMaker = SoundMaker.PLAY
  private val volIncTime = 3000.0f
  private val volDecTime = 3000.0f

  fun stop() {
    if (soundMaker == SoundMaker.PLAY) soundMaker = SoundMaker.STOP
  }

  init {
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
        while (volume < 1f && soundMaker == SoundMaker.PLAY) {
          mAudioTrack.setVolume(volume)
          volume = Math.min(AudioTrack.getMaxVolume(),
            AudioTrack.getMaxVolume() * (System.currentTimeMillis() - startTime) / volIncTime)
          Thread.sleep(10)
        }
      }
      while (soundMaker == SoundMaker.PLAY) {
        mAudioTrack.write(mBuffer, 0, mBuffer.size)
      }
      val stopTime = System.currentTimeMillis()
      thread(start = true) {
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
      }

      mAudioTrack.stop()
      mAudioTrack.release()
    }
  }

}