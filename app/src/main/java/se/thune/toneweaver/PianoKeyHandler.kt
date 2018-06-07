package se.thune.toneweaver

import android.util.Log
import java.util.*

class PianoKeyHandler(val mBuffer: ShortArray) : Observer {
  override fun update(o: Observable?, arg: Any?) {
    Log.d("PIANBO","getting observed")
    currentSound.drop()
  }

  private var currentSound : SoundTone = SoundTone(shortArrayOf())
  fun start() {
    currentSound.drop()
    currentSound = SoundTone(mBuffer)
  }
  fun stop() {
    currentSound.drop()
  }
}