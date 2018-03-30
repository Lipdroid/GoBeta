package lipdroid.com.gobeta.customViews;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.google.android.gms.maps.model.LatLng;

import lipdroid.com.gobeta.R;

/**
 * Created by mdmunirhossain on 3/30/18.
 */

public class PulseMarkerView extends MarkerView {
    private static final int STROKE_DIMEN = 2;

    private Animation scaleAnimation;
    private Paint strokeBackgroundPaint;
    private Paint backgroundPaint;
    private String text;
    private Paint textPaint;
    private AnimatorSet showAnimatorSet, hideAnimatorSet;
    private Context context;
    private int size;

    public PulseMarkerView(final Context context, final LatLng latLng, final Point point) {
        super(context, latLng, point);
        this.context = context;
        setVisibility(View.VISIBLE);
        setupSizes(context);
        setupScaleAnimation(context);
        setupBackgroundPaint(context);
        setupStrokeBackgroundPaint(context);
        setupTextPaint(context);
        setupShowAnimatorSet();
        setupHideAnimatorSet();
    }

    public PulseMarkerView(final Context context, final LatLng latLng, final Point point, final int position) {
        this(context, latLng, point);
        text = String.valueOf(position);
    }

    private void setupHideAnimatorSet() {
        Animator animatorScaleX = ObjectAnimator.ofFloat(this, View.SCALE_X, 1.0f, 0.f);
        Animator animatorScaleY = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1.0f, 0.f);
        Animator animator = ObjectAnimator.ofFloat(this, View.ALPHA, 1.f, 0.f).setDuration(300);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(final Animator animation) {
                super.onAnimationStart(animation);
                setVisibility(View.INVISIBLE);
                invalidate();
            }
        });
        hideAnimatorSet = new AnimatorSet();
        hideAnimatorSet.playTogether(animator, animatorScaleX, animatorScaleY);
    }

    private void setupSizes(final Context context) {
        size = 32;
    }

    private void setupShowAnimatorSet() {
        Animator animatorScaleX = ObjectAnimator.ofFloat(this, View.SCALE_X, 1.5f, 1.f);
        Animator animatorScaleY = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1.5f, 1.f);
        Animator animator = ObjectAnimator.ofFloat(this, View.ALPHA, 0.f, 1.f).setDuration(300);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(final Animator animation) {
                super.onAnimationStart(animation);
                setVisibility(View.VISIBLE);
                invalidate();
            }
        });
        showAnimatorSet = new AnimatorSet();
        showAnimatorSet.playTogether(animator, animatorScaleX, animatorScaleY);
    }

    private void setupScaleAnimation(final Context context) {
        scaleAnimation = AnimationUtils.loadAnimation(context, R.anim.pulse);
        scaleAnimation.setDuration(100);
    }

    private void setupTextPaint(final Context context) {
        textPaint = new Paint();
        textPaint.setColor(ContextCompat.getColor(context, R.color.white));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(12f);
    }

    private void setupStrokeBackgroundPaint(final Context context) {
        strokeBackgroundPaint = new Paint();
        strokeBackgroundPaint.setColor(ContextCompat.getColor(context, android.R.color.white));
        strokeBackgroundPaint.setStyle(Paint.Style.STROKE);
        strokeBackgroundPaint.setAntiAlias(true);
        strokeBackgroundPaint.setStrokeWidth(10);
    }

    private void setupBackgroundPaint(final Context context) {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
        backgroundPaint.setAntiAlias(true);
    }

    @Override
    public void setLayoutParams(final ViewGroup.LayoutParams params) {
        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        frameParams.width = 200;
        frameParams.height = 200;
        frameParams.leftMargin = point.x - frameParams.width / 2;
        frameParams.topMargin = point.y - frameParams.height / 2;
        super.setLayoutParams(frameParams);
    }

    public void pulse() {
        startAnimation(scaleAnimation);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        drawBackground(canvas);
        drawStrokeBackground(canvas);
        drawText(canvas);
        super.onDraw(canvas);
    }

    private void drawText(final Canvas canvas) {
        if(text != null && !TextUtils.isEmpty(text))
            canvas.drawText(text, size, (size - 20), textPaint);
    }

    private void drawStrokeBackground(final Canvas canvas) {
        canvas.drawCircle(size, size, 20, strokeBackgroundPaint);
    }

    private void drawBackground(final Canvas canvas) {
        canvas.drawCircle(size, size, size, backgroundPaint);
    }

    public void setText(String text) {
        this.text = text;
        invalidate();
    }

    @Override
    public void hide() {
        hideAnimatorSet.start();
    }

    @Override
    public void refresh(final Point point) {
        this.point = point;
        updatePulseViewLayoutParams(point);
    }

    @Override
    public void show() {
        showAnimatorSet.start();
    }

    public void showWithDelay(final int delay) {
        showAnimatorSet.setStartDelay(delay);
        showAnimatorSet.start();
    }

    public void updatePulseViewLayoutParams(final Point point) {
        this.point = point;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.width = 200;
        params.height = 200;
        params.leftMargin = point.x - params.width / 2;
        params.topMargin = point.y - params.height / 2;
        super.setLayoutParams(params);
        invalidate();
    }
}
