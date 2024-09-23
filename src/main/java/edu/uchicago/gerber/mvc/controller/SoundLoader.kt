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


object SoundLoader {
    /* A Looped clip is one that plays for an indefinite time until you call the .stopSound() method. Non-looped
		clips, which may have multiple instances that play concurrently, must be queued onto the ThreadPoolExecutor
		below. Make sure to place all sounds directly in the src/main/resources/sounds directory and suffix any looped
		clips with _loop.
	 */
    private var LOOPED_CLIPS_MAP: Map<String?, Clip>? = null

    // Load all looping sounds in the static context.
    init {
        val rootDirectory = Paths.get("src/main/resources/sounds")
        var localMap: Map<String?, Clip>? = null
        localMap = try {
            loadLoopedSounds(rootDirectory)
        } catch (e: IOException) {
            e.fillInStackTrace()
            throw ExceptionInInitializerError(e)
        }
        LOOPED_CLIPS_MAP = localMap
    }

    /* ThreadPoolExecutor for playing non-looped sounds. Limit the number of threads to 5 at a time. Sounds that can
	be played simultaneously, must be queued onto the soundExecutor at runtime.
	 */
    private val soundExecutor = Executors.newFixedThreadPool(5) as ThreadPoolExecutor
    private fun loopedCondition(str: String): Boolean {
        var str = str
        str = str.lowercase(Locale.getDefault())
        return str.endsWith("_loop.wav")
    }

    @Throws(IOException::class)
    private fun loadLoopedSounds(rootDirectory: Path): Map<String?, Clip> {
        val soundClips: MutableMap<String?, Clip> = HashMap()
        Files.walkFileTree(rootDirectory, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (loopedCondition(file.toString())) {
                    try {
                        val clip = getLoopClip(file)
                        if (clip != null) {
                            soundClips[file.fileName.toString()] = clip
                        }
                    } catch (e: Exception) {
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

    @Throws(Exception::class)
    private fun getLoopClip(fileName: Path): Clip? {
        var clip: Clip? = null
        try {
            // Adjust the path to be relative to the resources directory
            val relativePath = "/sounds/" + fileName.fileName.toString()
            val audioSrc = SoundLoader::class.java.getResourceAsStream(relativePath)
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

    // Used for both looped and non-looped clips
    fun playSound(strPath: String) {
        //Looped clips are fetched from existing static LOOP_SOUNDS_MAP at runtime.
        if (loopedCondition(strPath)) {
            try {
                LOOPED_CLIPS_MAP!![strPath]!!.loop(Clip.LOOP_CONTINUOUSLY)
            } catch (e: Exception) {
                //catch any exception and continue.
                e.fillInStackTrace()
            }
            return
        }
        //Non-looped clips are enqueued onto executor-threadpool at runtime.
        soundExecutor.execute {
            try {
                val clp = AudioSystem.getClip()
                val audioSrc = SoundLoader::class.java.getResourceAsStream("/sounds/$strPath")!!
                val bufferedIn: InputStream = BufferedInputStream(audioSrc)
                val aisStream = AudioSystem.getAudioInputStream(bufferedIn)
                clp.open(aisStream)
                clp.start()
            } catch (e: Exception) {
                System.err.println(e.message)
            }
        }
    }

    //Non-looped clips can not be stopped, they simply expire on their own. Calling this method on a
    // non-looped clip will do nothing.
    fun stopSound(strPath: String) {
        if (!loopedCondition(strPath)) return
        try {
            LOOPED_CLIPS_MAP!![strPath]!!.stop()
        } catch (e: Exception) {
            //catch any exception and continue.
            e.fillInStackTrace()
        }
    }
}

