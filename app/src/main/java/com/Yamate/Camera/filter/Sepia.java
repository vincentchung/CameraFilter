package com.Yamate.Camera.filter;

import android.opengl.GLES20;

import com.Yamate.Camera.Shader;

public class Sepia extends Filter {

    public Sepia(int w, int h) {
        super(w, h);
        //pic_filter_id=FragmentShaders.FILTER_NONE;
        // TODO Auto-generated constructor stub
    }
    void init()
    {
        mFramgment_glsl="sepia_fragment_shader.glsl";
        filter_shader=new Shader();
        filter_shader.setVF(true);
        filter_shader.init(mVertext_glsl,mFramgment_glsl);
        filter_shader.init_commitResource();
    }
    @Override
    public void onInit() {

        init();
    }

    @Override
    public void onSelected() {
        // TODO Auto-generated method stub

    }

}

