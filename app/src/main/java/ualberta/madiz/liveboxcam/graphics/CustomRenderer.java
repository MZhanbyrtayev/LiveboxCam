package ualberta.madiz.liveboxcam.graphics;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CustomRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "CustomRenderer";
    private RectangleFrame toDraw;
    private Triangle mTriangle;

    public void setFrame(RectangleFrame f){
        Log.d(TAG, "Set frame");
        toDraw = f;
    }

    public void setmTriangle(Triangle triangle){
        mTriangle = triangle;
    }
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        //mTriangle = new Triangle(custom);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int w, int h) {
        GLES20.glViewport(0,0, w, h);
        /*float ratio = (float) w/h;
        Matrix.frustumM(mProjectionMatrix, 0, -ratio,ratio,-1,1,3,7);*/
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        /*Matrix.setLookAtM(mViewMatrix, 0,0,0,-3,0f,0f,0f,0f,1.0f,0.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        */
        //Log.d(TAG, "Called");
        if(mTriangle!=null){
            mTriangle.draw();
        }
        /*if(toDraw!=null){
            toDraw.draw();
        }*/
    }

    public static int loadShader(int type, String shaderCode){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES30.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES30.glShaderSource(shader, shaderCode);

        GLES30.glCompileShader(shader);


        return shader;
    }


}
