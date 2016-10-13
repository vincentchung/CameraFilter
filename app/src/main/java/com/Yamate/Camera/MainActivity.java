package com.Yamate.Camera;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
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

public class MainActivity extends Activity {
    private GLSurfaceView mView;
    private Renderer FilterRenderer=null;
    private static ByteArrayOutputStream mYcameraOutputStream=null;
    private final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mView = (GLSurfaceView)findViewById(R.id.gl_surface_view);
        mView.setEGLContextClientVersion(2);
        FilterRenderer =new Renderer(this);
        mView.setRenderer(FilterRenderer);

        Util.PiCoreLog("view w:"+mView.getWidth()+",h:"+mView.getHeight());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ImageButton button = (ImageButton)findViewById(R.id.bn_capture);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(mYcameraOutputStream==null)
                    mYcameraOutputStream =new ByteArrayOutputStream();

                Util.setCapturing(true);
                FilterRenderer.TakePicture();
                mHandler.postDelayed(mYcameraRenderingTimer, 200);
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

    private static final int EFFECT_NUMBER=3;
    private static int mSelectFilter=0;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub

        if(event.getAction()==MotionEvent.ACTION_UP)
        {
            //switching filter
            mSelectFilter++;
            FilterRenderer.switchFilter(mSelectFilter%EFFECT_NUMBER);
        }
        return super.onTouchEvent(event);
    }

    //pic timer for saving the last result
    private Runnable mYcameraRenderingTimer = new Runnable() {
        public void run() {
            if(Util.getCapturing())
            {
                mHandler.postDelayed(mYcameraRenderingTimer, 200);
            }else
            {
                //done the rendering..
                mYcameraOutputStream.reset();
                if(FilterRenderer.getRenderResult(mYcameraOutputStream))
                {
                    Util.ImageToFile(mYcameraOutputStream);
                }

            }
        }
    };

}
