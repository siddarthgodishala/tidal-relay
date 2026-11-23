package edu.astate.siddarth.tidalrelay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toolbar;

import static edu.astate.siddarth.tidalrelay.HarborStyle.*;

public final class LogActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        Intent intent = getIntent();
        Watch report = Watch.restore(intent.getIntArrayExtra("log"));
        if (!report.complete()) {
            finish();
            return;
        }

        String operator = intent.getStringExtra("operator");
        boolean practice = intent.getBooleanExtra("practice", false);
        LinearLayout root = screen(this);
        Toolbar bar = toolbar(this, root, "Watch log");
        bar.getMenu().add("Station").setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS);
        bar.setOnMenuItemClickListener(item -> {
            finish();
            return true;
        });

        ScrollView scroll = new ScrollView(this);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout body = column(this);
        body.setPadding(dp(this, 20), dp(this, 17), dp(this, 20), dp(this, 20));
        scroll.addView(body);

        LinearLayout summary = column(this);
        summary.setPadding(dp(this, 17), dp(this, 16), dp(this, 17), dp(this, 18));
        summary.setBackground(shape(this, PANEL, 16, LINE));
        stack(body, summary, 0);

        TextView operatorLabel = type(this, (practice ? "PRACTICE  /  " : "CALL SIGN  /  ") + operator,
                12, MUTED, "monospace", 700);
        stack(summary, operatorLabel, 0);
        String verdict = report.cleared() ? "Watch cleared" : "Watch missed";
        TextView heading = type(this, verdict, 34, report.cleared() ? AQUA : CORAL, "sans-serif", 800);
        if (android.os.Build.VERSION.SDK_INT >= 28) heading.setAccessibilityHeading(true);
        stack(summary, heading, 7);
        String stats = report.received() + " of " + Watch.CHANNELS.length + " caught  ·  " + report.score() + " pts";
        stack(summary, type(this, stats, 17, PAPER, "sans-serif", 600), 8);
        int best = intent.getIntExtra("best", 0);
        if (!practice && best > 0)
            stack(summary, type(this, "Station record  " + best + " pts", 13, MUTED, "monospace", 500), 10);

        LinearLayout actions = new LinearLayout(this);
        Button replay = button(this, "Play again", true);
        Button share = button(this, "Share score", false);
        replay.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });
        share.setOnClickListener(v -> {
            Intent message = new Intent(Intent.ACTION_SEND);
            message.setType("text/plain");
            message.putExtra(Intent.EXTRA_TEXT,
                    "Tidal Relay · " + operator + "\n" + verdict + " · " + stats + (practice ? " (practice)" : ""));
            startActivity(Intent.createChooser(message, "Share watch score"));
        });
        actions.addView(replay, new LinearLayout.LayoutParams(0, -2, 1));
        LinearLayout.LayoutParams shareLayout = new LinearLayout.LayoutParams(0, -2, 1);
        shareLayout.leftMargin = dp(this, 9);
        actions.addView(share, shareLayout);
        stack(body, actions, 12);

        TextView detailTitle = type(this, "Transmissions", 20, PAPER, "sans-serif", 700);
        if (android.os.Build.VERSION.SDK_INT >= 28) detailTitle.setAccessibilityHeading(true);
        stack(body, detailTitle, 20);
        int[] log = report.snapshot();
        for (int index = 0; index < log.length; index++) {
            addLogRow(body, Watch.CHANNELS[index], Watch.Result.values()[log[index]]);
        }
    }

    private void addLogRow(LinearLayout body, String name, Watch.Result result) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(this, 12), dp(this, 9), dp(this, 12), dp(this, 9));
        row.setBackground(shape(this, PANEL, 8, LINE));
        row.addView(type(this, name, 15, PAPER, "sans-serif", 600),
                new LinearLayout.LayoutParams(0, -2, 1));
        TextView status = type(this, result.label, 13, result == Watch.Result.MISSED ? CORAL : AQUA,
                "sans-serif-medium", 500);
        row.addView(status);
        stack(body, row, 6);
    }
}
