package app.autolauncher;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/** Abre apps instaladas usando únicamente su intent de inicio oficial. */
final class Launch {

    private Launch() {
    }

    static boolean isLaunchable(Context c, String pkg) {
        return pkg != null && !pkg.isEmpty()
                && c.getPackageManager().getLaunchIntentForPackage(pkg) != null;
    }

    static boolean launch(Context c, String pkg) {
        if (pkg == null || pkg.isEmpty()) return false;
        Intent i = c.getPackageManager().getLaunchIntentForPackage(pkg);
        if (i == null) return false;
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            c.startActivity(i);
            return true;
        } catch (ActivityNotFoundException | SecurityException e) {
            return false;
        }
    }

    static CharSequence label(Context c, String pkg) {
        if (pkg == null) return null;
        PackageManager pm = c.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
            return pm.getApplicationLabel(ai);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
