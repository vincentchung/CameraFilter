package com.Yamate.Camera.filter;

import com.Yamate.Camera.Shader;

/**
 * Created by vincent on 08/10/2016.
 */

public class MagicPen extends Filter {
    public MagicPen(int w, int h) {
        super(w, h);
        mFramgment_glsl="magicpen_fragment_shader.glsl";
        mVertext_glsl="texture_vertext_notr_shader_3x3.glsl";
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
