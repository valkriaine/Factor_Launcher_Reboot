package com.factor.launcher.ui

import android.view.View
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.lifecycle.*

/**
 * ### The class that has a lifecycle and lifecycleScope
 *
 * These events can be used by custom components to handle lifecycle changes without implementing
 * any code inside the Activity or the Fragment.
 *
 * Locates the [LifecycleOwner] responsible for managing this [View], if present.
 * This may be used to scope work or heavyweight resources associated with the view
 * that may span cycles of the view becoming detached and reattached from a window.
 */
var View.lifecycleOwner: LifecycleOwner
    get() = tag as? LifecycleOwner ?: object : LifecycleOwner,
        LifecycleEventObserver {
        override val lifecycle = LifecycleRegistry(this)

        init {
            doOnAttach {
                findViewTreeLifecycleOwner()?.lifecycle?.addObserver(this)
            }
            doOnDetach {
                findViewTreeLifecycleOwner()?.lifecycle?.removeObserver(this)
                lifecycle.currentState = Lifecycle.State.DESTROYED
            }
        }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            lifecycle.currentState = event.targetState
        }

    }.also {
        lifecycleOwner = it
    }
    private set(value) {
        tag = value
    }


val View.lifecycle get() = lifecycleOwner.lifecycle

