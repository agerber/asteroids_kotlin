package edu.uchicago.gerber.mvc.controller

import java.awt.image.BufferedImage
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import javax.imageio.ImageIO


/*
Place all .png image assets in this directory src/main/resources/imgs or its subdirectories.
All raster images loaded in static context prior to runtime.
*/
object ImageLoader {
    private var IMAGE_MAP: Map<String?, BufferedImage?>? = null

    init {
        val rootDirectory = Paths.get("src/main/resources/imgs")
        var localMap: Map<String?, BufferedImage?>? = null
        try {
            localMap = loadPngImages(rootDirectory)
        } catch (e: IOException) {
            e.fillInStackTrace()
        }
        IMAGE_MAP = localMap
    }

    /*
     Walks the directory and sub-directories at root src/main/resources/imgs and returns a Map<String, BufferedImage>
     of images in that file hierarcy.
     */
    @Throws(IOException::class)
    private fun loadPngImages(rootDirectory: Path): Map<String?, BufferedImage?> {
        val pngImages: MutableMap<String?, BufferedImage?> = HashMap()
        Files.walkFileTree(rootDirectory, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (file.toString().lowercase(Locale.getDefault()).endsWith(".png")
                    && !file.toString().lowercase(Locale.getDefault()).contains("do_not_load.png")
                ) {
                    try {
                        val bufferedImage = ImageIO.read(file.toFile())
                        if (bufferedImage != null) {
                            //substring(18) removes "src/main/resources" so that keys/paths are consistent with
                            // static and non-static
                            pngImages[file.toString().lowercase(Locale.getDefault()).substring(18)] = bufferedImage
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

    //fetch the image from existing static map
    fun getImage(imagePath: String): BufferedImage? {
        return IMAGE_MAP!![imagePath.lowercase(Locale.getDefault())]
    }
}

