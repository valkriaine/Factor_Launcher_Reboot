package com.factor.chips.chipslayoutmanager.layouter.breaker;


import com.factor.chips.chipslayoutmanager.cache.IViewCacheStorage;
import com.factor.chips.chipslayoutmanager.layouter.AbstractLayouter;

class CacheRowBreaker extends RowBreakerDecorator {

    private IViewCacheStorage cacheStorage;

    CacheRowBreaker(IViewCacheStorage cacheStorage, ILayoutRowBreaker decorate) {
        super(decorate);
        this.cacheStorage = cacheStorage;
    }

    @Override
    public boolean isRowBroke(AbstractLayouter al) {
        boolean stopDueToCache = cacheStorage.isPositionEndsRow(al.getCurrentViewPosition());
        return super.isRowBroke(al) || stopDueToCache;
    }
}
