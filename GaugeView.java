package app.autolauncher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/** Velocímetro de 0 a 180 km/h, dibujado a mano para que sea ligero. */
public class GaugeView extends View {

    private static final float MAX = 180f;

    private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint prog = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tick = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint big = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint unit = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sub = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();

    private float speed;
    private String speedText = "--";
    private String subText = "";

    public GaugeView(Context c) {
        this(c, null);
    }

    public GaugeView(Context c, AttributeSet a) {
        super(c, a);
        track.setStyle(Paint.Style.STROKE);
        track.setStrokeCap(Paint.Cap.ROUND);
        track.setStrokeWidth(dp(8));
        track.setColor(0xFF1E262E);
        prog.set(track);
        prog.setColor(0xFF22D3EE);
        tick.setStrokeWidth(dp(2));
        tick.setColor(0xFF3A444E);
        label.setTextAlign(Paint.Align.CENTER);
        label.setTextSize(sp(13));
        label.setColor(0xFF8B95A0);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        big.setTextAlign(Paint.Align.CENTER);
        big.setColor(0xFFE8ECEF);
        big.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        unit.setTextAlign(Paint.Align.CENTER);
        unit.setTextSize(sp(16));
        unit.setColor(0xFFAEB7C0);
        sub.setTextAlign(Paint.Align.CENTER);
        sub.setTextSize(sp(12));
        sub.setColor(0xFF8B95A0);
        sub.setTypeface(Typeface.DEFAULT_BOLD);
        sub.setLetterSpacing(0.13f);
    }

    void setAccent(int color) {
        prog.setColor(color);
        invalidate();
    }

    /** speedKmh negativo = sin señal. */
    void setSpeed(float speedKmh, String subtitle) {
        speed = Math.max(0f, speedKmh);
        speedText = speedKmh < 0 ? "--" : String.valueOf(Math.round(speedKmh));
        subText = subtitle == null ? "" : subtitle;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight();
        float r = Math.min(w / 2f - dp(12), (h - dp(16)) / 1.75f);
        if (r <= 0) return;
        float cx = w / 2f;
        float cy = (h - 1.707f * r) / 2f + r;
        oval.set(cx - r, cy - r, cx + r, cy + r);

        for (int v = 0; v <= MAX; v += 10) {
            double a = Math.toRadians(135 + 1.5 * v);
            float r1 = r - dp(10);
            float r2 = v % 20 == 0 ? r - dp(20) : r - dp(15);
            c.drawLine(cx + (float) (r1 * Math.cos(a)), cy + (float) (r1 * Math.sin(a)),
                    cx + (float) (r2 * Math.cos(a)), cy + (float) (r2 * Math.sin(a)), tick);
            if (v % 20 == 0) {
                float rl = r - dp(36);
                c.drawText(String.valueOf(v), cx + (float) (rl * Math.cos(a)),
                        cy + (float) (rl * Math.sin(a)) + label.getTextSize() / 3f, label);
            }
        }
        c.drawArc(oval, 135, 270, false, track);
        float sweep = 270f * Math.min(speed, MAX) / MAX;
        if (sweep > 0.5f) c.drawArc(oval, 135, sweep, false, prog);

        big.setTextSize(r * 0.55f);
        c.drawText(speedText, cx, cy + big.getTextSize() * 0.3f, big);
        c.drawText("km/h", cx, cy + big.getTextSize() * 0.3f + dp(28), unit);
        c.drawText(subText, cx, cy + r * 0.62f, sub);
    }

    private float dp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    private float sp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, getResources().getDisplayMetrics());
    }
}
