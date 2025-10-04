package com.example.edgevision.renderer

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private val TAG = "GLRenderer"
    
    // Vertex shader code
    private val vertexShaderCode =
        "attribute vec4 vPosition;\n" +
        "attribute vec2 vTexCoord;\n" +
        "varying vec2 texCoord;\n" +
        "void main() {\n" +
        "  gl_Position = vPosition;\n" +
        "  texCoord = vTexCoord;\n" +
        "}"

    // Fragment shader code for texture rendering
    private val fragmentShaderCode =
        "precision mediump float;\n" +
        "uniform sampler2D tex;\n" +
        "varying vec2 texCoord;\n" +
        "void main() {\n" +
        "  gl_FragColor = texture2D(tex, texCoord);\n" +
        "}"

    // Quad vertices (x, y, z) and texture coordinates (s, t)
    private val quadVertices = floatArrayOf(
        // X, Y, Z, U, V
        -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,  // bottom left
         1.0f, -1.0f, 0.0f, 1.0f, 0.0f,  // bottom right
        -1.0f,  1.0f, 0.0f, 0.0f, 1.0f,  // top left
         1.0f,  1.0f, 0.0f, 1.0f, 1.0f   // top right
    )
    
    private val vertexStride = 5 * 4 // 5 components (x,y,z,s,t) * 4 bytes per float
    private val vertexCount = 4
    
    private var program: Int = 0
    private var vPositionHandle: Int = 0
    private var vTexCoordHandle: Int = 0
    private var textureHandle: Int = 0
    private var textureId: Int = -1
    
    private var vertexBuffer: FloatBuffer
    private var surfaceTexture: SurfaceTexture? = null
    private var updateTexture = false
    
    init {
        // Initialize vertex byte buffer for shape coordinates
        val bb = ByteBuffer.allocateDirect(quadVertices.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(quadVertices)
        vertexBuffer.position(0)
    }
    
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        
        // Enable transparency
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        
        // Initialize shaders and program
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        
        // Create OpenGL program
        program = GLES20.glCreateProgram().also {
            // Add the vertex shader to program
            GLES20.glAttachShader(it, vertexShader)
            // Add the fragment shader to program
            GLES20.glAttachShader(it, fragmentShader)
            // Create OpenGL program executables
            GLES20.glLinkProgram(it)
            
            // Check for linking errors
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(it, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: " + GLES20.glGetProgramInfoLog(it))
                GLES20.glDeleteProgram(it)
            }
        }
        
        // Get handle to vertex shader's vPosition member
        vPositionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        // Get handle to vertex shader's vTexCoord member
        vTexCoordHandle = GLES20.glGetAttribLocation(program, "vTexCoord")
        // Get handle to texture sampler
        textureHandle = GLES20.glGetUniformLocation(program, "tex")
        
        // Generate texture
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]
        
        // Bind the texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        
        // Set texture parameters
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        
        // Create SurfaceTexture
        surfaceTexture = SurfaceTexture(textureId).apply {
            setOnFrameAvailableListener {
                updateTexture = true
            }
        }
    }
    
    override fun onDrawFrame(gl: GL10?) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        
        // Update the texture if a new frame is available
        surfaceTexture?.let { st ->
            if (updateTexture) {
                st.updateTexImage()
                updateTexture = false
            }
        }
        
        // Add program to OpenGL environment
        GLES20.glUseProgram(program)
        
        // Enable vertex array and set the vertex data
        vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(vPositionHandle)
        GLES20.glVertexAttribPointer(
            vPositionHandle, 3,
            GLES20.GL_FLOAT, false,
            vertexStride, vertexBuffer
        )
        
        // Enable texture coordinate array and set the data
        vertexBuffer.position(3) // Move to the texture coordinate data
        GLES20.glEnableVertexAttribArray(vTexCoordHandle)
        GLES20.glVertexAttribPointer(
            vTexCoordHandle, 2,
            GLES20.GL_FLOAT, false,
            vertexStride, vertexBuffer
        )
        
        // Set the active texture unit to texture unit 0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        
        // Bind the texture to this unit
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        
        // Tell the texture uniform sampler to use this texture in the shader
        GLES20.glUniform1i(textureHandle, 0)
        
        // Draw the quad
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount)
        
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(vPositionHandle)
        GLES20.glDisableVertexAttribArray(vTexCoordHandle)
    }
    
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // Set the viewport
        GLES20.glViewport(0, 0, width, height)
    }
    
    fun getSurfaceTexture(): SurfaceTexture? {
        return surfaceTexture
    }
    
    private fun loadShader(type: Int, shaderCode: String): Int {
        // Create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        return GLES20.glCreateShader(type).also { shader ->
            // Add the source code to the shader and compile it
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
            
            // Check for compilation errors
            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] == 0) {
                Log.e(TAG, "Shader compilation error: ${GLES20.glGetShaderInfoLog(shader)}")
                GLES20.glDeleteShader(shader)
            }
        }
    }
    
    fun release() {
        surfaceTexture?.release()
        val textures = intArrayOf(textureId)
        GLES20.glDeleteTextures(1, textures, 0)
        GLES20.glDeleteProgram(program)
    }
}
