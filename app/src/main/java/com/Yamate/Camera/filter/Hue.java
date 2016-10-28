package com.Yamate.Camera.filter;

import com.Yamate.Camera.Shader;

/**
 * Created by vincent on 08/10/2016.
 */

public class Hue extends Filter {
    public Hue(int w, int h) {
        super(w, h);
    }
    void init()
    {
        mFramgment_glsl="hue_fragment_shader.glsl";
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
