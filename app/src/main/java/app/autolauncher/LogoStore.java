package app.autolauncher;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Guarda el logotipo de la bienvenida en la memoria privada de la app.
 * La imagen se vuelve a dibujar y guardar como PNG pequeño: así no se conserva
 * nada del archivo original (metadatos, datos ocultos).
 */
final class LogoStore {

    private static final long MAX_BYTES = 10L * 1024 * 1024; // 10 MB
    private static final int MAX_DIM = 512;
    private static final String NAME = "logo.png";

    private LogoStore() {
    }

    private static File file(Context c) {
        return new File(c.getFilesDir(), NAME);
    }

    static boolean exists(Context c) {
        return file(c).isFile();
    }

    static Bitmap load(Context c) {
        File f = file(c);
        if (!f.isFile()) return null;
        return BitmapFactory.decodeFile(f.getAbsolutePath());
    }

    static void delete(Context c) {
        //noinspection ResultOfMethodCallIgnored
        file(c).delete();
    }

    /** Ejecutar fuera del hilo principal. */
    static boolean importFrom(Context c, Uri uri) {
        byte[] data = readCapped(c.getContentResolver(), uri);
        if (data == null) return false;

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, bounds);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return false;

        int sample = 1;
        while (bounds.outWidth / (sample * 2) >= MAX_DIM || bounds.outHeight / (sample * 2) >= MAX_DIM) {
            sample *= 2;
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = sample;
        Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
        if (b == null) return false;

        float scale = Math.min(1f, (float) MAX_DIM / Math.max(b.getWidth(), b.getHeight()));
        if (scale < 1f) {
            Bitmap scaled = Bitmap.createScaledBitmap(b,
                    Math.max(1, Math.round(b.getWidth() * scale)),
                    Math.max(1, Math.round(b.getHeight() * scale)), true);
            if (scaled != b) b.recycle();
            b = scaled;
        }

        File tmp = new File(c.getFilesDir(), "logo.tmp");
        boolean ok;
        try (FileOutputStream out = new FileOutputStream(tmp)) {
            ok = b.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            ok = false;
        } finally {
            b.recycle();
        }
        if (!ok) {
            //noinspection ResultOfMethodCallIgnored
            tmp.delete();
            return false;
        }
        return tmp.renameTo(file(c));
    }

    private static byte[] readCapped(ContentResolver cr, Uri uri) {
        try (InputStream in = cr.openInputStream(uri)) {
            if (in == null) return null;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[16 * 1024];
            long total = 0;
            int n;
            while ((n = in.read(buf)) != -1) {
                total += n;
                if (total > MAX_BYTES) return null;
                bos.write(buf, 0, n);
            }
            return bos.toByteArray();
        } catch (IOException | SecurityException | IllegalArgumentException e) {
            return null;
        }
    }
}
