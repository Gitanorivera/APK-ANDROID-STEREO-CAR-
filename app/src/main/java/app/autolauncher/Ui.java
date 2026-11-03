package app.autolauncher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;

/** Piezas del diseño premium que dependen del color de acento o de la portada. */
final class Ui {

    private Ui() {
    }

    static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    static int alpha(int color, float a) {
        return (Math.round(a * 255) << 24) | (color & 0x00FFFFFF);
    }

    /** Mezcla dos colores: t = 0 da a, t = 1 da b. */
    static int blend(int a, int b, float t) {
        float s = 1f - t;
        return Color.rgb(
                Math.round(Color.red(a) * s + Color.red(b) * t),
                Math.round(Color.green(a) * s + Color.green(b) * t),
                Math.round(Color.blue(a) * s + Color.blue(b) * t));
    }

    /** El acento de noche es un poco más oscuro para no deslumbrar. */
    static int accent(int accent, boolean night) {
        return night ? blend(accent, Color.BLACK, 0.15f) : accent;
    }

    /** Botón principal (Android Auto, Inicio): de día, cristal de color; de noche, tenue. */
    static Drawable accentButton(Context c, int accent, boolean night, float radiusDp) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR, night
                ? new int[]{alpha(accent, 0.18f), alpha(accent, 0.08f)}
                : new int[]{blend(accent, Color.WHITE, 0.2f), blend(accent, 0xFF2B6FE0, 0.35f)});
        g.setCornerRadius(dp(c, radiusDp));
        g.setStroke(dp(c, 1), night ? alpha(accent, 0.28f) : alpha(Color.WHITE, 0.35f));
        return g;
    }

    /** Texto sobre el botón principal: oscuro de día, del color de acento de noche. */
    static int onAccent(int accent, boolean night) {
        return night ? blend(accent, Color.WHITE, 0.3f) : blend(accent, Color.BLACK, 0.86f);
    }

    static int onAccentSub(int accent, boolean night) {
        return night ? alpha(blend(accent, Color.WHITE, 0.3f), 0.7f) : blend(accent, Color.BLACK, 0.72f);
    }

    /** Cuadro del ícono dentro del botón principal. */
    static Drawable accentChip(Context c, int accent, boolean night) {
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(c, 18));
        g.setColor(night ? alpha(accent, 0.14f) : alpha(Color.BLACK, 0.12f));
        return g;
    }

    /** Opción activa del menú lateral. */
    static Drawable navActive(Context c, int accent) {
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(c, 18));
        g.setColor(alpha(accent, 0.13f));
        g.setStroke(dp(c, 1), alpha(accent, 0.3f));
        return g;
    }

    /** Tarjeta de música: vidrio con un resplandor del color de la portada. */
    static Drawable musicCard(Context c, Drawable glass, int glow) {
        GradientDrawable g = new GradientDrawable();
        g.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        g.setGradientCenter(0.12f, 0.18f);
        g.setGradientRadius(dp(c, 360));
        g.setColors(new int[]{glow, glow & 0x00FFFFFF});
        g.setCornerRadius(dp(c, 28));
        return new LayerDrawable(new Drawable[]{g, glass});
    }

    /** Color promedio de la portada, para el resplandor. Barato: reduce a 8×8 y promedia. */
    static int averageColor(Bitmap b) {
        if (b == null || b.isRecycled()) return 0;
        long r = 0, g = 0, bl = 0;
        try {
            Bitmap small = Bitmap.createScaledBitmap(b, 8, 8, true);
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < 8; x++) {
                    int p = small.getPixel(x, y);
                    r += Color.red(p);
                    g += Color.green(p);
                    bl += Color.blue(p);
                }
            }
            if (small != b) small.recycle();
        } catch (IllegalStateException | IllegalArgumentException e) {
            return 0; // Portadas en memoria de video no se pueden leer: se usa el resplandor por defecto.
        }
        int avg = Color.rgb((int) (r / 64), (int) (g / 64), (int) (bl / 64));
        // Que brille aunque la portada sea oscura.
        return blend(avg, Color.WHITE, 0.15f);
    }
}
