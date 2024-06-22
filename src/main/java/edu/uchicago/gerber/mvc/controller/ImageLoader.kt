package edu.uchicago.gerber.mvc.controller

import java.awt.image.BufferedImage
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import javax.imageio.ImageIO

/*
Place all .png image assets in this directory src/main/resources/imgs or its subdirectories.
*/
object ImageLoader {
    var IMAGES: Map<String?, BufferedImage?>? = null

    //load all images prior to runtime in the static context
    init {
        val rootDirectory = Paths.get("src/main/resources/imgs")
        var localMap: Map<String?, BufferedImage?>? = null
        try {
            localMap = loadPngImages(rootDirectory)
        } catch (e: IOException) {
            e.fillInStackTrace()
        }
        IMAGES = localMap
    }

    @Throws(IOException::class)
    private fun loadPngImages(rootDirectory: Path): Map<String?, BufferedImage?> {
        val pngImages: MutableMap<String?, BufferedImage?> = HashMap()
        Files.walkFileTree(rootDirectory, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (file.toString().lowercase(Locale.getDefault()).endsWith(".png")) {
                    try {
                        val bufferedImage = ImageIO.read(file.toFile())
                        if (bufferedImage != null) {
                            pngImages[file.fileName.toString()] = bufferedImage
                        }
                    } catch (e: IOException) {
                        e.fillInStackTrace()
                    }
                }
                return FileVisitResult.CONTINUE
            }

            override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult {
                // Handle the error here if necessary
                return FileVisitResult.CONTINUE
            }
        })
        return pngImages
    }
}
