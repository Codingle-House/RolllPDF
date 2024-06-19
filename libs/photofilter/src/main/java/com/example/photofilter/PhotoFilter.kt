package com.example.photofilter

import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Bitmap.createBitmap
import android.media.effect.Effect
import android.media.effect.EffectContext
import android.media.effect.EffectFactory.EFFECT_AUTOFIX
import android.media.effect.EffectFactory.EFFECT_DOCUMENTARY
import android.media.effect.EffectFactory.EFFECT_GRAYSCALE
import android.opengl.GLES20
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.glBindTexture
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY
import android.opengl.GLUtils.texImage2D
import com.example.photofilter.GLToolbox.initTexParams
import com.example.photofilter.filters.AutoFix
import com.example.photofilter.filters.Documentary
import com.example.photofilter.filters.Filter
import com.example.photofilter.filters.Grayscale
import com.example.photofilter.filters.None
import id.co.rolllpdf.core.Constant.FOUR
import id.co.rolllpdf.core.Constant.ONE
import id.co.rolllpdf.core.Constant.SIXTEEN
import id.co.rolllpdf.core.Constant.TWO
import id.co.rolllpdf.core.Constant.ZERO
import id.co.rolllpdf.core.orZero
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL10.GL_RGBA
import javax.microedition.khronos.opengles.GL10.GL_UNSIGNED_BYTE

class PhotoFilter(
    private var effectsView: GLSurfaceView,
    private var onProcessingCompletionListener: OnProcessingCompletionListener
) : GLSurfaceView.Renderer {
    private var bitmap: Bitmap? = null
    private val textureRenderer = TextureRenderer()
    private var initialized = false
    private var effectContext: EffectContext? = null
    private var currentEffect: Filter = None()
    private val textures = IntArray(TWO)
    private var imageWidth = ZERO
    private var imageHeight = ZERO
    private var imageEffect: Effect? = null

    init {
        effectsView.apply {
            setEGLContextClientVersion(TWO)
            setRenderer(this@PhotoFilter)
            renderMode = RENDERMODE_WHEN_DIRTY
        }
    }

    fun applyEffect(
        bitmap: Bitmap,
        effect: Filter? = null
    ) {
        this.bitmap?.recycle()
        this.bitmap = bitmap
        initialized = false
        effect?.let { currentEffect = it }
        effectsView.requestRender()
    }

    override fun onSurfaceCreated(
        gl: GL10,
        config: EGLConfig
    ) {
    }

    override fun onSurfaceChanged(
        gl: GL10,
        width: Int,
        height: Int
    ) {
        textureRenderer.updateViewSize(width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        if (!initialized) {
            effectContext = EffectContext.createWithCurrentGlContext()
            textureRenderer.init()
            loadTextures()
            initialized = true
        }
        if (currentEffect !is None) {
            initEffect()
            applyEffect()
        }
        renderResult()
    }

    private fun loadTextures() {
        if (bitmap != null) {
            GLES20.glGenTextures(TWO, textures, ZERO)
            imageWidth = bitmap?.width.orZero()
            imageHeight = bitmap?.height.orZero()
            textureRenderer.updateTextureSize(imageWidth, imageHeight)

            glBindTexture(GL_TEXTURE_2D, textures.first())
            texImage2D(GL_TEXTURE_2D, ZERO, bitmap, ZERO)
            initTexParams()
        }
    }

    private fun initEffect() {
        val effectFactory = effectContext?.factory
        if (imageEffect != null) imageEffect?.release()
        when (currentEffect) {
            is None -> return
            is AutoFix -> {
                imageEffect = effectFactory?.createEffect(EFFECT_AUTOFIX)
                imageEffect?.setParameter(SCALE, (currentEffect as AutoFix).scale)
            }

            is Documentary -> imageEffect = effectFactory?.createEffect(EFFECT_DOCUMENTARY)
            is Grayscale -> imageEffect = effectFactory?.createEffect(EFFECT_GRAYSCALE)
            else -> return
        }
    }

    private fun applyEffect() =
        imageEffect?.apply(textures[ZERO], imageWidth, imageHeight, textures[ONE])

    private fun renderResult() {
        if (currentEffect is None) textureRenderer.renderTexture(textures[ZERO])
        else textureRenderer.renderTexture(textures[ONE])
        captureBitmap()
    }

    private fun captureBitmap() {
        val egl = EGLContext.getEGL() as EGL10
        val gl = egl.eglGetCurrentContext().gl as GL10
        createBitmapFromGLSurface(gl).let { onProcessingCompletionListener.onProcessingComplete(it) }
    }

    private fun createBitmapFromGLSurface(gl: GL10): Bitmap {
        val screenshotSize = effectsView.width * effectsView.height
        val bb = ByteBuffer.allocateDirect(screenshotSize * FOUR).apply {
            order(ByteOrder.nativeOrder())
        }
        gl.glReadPixels(
            ZERO,
            ZERO,
            effectsView.width,
            effectsView.height,
            GL_RGBA,
            GL_UNSIGNED_BYTE,
            bb
        )
        val pixelsBuffer = IntArray(screenshotSize)
        bb.asIntBuffer().get(pixelsBuffer)

        for (i in 0 until screenshotSize) {
            pixelsBuffer[i] = ((pixelsBuffer[i] and -0xff0100)) or
                    ((pixelsBuffer[i] and 0x000000ff) shl SIXTEEN) or ((pixelsBuffer[i] and 0x00ff0000) shr SIXTEEN)
        }

        return createBitmap(effectsView.width, effectsView.height, ARGB_8888).apply {
            setPixels(
                pixelsBuffer, screenshotSize - effectsView.width, -effectsView.width, ZERO, ZERO,
                effectsView.width, effectsView.height
            )
        }
    }

    companion object {
        private const val SCALE = "scale"
    }
}