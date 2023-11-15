package edu.uchicago.gerber.mvc.controller

import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.LineUnavailableException
import javax.sound.sampled.UnsupportedAudioFileException

object Sound {

     //for sound playing. Limit the number of threads to 5 at a time.
     val soundExecutor = Executors.newFixedThreadPool(5) as ThreadPoolExecutor

    //for individual wav sounds (not looped)
    //http://stackoverflow.com/questions/26305/how-can-i-play-sound-in-java
    fun playSound(strPath: String) {
        //use coroutines here on the io dispatcher
       soundExecutor.execute {
           try {
               val clp = AudioSystem.getClip()
               val audioSrc = Sound::class.java.getResourceAsStream("/sounds/$strPath")
               val bufferedIn: InputStream = BufferedInputStream(audioSrc)
               val aisStream = AudioSystem.getAudioInputStream(bufferedIn)
               clp.open(aisStream)
               clp.start()
           } catch (e: Exception) {
               System.err.println(e.message)
           }
       }
    }

    //for looping wav clips
    //http://stackoverflow.com/questions/4875080/music-loop-in-java
    fun clipForLoopFactory(strPath: String): Clip {
        var clp: Clip? = null
        try {
            val audioSrc = Sound::class.java.getResourceAsStream("/sounds/$strPath")

            val bufferedIn: InputStream = BufferedInputStream(audioSrc)
            val aisStream = AudioSystem.getAudioInputStream(bufferedIn)
            clp = AudioSystem.getClip()
            clp.open(aisStream)
        } catch (e: UnsupportedAudioFileException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: LineUnavailableException) {
            e.printStackTrace()
        }
        return clp!!
    }
}
