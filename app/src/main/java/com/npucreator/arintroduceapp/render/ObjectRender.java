package com.npucreator.arintroduceapp.render;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Message;

import com.npucreator.arintroduceapp.ARApplicationSession;
import com.npucreator.arintroduceapp.util.ARUtils;
import com.npucreator.arintroduceapp.util.LoadingDialogHandler;
import com.npucreator.arintroduceapp.util.MathHelper;
import com.npucreator.arintroduceapp.util.Reference;
import com.npucreator.arintroduceapp.util.Texture;
import com.npucreator.arintroduceapp.object.MeshObject;
import com.npucreator.arintroduceapp.activity.DetectActivity;
import com.npucreator.arintroduceapp.object.CubeObject;
import com.vuforia.DeviceTrackableResult;
import com.vuforia.ImageTargetResult;
import com.vuforia.Matrix44F;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;
import com.vuforia.TrackableResultList;
import com.vuforia.Vuforia;

import java.lang.ref.WeakReference;
import java.util.Vector;

public class ObjectRender extends RenderBase implements RenderControl
{
    private static final String TAG = "ObjRender";

    private final WeakReference<DetectActivity> mActivityRef;

    private int shaderProgramID;
    private int vertexHandle;
    private int textureCoordHandle;
    private int mvpMatrixHandle;
    private int texSampler2DHandle;

    //要渲染的物体，即小人
    private MeshObject obj;
    public Handler handler;

    private boolean mModelIsLoaded = false;
    private boolean mIsTargetCurrentlyTracked = false;

    private static float OBJECT_SCALE_FLOAT = 0.5f;//0.003f;


    public ObjectRender(DetectActivity activity, ARApplicationSession session, Handler handler)
    {
        mActivityRef = new WeakReference<>(activity);
        vuforiaAppSession = session;

        mBackgroundRender = new BackgroundRender(this, mActivityRef.get(), vuforiaAppSession.getVideoMode(), 0.01f, 5f);

        this.handler = handler;
    }

    public void setActive(boolean active)
    {
        mBackgroundRender.setActive(active);
    }

    public void setTextures(Vector<Texture> textures){
        this.mTextures = textures;
    }

    public void setIsTargetCurrentlyTracked(TrackableResultList trackableResultList)
    {
        for(TrackableResult result : trackableResultList)
        {
            // Check the tracking status for result types
            // other than DeviceTrackableResult. ie: ImageTargetResult
            if (!result.isOfType(DeviceTrackableResult.getClassType()))
            {
                int currentStatus = result.getStatus();
                int currentStatusInfo = result.getStatusInfo();

                // The target is currently being tracked if the status is TRACKED|NORMAL
                if (currentStatus == TrackableResult.STATUS.TRACKED
                        || currentStatusInfo == TrackableResult.STATUS_INFO.NORMAL)
                {
                    mIsTargetCurrentlyTracked = true;
                    return;
                }
            }
        }

        mIsTargetCurrentlyTracked = false;
    }

    public boolean isTargetCurrentlyTracked()
    {
        return mIsTargetCurrentlyTracked;
    }

    /**
     * 更新摄像机传来视频的相关
     */
    public void updateRenderingPrimitives()
    {
        mBackgroundRender.updateRenderingPrimitives();
    }

    @Override
    public void renderFrame(State state, float[] projectionMatrix) {

        mBackgroundRender.renderVideoBackground();

        // Set the device pose matrix as identity
        Matrix44F devicePoseMatrix = MathHelper.Matrix44FIdentity();
        Matrix44F modelMatrix;

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        GLES20.glFrontFace(GLES20.GL_CCW);   // Back camera

        // Read device pose from the state and create a corresponding view matrix (inverse of the device pose)
        if (state.getDeviceTrackableResult() != null)
        {
            int statusInfo = state.getDeviceTrackableResult().getStatusInfo();
            int trackerStatus = state.getDeviceTrackableResult().getStatus();

            mActivityRef.get().checkForRelocalization(statusInfo);

            if (trackerStatus != TrackableResult.STATUS.NO_POSE)
            {
                modelMatrix = Tool.convertPose2GLMatrix(state.getDeviceTrackableResult().getPose());

                // We transpose here because Matrix44FInverse returns a transposed matrix
                devicePoseMatrix = MathHelper.Matrix44FTranspose(MathHelper.Matrix44FInverse(modelMatrix));
            }
        }

        TrackableResultList trackableResultList = state.getTrackableResults();

        // Determine if target is currently being tracked
        setIsTargetCurrentlyTracked(trackableResultList);

        // Iterate through trackable results and render any augmentations
        boolean flag = false;
        for (TrackableResult result : trackableResultList)
        {
            Trackable trackable = result.getTrackable();

            if (result.isOfType(ImageTargetResult.getClassType()) && result.getStatus() != TrackableResult.STATUS.LIMITED)
            {
                int textureIndex = 0;
                modelMatrix = Tool.convertPose2GLMatrix(result.getPose());

                //textureIndex = trackable.getName().equalsIgnoreCase("poker") ? 0 : 1;

                renderModel(projectionMatrix, devicePoseMatrix.getData(), modelMatrix.getData(), textureIndex);

                flag = true;
                ARUtils.checkGLError("Image Targets renderFrame");
            }
        }

        if (!flag)
            handler.sendEmptyMessage(Reference.HIDE);

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
    }

    private void renderModel(float[] projectionMatrix, float[] viewMatrix, float[] modelMatrix, int textureIndex)
    {
        //MeshObject model;
        float[] modelViewProjection = new float[16];


        // Apply local transformation to our model
        //Matrix.rotateM(modelMatrix,0, (float)mAngleY, 1, 0, 0);//旋转
        //Matrix.rotateM(modelMatrix,0 , (float)mAngleX, 0, 1, 0);//旋转
        Matrix.translateM(modelMatrix, 0, 0, 0, OBJECT_SCALE_FLOAT);
        Matrix.scaleM(modelMatrix, 0, OBJECT_SCALE_FLOAT, OBJECT_SCALE_FLOAT, OBJECT_SCALE_FLOAT);

        /** 注释掉的部分是用原生JAVA显示Mesh，可测试用，但是原生JAVA没法显示.fbx格式的，所以通过Unity导出jniLib来实现 **/
        //model = mTeapot;
        //model = myObject;
        //model = obj;

        // Combine device pose (view matrix) with model matrix
        Matrix.multiplyMM(modelMatrix, 0, viewMatrix, 0, modelMatrix, 0);

        // Do the final combination with the projection matrix
        Matrix.multiplyMM(modelViewProjection, 0, projectionMatrix, 0, modelMatrix, 0);

        // Activate the shader program and bind the vertex and tex coords
        GLES20.glUseProgram(shaderProgramID);

        //GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, model.getVertices());
        //GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, model.getTexCoords());

        //GLES20.glEnableVertexAttribArray(vertexHandle);
        //GLES20.glEnableVertexAttribArray(textureCoordHandle);

        // Activate texture 0, bind it, pass to shader
        //GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures.get(textureIndex).mTextureID[0]);
        //GLES20.glUniform1i(texSampler2DHandle, 0);

        // Pass the model view matrix to the shader
        //GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjection, 0);

        // Finally draw the model

        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, model.getNumObjectVertex());
        //GLES20.glDrawElements(GLES20.GL_TRIANGLES, model.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT, model.getIndices());


        // Disable the enabled arrays
        //GLES20.glDisableVertexAttribArray(vertexHandle);
        //GLES20.glDisableVertexAttribArray(textureCoordHandle);

        /**
         * 向UI线程发起更新模型的通知
         */
        Message msg = new Message();
        msg.what = Reference.SHOW;
        msg.obj = new Reference.ModelMatrix(modelViewProjection);
        handler.sendMessage(msg);
    }


    /**
     * 这个方法是用来初始化渲染模型的信息的，但是已经弃用，因为在unity module中初始化好了
     */
    @Override
    public void initRendering() {
        if (mTextures == null)
        {
            return;
        }

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
                : 1.0f);

        for (Texture t : mTextures)
        {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                    t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, t.mData);
        }

        shaderProgramID = ARUtils.createProgramFromShaderSrc(
                CubeShaders.CUBE_MESH_VERTEX_SHADER,
                CubeShaders.CUBE_MESH_FRAGMENT_SHADER);

        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexPosition");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "texSampler2D");

        if(!mModelIsLoaded)
        {
            /** 这里设置要渲染的物体 先用方块代替**/
            obj = new CubeObject();
            //myObject = new MyObject(mActivityRef.get().getAssets());

            mModelIsLoaded = true;

            // Hide the Loading Dialog
            mActivityRef.get().loadingDialogHandler
                    .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);

        }
    }
}
