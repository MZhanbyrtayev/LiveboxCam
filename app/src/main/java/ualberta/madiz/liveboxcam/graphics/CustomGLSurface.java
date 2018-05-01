package ualberta.madiz.liveboxcam.graphics;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class CustomGLSurface extends GLSurfaceView {
    private final CustomRenderer mRenderer;
    public CustomGLSurface(Context context) {
        super(context);
        setEGLContextClientVersion(3);
        setZOrderOnTop(true);
        setEGLConfigChooser(8,8,8,8,16,0);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        mRenderer = new CustomRenderer();
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
    public CustomGLSurface(Context context, AttributeSet set){
        super(context, set);
        setEGLContextClientVersion(3);
        setZOrderOnTop(true);
        setEGLConfigChooser(8,8,8,8,16,0);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        mRenderer = new CustomRenderer();
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public CustomRenderer getmRenderer() {
        return mRenderer;
    }
}
