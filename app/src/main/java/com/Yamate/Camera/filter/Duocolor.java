package com.Yamate.Camera.filter;

import android.opengl.GLES20;

import com.Yamate.Camera.Shader;
import com.Yamate.Camera.Util;

public class Duocolor extends Filter {

    public float mKeyColorR=0.0f;
    public float mKeyColorG=0.0f;
    public float mKeyColorB=0.0f;

    public Duocolor(int w, int h) {
        super(w, h);
        mFramgment_glsl="duocolor_fragment_shader.glsl";
    }
    @Override
    public void onInit() {
        //init();
        Util.setShaderVariableF(mGLProgram, "u_keyColorR", mKeyColorR);
        Util.setShaderVariableF(mGLProgram, "u_keyColorG", mKeyColorG);
        Util.setShaderVariableF(mGLProgram, "u_keyColorB", mKeyColorB);
        Util.setShaderVariableF(mGLProgram, "u_threshold", 0.8f); // 0.345f);
        Util.setShaderVariableF(mGLProgram, "u_slope", 0.8f); // 0.5f);
    }

    @Override
    public void onFRInit() {
        Util.setShaderVariableF(mFRGLProgram, "u_keyColorR", mKeyColorR);
        Util.setShaderVariableF(mFRGLProgram, "u_keyColorG", mKeyColorG);
        Util.setShaderVariableF(mFRGLProgram, "u_keyColorB", mKeyColorB);
        Util.setShaderVariableF(mFRGLProgram, "u_threshold", 0.8f); // 0.345f);
        Util.setShaderVariableF(mFRGLProgram, "u_slope", 0.8f); // 0.5f)
    }

    @Override
    public void onSelected() {
        // TODO Auto-generated method stub

    }

}

