package com.factor.chips.chipslayoutmanager.cache;


import androidx.recyclerview.widget.RecyclerView;

public class ViewCacheFactory {

    private final RecyclerView.LayoutManager layoutManager;

    public ViewCacheFactory(RecyclerView.LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    public IViewCacheStorage createCacheStorage() {
        return new ViewCacheStorage(layoutManager);
    }
}
