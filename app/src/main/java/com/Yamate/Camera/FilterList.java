package com.Yamate.Camera;

import com.Yamate.Camera.filter.*;
import java.util.ArrayList;

/**
 * Created by vincent on 14/10/2016.
 *
 * managing filter
 *
 * creating by UI and handle by UI
 *
 * passing the filter to Renderer for adding effect
 *
 */

public class FilterList {
    private ArrayList<Filter> mFilterList = new ArrayList<Filter>();
    int mCurrentFilter=0;

    public FilterList(int w,int h)
    {
        mFilterList.add(new Normal(w,h));
        mFilterList.add(new Sepia(w,h));
        mFilterList.add(new DuocolorR(w,h));
        mFilterList.add(new DuocolorG(w,h));
        mFilterList.add(new DuocolorB(w,h));
        mFilterList.add(new Arique(w,h));
        mFilterList.add(new Emboss(w,h));
        mFilterList.add(new Halftone(w,h));
        mFilterList.add(new Cartoon(w,h));
        mFilterList.add(new NightVision(w,h,R.mipmap.tex_noise01_512512));
        mFilterList.add(new MagicPen(w,h));
        mFilterList.add(new Sketch(w,h));
        mFilterList.add(new Hue(w,h));
    }

    public ArrayList<Filter> getFilters()
    {
        return mFilterList;
    }

    public Filter getCurrnectFilter()
    {
        return mFilterList.get(mCurrentFilter);
    }

    public void setCurrentFilter(int index)
    {
        mCurrentFilter=index;
    }

    public void setNextFilter()
    {
        mCurrentFilter++;

        if(mCurrentFilter==mFilterList.size())
            mCurrentFilter=0;

    }

    public int getFilterSize()
    {
        return mFilterList.size();
    }

    public void setFilter(int index)
    {
        mCurrentFilter=index;

    }
}
