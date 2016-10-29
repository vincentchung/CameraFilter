package com.Yamate.Camera.filter;

import com.Yamate.Camera.Shader;
import com.Yamate.Camera.Util;

/**
 * Created by vincent on 08/10/2016.
 */

public class Halftone extends Filter {
    public Halftone(int w, int h) {
        super(w, h);
        mFramgment_glsl="halftone_fragment_shader.glsl";
    }
    @Override
    public void onInit() {

        Util.setShaderVariableF(mGLProgram, "u_dots", 64.0f);
    }

    @Override
    public void onFRInit() {
        Util.setShaderVariableF(mFRGLProgram, "u_dots", 64.0f);
    }

    @Override
    public void onSelected() {
        // TODO Auto-generated method stub

    }
}
