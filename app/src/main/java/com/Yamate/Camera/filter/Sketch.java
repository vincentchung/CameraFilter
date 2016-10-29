package com.Yamate.Camera.filter;

import com.Yamate.Camera.Shader;

/**
 * Created by vincent on 08/10/2016.
 */

public class Sketch extends Filter {
    public Sketch(int w, int h) {
        super(w, h);
        mFramgment_glsl="sketch_fragment_shader.glsl";
        mVertext_glsl="texture_vertext_notr_shader_3x3.glsl";
    }
    @Override
    public void onInit() {

    }

    @Override
    public void onFRInit() {

    }

    @Override
    public void onSelected() {
        // TODO Auto-generated method stub

    }
}
