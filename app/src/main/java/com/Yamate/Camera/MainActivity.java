package com.Yamate.Camera;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.ImageReader;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v13.app.ActivityCompat;
import android.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import android.support.v13.app.FragmentCompat;

public class MainActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback, Renderer.RendererListener{
    private GLSurfaceView mView;
    private Renderer mFilterRenderer=null;
    private static ByteArrayOutputStream mYcameraOutputStream=null;
    private final Handler mHandler = new Handler();
    private Camera mCamera;
    private boolean mCameraInit=false;
    private FilterList filters=null;

    //adding premission request
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";
    private byte mCaptureBuffer[]=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //adding premission request
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }

        mView = (GLSurfaceView)findViewById(R.id.gl_surface_view);
        mView.setEGLContextClientVersion(2);
        mFilterRenderer =new Renderer(this);
        mFilterRenderer.setListener(this);
        mView.setRenderer(mFilterRenderer);


        Util.PiCoreLog("view w:"+mView.getWidth()+",h:"+mView.getHeight());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ImageButton button = (ImageButton)findViewById(R.id.bn_capture);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(mYcameraOutputStream==null)
                    mYcameraOutputStream =new ByteArrayOutputStream();

                Util.setCapturing(true);
                //FilterRenderer.TakePicture();
                //taking a capturing...
                mCamera.takePicture();
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



    //for adding premission request

    private void requestCameraPermission() {

        // BEGIN_INCLUDE(camera_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            new ConfirmationDialog().show(getFragmentManager(), FRAGMENT_DIALOG);

        } else {

            // Camera permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
        // END_INCLUDE(camera_permission_request)
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance(getString(R.string.request_permission))
                        .show(getFragmentManager(),FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onRenderBufferDone(ByteBuffer buffer) {
        //mYcameraOutputStream.write(mCaptureBuffer,0,mCaptureBuffer.length);
        mCaptureBuffer=null;
        Util.RawToJpeg(buffer.array(),mCamera.getCaptureSize().getWidth(),mCamera.getCaptureSize().getHeight());
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

    /**
     * Shows an error message dialog.
     */
    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }
    /**
     * Shows OK/Cancel confirmation dialog about camera permission.
     */
    public static class ConfirmationDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.request_permission)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Activity activity = getActivity();
                                    if (activity != null) {
                                        activity.finish();
                                    }
                                }
                            })
                    .create();
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
            Util.ImageToFile(mYcameraOutputStream);

            mFilterRenderer.setRenderToBuff(mCamera.getCaptureSize().getWidth(),mCamera.getCaptureSize().getHeight(),mCaptureBuffer);
            Util.setCapturing(true);
            Util.PiCoreLog("onImageAvailable");
            //mWaitingProcessPic++;
        }

    };

}
