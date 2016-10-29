package com.Yamate.Camera.filter;

import android.opengl.GLES20;

import com.Yamate.Camera.Shader;
import com.Yamate.Camera.Util;

/**
 * Created by vincent on 08/10/2016.
 */

public class Emboss extends Filter {
    public Emboss(int w, int h) {
        super(w, h);
        mFramgment_glsl="emboss_fragment_shader.glsl";
        mVertext_glsl="texture_vertext_notr_shader_3x3.glsl";
    }
    @Override
    public void onInit() {
        Util.setShaderVariableF(mGLProgram, "u_tintR", 0.0f);
        Util.setShaderVariableF(mGLProgram, "u_tintG", 0.0f);
        Util.setShaderVariableF(mGLProgram, "u_tintB", 0.3f);
    }

    @Override
    public void onFRInit() {
        Util.setShaderVariableF(mFRGLProgram, "u_tintR", 0.0f);
        Util.setShaderVariableF(mFRGLProgram, "u_tintG", 0.0f);
        Util.setShaderVariableF(mFRGLProgram, "u_tintB", 0.3f);
    }

    @Override
    public void onSelected() {
        // TODO Auto-generated method stub

    }
}
