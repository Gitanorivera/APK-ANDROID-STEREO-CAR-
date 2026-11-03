package app.autolauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.EnumMap;
import java.util.Map;

/** Ajustes del launcher: saludo, logotipo, color, accesos directos y borrado de datos. */
public class SettingsActivity extends Activity {

    private static final int REQ_LOGO = 20;
    private static final int REQ_PICK = 21;

    private static final int C_BG = 0xFF0B0C0E;
    private static final int C_LINE = 0xFF23262C;
    private static final int C_TEXT = 0xFFF2F3F5;
    private static final int C_TEXT2 = 0xFFA3A9B2;
    private static final int C_DANGER = 0xFFFF6B6B;

    private Prefs prefs;
    private LinearLayout content;
    private EditText nameInput;
    private Switch welcomeSwitch;
    private TextView logoStatus;
    private TextView mediaStatus;
    private LinearLayout swatches;
    private final Map<Slot, TextView> slotValues = new EnumMap<>(Slot.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new Prefs(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(C_BG);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(32), dp(24), dp(32), dp(48));
        scroll.addView(content);
        setContentView(scroll);

        buildHeader();
        buildGreeting();
        buildLogo();
        buildAccent();
        buildShortcuts();
        buildMedia();
        buildPrivacy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    // ---------------------------------------------------------------- secciones

    private void buildHeader() {
        LinearLayout row = hRow();
        ImageButton back = new ImageButton(this);
        back.setImageResource(R.drawable.ic_back);
        back.setImageTintList(android.content.res.ColorStateList.valueOf(C_TEXT));
        back.setBackgroundResource(R.drawable.bg_circle_dark);
        back.setContentDescription(getString(R.string.back));
        back.setOnClickListener(v -> finish());
        row.addView(back, new LinearLayout.LayoutParams(dp(56), dp(56)));
        TextView t = text("Ajustes del inicio", 28, C_TEXT, true);
        LinearLayout.LayoutParams lp = wrap();
        lp.leftMargin = dp(18);
        row.addView(t, lp);
        content.addView(row);
    }

    private void buildGreeting() {
        section("Saludo de bienvenida");
        hint("Aparece al encender el auto. El nombre es opcional: déjalo vacío para un saludo sin nombre.");

        nameInput = new EditText(this);
        nameInput.setSingleLine(true);
        nameInput.setHint("Nombre (opcional)");
        nameInput.setHintTextColor(0xFF6B7280);
        nameInput.setTextColor(C_TEXT);
        nameInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        nameInput.setBackgroundResource(R.drawable.bg_input);
        nameInput.setPadding(dp(16), dp(12), dp(16), dp(12));
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        nameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Prefs.MAX_NAME)});
        nameInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        nameInput.setText(prefs.name());
        nameInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveName();
                return true;
            }
            return false;
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(420), dp(56));
        lp.topMargin = dp(12);
        content.addView(nameInput, lp);

        LinearLayout row = hRow();
        row.addView(button("Guardar nombre", v -> saveName()));
        row.addView(button("Quitar nombre", v -> {
            prefs.setName("");
            nameInput.setText("");
            hideKeyboard();
            toast("Saludo sin nombre");
        }), spaced());
        content.addView(row, topMargin(12));

        welcomeSwitch = new Switch(this);
        welcomeSwitch.setText("Mostrar bienvenida al encender");
        welcomeSwitch.setTextColor(C_TEXT);
        welcomeSwitch.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        welcomeSwitch.setMinHeight(dp(56));
        welcomeSwitch.setOnCheckedChangeListener((b, on) -> prefs.setWelcomeEnabled(on));
        content.addView(welcomeSwitch, new LinearLayout.LayoutParams(dp(420), dp(56)));

        content.addView(button("Probar bienvenida", v -> {
            if (!prefs.welcomeEnabled()) {
                toast("Activa primero la bienvenida");
                return;
            }
            prefs.requestPreview();
            finish();
        }), topMargin(8));
    }

    private void buildLogo() {
        section("Logotipo de la bienvenida");
        logoStatus = text("", 15, C_TEXT2, false);
        content.addView(logoStatus, topMargin(4));
        LinearLayout row = hRow();
        row.addView(button("Elegir imagen", v -> pickLogo()));
        row.addView(button("Quitar logotipo", v -> {
            LogoStore.delete(this);
            refresh();
            toast("Logotipo quitado");
        }), spaced());
        content.addView(row, topMargin(12));
    }

    private void buildAccent() {
        section("Color de acento");
        swatches = hRow();
        for (int i = 0; i < Prefs.ACCENTS.length; i++) {
            final int idx = i;
            View sw = new View(this);
            sw.setContentDescription("Color " + (i + 1));
            sw.setClickable(true);
            sw.setFocusable(true);
            sw.setOnClickListener(v -> {
                prefs.setAccentIndex(idx);
                refresh();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(56), dp(56));
            if (i > 0) lp.leftMargin = dp(16);
            swatches.addView(sw, lp);
        }
        content.addView(swatches, topMargin(12));
    }

    private void buildShortcuts() {
        section("Accesos directos");
        hint("También puedes mantener presionado un botón del inicio para cambiar su app.");
        for (Slot s : Slot.values()) {
            LinearLayout row = hRow();
            row.setMinimumHeight(dp(64));
            TextView name = text(s.label, 17, C_TEXT, true);
            row.addView(name, new LinearLayout.LayoutParams(dp(170), LinearLayout.LayoutParams.WRAP_CONTENT));
            TextView value = text("", 16, C_TEXT2, false);
            slotValues.put(s, value);
            row.addView(value, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(button("Cambiar", v -> {
                Intent i = new Intent(this, AppsActivity.class);
                i.putExtra(AppsActivity.EXTRA_PICK_SLOT, s.name());
                startActivityForResult(i, REQ_PICK);
            }));
            row.addView(button("Automático", v -> {
                prefs.setShortcut(s, null);
                final android.content.Context app = getApplicationContext();
                new Thread(() -> {
                    Defaults.assignMissing(app, prefs);
                    runOnUiThread(() -> {
                        if (!isFinishing() && !isDestroyed()) refresh();
                    });
                }, "defaults").start();
            }), spaced());
            content.addView(row, topMargin(8));
        }
    }

    private void buildMedia() {
        section("Canción en pantalla");
        hint("Android pide el permiso \"Acceso a notificaciones\" para leer qué canción suena. "
                + "Esta app no lee tus notificaciones y no tiene internet.");
        Switch direct = new Switch(this);
        direct.setText("Mandar los botones de música directo a ZLink");
        direct.setTextColor(C_TEXT);
        direct.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        direct.setChecked(prefs.mediaDirect());
        direct.setOnCheckedChangeListener((b, on) -> prefs.setMediaDirect(on));
        content.addView(direct, new LinearLayout.LayoutParams(dp(520), dp(56)));
        hint("Si los botones de la pantalla no cambian la canción, prueba apagando o encendiendo esta opción.");
        mediaStatus = text("", 15, C_TEXT2, false);
        content.addView(mediaStatus, topMargin(8));
        LinearLayout row = hRow();
        row.addView(button("Abrir permiso", v -> {
            try {
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
            } catch (ActivityNotFoundException e) {
                toast("Búscalo en Ajustes de Android > Acceso a notificaciones");
            }
        }));
        row.addView(button("Ajustes de Android", v -> {
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            } catch (ActivityNotFoundException e) {
                toast("No se pudieron abrir los ajustes");
            }
        }), spaced());
        content.addView(row, topMargin(12));
    }

    private void buildPrivacy() {
        section("Privacidad");
        hint("Esta app no tiene permiso de internet: nada sale de la pantalla. "
                + "Tu nombre, logotipo, accesos y los totales del viaje se guardan solo aquí y no se respaldan. "
                + "El GPS se usa solo para velocidad, distancia y altitud; no se guarda ninguna ubicación.");
        TextView wipe = button("Borrar mis datos (para vender el auto)", v -> confirmWipe());
        wipe.setTextColor(C_DANGER);
        content.addView(wipe, topMargin(16));
    }

    // ---------------------------------------------------------------- acciones

    private void saveName() {
        String clean = Prefs.sanitizeName(nameInput.getText().toString());
        if (clean == null) {
            nameInput.setError("Usa solo letras y espacios (máximo " + Prefs.MAX_NAME + ")");
            return;
        }
        prefs.setName(clean);
        nameInput.setText(clean);
        nameInput.setSelection(clean.length());
        hideKeyboard();
        toast(clean.isEmpty() ? "Saludo sin nombre" : "Nombre guardado");
    }

    private void pickLogo() {
        Intent open = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE).setType("image/*");
        try {
            startActivityForResult(open, REQ_LOGO);
            return;
        } catch (ActivityNotFoundException ignored) {
            // Algunas pantallas no traen el selector de documentos.
        }
        Intent get = new Intent(Intent.ACTION_GET_CONTENT)
                .addCategory(Intent.CATEGORY_OPENABLE).setType("image/*");
        try {
            startActivityForResult(get, REQ_LOGO);
        } catch (ActivityNotFoundException e) {
            toast("No hay un explorador de archivos instalado");
        }
    }

    private void confirmWipe() {
        new AlertDialog.Builder(this)
                .setTitle("¿Borrar tus datos?")
                .setMessage("Se borran el nombre del saludo, el logotipo, el color, los accesos directos y los datos del viaje. "
                        + "No se puede deshacer.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Borrar", (d, w) -> {
                    prefs.clearAll();
                    Trip.I.reset(prefs);
                    LogoStore.delete(this);
                    nameInput.setText("");
                    refresh();
                    toast("Datos borrados");
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;
        if (requestCode == REQ_LOGO) {
            final Uri uri = data.getData();
            if (uri == null) return;
            logoStatus.setText("Procesando imagen…");
            final Context app = getApplicationContext();
            new Thread(() -> {
                boolean ok = LogoStore.importFrom(app, uri);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    toast(ok ? "Logotipo guardado" : "No se pudo usar esa imagen");
                    refresh();
                });
            }, "logo-import").start();
        } else if (requestCode == REQ_PICK) {
            Slot s = Slot.from(data.getStringExtra(AppsActivity.EXTRA_PICK_SLOT));
            String pkg = data.getStringExtra(AppsActivity.EXTRA_PACKAGE);
            if (s != null && Launch.isLaunchable(this, pkg)) {
                prefs.setShortcut(s, pkg);
                refresh();
            }
        }
    }

    private void refresh() {
        if (!nameInput.hasFocus()) nameInput.setText(prefs.name());
        welcomeSwitch.setChecked(prefs.welcomeEnabled());
        logoStatus.setText(LogoStore.exists(this) ? "Logotipo cargado" : "Sin logotipo");

        int selected = prefs.accentIndex();
        for (int i = 0; i < swatches.getChildCount(); i++) {
            GradientDrawable g = new GradientDrawable();
            g.setShape(GradientDrawable.OVAL);
            g.setColor(Prefs.ACCENTS[i]);
            if (i == selected) g.setStroke(dp(4), C_TEXT);
            View sw = swatches.getChildAt(i);
            sw.setBackground(g);
            sw.setSelected(i == selected);
        }

        for (Slot s : Slot.values()) {
            CharSequence label = Launch.label(this, prefs.shortcut(s));
            TextView v = slotValues.get(s);
            if (v != null) v.setText(label == null ? "Sin asignar" : label);
        }

        mediaStatus.setText(MediaListenerService.isEnabled(this) ? "Permiso activo: la canción se muestra en el inicio"
                : "Permiso apagado: los botones funcionan, pero no se ve la canción");
    }

    // ---------------------------------------------------------------- ayudantes de UI

    private void section(String title) {
        TextView t = text(title, 20, C_TEXT, true);
        content.addView(t, topMargin(32));
        View line = new View(this);
        line.setBackgroundColor(C_LINE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
        lp.topMargin = dp(8);
        content.addView(line, lp);
    }

    private void hint(String s) {
        TextView t = text(s, 15, C_TEXT2, false);
        t.setLineSpacing(0, 1.2f);
        content.addView(t, topMargin(10));
    }

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(color);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private TextView button(String label, View.OnClickListener click) {
        TextView b = text(label, 16, C_TEXT, true);
        b.setGravity(Gravity.CENTER);
        b.setBackgroundResource(R.drawable.bg_button);
        b.setPadding(dp(20), 0, dp(20), 0);
        b.setMinHeight(dp(52));
        b.setClickable(true);
        b.setFocusable(true);
        b.setOnClickListener(click);
        return b;
    }

    private LinearLayout hRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        return row;
    }

    private LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams spaced() {
        LinearLayout.LayoutParams lp = wrap();
        lp.leftMargin = dp(12);
        return lp;
    }

    private LinearLayout.LayoutParams topMargin(int top) {
        LinearLayout.LayoutParams lp = wrap();
        lp.topMargin = dp(top);
        return lp;
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(nameInput.getWindowToken(), 0);
        nameInput.clearFocus();
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private int dp(int v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics()));
    }
}
