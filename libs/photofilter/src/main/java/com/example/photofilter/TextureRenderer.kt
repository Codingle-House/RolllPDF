package com.example.photofilter

import android.opengl.GLES20
import android.opengl.GLES20.GL_BLEND
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_FRAMEBUFFER
import android.opengl.GLES20.GL_TEXTURE0
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.GL_TRIANGLE_STRIP
import android.opengl.GLES20.glActiveTexture
import android.opengl.GLES20.glBindFramebuffer
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glDeleteProgram
import android.opengl.GLES20.glDisable
import android.opengl.GLES20.glDrawArrays
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniform1i
import android.opengl.GLES20.glUseProgram
import android.opengl.GLES20.glVertexAttribPointer
import android.opengl.GLES20.glViewport
import com.example.photofilter.GLToolbox.checkGlError
import com.example.photofilter.GLToolbox.createProgram
import java.nio.ByteBuffer.allocateDirect
import java.nio.ByteOrder
import java.nio.FloatBuffer

class TextureRenderer {

    private var mProgram: Int = 0
    private var mTexSamplerHandle: Int = 0
    private var mTexCoordHandle: Int = 0
    private var mPosCoordHandle: Int = 0

    private var mTexVertices: FloatBuffer? = null
    private var mPosVertices: FloatBuffer? = null

    private var mViewWidth: Int = 0
    private var mViewHeight: Int = 0

    private var mTexWidth: Int = 0
    private var mTexHeight: Int = 0

    fun init() {
        // Create program
        mProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER)

        // Bind attributes and uniforms
        mTexSamplerHandle = glGetUniformLocation(mProgram, "tex_sampler")
        mTexCoordHandle = glGetAttribLocation(mProgram, "a_texcoord")
        mPosCoordHandle = glGetAttribLocation(mProgram, "a_position")

        // Setup coordinate buffers
        mTexVertices = allocateDirect(TEX_VERTICES.size * FLOAT_SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        mTexVertices?.put(TEX_VERTICES)?.position(0)
        mPosVertices = allocateDirect(POS_VERTICES.size * FLOAT_SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        mPosVertices?.put(POS_VERTICES)?.position(0)
    }

    fun tearDown() = glDeleteProgram(mProgram)

    fun updateTextureSize(
        texWidth: Int,
        texHeight: Int
    ) {
        mTexWidth = texWidth
        mTexHeight = texHeight
        computeOutputVertices()
    }

    fun updateViewSize(
        viewWidth: Int,
        viewHeight: Int
    ) {
        mViewWidth = viewWidth
        mViewHeight = viewHeight
        computeOutputVertices()
    }

    fun renderTexture(texId: Int) {
        // Bind default FBO
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        // Use our shader program
        glUseProgram(mProgram)
        checkGlError("glUseProgram")

        // Set viewport
        glViewport(0, 0, mViewWidth, mViewHeight)
        checkGlError("glViewport")

        // Disable blending
        glDisable(GL_BLEND)

        // Set the vertex attributes
        glVertexAttribPointer(mTexCoordHandle, 2, GL_FLOAT, false, 0, mTexVertices)
        glEnableVertexAttribArray(mTexCoordHandle)
        glVertexAttribPointer(mPosCoordHandle, 2, GL_FLOAT, false, 0, mPosVertices)
        glEnableVertexAttribArray(mPosCoordHandle)
        checkGlError("vertex attribute setup")

        // Set the input texture
        glActiveTexture(GL_TEXTURE0)
        checkGlError("glActiveTexture")
        glBindTexture(GL_TEXTURE_2D, texId)
        checkGlError("glBindTexture")
        glUniform1i(mTexSamplerHandle, 0)

        // Draw
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
    }

    private fun computeOutputVertices() {
        if (mPosVertices != null) {
            val imgAspectRatio = mTexWidth / mTexHeight.toFloat()
            val viewAspectRatio = mViewWidth / mViewHeight.toFloat()
            val relativeAspectRatio = viewAspectRatio / imgAspectRatio
            val x0: Float
            val y0: Float
            val x1: Float
            val y1: Float
            if (relativeAspectRatio > 1.0f) {
                x0 = -1.0f / relativeAspectRatio
                y0 = -1.0f
                x1 = 1.0f / relativeAspectRatio
                y1 = 1.0f
            } else {
                x0 = -1.0f
                y0 = -relativeAspectRatio
                x1 = 1.0f
                y1 = relativeAspectRatio
            }
            val coords = floatArrayOf(x0, y0, x1, y0, x0, y1, x1, y1)
            mPosVertices?.put(coords)?.position(0)
        }
    }

    companion object {

        private val VERTEX_SHADER = "attribute vec4 a_position;\n" +
                "attribute vec2 a_texcoord;\n" +
                "varying vec2 v_texcoord;\n" +
                "void main() {\n" +
                "  gl_Position = a_position;\n" +
                "  v_texcoord = a_texcoord;\n" +
                "}\n"

        private val FRAGMENT_SHADER = "precision mediump float;\n" +
                "uniform sampler2D tex_sampler;\n" +
                "varying vec2 v_texcoord;\n" +
                "void main() {\n" +
                "  gl_FragColor = texture2D(tex_sampler, v_texcoord);\n" +
                "}\n"

        private val TEX_VERTICES = floatArrayOf(0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f)

        private val POS_VERTICES = floatArrayOf(-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f)

        private const val FLOAT_SIZE_BYTES = 4
    }

}