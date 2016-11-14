package com.Yamate.Camera.filter;

/**
 * Created by vincent on 08/10/2016.
 */

public class Lomo extends Filter {

    public Lomo(int w, int h) {
        super(w, h);
        mFramgment_glsl="lomo_fragment_shader.glsl";
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
