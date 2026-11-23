package edu.astate.siddarth.tidalrelay;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;

final class HarborStyle {
    static final int NIGHT = 0xff0b1725;
    static final int PANEL = 0xff14283a;
    static final int LINE = 0xff2d4b5e;
    static final int PAPER = 0xffedf4f1;
    static final int MUTED = 0xffa8c1cd;
    static final int GOLD = 0xffffd585;
    static final int AQUA = 0xff67d9cd;
    static final int CORAL = 0xffff996b;

    static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    static GradientDrawable shape(Context context, int color, int radius, int border) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadius(dp(context, radius));
        if (border != Color.TRANSPARENT) background.setStroke(dp(context, 1), border);
        return background;
    }

    static TextView type(Context context, String words, int size, int color, String font, int weight) {
        TextView label = new TextView(context);
        label.setText(words);
        label.setTextSize(size);
        label.setTextColor(color);
        label.setTypeface(Typeface.create(Typeface.create(font, Typeface.NORMAL), weight, false));
        label.setIncludeFontPadding(false);
        return label;
    }

    static Button button(Context context, String words, boolean primary) {
        Button button = new Button(context);
        button.setText(words);
        button.setTextColor(primary ? NIGHT : PAPER);
        button.setAllCaps(false);
        button.setTextSize(16);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        button.setMinHeight(dp(context, 50));
        button.setPadding(dp(context, 14), dp(context, 9), dp(context, 14), dp(context, 9));
        button.setBackground(new RippleDrawable(ColorStateList.valueOf(0x447e99a8),
                shape(context, primary ? GOLD : PANEL, 12, primary ? Color.TRANSPARENT : LINE), null));
        return button;
    }

    static LinearLayout column(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    static void stack(LinearLayout column, View view, int gap) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(column.getContext(), gap);
        column.addView(view, params);
    }

    static LinearLayout screen(Activity activity) {
        Window window = activity.getWindow();
        if (android.os.Build.VERSION.SDK_INT >= 30) window.setDecorFitsSystemWindows(false);
        LinearLayout base = column(activity);
        base.setBackgroundColor(NIGHT);
        if (android.os.Build.VERSION.SDK_INT >= 30) base.setOnApplyWindowInsetsListener((view, insets) -> {
            android.graphics.Insets bars = insets.getInsets(
                    android.view.WindowInsets.Type.systemBars() | android.view.WindowInsets.Type.displayCutout()
                            | android.view.WindowInsets.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        activity.setContentView(base);
        return base;
    }

    static Toolbar toolbar(Activity activity, LinearLayout base, String title) {
        Toolbar bar = new Toolbar(activity);
        bar.setTitle(title);
        bar.setTitleTextColor(PAPER);
        bar.setBackgroundColor(PANEL);
        base.addView(bar, new LinearLayout.LayoutParams(-1, dp(activity, 54)));
        return bar;
    }
}
