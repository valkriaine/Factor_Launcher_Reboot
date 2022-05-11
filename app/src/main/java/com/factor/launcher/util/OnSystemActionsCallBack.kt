package com.factor.launcher.util

//implement in fragments to handle onBackPressed
interface OnSystemActionsCallBack
{
    // on back button pressed
    fun onBackPressed(): Boolean

    // on home button pressed
    fun onNewIntent() : Boolean
}