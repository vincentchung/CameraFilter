package com.Yamate.Camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.util.Size;

import com.Yamate.Camera.filter.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    ByteBuffer mOputTexBuffer=null;
    int mCaptureWidth=0;
    int mCaptureHeight=0;
    private final int mGLlimitHeight=2048;
    private final int mGLlimitWidth=2048;
    private final int mPixelbytes=4;
    // render to texture variables
    int[] mFbBuffer, mFbDepthRb, mFbRenderTex;
    int mFbSizeX;
    int mFbSizeY;
    int FR_tex=-1;

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

    public void createFBO(int sizeX, int sizeY) {
        Util.PiCoreLog("creating framebuffer object " + sizeX + "x" +sizeY);
        mFbBuffer = new int[1];
        mFbDepthRb = new int[1];
        mFbRenderTex = new int[1];

        mFbSizeX = sizeX;
        mFbSizeY = sizeY;

        // generate
        GLES20.glGenFramebuffers(1, mFbBuffer, 0); // create a single framebuffer
        GLES20.glGenRenderbuffers(1, mFbDepthRb, 0); // create a single renderbuffer
        GLES20.glGenTextures(1, mFbRenderTex, 0); // create a texture
        Util.PiCoreLog("Framebuffer texture #" + mFbRenderTex[0]);

        // generate color texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFbRenderTex[0]);

        // parameters
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);

        // create an empty intbuffer first?
        //int[] buf = new int[sizeX * sizeY];
        //int[] buf = new int[sizeX * sizeY];
        //mFbTexBuffer = ByteBuffer.allocateDirect(buf.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();;
        if(mOputTexBuffer==null)
            mOputTexBuffer = ByteBuffer.allocateDirect(sizeX*sizeY*mPixelbytes).order(ByteOrder.nativeOrder());

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, sizeX, sizeY, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, mOputTexBuffer);

        // create render buffer and bind 16-bit depth buffer
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mFbDepthRb[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, sizeX, sizeY);
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

                mFilterList.add(new Arique(mCamera.getCameraSize().getWidth(),mCamera.getCameraSize().getHeight()));
                mFilterList.add(new Emboss(mCamera.getCameraSize().getWidth(),mCamera.getCameraSize().getHeight()));
                mOputTexBuffer = ByteBuffer.allocateDirect(mGLlimitHeight * mGLlimitWidth * mPixelbytes);
                mOputTexBuffer.order(ByteOrder.nativeOrder());
                createFBO(mGLlimitHeight,mGLlimitHeight);
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
        //check if camera is on done in capturing....
        {
            //////////////////////////////////////////////
            if(Util.getCapturing())
            {
                //using file path
                byte mCapturedata[]=mCamera.getCapturedBuffer();

                /*
                File file = new File(mActivity.getExternalFilesDir(null), "pic.jpg");

                try {
                    FileInputStream input=new FileInputStream(file);
                    mCapturedata=new byte[(int)file.length()];
                    input.read(mCapturedata);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }catch (IOException e) {
                    e.printStackTrace();
                }*/

                if(mCapturedata==null)
                {
                    //keeping rendering VF mode
                    normalRender(filter.filter_shader);
                    //render2ndpassbuffer();
                    return;
                }

                mCaptureWidth=mCamera.getCaptureSize().getWidth();
                mCaptureHeight=mCamera.getCaptureSize().getHeight();

                filter.onTakePicture(mCaptureWidth, mCaptureHeight);

                if(filter.getGPUsupport())
                {
                    int blocks=filter.getBlocknumber();
                    for(int i=1;i<(blocks+1);i++)
                    {
                        Util.PiCoreLog("capturing:w:"+mCaptureWidth+",h:"+mCaptureHeight);
                        //decoding JPEG in 1/4 region
                        //before render, raise the event to effect
                        Bitmap GLsizeBmp=filter.onFRrender(i,mCapturedata,mCaptureWidth, mCaptureHeight);
                        GLsizeBmp.copyPixelsToBuffer(mOputTexBuffer);
                        //debug: make sure the image decoding is correct
                        //Util.RawToJpeg(mOputTexBuffer.array(), GLsizeBmp.getWidth(),GLsizeBmp.getHeight());
                        int RS_width=GLsizeBmp.getWidth();
                        int RS_height=GLsizeBmp.getHeight();
                        Util.PiCoreLog("uploadFRTextureFromBuffer:w:"+RS_width+",h:"+RS_height);
                        //uploading the bmp to texture 01
                        FR_tex=Util.uploadFRTextureFromBuffer(RS_width, RS_height, mOputTexBuffer,FR_tex);
                        GLsizeBmp.recycle();
                        //GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
                        //GLES20.glm GLES20.GL_TEXTURE1
                        //rendering to screen
                        //render2ndpassbuffer();
                        //rendering to FBO
                        renderToFramebuffer();
                        //block function
                        //make sure openGL done his job before reading pixel
                        GLES20.glFlush();
                        GLES20.glFinish();
                        //read from GL to buffer
                        int t=(int) Math.sqrt(blocks);
                        if(filter.getRenderPixels()==2)
                        {
                            GLES20.glReadPixels(0, 0, mCaptureWidth/t,mCaptureHeight/t, GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, mOputTexBuffer);
                        }else
                        {
                            //GLES20.glReadPixels(0, 0, mCaptureWidth/t,mCaptureHeight/t, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mOputTexBuffer);
                            GLES20.glReadPixels(0, 0, mCaptureWidth/t,mCaptureHeight/t, GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, mOputTexBuffer);
                        }

                        GLES20.glFinish();
                        //Util.RawToJpeg(mOputTexBuffer.array(), mCaptureWidth/t,mCaptureHeight/t);
                        //copy to map

                        //Util.RenderFullMapRawBuffer(mOputTexBuffer,i,mCaptureWidth/2,mCaptureHeight/2);
                        //mOputTexBuffer.clear();
                        //Util.RawToJpeg(mOputTexBuffer.array(), GLsizeBmp.getWidth(),GLsizeBmp.getHeight());
                        filter.onRenderBlockDone(mOputTexBuffer, i);

                        //clear the texture
                        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
                        //clear the color
                        GLES20.glClearColor(.0f, .0f, .0f, 1.0f);
                        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
                        GLES20.glFinish();
                    }
                }else
                {
                    filter.processEffect(mCapturedata, mCaptureWidth, mCaptureHeight);
                }

                filter.onFinalRender();
                mCapturedata=null;
                Util.PiCoreLog("capturing done");
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
                //clear the color
                GLES20.glClearColor(.0f, .0f, .0f, 1.0f);
                GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
                //reset the view port
                GLES20.glViewport(0, 0, mCamera.getCameraSize().getWidth(),mCamera.getCameraSize().getHeight());
                Util.setCapturing(false);
                return;
            }
        }
        mCamera.updateTexture();
        //Util.PiCoreLog("normalRender VF");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //render to viewfinder
        normalRender(filter.filter_shader);

    }

    private boolean renderToFramebuffer() {

        Shader currentShader=mFilterList.get(mCurrentFilter).filter_FR_shader;

        //GLES20.glViewport(0, 0, render_bmp.getWidth(), render_bmp.getHeight()); // set viewport to framebuffer size
        GLES20.glViewport(0, 0, mCaptureWidth/2, mCaptureHeight/2); // set viewport to framebuffer size
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFbBuffer[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mFbRenderTex[0], 0);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, mFbDepthRb[0]);
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e("PiCore", "Error in renderToFramebuffer init");
            return false;
        }
        normalRender(currentShader);

        return true;
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

    public boolean getRenderResult(ByteArrayOutputStream outputStream)
    {
        return mFilterList.get(mCurrentFilter).renderCompressResult(outputStream);
    }
}