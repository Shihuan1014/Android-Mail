package edu.hnu.mail.util;

import android.view.View;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

public abstract class TouchPtrHandler implements PtrHandler {

    /**
     * 手动设置是否可用下拉刷新
     */
    private boolean canScrollUp = true;

    /**
     * 手动设置是否可用下拉刷新
     * @param canScrollUp true则可下拉刷新，false则不可下拉刷新
     */
    public void setCanScrollUp(boolean canScrollUp) {
        this.canScrollUp = canScrollUp;
    }

    @Override
    public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
        return canScrollUp && PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
    }
}
