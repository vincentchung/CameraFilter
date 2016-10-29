package com.Yamate.Camera.filter;

import android.opengl.GLES20;

import com.Yamate.Camera.Shader;

/**
 * Created by vincent on 08/10/2016.
 */

public class Cyan extends Filter {

    public Cyan(int w, int h) {
        super(w, h);
        mFramgment_glsl="cyan_fragment_shader.glsl";
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
