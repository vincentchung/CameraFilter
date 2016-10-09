package com.Yamate.Camera;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Size;

import com.Yamate.Camera.filter.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

public class Renderer implements GLSurfaceView.Renderer {
    private Activity mActivity;
    private Camera mCamera;
    private boolean mConfigured = false;

    //filter api part
    //private Filter filter[]=null;
    int mCurrentFilter=0;
    ArrayList<Filter> mFilterList = new ArrayList<Filter>();
    private int mViewfinderTextureName=-1;
    private SurfaceTexture mSurfaceTexture=null;
    private int mLastCameraFrameCount;
    private float[] mTransformMatrix;

    public Renderer(Activity activity) {
        mActivity = activity;
        Util.PiCoreLog("Renderer");
        GLES20.glReleaseShaderCompiler();
        mTransformMatrix = new float[16];
        mLastCameraFrameCount = Util.getReportedFrameCounter();
        Util.setContext(mActivity);
    }

    void create_surfaceTexture()
    {
        if(mViewfinderTextureName==-1)
        {
            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);
            Util.checkGlError("glGenTextures - viewfinder");
            mViewfinderTextureName = textures[0];
            Util.PiCoreLog("Viewfinder texture name " + mViewfinderTextureName);
        }
        mSurfaceTexture = new SurfaceTexture(mViewfinderTextureName);
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                //mCameraFrameCount.incrementAndGet();
                Util.addCameraFrameCount();
                //Util.PiCoreLog("onFrameAvailable");
            }
        });

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mViewfinderTextureName);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        create_surfaceTexture();
        //open camera
        mCamera = new Camera(mActivity, mViewfinderTextureName);
        mCamera.open();
    }

    public void onDrawFrame(GL10 unused ) {
        GLES20.glClearColor(0.5f, 0.5f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        if (!mConfigured) {
            if (mConfigured = mCamera.getInitialized()) {
                mCamera.setCameraRotation();
                setConfig();
                //make sure camera is ready and config the viewfinder size
                mFilterList.add(new Normal(mCamera.getCameraSize().getWidth(),mCamera.getCameraSize().getHeight()));
                mFilterList.add(new Sepia(mCamera.getCameraSize().getWidth(),mCamera.getCameraSize().getHeight()));
                mFilterList.add(new Duocolor(mCamera.getCameraSize().getWidth(),mCamera.getCameraSize().getHeight()));
                Util.setInitCompleted(true);
            } else {
                return;
            }
        }
        //on switching filter!!
        if(!Util.getInitCompleted())
        {
            Util.PiCoreLog("filter not ready yet...");
            return;
        }
        Filter filter=mFilterList.get(mCurrentFilter);

        filter.onDrawing();
        mCamera.updateTexture();
        //Util.PiCoreLog("normalRender VF");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //render to viewfinder
        normalRender(filter.filter_shader);

    }

    public void onSurfaceChanged (GL10 unused, int width, int height) {
        mConfigured = false;
    }

    private void setConfig() {
        /*
        switch(mCamera.getCameraRotation()) {
            case ROTATION_0:
                mTexCoordBuffer.put(TEX_COORDS_ROTATION_0);
                break;
            case ROTATION_90:
                mTexCoordBuffer.put(TEX_COORDS_ROTATION_90);
                break;
            case ROTATION_180:
                mTexCoordBuffer.put(TEX_COORDS_ROTATION_180);
                break;
            case ROTATION_270:
                mTexCoordBuffer.put(TEX_COORDS_ROTATION_270);
                break;
        }
        mTexCoordBuffer.position(0);
*/
        Point displaySize = new Point();
        mActivity.getWindowManager().getDefaultDisplay().getSize(displaySize);
        Size textureSize = mCamera.getCameraSize();
        Point textureOrigin = new Point(
                (displaySize.x - textureSize.getWidth()) / 2,
                (displaySize.y - textureSize.getHeight()) / 2);

        GLES20.glViewport(0, 0, textureSize.getWidth(), textureSize.getHeight());
        }

    private void normalRender(Shader currentShader) {
        //PicShader currentShader=PF[mEffectType].filter_shader;

        //if(Util.getCameraEnabled())
        {
            GLES20.glUseProgram(currentShader.getGLProgram());
            //GLES20.glViewport(0, 0, mViewWidth, mViewHeight);
            Util.checkGlError("glViewport");
            //Log.v(TAG, "In onDrawFrame");

            //int lViewfinderTexHandle=4;
            int lViewfinderTexHandle=currentShader.getViewfinderHandle();
            int lTexCoordHandle=currentShader.getTexCoordHandle();
            int lTriangleVerticesHandle=currentShader.getTriangleVHandle();
            int lTransformHandle=currentShader.getTransformHandle();
            int lFrameTexCoordHandle=currentShader.getFrameTexCoordHandle();
            int lRunningTimeHandle=currentShader.getRunningTimeHandle();
            int cameraFrameCount = Util.getCameraFrameCount();
            if (mLastCameraFrameCount != cameraFrameCount) {
                //Util.PiCoreLog("update texture");
                Util.addReportedFrameCount();
                mSurfaceTexture.updateTexImage();
                mSurfaceTexture.getTransformMatrix(mTransformMatrix);
                GLES20.glUniformMatrix4fv(lTransformHandle, 1, false, mTransformMatrix, 0);
                //printTransformMatrix(m1TransformMatrix);
                Util.checkGlError("glUniformMatrix4fv");
                mLastCameraFrameCount = cameraFrameCount;
                GLES20.glUniform1i(lRunningTimeHandle, cameraFrameCount);
            }
            GLES20.glDisable(GLES20.GL_BLEND);
            Util.checkGlError("setup");
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            Util.checkGlError("setup");
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mViewfinderTextureName);
            Util.checkGlError("setup");

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE); // so that we don't read outside viewfinder
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE); // so that we don't read outside viewfinder
            Util.checkGlError("setup");
            if(currentShader.getVF())
            {
                GLES20.glUniform1i(lViewfinderTexHandle, 0);

                Util.checkGlError("setup");
            }


            GLES20.glVertexAttribPointer(lTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, currentShader.getTextureVertices());
            GLES20.glEnableVertexAttribArray(lTexCoordHandle);

            //GLES20.glEnableVertexAttribArray(lFrameTexCoordHandle);

            Util.checkGlError("setup");
            GLES20.glEnableVertexAttribArray(lTriangleVerticesHandle);
            Util.checkGlError("setup");
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
            Util.checkGlError("glDrawArrays");
        }
        //else {
        //    GLES20.glClearColor(0,0,0,0);
        //}

    }


    public boolean switchFilter(int e)
    {
        if(mCurrentFilter!=e)
        {
            //if(Util.getCapturing())
            //    return false;

            Util.PiCoreLog("Releasing SurfaceTextureView");

            Util.setInitCompleted(false);

            mFilterList.get(mCurrentFilter).unSelected();
            mCurrentFilter=e;
            mFilterList.get(mCurrentFilter).setFiliterEnable();

            GLES20.glFlush();
            Util.setInitCompleted(true);
            return true;
        }
        return false;
    }

    public void TakePicture()
    {
        mCamera.takePicture();
    }
}