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
        mFramgment_glsl="cartoon_fragment_shader.glsl";
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
