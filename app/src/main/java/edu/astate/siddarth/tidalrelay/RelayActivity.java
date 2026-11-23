package edu.astate.siddarth.tidalrelay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toolbar;

import java.util.Locale;

import static edu.astate.siddarth.tidalrelay.HarborStyle.*;

public final class RelayActivity extends Activity {
    private Watch watch = new Watch();
    private String operator = "";
    private boolean started;
    private boolean review;
    private boolean visible;
    private boolean modal;
    private boolean practice;
    private long elapsed;
    private long tickStart;
    private float finalHeading;
    private LinearLayout frame;
    private SignalDial dial;
    private TextView metric;
    private TextView channel;
    private TextView outcome;
    private Button control;
    private EditText callsign;
    private SharedPreferences records;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        records = getSharedPreferences("relay_records", MODE_PRIVATE);
        operator = records.getString("operator", "");
        if (state != null) {
            operator = state.getString("operator", operator);
            started = state.getBoolean("started");
            review = state.getBoolean("review");
            practice = state.getBoolean("practice");
            elapsed = state.getLong("elapsed");
            finalHeading = state.getFloat("heading");
            watch = Watch.restore(state.getIntArray("log"));
        }
        render();
    }

    @Override protected void onResume() {
        super.onResume();
        visible = true;
        tickStart = SystemClock.uptimeMillis();
        if (dial != null) dial.postOnAnimation(tick);
    }

    @Override protected void onPause() {
        elapsed = elapsedNow();
        visible = false;
        if (dial != null) dial.removeCallbacks(tick);
        super.onPause();
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString("operator", !started && callsign != null ? callsign.getText().toString() : operator);
        state.putBoolean("started", started);
        state.putBoolean("review", review);
        state.putBoolean("practice", practice);
        state.putLong("elapsed", elapsedNow());
        state.putFloat("heading", finalHeading);
        state.putIntArray("log", watch.snapshot());
    }

    private boolean sweeping() { return started && !review && !modal && !watch.complete(); }

    private long elapsedNow() {
        return visible && sweeping() ? elapsed + SystemClock.uptimeMillis() - tickStart : elapsed;
    }

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (!visible || !sweeping()) return;
            aim(watch.sweep(elapsedNow()), watch.count(), true);
            dial.postOnAnimation(this);
        }
    };

    private void aim(float heading, int channelIndex, boolean canSend) {
        float window = Watch.window(channelIndex, practice);
        dial.aim(heading, Watch.HEADINGS[channelIndex], window, canSend);
    }

    private void render() {
        if (dial != null) dial.removeCallbacks(tick);
        frame = screen(this);
        Toolbar bar = toolbar(this, frame, "Tidal Relay");
        MenuItem help = bar.getMenu().add(0, 1, 0, "Guide");
        help.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        MenuItem restart = bar.getMenu().add(0, 2, 1, "Restart watch");
        restart.setVisible(started);
        bar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) showGuide();
            else confirmRestart();
            return true;
        });

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        frame.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout body = column(this);
        body.setPadding(dp(this, 20), dp(this, 15), dp(this, 20), dp(this, 19));
        scroll.addView(body);
        boolean wide = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                && getResources().getConfiguration().screenWidthDp >= 580;
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(wide ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        stack(body, content, 0);
        LinearLayout chart = column(this);
        LinearLayout controls = wide ? column(this) : chart;
        if (wide) {
            content.addView(chart, new LinearLayout.LayoutParams(0, -2, 1f));
            LinearLayout.LayoutParams right = new LinearLayout.LayoutParams(0, -2, 1f);
            right.leftMargin = dp(this, 20);
            content.addView(controls, right);
        } else {
            stack(content, chart, 0);
        }

        String eyebrow = started ? (practice ? "PRACTICE WATCH" : "HARBOR WATCH") : "LIGHTHOUSE SIGNAL STATION";
        TextView tag = type(this, eyebrow, 11, AQUA, "monospace", 700);
        tag.setLetterSpacing(.15f);
        stack(chart, tag, 0);
        dial = new SignalDial(this);
        dial.setOnClickListener(v -> send());
        if (started) {
            channel = type(this, "", 27, PAPER, "sans-serif", 700);
            if (android.os.Build.VERSION.SDK_INT >= 28) channel.setAccessibilityHeading(true);
            stack(chart, channel, 6);
            metric = type(this, "", 13, MUTED, "monospace", 500);
            stack(chart, metric, 8);
        } else {
            TextView title = type(this, "Signal the harbor", 32, PAPER, "sans-serif", 800);
            if (android.os.Build.VERSION.SDK_INT >= 28) title.setAccessibilityHeading(true);
            stack(chart, title, 6);
        }

        int dialHeight = wide ? 220 : Math.min(275, Math.max(194,
                getResources().getConfiguration().screenHeightDp / (started ? 3 : 4)));
        LinearLayout.LayoutParams dialSize = new LinearLayout.LayoutParams(-1, dp(this, dialHeight));
        dialSize.topMargin = dp(this, 12);
        chart.addView(dial, dialSize);
        if (started) {
            outcome = type(this, "", 15, PAPER, "sans-serif", 500);
            outcome.setMinLines(2);
            outcome.setGravity(Gravity.CENTER_VERTICAL);
            outcome.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
            stack(controls, outcome, wide ? 29 : 9);
            control = button(this, "", true);
            stack(controls, control, 7);
            refresh();
        } else {
            showBriefing(controls, wide);
        }
    }

    private void showBriefing(LinearLayout controls, boolean wide) {
        stack(controls, type(this, "Send eight signals from the lighthouse. Catch six to clear the watch.",
                17, PAPER, "sans-serif", 500), wide ? 30 : 13);
        TextView label = type(this, "RADIO CALL SIGN", 11, AQUA, "monospace", 700);
        label.setLetterSpacing(.12f);
        stack(controls, label, 13);
        callsign = new EditText(this);
        int id = 201;
        callsign.setId(id);
        label.setLabelFor(id);
        callsign.setTextColor(PAPER);
        callsign.setHintTextColor(MUTED);
        callsign.setHint("e.g. Shorebird");
        callsign.setText(operator);
        callsign.setTextSize(16);
        callsign.setSingleLine(true);
        callsign.setSelectAllOnFocus(true);
        callsign.setFilters(new InputFilter[]{new InputFilter.LengthFilter(24)});
        callsign.setPadding(dp(this, 14), dp(this, 9), dp(this, 14), dp(this, 9));
        callsign.setMinHeight(dp(this, 44));
        callsign.setBackground(shape(this, PANEL, 10, LINE));
        stack(controls, callsign, 5);
        controls.setFocusableInTouchMode(true);
        controls.requestFocus();
        Button start = button(this, "Begin watch", true);
        start.setOnClickListener(v -> begin());
        stack(controls, start, 11);
        int best = records.getInt("best", 0);
        if (best > 0) stack(controls, type(this, "Station record  " + best + " pts",
                12, MUTED, "monospace", 500), 10);
    }

    private void begin() {
        operator = callsign.getText().toString().trim();
        if (operator.isEmpty()) operator = "Keeper";
        practice = operator.toLowerCase(Locale.ROOT).equals("low tide");
        if (!practice) records.edit().putString("operator", operator).apply();
        android.view.inputmethod.InputMethodManager keyboard =
                getSystemService(android.view.inputmethod.InputMethodManager.class);
        keyboard.hideSoftInputFromWindow(callsign.getWindowToken(), 0);
        started = true;
        next();
        render();
    }

    private void next() {
        review = false;
        elapsed = 0;
        tickStart = SystemClock.uptimeMillis();
    }

    private void send() {
        if (!visible || !sweeping()) return;
        elapsed = elapsedNow();
        finalHeading = watch.sweep(elapsed);
        Watch.Result result = watch.transmit(finalHeading, practice);
        review = true;
        dial.removeCallbacks(tick);
        if (watch.cleared() && !practice && watch.score() > records.getInt("best", 0)) {
            records.edit().putInt("best", watch.score()).apply();
        }
        refresh();
        dial.acknowledge(result);
    }

    private void refresh() {
        int index = watch.count() - (review ? 1 : 0);
        channel.setText(Watch.CHANNELS[index]);
        metric.setText(String.format(Locale.US, "%d/%d SIGNALS   •   %d CAUGHT   •   %d PTS",
                index + 1, Watch.CHANNELS.length, watch.received(), watch.score()));
        if (review) {
            Watch.Result result = watch.latest();
            String message = result == Watch.Result.MISSED ? "Missed the window" : result.label + "  +" + result.points;
            outcome.setText(message + (watch.complete() ? (watch.cleared() ? " · Watch cleared" : " · Watch missed") : ""));
            outcome.setTextColor(result == Watch.Result.MISSED ? CORAL : AQUA);
            control.setText(watch.complete() ? "Open watch log" : "Tune next channel");
            control.setOnClickListener(v -> {
                if (watch.complete()) showLog();
                else {
                    next();
                    refresh();
                }
            });
            aim(finalHeading, index, false);
        } else {
            outcome.setText("Send when the gold beam crosses the teal arc.");
            outcome.setTextColor(PAPER);
            control.setText("Send signal");
            control.setOnClickListener(v -> send());
            aim(watch.sweep(elapsedNow()), index, true);
            dial.removeCallbacks(tick);
            if (visible) dial.postOnAnimation(tick);
        }
    }

    private void showGuide() {
        pausedDialog(new AlertDialog.Builder(this).setTitle("Keeper’s guide")
                .setMessage("Wait for the gold beam to reach the teal arc. Tap Send signal or tap the chart. A centered shot earns 150 points. Other shots within the arc earn 100. Each channel gets one shot. Catch at least six of eight to clear the watch.")
                .setPositiveButton("Back to station", null));
    }

    private void confirmRestart() {
        pausedDialog(new AlertDialog.Builder(this).setTitle("Restart this watch?")
                .setMessage("Your current watch will be cleared.")
                .setNegativeButton("Keep playing", null)
                .setPositiveButton("Restart", (dialog, option) -> reset()));
    }

    private void pausedDialog(AlertDialog.Builder builder) {
        elapsed = elapsedNow();
        modal = true;
        dial.removeCallbacks(tick);
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(v -> {
            modal = false;
            tickStart = SystemClock.uptimeMillis();
            dial.postOnAnimation(tick);
        });
        dialog.show();
    }

    private void reset() {
        watch = new Watch();
        started = false;
        next();
        render();
    }

    @SuppressWarnings("deprecation")
    private void showLog() {
        Intent log = new Intent(this, LogActivity.class);
        log.putExtra("operator", operator);
        log.putExtra("practice", practice);
        log.putExtra("log", watch.snapshot());
        log.putExtra("best", records.getInt("best", 0));
        startActivityForResult(log, 9);
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == 9 && result == RESULT_OK) reset();
    }
}
