package ualberta.madiz.liveboxcam.graphics;

import android.opengl.GLES30;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class RectangleFrame {
    static final int COORDS_NUM = 3;
    private static final String TAG = "RectangleFrame";
    private final int program;
    private FloatBuffer buffer;
    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";
    static float vertices[] = new float[12];
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
    public RectangleFrame(float[] coords){
        vertices = coords;
        Log.d(TAG,"v:"+vertices.length);
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length*4);
        bb.order(ByteOrder.nativeOrder());
        buffer = bb.asFloatBuffer();
        buffer.put(vertices);
        buffer.position(0);
        int vertexShader = CustomRenderer.loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = CustomRenderer.loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode);
        program = GLES30.glCreateProgram();
        GLES30.glAttachShader(program, vertexShader);
        GLES30.glAttachShader(program, fragmentShader);
        GLES30.glLinkProgram(program);
    }

    private int mPositionHandle;
    private int mColorHandle;

    private final int vertexCount = vertices.length / COORDS_NUM;
    private final int vertexStride = COORDS_NUM * 4; // 4 bytes per vertex

    public void draw() {
        // Add program to OpenGL ES environment
        GLES30.glUseProgram(program);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES30.glGetAttribLocation(program, "vPosition");

        // Enable a handle to the triangle vertices
        GLES30.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES30.glVertexAttribPointer(mPositionHandle, COORDS_NUM,
                GLES30.GL_FLOAT, false,
                vertexStride, buffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES30.glGetUniformLocation(program, "vColor");

        // Set color for drawing the triangle
        GLES30.glUniform4fv(mColorHandle, 1, color, 0);

        // Draw the triangle
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount);

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(mPositionHandle);
    }
}
