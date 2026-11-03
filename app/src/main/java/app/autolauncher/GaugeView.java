package app.autolauncher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Velocímetro de 0 a 180 km/h, dibujado a mano para que sea ligero:
 * arco con degradado del color de acento, marca de la velocidad máxima del viaje y rumbo.
 */
public class GaugeView extends View {

    private static final float MAX = 180f;
    private static final float START = 135f;
    private static final float SWEEP = 270f;

    private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint prog = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tickMajor = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tickMinor = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint big = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint unit = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sub = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint maxMark = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotRing = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();
    private final Matrix rotate = new Matrix();

    private int accent;
    private boolean night;
    private float speed;
    private float maxSpeed;
    private String speedText = "--";
    private String subText = "";

    public GaugeView(Context c) {
        this(c, null);
    }

    public GaugeView(Context c, AttributeSet a) {
        super(c, a);
        accent = c.getColor(R.color.accent);
        track.setStyle(Paint.Style.STROKE);
        track.setStrokeCap(Paint.Cap.ROUND);
        track.setStrokeWidth(dp(12));
        track.setColor(c.getColor(R.color.track));
        prog.set(track);
        tickMajor.setStrokeCap(Paint.Cap.ROUND);
        tickMajor.setStrokeWidth(dp(2.5f));
        tickMajor.setColor(c.getColor(R.color.tick_major));
        tickMinor.setStrokeCap(Paint.Cap.ROUND);
        tickMinor.setStrokeWidth(dp(2));
        tickMinor.setColor(c.getColor(R.color.tick_minor));
        label.setTextAlign(Paint.Align.CENTER);
        label.setTextSize(sp(13));
        label.setColor(c.getColor(R.color.text2));
        label.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        big.setTextAlign(Paint.Align.CENTER);
        big.setColor(c.getColor(R.color.clock));
        big.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
        big.setFontFeatureSettings("tnum");
        unit.setTextAlign(Paint.Align.CENTER);
        unit.setTextSize(sp(17));
        unit.setColor(c.getColor(R.color.text2));
        unit.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        sub.setTextAlign(Paint.Align.CENTER);
        sub.setTextSize(sp(13));
        sub.setTypeface(Typeface.DEFAULT_BOLD);
        sub.setLetterSpacing(0.15f);
        maxMark.setStrokeCap(Paint.Cap.ROUND);
        maxMark.setStrokeWidth(dp(4));
        maxMark.setColor(c.getColor(R.color.max));
        dotFill.setColor(c.getColor(R.color.gauge_dot));
        dotRing.setStyle(Paint.Style.STROKE);
        dotRing.setStrokeWidth(dp(4));
    }

    void setAccent(int color, boolean night) {
        this.accent = color;
        this.night = night;
        invalidate();
    }

    /** speedKmh negativo = sin señal. */
    void setSpeed(float speedKmh, float maxKmh, String subtitle) {
        speed = Math.max(0f, speedKmh);
        maxSpeed = Math.max(0f, maxKmh);
        speedText = speedKmh < 0 ? "--" : String.valueOf(Math.round(speedKmh));
        subText = subtitle == null ? "" : subtitle;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight();
        float r = Math.min(w / 2f - dp(16), (h - dp(16)) / 1.75f);
        if (r <= 0) return;
        float cx = w / 2f;
        float cy = (h - 1.707f * r) / 2f + r;
        oval.set(cx - r, cy - r, cx + r, cy + r);

        for (int v = 0; v <= MAX; v += 10) {
            double a = Math.toRadians(START + SWEEP / MAX * v);
            boolean major = v % 20 == 0;
            float r1 = r - dp(18);
            float r2 = major ? r - dp(32) : r - dp(26);
            c.drawLine(cx + (float) (r1 * Math.cos(a)), cy + (float) (r1 * Math.sin(a)),
                    cx + (float) (r2 * Math.cos(a)), cy + (float) (r2 * Math.sin(a)), major ? tickMajor : tickMinor);
            if (major) {
                float rl = r - dp(52);
                c.drawText(String.valueOf(v), cx + (float) (rl * Math.cos(a)),
                        cy + (float) (rl * Math.sin(a)) + label.getTextSize() / 3f, label);
            }
        }
        c.drawArc(oval, START, SWEEP, false, track);

        float sweep = SWEEP * Math.min(speed, MAX) / MAX;
        if (sweep > 0.5f) {
            int end = night ? accent : Ui.blend(accent, 0xFF6C8CFF, 0.6f);
            SweepGradient sg = new SweepGradient(cx, cy, new int[]{accent, end, accent},
                    new float[]{0f, SWEEP / 360f, 1f});
            rotate.setRotate(START, cx, cy);
            sg.setLocalMatrix(rotate);
            prog.setShader(sg);
            if (night) prog.clearShadowLayer();
            else prog.setShadowLayer(dp(8), 0, 0, Ui.alpha(accent, 0.55f));
            c.drawArc(oval, START, sweep, false, prog);

            double ea = Math.toRadians(START + sweep);
            float ex = cx + (float) (r * Math.cos(ea));
            float ey = cy + (float) (r * Math.sin(ea));
            dotRing.setColor(accent);
            c.drawCircle(ex, ey, dp(9), dotFill);
            c.drawCircle(ex, ey, dp(9), dotRing);
        }

        if (maxSpeed > 0.5f) {
            double ma = Math.toRadians(START + SWEEP * Math.min(maxSpeed, MAX) / MAX);
            float m1 = r + dp(10), m2 = r - dp(10);
            c.drawLine(cx + (float) (m1 * Math.cos(ma)), cy + (float) (m1 * Math.sin(ma)),
                    cx + (float) (m2 * Math.cos(ma)), cy + (float) (m2 * Math.sin(ma)), maxMark);
        }

        big.setTextSize(r * 0.62f);
        c.drawText(speedText, cx, cy + big.getTextSize() * 0.32f, big);
        c.drawText("km/h", cx, cy + big.getTextSize() * 0.32f + dp(30), unit);
        sub.setColor(accent);
        c.drawText(subText, cx, cy + r * 0.66f, sub);
    }

    private float dp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    private float sp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, getResources().getDisplayMetrics());
    }
}
