package com.npucreator.arintroduceapp.render;

import android.opengl.GLSurfaceView;
import android.util.Log;

import com.npucreator.arintroduceapp.ARApplicationSession;
import com.npucreator.arintroduceapp.util.Texture;

import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class RenderBase implements GLSurfaceView.Renderer {

    private static final String TAG = "RenderBase";

    protected BackgroundRender mBackgroundRender;
    protected ARApplicationSession vuforiaAppSession;
    protected Vector<Texture> mTextures;

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        Log.d(TAG, "GLRenderer.onSurfaceCreated");

        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();

        mBackgroundRender.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {

        Log.d(TAG, "GLRenderer.onSurfaceChanged");

        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);

        // RenderingPrimitives to be updated when some rendering change is done
        onConfigurationChanged();
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        // Call our function to render content from SampleAppRenderer class
        mBackgroundRender.render();
    }

    public void onConfigurationChanged()
    {
        mBackgroundRender.onConfigurationChanged();
    }

}
