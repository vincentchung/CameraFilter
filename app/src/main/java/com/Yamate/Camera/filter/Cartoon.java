package com.Yamate.Camera.filter;

import android.opengl.GLES20;

import com.Yamate.Camera.Shader;
import com.Yamate.Camera.Util;

/**
 * Created by vincent on 08/10/2016.
 */

public class Cartoon extends Filter {
    public Cartoon(int w, int h) {
        super(w, h);
    }
    void init()
    {
        mFramgment_glsl="cartoon_fragment_shader.glsl";
        //mVertext_glsl="texture_vertext_notr_shader_3x3.glsl";
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
