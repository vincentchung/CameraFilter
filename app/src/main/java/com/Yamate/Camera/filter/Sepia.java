package com.Yamate.Camera.filter;

import android.opengl.GLES20;

import com.Yamate.Camera.Shader;

public class Sepia extends Filter {

    public Sepia(int w, int h) {
        super(w, h);
        mFramgment_glsl="sepia_fragment_shader.glsl";
    }
    @Override
    public void onInit() {

        //init();
    }

    @Override
    public void onFRInit() {

    }

    @Override
    public void onSelected() {
        // TODO Auto-generated method stub

    }

}

