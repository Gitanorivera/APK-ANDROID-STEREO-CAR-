package app.autolauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Cajón de apps. Con EXTRA_PICK_SLOT funciona como selector para un acceso directo. */
public class AppsActivity extends Activity {

    static final String EXTRA_PICK_SLOT = "app.autolauncher.PICK_SLOT";
    static final String EXTRA_PACKAGE = "app.autolauncher.PACKAGE";

    private static final class AppEntry {
        final String pkg;
        final String label;
        final Drawable icon;

        AppEntry(String pkg, String label, Drawable icon) {
            this.pkg = pkg;
            this.label = label;
            this.icon = icon;
        }
    }

    private final List<AppEntry> apps = new ArrayList<>();
    private AppAdapter adapter;
    private Slot pickSlot;
    private volatile boolean destroyed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);
        pickSlot = Slot.from(getIntent().getStringExtra(EXTRA_PICK_SLOT));

        TextView header = findViewById(R.id.header);
        header.setText(pickSlot == null ? getString(R.string.all_apps)
                : getString(R.string.pick_for, pickSlot.label));
        findViewById(R.id.back).setOnClickListener(v -> finish());

        GridView grid = findViewById(R.id.grid);
        adapter = new AppAdapter();
        grid.setAdapter(adapter);
        grid.setOnItemClickListener((parent, view, position, id) -> onPick(apps.get(position)));
        loadApps();
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        super.onDestroy();
    }

    private void loadApps() {
        final PackageManager pm = getPackageManager();
        final String self = getPackageName();
        new Thread(() -> {
            Intent q = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> found = pm.queryIntentActivities(q, 0);
            List<AppEntry> out = new ArrayList<>();
            Set<String> seen = new HashSet<>();
            for (ResolveInfo ri : found) {
                String pkg = ri.activityInfo.packageName;
                if (pkg.equals(self) || !seen.add(pkg)) continue;
                out.add(new AppEntry(pkg, String.valueOf(ri.loadLabel(pm)), ri.loadIcon(pm)));
            }
            Collator col = Collator.getInstance(new Locale("es", "MX"));
            out.sort((x, y) -> col.compare(x.label, y.label));
            runOnUiThread(() -> {
                if (destroyed) return;
                apps.clear();
                apps.addAll(out);
                adapter.notifyDataSetChanged();
            });
        }, "apps-loader").start();
    }

    private void onPick(AppEntry e) {
        if (pickSlot != null) {
            Intent r = new Intent();
            r.putExtra(EXTRA_PICK_SLOT, pickSlot.name());
            r.putExtra(EXTRA_PACKAGE, e.pkg);
            setResult(RESULT_OK, r);
            finish();
        } else if (!Launch.launch(this, e.pkg)) {
            Toast.makeText(this, R.string.app_missing, Toast.LENGTH_SHORT).show();
        }
    }

    private final class AppAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return apps.size();
        }

        @Override
        public Object getItem(int position) {
            return apps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = LayoutInflater.from(AppsActivity.this).inflate(R.layout.app_item, parent, false);
            }
            AppEntry e = apps.get(position);
            ((ImageView) v.findViewById(R.id.icon)).setImageDrawable(e.icon);
            ((TextView) v.findViewById(R.id.label)).setText(e.label);
            return v;
        }
    }
}
