package edu.astate.siddarth.tidalrelay;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.Property;
import android.view.MotionEvent;
import android.view.View;

import static edu.astate.siddarth.tidalrelay.HarborStyle.*;

final class SignalDial extends View {
    private static final Property<SignalDial, Float> WAVE = new Property<>(Float.class, "wave") {
        public Float get(SignalDial dial) { return dial.wave; }
        public void set(SignalDial dial, Float value) { dial.wave = value; dial.invalidate(); }
    };
    private static final Property<SignalDial, Float> FLASH = new Property<>(Float.class, "flash") {
        public Float get(SignalDial dial) { return dial.flash; }
        public void set(SignalDial dial, Float value) { dial.flash = value; dial.invalidate(); }
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF area = new RectF();
    private final Path path = new Path();
    private float heading = 45;
    private float target = 285;
    private float tolerance = 28;
    private float wave = 1;
    private float flash;
    private int signalColor = GOLD;
    private boolean interactive;
    private AnimatorSet response;

    SignalDial(Context context) {
        super(context);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    void aim(float heading, float target, float tolerance, boolean interactive) {
        this.heading = heading;
        this.target = target;
        this.tolerance = tolerance;
        this.interactive = interactive;
        invalidate();
    }

    void acknowledge(Watch.Result result) {
        if (response != null) response.cancel();
        signalColor = result == Watch.Result.MISSED ? CORAL : AQUA;
        wave = 0;
        flash = 0.9f;
        response = new AnimatorSet();
        response.playTogether(
                ObjectAnimator.ofFloat(this, WAVE, 0f, 1f).setDuration(560),
                ObjectAnimator.ofFloat(this, FLASH, 0.9f, 0).setDuration(390)
        );
        response.start();
    }

    @Override protected void onDetachedFromWindow() {
        if (response != null) response.cancel();
        super.onDetachedFromWindow();
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (!interactive && event.getActionMasked() == MotionEvent.ACTION_DOWN) return false;
        if (event.getActionMasked() == MotionEvent.ACTION_UP && interactive
                && event.getX() >= 0 && event.getX() < getWidth()
                && event.getY() >= 0 && event.getY() < getHeight()) {
            performClick();
        }
        return true;
    }

    @Override public boolean performClick() { return super.performClick(); }

    private void fill(int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
    }

    private void stroke(int color, float width) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(width);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        fill(PANEL);
        paint.setShader(new LinearGradient(0, 0, 0, getHeight(), 0xff234259, 0xff102337, Shader.TileMode.CLAMP));
        area.set(0, 0, getWidth(), getHeight());
        float corner = dp(getContext(), 16);
        canvas.drawRoundRect(area, corner, corner, paint);
        paint.setShader(null);

        float size = Math.min(getWidth(), getHeight());
        canvas.save();
        canvas.translate((getWidth() - size) / 2f, (getHeight() - size) / 2f);
        canvas.scale(size / 300f, size / 300f);
        drawWater(canvas);
        drawCompass(canvas);
        drawLighthouse(canvas);
        if (wave < 1) {
            int alpha = (int) (180 * (1 - wave));
            stroke((signalColor & 0xffffff) | (alpha << 24), 3);
            canvas.drawCircle(150, 150, 26 + 94 * wave, paint);
        }
        if (flash > 0) {
            int alpha = (int) (220 * flash);
            fill((signalColor & 0xffffff) | (alpha << 24));
            canvas.drawCircle(150, 122, 10f, paint);
        }
        canvas.restore();
    }

    private void drawWater(Canvas canvas) {
        stroke(0x35439baf, 1);
        for (int row = 0; row < 9; row++) {
            int y = 104 + row * 21;
            path.reset();
            path.moveTo(0, y);
            for (int x = 0; x < 300; x += 60) {
                path.rQuadTo(15, -9, 30, 0);
                path.rQuadTo(15, 9, 30, 0);
            }
            canvas.drawPath(path, paint);
        }
        fill(0x809bbdce);
        for (int i = 0; i < 22; i++) {
            float x = 12 + (i * 79) % 277;
            float y = 15 + (i * 47) % 106;
            canvas.drawCircle(x, y, i % 4 == 0 ? 1.8f : 0.9f, paint);
        }
    }

    private void drawCompass(Canvas canvas) {
        fill(0xc011293d);
        canvas.drawCircle(150, 150, 108, paint);
        for (int radius = 48; radius <= 108; radius += 30) {
            stroke(0xff365a6d, 1);
            canvas.drawCircle(150, 150, radius, paint);
        }
        for (int tick = 0; tick < 48; tick++) {
            canvas.save();
            canvas.rotate(tick * 7.5f, 150, 150);
            stroke(tick % 4 == 0 ? MUTED : LINE, tick % 4 == 0 ? 1.6f : 1f);
            canvas.drawLine(150, 44, 150, tick % 4 == 0 ? 53 : 49, paint);
            canvas.restore();
        }
        fill(MUTED);
        paint.setTypeface(Typeface.MONOSPACE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(11);
        canvas.drawText("N", 150, 31, paint);
        canvas.drawText("E", 271, 154, paint);
        canvas.drawText("S", 150, 275, paint);
        canvas.drawText("W", 29, 154, paint);

        area.set(49, 49, 251, 251);
        stroke(0xff214654, 16);
        canvas.drawArc(area, target - 90 - tolerance, 2 * tolerance, false, paint);
        stroke(AQUA, 7);
        canvas.drawArc(area, target - 90 - tolerance, 2 * tolerance, false, paint);
        stroke(PAPER, 3);
        canvas.drawArc(area, target - 90 - 5, 10, false, paint);

        canvas.save();
        canvas.rotate(heading, 150, 150);
        fill(0x66ffd585);
        path.reset();
        path.moveTo(150, 150);
        path.lineTo(137, 59);
        path.lineTo(163, 59);
        path.close();
        canvas.drawPath(path, paint);
        stroke(GOLD, 2);
        canvas.drawLine(150, 144, 150, 54, paint);
        fill(GOLD);
        canvas.drawCircle(150, 52, 4.5f, paint);
        canvas.restore();
    }

    private void drawLighthouse(Canvas canvas) {
        fill(0xff0b1827);
        canvas.drawOval(109, 180, 191, 201, paint);
        fill(PAPER);
        path.reset();
        path.moveTo(140, 135);
        path.lineTo(160, 135);
        path.lineTo(168, 187);
        path.lineTo(132, 187);
        path.close();
        canvas.drawPath(path, paint);
        fill(CORAL);
        path.reset();
        path.moveTo(138, 148);
        path.lineTo(162, 148);
        path.lineTo(164, 160);
        path.lineTo(136, 160);
        path.close();
        canvas.drawPath(path, paint);
        fill(GOLD);
        canvas.drawRoundRect(139, 118, 161, 134, 3, 3, paint);
        fill(NIGHT);
        canvas.drawRect(148, 119, 152, 135, paint);
        path.reset();
        path.moveTo(134, 118);
        path.lineTo(150, 105);
        path.lineTo(166, 118);
        path.close();
        canvas.drawPath(path, paint);
        canvas.drawRoundRect(146, 173, 154, 187, 3, 3, paint);
        stroke(AQUA, 1.8f);
        canvas.drawArc(122, 192, 178, 207, 5, 170, false, paint);
    }
}
