package com.Yamate.Camera.filter;

import com.Yamate.Camera.Shader;

/**
 * Created by vincent on 08/10/2016.
 */

public class Sketch extends Filter {
    public Sketch(int w, int h) {
        super(w, h);
    }
    void init()
    {
        mFramgment_glsl="sketch_fragment_shader.glsl";
        mVertext_glsl="texture_vertext_notr_shader_3x3.glsl";
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
    public void onFRInit() {

    }

    @Override
    public void onSelected() {
        // TODO Auto-generated method stub

    }
}
