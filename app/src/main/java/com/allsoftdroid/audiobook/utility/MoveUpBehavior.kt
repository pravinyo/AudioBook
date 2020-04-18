package com.allsoftdroid.audiobook.utility

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar.SnackbarLayout


class MoveUpBehavior:CoordinatorLayout.Behavior<View>() {

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        return dependency is SnackbarLayout
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        val translationY =
            Math.min(0f, dependency.translationY - dependency.height)
        child.translationY = translationY+1
        return true
    }
}