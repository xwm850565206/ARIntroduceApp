package com.npucreator.arintroduceapp.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.npucreator.arintroduceapp.ARApplicationControl;
import com.npucreator.arintroduceapp.ARApplicationSession;
import com.npucreator.arintroduceapp.ARException;
import com.npucreator.arintroduceapp.R;
import com.npucreator.arintroduceapp.audio.IAudioHelper;
import com.npucreator.arintroduceapp.audio.SimpleAudioHelper;
import com.npucreator.arintroduceapp.render.ObjectRender;
import com.npucreator.arintroduceapp.render.RenderTest;
import com.npucreator.arintroduceapp.surfaceView.ARSurfaceViewBase;
import com.npucreator.arintroduceapp.surfaceView.SurfaceTest;
import com.npucreator.arintroduceapp.util.ARAppTimer;
import com.npucreator.arintroduceapp.util.ARMessage;
import com.npucreator.arintroduceapp.util.LoadingDialogHandler;
import com.npucreator.arintroduceapp.util.Reference;
import com.npucreator.arintroduceapp.util.Texture;
import com.vuforia.DataSet;
import com.vuforia.DeviceTracker;
import com.vuforia.ObjectTracker;
import com.vuforia.PIXEL_FORMAT;
import com.vuforia.PositionalDeviceTracker;
import com.vuforia.RuntimeImageSource;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Trackable;
import com.vuforia.TrackableList;
import com.vuforia.TrackableResult;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vec2I;
import com.vuforia.Vuforia;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Vector;

/**
 * 这个类是用来显示AR和播放声音的类,也是这个app的核心类
 */
public class DetectActivity extends ARActivityBase implements ARApplicationControl
{
    private static final String TAG = "DetectActivity";

    /** 这个对象负责操作AR的整个生命周期 **/
    private ARApplicationSession vuforiaAppSession;

    /** 这几个对象用来储存识别图片和识别参数的数据 **/
    private DataSet mCurrentDataset;
    private int mCurrentDatasetSelectionIndex = 0;
    private int mStartDatasetsIndex = 0;
    private int mDatasetsNumber = 0;
    private final ArrayList<String> mDatasetStrings = new ArrayList<>();
    private final ArrayList<String> mRuntimeImageSources = new ArrayList<>();


    /** test code **/
    private SurfaceTest surfaceTest;
    private RenderTest renderTest;
    /** end **/

    /** 这个对象是视频背景依托的view **/
    private ARSurfaceViewBase mSurfaceView;
    /** 这个对象用来渲染人物模型 **/
    private ObjectRender objectRender;

    /** 这个对象用来储存要渲染人物模型的纹理 实际上未使用 **/
    private Vector<Texture> mTextures;

    /** 这个对象用来显示等待条框架 **/
    private RelativeLayout mUILayout;
    /** 这个对象是人物模型依托的框架 **/
    private FrameLayout mUnityLayout;

    /** 这个对象用来向使用者传递信息 **/
    private ARMessage mMessage;
    /** 这两个个对象负责启动以及其他事物计时的事宜 **/
    private ARAppTimer mRelocalizationTimer;
    private ARAppTimer mStatusDelayTimer;

    /** 这个对象同上 **/
    private int mCurrentStatusInfo;

    /** 这个对象是等待条的控制对象 **/
    public final LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);

    /** 这个对象是用来通知信息的 **/
    private AlertDialog mErrorDialog;

    /** 设备信息 **/
    private boolean mIsDroidDevice = false;

    /** 这个对象用来控制人物渲染以及动作和声音 **/
    public Handler handler;

    /** 控制音频的类 **/
    private IAudioHelper audioHelper;

    /** 测试用的类，用来控制人物是否跳舞 **/
    private Button button;
    private boolean mCanDance = true;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);

        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCanDance = !mCanDance;
            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case Reference.SHOW:
                        if (mUnityLayout != null) {
                            Reference.ModelMatrix matrix = (Reference.ModelMatrix) msg.obj;
                            mUnityPlayer.UnitySendMessage("Zoe_dance", "reMatrix", matrix.toString());
                            mUnityPlayer.UnitySendMessage("Zoe_dance", "showZoe", "");
                            if (mCanDance)
                                mUnityPlayer.UnitySendMessage("Zoe_dance", "playDance", "");
                            audioHelper.play();
                            Log.d(TAG, "show model");
                        }
                        break;
                    case Reference.HIDE:
                        if (mUnityLayout != null) {
                            mUnityPlayer.UnitySendMessage("Zoe_dance", "hideZoe", "");
                            if (mCanDance)
                                mUnityPlayer.UnitySendMessage("Zoe_dance", "stopDance", "");
                            audioHelper.pause();
                        }
                        break;
                }
          }
        };


        /** test code **//*
        surfaceTest = new SurfaceTest(this);
        renderTest = new RenderTest();
        surfaceTest.setRenderer(renderTest);

        setContentView(surfaceTest, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        /** end **/

        vuforiaAppSession = new ARApplicationSession(this);

        startLoadingAnimation();

        audioHelper = new SimpleAudioHelper(this);

        /** 这里要添加所需识别图片的xml和.png **/

        mDatasetStrings.add("poker.xml");

        mRuntimeImageSources.add("poker.jpg");

        /*************************/

        vuforiaAppSession
                .initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Load any sample specific textures:
        mTextures = new Vector<>();
        loadTextures();

        mIsDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith("droid");

        // Relocalization timer and message
        mMessage = new ARMessage(this, mUILayout, mUILayout.findViewById(R.id.topbar_layout), false);
        mRelocalizationTimer = new ARAppTimer(10000, 1000)
        {
            @Override
            public void onFinish()
            {
                if (vuforiaAppSession != null)
                {
                    vuforiaAppSession.resetDeviceTracker();
                }

                super.onFinish();
            }
        };

        mStatusDelayTimer = new ARAppTimer(1000, 1000)
        {
            @Override
            public void onFinish()
            {
                if (objectRender.isTargetCurrentlyTracked())
                {
                    super.onFinish();
                    return;
                }

                if (!mRelocalizationTimer.isRunning())
                {
                    mRelocalizationTimer.startTimer();
                }

                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mMessage.show(getString(R.string.instruct_relocalize));
                    }
                });

                super.onFinish();
            }
        };

        Log.d(TAG, "created finish");
    }

    /**
     * 初始化GLSurfaceView 以及对应的Renderer
     */
    private void initApplicationAR()
    {
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        mSurfaceView = new ARSurfaceViewBase(getApplicationContext());
        mSurfaceView.init(translucent, depthSize, stencilSize);


        objectRender = new ObjectRender(this, vuforiaAppSession, handler);
        objectRender.setTextures(mTextures);

        mSurfaceView.setRenderer(objectRender);
        mSurfaceView.setPreserveEGLContextOnPause(true);

        setRendererReference(objectRender);
    }

    /**
     * 等待动画，当从MainActivity跳转到DetectActivity时出现
     */
    private void startLoadingAnimation()
    {
        mUILayout = (RelativeLayout) View.inflate(getApplicationContext(), R.layout.camera_overlay, null);

        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);

        RelativeLayout topbarLayout = mUILayout.findViewById(R.id.topbar_layout);
        topbarLayout.setVisibility(View.VISIBLE);

        TextView title = mUILayout.findViewById(R.id.topbar_title);
        title.setText(getText(R.string.app_name));

        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
                .findViewById(R.id.loading_indicator);

        // Shows the loading indicator at start
        loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);

        // Adds the inflated layout to the view
        addContentView(mUILayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

    }

    /**
     * 加载纹理贴图
     */
    private void loadTextures() {
        mTextures.add(Texture.loadTextureFromApk("model/test.png", //TextureTeapotBrass.png",
                getAssets()));
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mSurfaceView != null)
        {
            mSurfaceView.setVisibility(View.INVISIBLE);
            mSurfaceView.onPause();
        }

        vuforiaAppSession.onPause();
        audioHelper.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        showProgressIndicator(true);

        // This is needed for some Droid devices to force portrait
        if (mIsDroidDevice)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        vuforiaAppSession.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();

        try
        {
            vuforiaAppSession.stopAR();
        } catch (ARException e)
        {
            Log.e(TAG, e.getString());
        }

        // Unload texture:
        mTextures.clear();
        mTextures = null;

        audioHelper.release();

        System.gc();
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        Log.d(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);

        vuforiaAppSession.onConfigurationChanged();
    }

    /**
     * 初始化跟踪器，这步应该在加载跟踪器和资源后面
     * @return
     */
    @Override
    public boolean doInitTrackers() {

        // Indicate if the trackers were initialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();

        Tracker tracker = tManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null)
        {
            Log.e(TAG, "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else
        {
            Log.i(TAG, "Tracker successfully initialized");
        }

        // Initialize the Positional Device Tracker
        DeviceTracker deviceTracker = (PositionalDeviceTracker)
                tManager.initTracker(PositionalDeviceTracker.getClassType());

        if (deviceTracker != null)
        {
            Log.i(TAG, "Successfully initialized Device Tracker");
        }
        else
        {
            Log.e(TAG, "Failed to initialize Device Tracker");
        }

        return result;
    }

    /**
     * 这里加载跟踪识别图像的数据和所需要的类
     * @return
     */
    @Override
    public boolean doLoadTrackersData()
    {
        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager.
                getTracker(ObjectTracker.getClassType());

        if (objectTracker == null)
            return false;

        if (mCurrentDataset == null)
            mCurrentDataset = objectTracker.createDataSet();

        if (mCurrentDataset == null)
            return false;

        if (mCurrentDatasetSelectionIndex == 0){
            /// Creating a dataset with multiple image targets created from images loaded at run time using the native
            /// Java BitmapFactory class to access raw pixel data.
            /// A second version loads from the image file via Vuforia instead.
            /// This code block creates a Vuforia::DataSet containing all the images from the mRuntimeImageSources, or a nullptr if one of the
            /// Images couldn't be loaded
            ///
            /// The steps to use the Instant Image Target api are highlighted with "Instant Image Target Step <X>" in comments


            // Instant Image Target Step 1:
            // retrieve the RuntimeImageSource from the object tracker. The same instance can be used to
            // create multiple image targets
            RuntimeImageSource runtimeImageSource = objectTracker.getRuntimeImageSource();

            int apiSelector = 0;
            for(String imageFileName : mRuntimeImageSources) {

                if(apiSelector % 2 == 0) {
                    // Get the AssetManager to be able to load a file from the packaged resources
                    AssetManager assets = getAssets();

                    Bitmap image = null;
                    try {
                        // Initialize BitmapImage from an InputStream created from the AssetManager
                        InputStream inputStream = assets.open(imageFileName, AssetManager.ACCESS_BUFFER);
                        image = BitmapFactory.decodeStream(inputStream);
                        inputStream.close();
                    } catch (java.io.IOException e) {
                        Log.e(TAG, "ERROR: failed to load image");
                        objectTracker.destroyDataSet(mCurrentDataset);
                        return false;
                    }

                    // Get the image meta information
                    int width = image.getWidth();
                    int height = image.getHeight();

                    int bytesPerPixel = image.getByteCount() / (width * height);

                    int bytes = image.getByteCount();

                    // Create a new buffer; use allocateDirect so C++ can access the data
                    ByteBuffer buffer = ByteBuffer.allocateDirect(bytes);
                    image.copyPixelsToBuffer(buffer);

                    // Calculate the Vuforia::PixelFormat based on the number of bytes used to represent a pixel in the image
                    int format;
                    switch (bytesPerPixel) {
                        case 1:
                            format = PIXEL_FORMAT.GRAYSCALE;
                            break;
                        case 3:
                            format = PIXEL_FORMAT.RGB888;
                            break;
                        case 4:
                            format = PIXEL_FORMAT.RGBA8888;
                            break;
                        default:
                            format = PIXEL_FORMAT.UNKNOWN_FORMAT;
                            return false;
                    }

                    String targetName = imageFileName.substring(0, imageFileName.lastIndexOf('.'));
                    // Instant Image Target Step 2:
                    // Configure the RuntimeImageSource with the data from the loaded image.
                    if (!runtimeImageSource.setImage(buffer, format, new Vec2I(width, height), 0.247f, targetName)) {
                        Log.e(TAG, "ERROR: failed to load from image");
                        objectTracker.destroyDataSet(mCurrentDataset);
                        return false;
                    }
                }
                else
                {
                    // Instant Image Target Step 2:
                    // Configure the RuntimeImageSource with path to the file and the path type (see STORAGE_TYPE for options)
                    String targetName = imageFileName.substring(0, imageFileName.lastIndexOf('.'));
                    if (!runtimeImageSource.setFile(imageFileName, STORAGE_TYPE.STORAGE_APPRESOURCE, 0.247f, targetName)) {
                        Log.e(TAG, "ERROR: failed to load from image");
                        objectTracker.destroyDataSet(mCurrentDataset);
                        return false;
                    }
                }
                // Instant Image Target Step 3:
                // Use the RuntimeImageSource instance to create the Trackable in the specified Vuforia::DataSet.
                mCurrentDataset.createTrackable(runtimeImageSource);
                ++apiSelector;
            }
        }
        else {
            if (!mCurrentDataset.load(
                    mDatasetStrings.get(mCurrentDatasetSelectionIndex),
                    STORAGE_TYPE.STORAGE_APPRESOURCE)) {
                return false;
            }
        }

        if (!objectTracker.activateDataSet(mCurrentDataset))
        {
            return false;
        }

        TrackableList trackableList = mCurrentDataset.getTrackables();
        for (Trackable trackable : trackableList)
        {
            String name = "Current Dataset : " + trackable.getName();
            trackable.setUserData(name);
            Log.d(TAG, "UserData:Set the following user data "
                    + trackable.getUserData());
        }

        return true;

    }

    @Override
    public boolean doStartTrackers() {
        // Indicate if the trackers were started correctly
        boolean result = true;

        TrackerManager trackerManager = TrackerManager.getInstance();

        Tracker objectTracker = trackerManager.getTracker(ObjectTracker.getClassType());

        if (objectTracker != null && objectTracker.start())
        {
            Log.i(TAG, "Successfully started Object Tracker");
        }
        else
        {
            Log.e(TAG, "Failed to start Object Tracker");
            result = false;
        }

        return result;
    }

    @Override
    public boolean doStopTrackers() {
        // Indicate if the trackers were stopped correctly
        boolean result = true;

        TrackerManager trackerManager = TrackerManager.getInstance();

        Tracker objectTracker = trackerManager.getTracker(ObjectTracker.getClassType());
        if (objectTracker != null)
        {
            objectTracker.stop();
            Log.i(TAG, "Successfully stopped object tracker");
        }
        else
        {
            Log.e(TAG, "Failed to stop object tracker");
            result = false;
        }

        return result;
    }

    /**
     * 卸载加载好的类和图片资源
     * @return
     */
    @Override
    public boolean doUnloadTrackersData() {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());

        if (objectTracker == null)
        {
            return false;
        }

        if (mCurrentDataset != null && mCurrentDataset.isActive())
        {
            if (objectTracker.getActiveDataSets().at(0).equals(mCurrentDataset)
                    && !objectTracker.deactivateDataSet(mCurrentDataset))
            {
                result = false;
            }
            else if (!objectTracker.destroyDataSet(mCurrentDataset))
            {
                result = false;
            }

            mCurrentDataset = null;
        }

        return result;
    }

    @Override
    public boolean doDeinitTrackers() {
        TrackerManager tManager = TrackerManager.getInstance();

        // Indicate if the trackers were deinitialized correctly
        boolean result = tManager.deinitTracker(ObjectTracker.getClassType());
        tManager.deinitTracker(PositionalDeviceTracker.getClassType());

        return result;
    }

    @Override
    public void onInitARDone(ARException e) {

        if (e == null)
        {
            initApplicationAR();
            objectRender.setActive(true);

            FrameLayout surfaceLayout = (FrameLayout)findViewById(R.id.glsurface_layout);
            surfaceLayout.addView(mSurfaceView);

            mUnityLayout = (FrameLayout)findViewById(R.id.unity_layout);
            mUnityLayout.addView(mUnityPlayer.getView());
            mUnityPlayer.UnitySendMessage("Zoe_dance", "hideZoe", "");

            mUILayout.bringToFront();
            mUILayout.setBackgroundColor(Color.TRANSPARENT);

            vuforiaAppSession.startAR();
        }
        else
        {
            Log.e(TAG, e.getString());
            showInitializationErrorMessage(e.getString());
        }
    }

    private void showInitializationErrorMessage(String message){
        final String errorMessage = message;
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                if (mErrorDialog != null)
                {
                    mErrorDialog.dismiss();
                }

                // Generates an Alert Dialog to show the error message
                AlertDialog.Builder builder = new AlertDialog.Builder(DetectActivity.this);
                builder
                        .setMessage(errorMessage)
                        .setTitle(getString(R.string.INIT_ERROR))
                        .setCancelable(false)
                        .setIcon(0)
                        .setPositiveButton(getString(R.string.button_OK),
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        finish();
                                    }
                                });

                mErrorDialog = builder.create();
                mErrorDialog.show();
            }
        });
    }

    @Override
    public void onVuforiaUpdate(State state) {

    }

    @Override
    public void onVuforiaResumed() {
        if (mSurfaceView != null)
        {
            mSurfaceView.setVisibility(View.VISIBLE);
            mSurfaceView.onResume();
        }
    }

    @Override
    public void onVuforiaStarted()
    {
        objectRender.updateRenderingPrimitives();

        showProgressIndicator(false);
    }

    private void showProgressIndicator(boolean show)
    {
        if (show)
        {
            loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);
        }
        else
        {
            loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
        }
    }

    public void checkForRelocalization(final int statusInfo)
    {
        if (mCurrentStatusInfo == statusInfo)
        {
            return;
        }

        mCurrentStatusInfo = statusInfo;

        if (mCurrentStatusInfo == TrackableResult.STATUS_INFO.RELOCALIZING)
        {
            // If the status is RELOCALIZING, start the timer
            if (!mStatusDelayTimer.isRunning())
            {
                mStatusDelayTimer.startTimer();
            }
        }
        else
        {
            // If the status is not RELOCALIZING, stop the timers and hide the message
            if (mStatusDelayTimer.isRunning())
            {
                mStatusDelayTimer.stopTimer();
            }

            if (mRelocalizationTimer.isRunning())
            {
                mRelocalizationTimer.stopTimer();
            }

            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    if (mMessage != null)
                    {
                        mMessage.hide();
                    }
                }
            });
        }
    }
}
