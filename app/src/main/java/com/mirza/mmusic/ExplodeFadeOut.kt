package com.mirza.mmusic

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.os.Build
import android.transition.Explode
import android.transition.TransitionValues
import android.view.View
import android.view.ViewGroup

/**
 * Created by avantari on 11/22/17.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class ExplodeFadeOut : Explode() {
    init {
        propagation = null
    }

    override fun onAppear(sceneRoot: ViewGroup?, view: View?, startValues: TransitionValues?,
                          endValues: TransitionValues?): Animator {

        val explodeAnimator = super.onAppear(sceneRoot, view, startValues, endValues)
        val fadeInAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)

        return animatorSet(explodeAnimator, fadeInAnimator)
    }

    override fun onDisappear(sceneRoot: ViewGroup?, view: View?, startValues: TransitionValues?,
                             endValues: TransitionValues?): Animator {
        val explodeAnimator = super.onDisappear(sceneRoot, view, startValues, endValues)
        val fadeOutAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f)

        return animatorSet(explodeAnimator, fadeOutAnimator)
    }

    override fun captureStartValues(transitionValues: TransitionValues) {
        super.captureStartValues(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        super.captureEndValues(transitionValues)
    }

    private fun animatorSet(explodeAnimator: Animator, fadeAnimator: Animator): AnimatorSet {
        val animatorSet = AnimatorSet()
        animatorSet.play(explodeAnimator).with(fadeAnimator)
        return animatorSet
    }
}