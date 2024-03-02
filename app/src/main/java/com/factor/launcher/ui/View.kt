package com.factor.launcher.ui

import android.view.View
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.lifecycle.*
import com.factor.launcher.R
import kotlinx.coroutines.*

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
        private val lifecycle = LifecycleRegistry(this)

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

        override fun getLifecycle(): Lifecycle {
            return lifecycle
        }
    }.also {
        lifecycleOwner = it
    }
    private set(value) {
        tag = value
    }

/**
 * ### Lifecycle of the view.
 *
 * These events can be used by custom components to handle lifecycle changes without implementing
 * any code inside the Activity or the Fragment.
 *
 * Locates the [LifecycleOwner] responsible for managing this [View], if present.
 * This may be used to scope work or heavyweight resources associated with the view
 * that may span cycles of the view becoming detached and reattached from a window.
 */
val View.lifecycle get() = lifecycleOwner.lifecycle

/**
 * ### [CoroutineScope] tied to this [View] [LifecycleOwner]'s [Lifecycle].
 *
 * This scope will be cancelled when the [Lifecycle] is destroyed.
 *
 * This scope is bound to
 * [Dispatchers.Main.immediate][kotlinx.coroutines.MainCoroutineDispatcher.immediate].
 */
val View.lifecycleScope get() = lifecycleOwner.lifecycleScope

/**
 * Runs the given block when the [View]'s [Lifecycle] is at least in  [Lifecycle.State.CREATED]
 * state.
 *
 * @see Lifecycle.whenStateAtLeast for details
 */
suspend fun <T> View.whenCreated(block: suspend CoroutineScope.() -> T): T =
    lifecycleOwner.whenCreated(block)

/**
 * Runs the given block when the [View]'s [Lifecycle] is at least in [Lifecycle.State.STARTED]
 * state.
 *
 * @see Lifecycle.whenStateAtLeast for details
 */
suspend fun <T> View.whenStarted(block: suspend CoroutineScope.() -> T): T =
    lifecycleOwner.whenStarted(block)

/**
 * Runs the given block when the [View]'s [Lifecycle] is at least in [Lifecycle.State.RESUMED]
 * state.
 *
 * @see Lifecycle.whenStateAtLeast for details
 */
suspend fun <T> View.whenResumed(block: suspend CoroutineScope.() -> T): T =
    lifecycleOwner.whenResumed(block)

/**
 * Performs the given action when this view is created. If the view is already created the action
 * will be performed immediately, otherwise the action will be performed after the view is next
 * created.
 *
 * The action will only be invoked once, and any listeners will then be removed.
 */
inline fun View.doOnCreate(crossinline action: (view: View) -> Unit) {
    if (lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
        action(this)
    } else {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                lifecycle.removeObserver(this)
                action(this@doOnCreate)
            }
        })
    }
}

/**
 * Performs the given action when this view is started. If the view is already started the action
 * will be performed immediately, otherwise the action will be performed after the view is next
 * started.
 *
 * The action will only be invoked once, and any listeners will then be removed.
 */
inline fun View.doOnStart(crossinline action: (view: View) -> Unit) {
    if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
        action(this)
    } else {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                lifecycle.removeObserver(this)
                action(this@doOnStart)
            }
        })
    }
}

/**
 * Performs the given action when this view is resumed. If the view is already resumed the action
 * will be performed immediately, otherwise the action will be performed after the view is next
 * resumed.
 *
 * The action will only be invoked once, and any listeners will then be removed.
 */
inline fun View.doOnResume(crossinline action: (view: View) -> Unit) {
    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        action(this)
    } else {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                lifecycle.removeObserver(this)
                action(this@doOnResume)
            }
        })
    }
}

/**
 * Performs the given action when this view is paused. The action will be performed after the view
 * is next paused.
 *
 * The action will only be invoked once, and any listeners will then be removed.
 */
inline fun View.doOnPause(crossinline action: (view: View) -> Unit) {
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onPause(owner: LifecycleOwner) {
            lifecycle.removeObserver(this)
            action(this@doOnPause)
        }
    })
}

/**
 * Performs the given action when this view is stopped. The action will be performed after the view
 * is next stopped.
 *
 * The action will only be invoked once, and any listeners will then be removed.
 */
inline fun View.doOnStop(crossinline action: (view: View) -> Unit) {
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            lifecycle.removeObserver(this)
            action(this@doOnStop)
        }
    })
}

/**
 * Performs the given action when this view is destroyed. The action will be performed after the view
 * is next destroyed.
 *
 * The action will only be invoked once, and any listeners will then be removed.
 */
inline fun View.doOnDestroy(crossinline action: (view: View) -> Unit) {
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            lifecycle.removeObserver(this)
            action(this@doOnDestroy)
        }
    })
}

/**
 * ### Adds a LifecycleObserver that will be notified when the LifecycleOwner changes state
 *
 * The given observer will be brought to the current state of the LifecycleOwner.
 * For example, if the LifecycleOwner is in [State.STARTED] state, the given observer
 * will receive [Event.ON_CREATE], [Event.ON_START] events.
 *
 * @param onCreate Notifies that {@code ON_CREATE} event occurred.
 * This method will be called after the {@link LifecycleOwner}'s {@code onCreate} method returns.
 * @param onStart Notifies that {@code ON_START} event occurred.
 * This method will be called after the {@link LifecycleOwner}'s {@code onStart} method returns.
 * @param onResume Notifies that {@code ON_RESUME} event occurred.
 * This method will be called after the {@link LifecycleOwner}'s {@code onResume} method returns.
 * @param onPause Notifies that {@code ON_PAUSE} event occurred.
 * This method will be called before the {@link LifecycleOwner}'s {@code onPause} method
 * is called.
 * @param onStop Notifies that {@code ON_STOP} event occurred.
 * This method will be called before the {@link LifecycleOwner}'s {@code onStop} method
 * is called.
 * @param onDestroy Notifies that {@code ON_DESTROY} event occurred.
 * This method will be called before the {@link LifecycleOwner}'s {@code onDestroy} method
 * is called.
 */
fun Lifecycle.addObserver(
    onCreate: ((owner: LifecycleOwner) -> Unit)? = null,
    onStart: ((owner: LifecycleOwner) -> Unit)? = null,
    onResume: ((owner: LifecycleOwner) -> Unit)? = null,
    onPause: ((owner: LifecycleOwner) -> Unit)? = null,
    onStop: ((owner: LifecycleOwner) -> Unit)? = null,
    onDestroy: ((owner: LifecycleOwner) -> Unit)? = null
) {
    addObserver(object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            onCreate?.invoke(owner)
        }

        override fun onStart(owner: LifecycleOwner) {
            onStart?.invoke(owner)
        }

        override fun onResume(owner: LifecycleOwner) {
            onResume?.invoke(owner)
        }

        override fun onPause(owner: LifecycleOwner) {
            onPause?.invoke(owner)
        }

        override fun onStop(owner: LifecycleOwner) {
            onStop?.invoke(owner)
        }

        override fun onDestroy(owner: LifecycleOwner) {
            onDestroy?.invoke(owner)
        }
    })
}