package edu.uchicago.gerber.mvc.controller

import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import javax.sound.sampled.*

object Sound {

     //for sound playing. Limit the number of threads to 5 at a time.
     val soundExecutor = Executors.newFixedThreadPool(5) as ThreadPoolExecutor


    var LOOP_SOUNDS: Map<String, Clip>? = null

    // Load all looping sounds prior to runtime in the static context. Other sounds, which may have multiple instances
    // and played simultaneously, must be queued onto the soundExecutor.
    init {
        val rootDirectory = Paths.get("src/main/resources/sounds")
        var localMap: Map<String, Clip> = HashMap()
        try {
            localMap = loadLoopedSounds(rootDirectory)
        } catch (e: IOException) {
            e.fillInStackTrace()
            throw ExceptionInInitializerError(e)
        }
        LOOP_SOUNDS = localMap
        for (s in LOOP_SOUNDS!!.keys) {
            println(s)
        }
    }


    @Throws(IOException::class)
    private fun loadLoopedSounds(rootDirectory: Path): Map<String, Clip> {
        val soundClips: MutableMap<String, Clip> = HashMap()
        Files.walkFileTree(rootDirectory, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (file.toString().lowercase(Locale.getDefault()).endsWith("_loop.wav")) {
                    try {
                        val clip = getLoopedClip(file)
                        if (clip != null) {
                            soundClips[file.fileName.toString()] = clip
                        }
                    } catch (e: java.lang.Exception) {
                        e.fillInStackTrace()
                    }
                }
                return FileVisitResult.CONTINUE
            }

            override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult {
                exc.fillInStackTrace()
                return FileVisitResult.CONTINUE
            }
        })
        return soundClips
    }

    @Throws(java.lang.Exception::class)
    private fun getLoopedClip(fileName: Path): Clip? {
        var clip: Clip? = null
        try {
            // Adjust the path to be relative to the resources directory
            val relativePath = "/sounds/" + fileName.fileName.toString()
            val audioSrc = Sound::class.java.getResourceAsStream(relativePath)
                ?: throw IOException("No such sound file exists at $relativePath")
            val bufferedIn: InputStream = BufferedInputStream(audioSrc)
            val aisStream = AudioSystem.getAudioInputStream(bufferedIn)
            clip = AudioSystem.getClip()
            clip.open(aisStream)
        } catch (e: UnsupportedAudioFileException) {
            e.fillInStackTrace()
            throw e
        } catch (e: IOException) {
            e.fillInStackTrace()
            throw e
        } catch (e: LineUnavailableException) {
            e.fillInStackTrace()
            throw e
        }
        return clip
    }


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


}
