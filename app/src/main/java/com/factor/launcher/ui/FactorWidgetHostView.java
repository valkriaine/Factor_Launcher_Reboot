package com.factor.launcher.ui;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
public class FactorWidgetHostView extends AppWidgetHostView
{
    private boolean mHasPerformedLongPress;

    private CheckForLongPress mPendingCheckForLongPress;

    public FactorWidgetHostView(Context context)
    {
        super(context);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        // Consume any touch events for ourselves after long press is triggered
        if (mHasPerformedLongPress)
        {
            mHasPerformedLongPress = false;
            return true;
        }

        // Watch for long press events at this level to make sure
        // users can always pick up this widget
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                {
                postCheckForLongClick();
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mHasPerformedLongPress = false;
                if (mPendingCheckForLongPress != null)
                {
                    removeCallbacks(mPendingCheckForLongPress);
                }
                break;
        }

        // Otherwise continue letting touch events fall through to children
        return false;
    }

    class CheckForLongPress implements Runnable
    {
        private int mOriginalWindowAttachCount;
        public void run()
        {
            if ((getParent() != null) && hasWindowFocus()
                    && mOriginalWindowAttachCount == getWindowAttachCount()
                    && !mHasPerformedLongPress)
            {
                if (performLongClick())
                {
                    mHasPerformedLongPress = true;
                }
            }
        }
        public void rememberWindowAttachCount() {
            mOriginalWindowAttachCount = getWindowAttachCount();
        }
    }
    private void postCheckForLongClick()
    {
        mHasPerformedLongPress = false;
        if (mPendingCheckForLongPress == null)
        {
            mPendingCheckForLongPress = new CheckForLongPress();
        }
        mPendingCheckForLongPress.rememberWindowAttachCount();
        postDelayed(mPendingCheckForLongPress, ViewConfiguration.getLongPressTimeout());
    }
    @Override
    public void cancelLongPress()
    {
        super.cancelLongPress();
        mHasPerformedLongPress = false;
        if (mPendingCheckForLongPress != null)
        {
            removeCallbacks(mPendingCheckForLongPress);
        }
    }
}