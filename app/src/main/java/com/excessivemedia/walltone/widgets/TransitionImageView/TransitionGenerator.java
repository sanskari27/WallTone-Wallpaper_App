package com.excessivemedia.walltone.widgets.TransitionImageView;

import android.graphics.RectF;

interface TransitionGenerator {

    Transition generateNextTransition(RectF drawableBounds, RectF viewport);

}
