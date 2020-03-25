package com.npucreator.arintroduceapp.activity;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;

import com.npucreator.arintroduceapp.render.RenderBase;
import com.npucreator.unity.UnityPlayerActivity;


/**
 * 这个类是 AR activity的基类，用于进行权限和版本判断，以及横屏和竖屏的判断
 */
public class ARActivityBase extends UnityPlayerActivity
{
    /** 这两个对象用来调控和监听横屏和竖屏的问题 **/
    private DisplayManager.DisplayListener mDisplayListener;
    private DisplayManager mDisplayManager;

    /** 记录横屏还是竖屏 **/
    private int mDeviceOrientation;

    /** 这个render对象用于渲染 **/
    protected RenderBase mBaseRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /** 判断一下运行的版本和我们所期望的版本 **/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
        {
            /** 初始化监听横屏和竖屏 **/
            mDisplayListener = new DisplayManager.DisplayListener()
            {
                @Override
                public void onDisplayAdded(int displayId)
                {
                }

                @Override
                public void onDisplayChanged(int displayId)
                {
                    int newOrientation = getDeviceOrientation();

                    if (mDeviceOrientation != newOrientation)
                    {
                        // onSurfaceChanged() does not get called when switching from
                        // portrait to reverse portrait or landscape to reverse landscape,
                        // so we must handle this explicitly here
                        // For upside down rotation, the difference in orientation values will be 2
                        // ie: Surface.ROTATION_0 has an enum of 0 and Surface.ROTATION_180 has an enum of 2
                        boolean isUpsideDownRotation = Math.abs(mDeviceOrientation - newOrientation) == 2;

                        if (isUpsideDownRotation)
                        {
                            if (mBaseRenderer != null)
                            {
                                mBaseRenderer.onConfigurationChanged();
                            }
                        }

                        mDeviceOrientation = newOrientation;
                    }
                }

                @Override
                public void onDisplayRemoved(int displayId)
                {
                }
            };

            mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        }
    }


    @Override
    protected void onResume()
    {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                && mDisplayManager != null)
        {
            mDisplayManager.registerDisplayListener(mDisplayListener, null);
        }
    }


    @Override
    protected void onPause()
    {
        super.onPause();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                && mDisplayManager != null)
        {
            mDisplayManager.unregisterDisplayListener(mDisplayListener);
        }
    }


    private int getDeviceOrientation()
    {
        return getWindowManager().getDefaultDisplay().getRotation();
    }


    public void setRendererReference(RenderBase renderer)
    {
        mBaseRenderer = renderer;
    }
}
