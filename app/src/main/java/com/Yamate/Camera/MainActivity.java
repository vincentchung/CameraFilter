package com.Yamate.Camera;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowManager;

public class MainActivity extends Activity {
    private GLSurfaceView mView;
    private Renderer FilterRenderer=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mView = (GLSurfaceView)findViewById(R.id.gl_surface_view);
        mView.setEGLContextClientVersion(2);
        FilterRenderer =new Renderer(this);
        mView.setRenderer(FilterRenderer);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
}
