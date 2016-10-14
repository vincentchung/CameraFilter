package com.Yamate.Camera.filter;

import android.opengl.GLES20;

import com.Yamate.Camera.Shader;
import com.Yamate.Camera.Util;

public class Duocolor extends Filter {

    public Duocolor(int w, int h) {
        super(w, h);
        //pic_filter_id=FragmentShaders.FILTER_NONE;
        // TODO Auto-generated constructor stub
    }
    void init()
    {
        mFramgment_glsl="duocolor_fragment_shader.glsl";
        filter_shader=new Shader();
        filter_shader.init(mVertext_glsl,mFramgment_glsl);
        //int handle1=filter_shader.init_addtexName("s_capturingtexture");
        filter_shader.init_commitResource();
        mGLProgram=filter_shader.getGLProgram();

        Util.setShaderVariableF(mGLProgram, "u_keyColorR", 1.0f);
        Util.setShaderVariableF(mGLProgram, "u_keyColorG", 0.0f);
        Util.setShaderVariableF(mGLProgram, "u_keyColorB", 0.0f);
        Util.setShaderVariableF(mGLProgram, "u_threshold", 0.8f); // 0.345f);
        Util.setShaderVariableF(mGLProgram, "u_slope", 0.8f); // 0.5f);
        //GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        //Util.loadGLTextureFromResource(R.drawable.texture, PicoreOpenGLTestActivity.getAppContext(), true);
    }
    @Override
    public void onInit() {


/*
        VERTEX_SHADER=VertexShaders.SIMPLE_VERTEX_SHADER;
        FRAGMENT_SHADER=FragmentShaders.SIMPLE_FRAGMENT_SHADER;
        mVertexShader=VertexShaders.mSimpleVertexShader;
        mFRVertexShader=VertexShaders.mFRSimpleVertexShader;
        //mVertexShader = Util.loadShader(GLES20.GL_VERTEX_SHADER, FragmentShaders.VF_DEFINE+VertexShaders.SIMPLE_VERTEX_SHADER);

        mPixelShader = Util.loadShader(GLES20.GL_FRAGMENT_SHADER, FragmentShaders.VF_DEFINE+FragmentShaders.SIMPLE_FRAGMENT_SHADER);
        */
        init();
    }

    @Override
    public void onSelected() {
        // TODO Auto-generated method stub

    }

}

