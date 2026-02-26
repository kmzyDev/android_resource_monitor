package com.resource_monitor

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLRenderer : GLSurfaceView.Renderer {
    private var frameCount = 0
    private var lastTime = System.currentTimeMillis()
    var fps = 0
    
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var indexBuffer: ByteBuffer
    private var programHandle = 0
    
    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    
    private var angle = 0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        
        initBuffers()
        initShaders()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        
        val ratio = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 45f, ratio, 0.1f, 100f)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1f, 0f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, angle, 1f, 1f, 1f)
        angle += 2f
        
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)
        
        drawCube()
        
        frameCount++
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastTime
        
        if (elapsed >= 1000) {
            fps = frameCount
            frameCount = 0
            lastTime = currentTime
        }
    }

    private fun drawCube() {
        GLES20.glUseProgram(programHandle)
        
        val positionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position")
        val mvpMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix")
        
        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)
        GLES20.glEnableVertexAttribArray(positionHandle)
        
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 36, GLES20.GL_UNSIGNED_BYTE, indexBuffer)
        
        GLES20.glDisableVertexAttribArray(positionHandle)
    }

    private fun initBuffers() {
        val vertices = floatArrayOf(
            // 前面
            -1f, -1f, 1f,   1f, -1f, 1f,   1f, 1f, 1f,   -1f, 1f, 1f,
            // 背面
            -1f, -1f, -1f,   -1f, 1f, -1f,   1f, 1f, -1f,   1f, -1f, -1f,
            // 上面
            -1f, 1f, -1f,   -1f, 1f, 1f,   1f, 1f, 1f,   1f, 1f, -1f,
            // 下面
            -1f, -1f, -1f,   1f, -1f, -1f,   1f, -1f, 1f,   -1f, -1f, 1f,
            // 右面
            1f, -1f, -1f,   1f, 1f, -1f,   1f, 1f, 1f,   1f, -1f, 1f,
            // 左面
            -1f, -1f, -1f,   -1f, -1f, 1f,   -1f, 1f, 1f,   -1f, 1f, -1f
        )
        
        val indices = byteArrayOf(
            0, 1, 2,   0, 2, 3,
            4, 5, 6,   4, 6, 7,
            8, 9, 10,   8, 10, 11,
            12, 13, 14,   12, 14, 15,
            16, 17, 18,   16, 18, 19,
            20, 21, 22,   20, 22, 23
        )
        
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(vertices)
                position(0)
            }
        
        indexBuffer = ByteBuffer.allocateDirect(indices.size)
            .order(ByteOrder.nativeOrder())
            .apply {
                put(indices)
                position(0)
            }
    }

    private fun initShaders() {
        val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)
        
        programHandle = GLES20.glCreateProgram().also { program ->
            GLES20.glAttachShader(program, vertexShader)
            GLES20.glAttachShader(program, fragmentShader)
            GLES20.glLinkProgram(program)
        }
    }

    private fun compileShader(type: Int, code: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, code)
            GLES20.glCompileShader(shader)
        }
    }

    companion object {
        private const val VERTEX_SHADER_CODE = """
            uniform mat4 u_MVPMatrix;
            attribute vec4 a_Position;
            
            void main() {
                gl_Position = u_MVPMatrix * a_Position;
            }
        """
        
        private const val FRAGMENT_SHADER_CODE = """
            precision mediump float;
            
            void main() {
                gl_FragColor = vec4(0.5, 0.8, 1.0, 1.0);
            }
        """
    }
}
