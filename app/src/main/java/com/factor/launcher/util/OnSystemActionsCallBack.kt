package com.factor.launcher.util

//implement in fragments to handle onBackPressed
interface OnSystemActionsCallBack
{
    fun onBackPressed(): Boolean

    fun onNewIntent() : Boolean
}