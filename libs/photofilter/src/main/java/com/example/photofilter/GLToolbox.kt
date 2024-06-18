package com.example.photofilter

import android.opengl.GLES20
import android.opengl.GLES20.GL_CLAMP_TO_EDGE
import android.opengl.GLES20.GL_COMPILE_STATUS
import android.opengl.GLES20.GL_FRAGMENT_SHADER
import android.opengl.GLES20.GL_LINEAR
import android.opengl.GLES20.GL_LINK_STATUS
import android.opengl.GLES20.GL_NO_ERROR
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.GL_TEXTURE_MAG_FILTER
import android.opengl.GLES20.GL_TEXTURE_MIN_FILTER
import android.opengl.GLES20.GL_TEXTURE_WRAP_S
import android.opengl.GLES20.GL_TEXTURE_WRAP_T
import android.opengl.GLES20.GL_TRUE
import android.opengl.GLES20.GL_VERTEX_SHADER
import android.opengl.GLES20.glAttachShader
import android.opengl.GLES20.glCompileShader
import android.opengl.GLES20.glCreateProgram
import android.opengl.GLES20.glCreateShader
import android.opengl.GLES20.glDeleteProgram
import android.opengl.GLES20.glDeleteShader
import android.opengl.GLES20.glGetError
import android.opengl.GLES20.glGetProgramInfoLog
import android.opengl.GLES20.glGetProgramiv
import android.opengl.GLES20.glGetShaderInfoLog
import android.opengl.GLES20.glLinkProgram
import android.opengl.GLES20.glShaderSource
import android.opengl.GLES20.glTexParameteri
import id.co.rolllpdf.core.Constant.ONE
import id.co.rolllpdf.core.Constant.ZERO

object GLToolbox {

    private fun loadShader(
        shaderType: Int,
        source: String
    ): Int {
        val shader = glCreateShader(shaderType)
        if (shader != ZERO) {
            glShaderSource(shader, source)
            glCompileShader(shader)
            val compiled = IntArray(ONE)
            GLES20.glGetShaderiv(shader, GL_COMPILE_STATUS, compiled, 0)
            if (compiled.first() == ZERO) {
                val info = glGetShaderInfoLog(shader)
                glDeleteShader(shader)
                throw RuntimeException("Could not compile shader $shaderType:$info")
            }
        }
        return shader
    }

    fun createProgram(
        vertexSource: String,
        fragmentSource: String
    ): Int {
        val vertexShader = loadShader(GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) {
            return 0
        }
        val pixelShader = loadShader(GL_FRAGMENT_SHADER, fragmentSource)
        if (pixelShader == 0) {
            return 0
        }

        val program = glCreateProgram()
        if (program != 0) {
            glAttachShader(program, vertexShader)
            checkGlError("glAttachShader")
            glAttachShader(program, pixelShader)
            checkGlError("glAttachShader")
            glLinkProgram(program)
            val linkStatus = IntArray(1)
            glGetProgramiv(program, GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GL_TRUE) {
                val info = glGetProgramInfoLog(program)
                glDeleteProgram(program)
                throw RuntimeException("Could not link program: $info")
            }
        }
        return program
    }

    fun checkGlError(op: String) {
        val error = glGetError()
        if (error != GL_NO_ERROR) {
            throw RuntimeException("$op: glError $error")
        }
    }

    @JvmStatic fun initTexParams() {
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
    }

}