package com.Yamate.Camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.ImageReader;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends Activity implements Renderer.RendererListener{
    private GLSurfaceView mView;
    private Renderer mFilterRenderer=null;
    private static ByteArrayOutputStream mYcameraOutputStream=null;
    private Camera mCamera;
    private boolean mCameraInit=false;
    private FilterList filters=null;

    private byte mCaptureBuffer[]=null;

    //exif data for capturing jpeg..
    ExifInterface mExifJpeg=null;
    //camcorder
    private boolean mIsRecordingVideo=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);


        //adding premission request
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mView = (GLSurfaceView)findViewById(R.id.gl_surface_view);
        mView.setEGLContextClientVersion(2);
        mFilterRenderer =new Renderer(this);
        mFilterRenderer.setListener(this);
        mView.setRenderer(mFilterRenderer);


        //Util.PiCoreLog("view w:"+mView.getWidth()+",h:"+mView.getHeight());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ImageButton button = (ImageButton)findViewById(R.id.bn_capture);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                /*
                if(mYcameraOutputStream==null)
                    mYcameraOutputStream =new ByteArrayOutputStream();

                mCamera.takePicture();
                */

                if (mIsRecordingVideo) {
                    mIsRecordingVideo=false;
                    mCamera.stopRecordingVideo();
                } else {
                    mIsRecordingVideo=true;
                    mCamera.startRecordingVideo();
                }

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mView.onResume();
    }

    //private static final int EFFECT_NUMBER=5;
    private static int mSelectFilter=0;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub

        if(event.getAction()==MotionEvent.ACTION_UP)
        {
            //switching filter
            int total_num=mFilterRenderer.getFilterSize();
            mSelectFilter++;
            mFilterRenderer.switchFilter(mSelectFilter%total_num);
        }
        return super.onTouchEvent(event);
    }


    @Override
    public void onRenderBufferDone(ByteBuffer buffer) {
        Util.RawToJpeg(buffer.array(),mCamera.getCaptureSize().getWidth(),mCamera.getCaptureSize().getHeight());
        //Util.RawToJpeg(buffer.array(),mCamera.getCaptureSize().getWidth(),mCamera.getCaptureSize().getHeight(),mExifJpeg);

    }

    @Override
    public void onRenderSurfaceCreated(int textName) {


        mCamera = new Camera(this, textName);
        mCamera.setCaptueImageListener(mOnCameraImageAvailableListener);
        mCamera.open();

    }

    @Override
    public void onRenderDraw() {

        if(!mCameraInit)
        {
            if(mCamera.getTexture()!=null)
            {
                mFilterRenderer.setViewFinderSize(mCamera.getCameraSize().getWidth(),mCamera.getCameraSize().getHeight());
                mCamera.setCameraRotation();
                mFilterRenderer.setSurfaceTexture(mCamera.getTexture());
                mFilterRenderer.setCameraConfig(true);
                mCameraInit=true;
            }
        }
    }

    private final ImageReader.OnImageAvailableListener mOnCameraImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {

            ByteBuffer buffer = reader.acquireNextImage().getPlanes()[0].getBuffer();

            mCaptureBuffer = new byte[buffer.remaining()];

            buffer.get(mCaptureBuffer);

            if(mYcameraOutputStream==null)
                mYcameraOutputStream =new ByteArrayOutputStream();

            mYcameraOutputStream.write(mCaptureBuffer,0,mCaptureBuffer.length);
            //Util.ImageToFile(mYcameraOutputStream);



            try {
                mExifJpeg=new ExifInterface(Util.ImageToFile(mYcameraOutputStream));
                //mYcameraOutputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            Util.PiCoreLog("mExifJpeg:"+mExifJpeg.toString());
            //mYcameraOutputStream=null;

            BitmapFactory.Options BO=new BitmapFactory.Options();
            BO.inPreferredConfig= Bitmap.Config.RGB_565;
            //BO.outHeight=mCaptureHeight;
            //BO.outWidth=mCaptureWidth;

            BO.inSampleSize=1;
            //BO.inSampleSize=1;
            Bitmap bMap = BitmapFactory.decodeByteArray(mCaptureBuffer,0,mCaptureBuffer.length,BO);

            //bMap.copyPixelsToBuffer(mOputTexBuffer);
            //decode to bmp format
            mFilterRenderer.setRenderToBuff(bMap);
            Util.setCapturing(true);
            Util.PiCoreLog("onImageAvailable");
            //mWaitingProcessPic++;
        }

    };



}
