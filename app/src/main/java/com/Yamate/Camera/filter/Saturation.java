package com.Yamate.Camera.filter;

public class Saturation extends Filter {

    public Saturation(int w, int h) {
        super(w, h);
        mFramgment_glsl="saturation_fragment_shader.glsl";
    }
    @Override
    public void onInit() {

        //init();
    }

    @Override
    public void onFRInit() {

    }

    @Override
    public void onSelected() {
        // TODO Auto-generated method stub

    }

}

