package app.autolauncher;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Pantalla de inicio V2: menú lateral, Inicio (hora, Android Auto, música) y Tablero GPS. */
public class MainActivity extends Activity implements Trip.Listener {

    private static final int REQ_PICK = 10;
    private static final int REQ_LOCATION = 11;
    private static final long WELCOME_AFTER_OFF_MS = 5 * 60 * 1000L;
    private static final long WELCOME_DURATION_MS = 3500L;
    private static final Locale ES = new Locale("es", "MX");
    private static final int C_TEXT = 0xFFE8ECEF;
    private static final int C_TEXT2 = 0xFFAEB7C0;
    private static final int C_TEXT3 = 0xFF8B95A0;
    private static final int C_OK = 0xFF34D399;
    private static final int C_WARN = 0xFFFFB020;

    /** La bienvenida se muestra una vez por arranque del sistema/proceso. */
    private static boolean sWelcomeShown;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Prefs prefs;
    private MediaSessionManager sessionManager;
    private MediaController controller;
    private boolean sessionsListening;
    private boolean resumed;
    private boolean receiverRegistered;
    private boolean boardVisible;
    private long screenOffAt;
    private long durationMs;

    private TextView clock, date, autoSub, sideStatus, homeSpeed, homeSpeedUnit, boardClock;
    private TextView title, artist, source, pos, dur, mediaAccess;
    private TextView tripDist, tripTime, tripAvg, tripMax, tripAlt, gpsState, gpsGrant;
    private View autoButton, sourceDot, progressGroup, panelHome, panelBoard;
    private ImageView art, coverIcon, statusBt, statusWifi, statusGps, sideLogo;
    private ImageButton play;
    private ProgressBar progress;
    private GaugeView gauge;
    private View navHome, navBoard;
    private View welcome, welcomeBar;
    private TextView welcomeTitle, welcomeDate;
    private ImageView welcomeLogo;

    // ---------------------------------------------------------------- ciclo de vida

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = new Prefs(this);
        Trip.I.load(prefs);
        sessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        bindViews();
        buildMenus();
        registerSystemReceiver();
        assignDefaultApps();
        if (!sWelcomeShown) {
            sWelcomeShown = true;
            showWelcome();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumed = true;
        applyAccent();
        updateClock();
        updateStatus();
        updateShortcutLabels();
        loadSideLogo();
        attachMedia();
        TripService.start(this);
        Trip.I.addListener(this);
        onTripUpdate();
        handler.removeCallbacks(gpsTick);
        handler.postDelayed(gpsTick, 2000);
        if (prefs.consumePreview()) showWelcome();
    }

    @Override
    protected void onPause() {
        super.onPause();
        resumed = false;
        handler.removeCallbacks(progressTick);
        handler.removeCallbacks(gpsTick);
        Trip.I.removeListener(this);
        detachMedia();
    }

    @Override
    protected void onDestroy() {
        if (receiverRegistered) {
            unregisterReceiver(systemReceiver);
            receiverRegistered = false;
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // Es la pantalla de inicio: "atrás" solo cierra la bienvenida o regresa del tablero.
        if (welcome.getVisibility() == View.VISIBLE) hideWelcome();
        else if (boardVisible) showBoard(false);
    }

    // ---------------------------------------------------------------- vistas

    private void bindViews() {
        clock = findViewById(R.id.clock);
        date = findViewById(R.id.date);
        boardClock = findViewById(R.id.board_clock);
        statusBt = findViewById(R.id.status_bt);
        statusWifi = findViewById(R.id.status_wifi);
        statusGps = findViewById(R.id.status_gps);
        sideStatus = findViewById(R.id.side_status);
        sideLogo = findViewById(R.id.side_logo);
        autoButton = findViewById(R.id.auto_button);
        autoSub = findViewById(R.id.auto_sub);
        homeSpeed = findViewById(R.id.home_speed);
        homeSpeedUnit = findViewById(R.id.home_speed_unit);
        coverIcon = findViewById(R.id.cover_icon);
        art = findViewById(R.id.art);
        art.setClipToOutline(true);
        source = findViewById(R.id.source);
        sourceDot = findViewById(R.id.source_dot);
        title = findViewById(R.id.title);
        artist = findViewById(R.id.artist);
        mediaAccess = findViewById(R.id.media_access);
        progressGroup = findViewById(R.id.progress_group);
        progress = findViewById(R.id.progress);
        pos = findViewById(R.id.pos);
        dur = findViewById(R.id.dur);
        play = findViewById(R.id.play);
        panelHome = findViewById(R.id.panel_home);
        panelBoard = findViewById(R.id.panel_board);
        gauge = findViewById(R.id.gauge);
        tripDist = findViewById(R.id.trip_dist);
        tripTime = findViewById(R.id.trip_time);
        tripAvg = findViewById(R.id.trip_avg);
        tripMax = findViewById(R.id.trip_max);
        tripAlt = findViewById(R.id.trip_alt);
        gpsState = findViewById(R.id.gps_state);
        gpsGrant = findViewById(R.id.gps_grant);
        welcome = findViewById(R.id.welcome);
        welcomeBar = findViewById(R.id.welcome_bar);
        welcomeTitle = findViewById(R.id.welcome_title);
        welcomeDate = findViewById(R.id.welcome_date);
        welcomeLogo = findViewById(R.id.welcome_logo);

        autoButton.setOnClickListener(v -> openSlot(Slot.AUTO));
        autoButton.setOnLongClickListener(longPick(Slot.AUTO));
        findViewById(R.id.prev).setOnClickListener(v -> onPrev());
        findViewById(R.id.next).setOnClickListener(v -> onNext());
        play.setOnClickListener(v -> onPlayPause());
        mediaAccess.setOnClickListener(v -> openNotificationAccess());
        welcome.setOnClickListener(v -> hideWelcome());
        findViewById(R.id.speed_row).setOnClickListener(v -> {
            if (!TripService.hasPermission(this)) requestLocation();
            else showBoard(true);
        });
        gpsGrant.setOnClickListener(v -> requestLocation());
        findViewById(R.id.trip_reset).setOnClickListener(v -> {
            Trip.I.reset(prefs);
            Toast.makeText(this, "Viaje reiniciado", Toast.LENGTH_SHORT).show();
        });
    }

    private void buildMenus() {
        LinearLayout side = findViewById(R.id.sidebar);
        navHome = addNavItem(side, R.drawable.ic_home, R.string.nav_home, v -> {
            hideWelcome();
            showBoard(false);
        }, null);
        navBoard = addNavItem(side, R.drawable.ic_gauge, R.string.nav_board, v -> {
            showBoard(true);
            if (!TripService.hasPermission(this)) requestLocation();
        }, null);
        addNavItem(side, R.drawable.ic_car, R.string.nav_auto, v -> openSlot(Slot.AUTO), longPick(Slot.AUTO));
        addNavItem(side, R.drawable.ic_music, R.string.nav_music, v -> openSlot(Slot.MUSIC), longPick(Slot.MUSIC));
        addNavItem(side, R.drawable.ic_phone, R.string.nav_phone, v -> openSlot(Slot.PHONE), longPick(Slot.PHONE));
        addNavItem(side, R.drawable.ic_apps, R.string.nav_apps,
                v -> startActivity(new Intent(this, AppsActivity.class)), null);
        addNavItem(side, R.drawable.ic_settings, R.string.nav_settings,
                v -> startActivity(new Intent(this, SettingsActivity.class)), null);

        LinearLayout quick = findViewById(R.id.quick);
        addQuickItem(quick, R.drawable.ic_radio, R.string.q_radio, Slot.RADIO);
        addQuickItem(quick, R.drawable.ic_bt, R.string.q_bt, Slot.BLUETOOTH);
        addQuickItem(quick, R.drawable.ic_eq, R.string.q_eq, Slot.EQ);
        addQuickItem(quick, R.drawable.ic_vehicle, R.string.q_vehicle, Slot.VEHICLE);
    }

    private View.OnLongClickListener longPick(Slot slot) {
        return v -> {
            pickFor(slot);
            return true;
        };
    }

    private View addNavItem(LinearLayout side, int icon, int label, View.OnClickListener click,
                            View.OnLongClickListener longClick) {
        View item = LayoutInflater.from(this).inflate(R.layout.side_item, side, false);
        ((ImageView) item.findViewById(R.id.icon)).setImageResource(icon);
        ((TextView) item.findViewById(R.id.label)).setText(label);
        item.setContentDescription(getString(label));
        item.setOnClickListener(click);
        if (longClick != null) item.setOnLongClickListener(longClick);
        side.addView(item);
        return item;
    }

    private void addQuickItem(LinearLayout quick, int icon, int label, Slot slot) {
        View item = LayoutInflater.from(this).inflate(R.layout.quick_item, quick, false);
        ((ImageView) item.findViewById(R.id.icon)).setImageResource(icon);
        ((TextView) item.findViewById(R.id.label)).setText(label);
        item.setContentDescription(getString(label));
        item.setOnClickListener(v -> openSlot(slot));
        item.setOnLongClickListener(longPick(slot));
        if (quick.getChildCount() > 0) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) item.getLayoutParams();
            lp.setMarginStart(dp(12));
            item.setLayoutParams(lp);
        }
        quick.addView(item);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private void showBoard(boolean board) {
        boardVisible = board;
        panelHome.setVisibility(board ? View.GONE : View.VISIBLE);
        panelBoard.setVisibility(board ? View.VISIBLE : View.GONE);
        applyAccent();
        onTripUpdate();
    }

    private void styleNav(View item, boolean active, int accent) {
        if (item == null) return;
        item.setBackgroundResource(active ? R.drawable.bg_side_active : R.drawable.bg_side_item);
        ((ImageView) item.findViewById(R.id.icon)).setImageTintList(ColorStateList.valueOf(active ? accent : C_TEXT2));
        ((TextView) item.findViewById(R.id.label)).setTextColor(active ? accent : C_TEXT2);
    }

    private void applyAccent() {
        int a = prefs.accent();
        ColorStateList csl = ColorStateList.valueOf(a);
        autoButton.setBackgroundTintList(csl);
        coverIcon.setImageTintList(csl);
        sourceDot.setBackgroundTintList(csl);
        welcomeBar.setBackgroundTintList(csl);
        progress.setProgressTintList(csl);
        gauge.setAccent(a);
        styleNav(navHome, !boardVisible, a);
        styleNav(navBoard, boardVisible, a);
    }

    private void loadSideLogo() {
        Bitmap logo = LogoStore.load(this);
        if (logo != null) {
            sideLogo.setImageBitmap(logo);
            sideLogo.setVisibility(View.VISIBLE);
        } else {
            sideLogo.setImageDrawable(null);
            sideLogo.setVisibility(View.GONE);
        }
    }

    private void assignDefaultApps() {
        final Context app = getApplicationContext();
        new Thread(() -> {
            Defaults.assignMissing(app, prefs);
            runOnUiThread(() -> {
                if (!isFinishing() && !isDestroyed()) updateShortcutLabels();
            });
        }, "defaults").start();
    }

    // ---------------------------------------------------------------- reloj y estado

    private void updateClock() {
        Date now = new Date();
        boolean is24 = android.text.format.DateFormat.is24HourFormat(this);
        String time = new SimpleDateFormat(is24 ? "HH:mm" : "h:mm", ES).format(now);
        clock.setText(time);
        boardClock.setText(time);
        date.setText(new SimpleDateFormat("EEEE d 'de' MMMM", ES).format(now).toUpperCase(ES));
    }

    private void updateStatus() {
        boolean btOn = false;
        boolean btKnown = true;
        try {
            BluetoothAdapter ad = BluetoothAdapter.getDefaultAdapter();
            if (ad == null) btKnown = false;
            else btOn = ad.isEnabled();
        } catch (SecurityException e) {
            btKnown = false;
        }
        statusBt.setVisibility(btKnown ? View.VISIBLE : View.GONE);
        statusBt.setAlpha(btOn ? 1f : 0.35f);
        sideStatus.setVisibility(btKnown ? View.VISIBLE : View.GONE);
        sideStatus.setText(btOn ? R.string.bt_on_status : R.string.bt_off_status);
        sideStatus.setTextColor(btOn ? C_OK : C_TEXT3);
        sideStatus.setCompoundDrawableTintList(ColorStateList.valueOf(btOn ? C_OK : C_TEXT3));

        try {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            statusWifi.setVisibility(wm == null ? View.GONE : View.VISIBLE);
            statusWifi.setAlpha(wm != null && wm.isWifiEnabled() ? 1f : 0.35f);
        } catch (SecurityException e) {
            statusWifi.setVisibility(View.GONE);
        }
    }

    private final BroadcastReceiver systemReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            String a = i.getAction();
            if (a == null) return;
            switch (a) {
                case Intent.ACTION_TIME_TICK:
                case Intent.ACTION_TIME_CHANGED:
                case Intent.ACTION_TIMEZONE_CHANGED:
                    updateClock();
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    screenOffAt = SystemClock.elapsedRealtime();
                    break;
                case Intent.ACTION_SCREEN_ON:
                    if (screenOffAt > 0
                            && SystemClock.elapsedRealtime() - screenOffAt >= WELCOME_AFTER_OFF_MS) {
                        showWelcome();
                    }
                    screenOffAt = 0;
                    break;
                default:
                    updateStatus();
            }
        }
    };

    private void registerSystemReceiver() {
        // Solo transmisiones del sistema (ninguna otra app puede enviarlas).
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_TIME_TICK);
        f.addAction(Intent.ACTION_TIME_CHANGED);
        f.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        f.addAction(Intent.ACTION_SCREEN_OFF);
        f.addAction(Intent.ACTION_SCREEN_ON);
        f.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        f.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(systemReceiver, f);
        receiverRegistered = true;
    }

    // ---------------------------------------------------------------- GPS y tablero

    private void requestLocation() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode != REQ_LOCATION) return;
        if (TripService.hasPermission(this)) {
            TripService.start(this);
            showBoard(true);
        }
        onTripUpdate();
    }

    /** Revisa cada 2 s si se perdió la señal (sin señal no llegan actualizaciones). */
    private final Runnable gpsTick = new Runnable() {
        @Override
        public void run() {
            onTripUpdate();
            if (resumed) handler.postDelayed(this, 2000);
        }
    };

    @Override
    public void onTripUpdate() {
        Trip t = Trip.I;
        boolean perm = TripService.hasPermission(this);
        boolean fix = perm && t.hasFix();

        statusGps.setAlpha(fix ? 1f : 0.35f);
        if (!perm) {
            homeSpeed.setText(R.string.tap_gps);
            homeSpeed.setTextSize(14);
            homeSpeedUnit.setVisibility(View.GONE);
        } else {
            homeSpeed.setText(fix ? String.valueOf(Math.round(t.speedKmh)) : "--");
            homeSpeed.setTextSize(22);
            homeSpeedUnit.setVisibility(View.VISIBLE);
        }
        if (!boardVisible) return;

        String sub = !perm ? "GPS SIN PERMISO" : !fix ? "BUSCANDO SEÑAL"
                : t.hasBearing ? Trip.heading(t.bearing) : "";
        gauge.setSpeed(fix ? t.speedKmh : -1f, sub);
        gauge.setContentDescription(fix ? "Velocidad " + Math.round(t.speedKmh) + " kilómetros por hora" : sub);

        tripDist.setText(String.format(Locale.US, "%.1f", t.distanceM / 1000.0));
        long min = t.movingMs / 60_000;
        tripTime.setText(String.format(Locale.US, "Tiempo: %02d:%02d h", min / 60, min % 60));
        tripAvg.setText(String.valueOf(Math.round(t.avgKmh())));
        tripMax.setText(String.valueOf(Math.round(t.maxKmh)));
        tripAlt.setText(fix && t.hasAltitude ? String.format(Locale.US, "%,d", Math.round(t.altitudeM)) : "--");

        int dot = !perm ? C_TEXT3 : fix ? C_OK : C_WARN;
        gpsState.setText(!perm ? R.string.gps_off : fix ? R.string.gps_ok : R.string.gps_search);
        gpsState.setCompoundDrawableTintList(ColorStateList.valueOf(dot));
        gpsGrant.setVisibility(perm ? View.GONE : View.VISIBLE);
    }

    // ---------------------------------------------------------------- bienvenida

    private final Runnable hideWelcomeRunnable = this::hideWelcome;

    private void showWelcome() {
        if (!prefs.welcomeEnabled()) return;
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = getString(h < 12 ? R.string.good_morning
                : h < 19 ? R.string.good_afternoon : R.string.good_evening);
        String name = prefs.name();
        welcomeTitle.setText(name.isEmpty() ? greeting : getString(R.string.greeting_fmt, greeting, name));

        updateClock();
        welcomeDate.setText(clock.getText() + " · " + date.getText());

        Bitmap logo = LogoStore.load(this);
        if (logo != null) {
            welcomeLogo.setImageBitmap(logo);
            welcomeLogo.setVisibility(View.VISIBLE);
        } else {
            welcomeLogo.setImageDrawable(null);
            welcomeLogo.setVisibility(View.GONE);
        }
        welcome.setVisibility(View.VISIBLE);
        handler.removeCallbacks(hideWelcomeRunnable);
        handler.postDelayed(hideWelcomeRunnable, WELCOME_DURATION_MS);
    }

    private void hideWelcome() {
        handler.removeCallbacks(hideWelcomeRunnable);
        welcome.setVisibility(View.GONE);
        welcomeLogo.setImageDrawable(null);
    }

    // ---------------------------------------------------------------- accesos directos

    private void openSlot(Slot s) {
        String pkg = prefs.shortcut(s);
        if (pkg == null && s == Slot.BLUETOOTH) {
            try {
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                return;
            } catch (ActivityNotFoundException ignored) {
                // Sigue al selector de apps.
            }
        }
        if (pkg == null) {
            pickFor(s);
            return;
        }
        if (!Launch.launch(this, pkg)) {
            Toast.makeText(this, R.string.app_missing, Toast.LENGTH_SHORT).show();
            pickFor(s);
        }
    }

    private void pickFor(Slot s) {
        Intent i = new Intent(this, AppsActivity.class);
        i.putExtra(AppsActivity.EXTRA_PICK_SLOT, s.name());
        startActivityForResult(i, REQ_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQ_PICK || resultCode != RESULT_OK || data == null) return;
        Slot s = Slot.from(data.getStringExtra(AppsActivity.EXTRA_PICK_SLOT));
        String pkg = data.getStringExtra(AppsActivity.EXTRA_PACKAGE);
        if (s != null && Launch.isLaunchable(this, pkg)) {
            prefs.setShortcut(s, pkg);
            updateShortcutLabels();
        }
    }

    private void updateShortcutLabels() {
        CharSequence label = Launch.label(this, prefs.shortcut(Slot.AUTO));
        autoSub.setText(label == null ? getString(R.string.auto_sub_unset)
                : getString(R.string.auto_sub_fmt, label));
        autoButton.setContentDescription(getString(R.string.auto_title) + ". " + autoSub.getText());
    }

    // ---------------------------------------------------------------- música

    private ComponentName listenerComponent() {
        return new ComponentName(this, MediaListenerService.class);
    }

    private boolean hasMediaAccess() {
        ComponentName me = listenerComponent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            return nm != null && nm.isNotificationListenerAccessGranted(me);
        }
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (flat == null) return false;
        for (String part : flat.split(":")) {
            if (me.equals(ComponentName.unflattenFromString(part))) return true;
        }
        return false;
    }

    private void openNotificationAccess() {
        try {
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Busca \"Acceso a notificaciones\" en Ajustes de Android",
                    Toast.LENGTH_LONG).show();
        }
    }

    private final MediaSessionManager.OnActiveSessionsChangedListener sessionsChanged =
            this::pickController;

    private void attachMedia() {
        boolean access = hasMediaAccess();
        mediaAccess.setVisibility(access ? View.GONE : View.VISIBLE);
        if (sessionManager == null || !access) {
            setController(null);
            return;
        }
        try {
            sessionManager.addOnActiveSessionsChangedListener(sessionsChanged, listenerComponent(), handler);
            sessionsListening = true;
            pickController(sessionManager.getActiveSessions(listenerComponent()));
        } catch (SecurityException e) {
            mediaAccess.setVisibility(View.VISIBLE);
            setController(null);
        }
    }

    private void detachMedia() {
        if (sessionsListening && sessionManager != null) {
            try {
                sessionManager.removeOnActiveSessionsChangedListener(sessionsChanged);
            } catch (RuntimeException ignored) {
                // Nada que limpiar.
            }
        }
        sessionsListening = false;
        setController(null);
    }

    private void pickController(List<MediaController> list) {
        MediaController pick = null;
        if (list != null) {
            for (MediaController c : list) {
                PlaybackState st = c.getPlaybackState();
                if (st != null && st.getState() == PlaybackState.STATE_PLAYING) {
                    pick = c;
                    break;
                }
            }
            if (pick == null && !list.isEmpty()) pick = list.get(0);
        }
        if (pick != null && controller != null
                && pick.getSessionToken().equals(controller.getSessionToken())) {
            renderMedia();
            return;
        }
        setController(pick);
    }

    private final MediaController.Callback controllerCallback = new MediaController.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            renderMedia();
        }

        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            renderMedia();
        }

        @Override
        public void onSessionDestroyed() {
            setController(null);
            if (sessionsListening && sessionManager != null) {
                try {
                    pickController(sessionManager.getActiveSessions(listenerComponent()));
                } catch (SecurityException ignored) {
                    // Sin acceso: se queda en "Sin reproducción".
                }
            }
        }
    };

    private void setController(MediaController c) {
        if (controller != null) controller.unregisterCallback(controllerCallback);
        controller = c;
        if (c != null) c.registerCallback(controllerCallback, handler);
        renderMedia();
    }

    private void renderMedia() {
        handler.removeCallbacks(progressTick);
        if (controller == null) {
            boolean zlink = prefs.shortcut(Slot.AUTO) != null;
            title.setText(zlink ? getString(R.string.zlink_title) : getString(R.string.no_media_title));
            artist.setText(R.string.no_media_sub);
            source.setText(zlink ? "ANDROID AUTO" : getString(R.string.source_none).toUpperCase(ES));
            art.setImageDrawable(null);
            art.setVisibility(View.GONE);
            progressGroup.setVisibility(View.INVISIBLE);
            setPlayIcon(false);
            return;
        }

        MediaMetadata m = controller.getMetadata();
        CharSequence t = null, a = null;
        Bitmap bmp = null;
        long d = 0;
        if (m != null) {
            t = firstText(m.getText(MediaMetadata.METADATA_KEY_TITLE),
                    m.getText(MediaMetadata.METADATA_KEY_DISPLAY_TITLE));
            a = firstText(m.getText(MediaMetadata.METADATA_KEY_ARTIST),
                    m.getText(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE),
                    m.getText(MediaMetadata.METADATA_KEY_ALBUM_ARTIST));
            bmp = m.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
            if (bmp == null) bmp = m.getBitmap(MediaMetadata.METADATA_KEY_ART);
            if (bmp == null) bmp = m.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON);
            d = m.getLong(MediaMetadata.METADATA_KEY_DURATION);
        }
        title.setText(TextUtils.isEmpty(t) ? getString(R.string.unknown_title) : t);
        artist.setText(TextUtils.isEmpty(a) ? "" : a);

        CharSequence app = Launch.label(this, controller.getPackageName());
        source.setText(app == null ? getString(R.string.source_none).toUpperCase(ES)
                : app.toString().toUpperCase(ES));

        if (bmp != null) {
            art.setImageBitmap(bmp);
            art.setVisibility(View.VISIBLE);
        } else {
            art.setImageDrawable(null);
            art.setVisibility(View.GONE);
        }

        PlaybackState st = controller.getPlaybackState();
        boolean playing = st != null && st.getState() == PlaybackState.STATE_PLAYING;
        setPlayIcon(playing);

        durationMs = d;
        if (d > 0 && st != null) {
            progressGroup.setVisibility(View.VISIBLE);
            updateProgress();
            if (playing && resumed) handler.postDelayed(progressTick, 1000);
        } else {
            progressGroup.setVisibility(View.INVISIBLE);
        }
    }

    private static CharSequence firstText(CharSequence... options) {
        for (CharSequence o : options) if (!TextUtils.isEmpty(o)) return o;
        return null;
    }

    private void setPlayIcon(boolean playing) {
        play.setImageResource(playing ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    private final Runnable progressTick = new Runnable() {
        @Override
        public void run() {
            updateProgress();
            PlaybackState st = controller == null ? null : controller.getPlaybackState();
            if (resumed && st != null && st.getState() == PlaybackState.STATE_PLAYING) {
                handler.postDelayed(this, 1000);
            }
        }
    };

    private void updateProgress() {
        if (controller == null || durationMs <= 0) return;
        PlaybackState st = controller.getPlaybackState();
        if (st == null) return;
        long p = st.getPosition();
        if (st.getState() == PlaybackState.STATE_PLAYING && st.getLastPositionUpdateTime() > 0) {
            p += (long) ((SystemClock.elapsedRealtime() - st.getLastPositionUpdateTime())
                    * st.getPlaybackSpeed());
        }
        p = Math.max(0, Math.min(p, durationMs));
        progress.setProgress((int) (p * 1000 / durationMs));
        pos.setText(formatTime(p));
        dur.setText(formatTime(durationMs));
    }

    private static String formatTime(long ms) {
        long s = ms / 1000;
        long h = s / 3600, m = (s % 3600) / 60, sec = s % 60;
        return h > 0 ? String.format(Locale.US, "%d:%02d:%02d", h, m, sec)
                : String.format(Locale.US, "%d:%02d", m, sec);
    }

    private void onPrev() {
        if (controller != null) controller.getTransportControls().skipToPrevious();
        else sendMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
    }

    private void onNext() {
        if (controller != null) controller.getTransportControls().skipToNext();
        else sendMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT);
    }

    private void onPlayPause() {
        if (controller != null) {
            PlaybackState st = controller.getPlaybackState();
            if (st != null && st.getState() == PlaybackState.STATE_PLAYING) {
                controller.getTransportControls().pause();
            } else {
                controller.getTransportControls().play();
            }
        } else {
            sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
        }
    }

    /**
     * Sin información de la canción (caso ZLink): manda el botón multimedia directo a la app
     * de Android Auto elegida. Si no hay, usa la vía estándar del sistema.
     */
    private void sendMediaKey(int code) {
        String target = prefs.shortcut(Slot.AUTO);
        long t = SystemClock.uptimeMillis();
        if (prefs.mediaDirect() && target != null && Launch.label(this, target) != null) {
            for (int action : new int[]{KeyEvent.ACTION_DOWN, KeyEvent.ACTION_UP}) {
                Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON).setPackage(target);
                i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(t, t, action, code, 0));
                sendBroadcast(i);
            }
            return;
        }
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am == null) return;
        am.dispatchMediaKeyEvent(new KeyEvent(t, t, KeyEvent.ACTION_DOWN, code, 0));
        am.dispatchMediaKeyEvent(new KeyEvent(t, t, KeyEvent.ACTION_UP, code, 0));
    }
}
