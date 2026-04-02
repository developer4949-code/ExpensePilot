package com.debiprasaddas.expensepilot.ui.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.debiprasaddas.expensepilot.R;
import com.debiprasaddas.expensepilot.util.FinanceAnalytics;

import java.util.ArrayList;
import java.util.List;

public class TrendChartView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<FinanceAnalytics.TrendPoint> points = new ArrayList<>();

    public TrendChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        barPaint.setColor(ContextCompat.getColor(context, R.color.chart_one));
        labelPaint.setColor(ContextCompat.getColor(context, R.color.ink_muted));
        labelPaint.setTextSize(sp(12));
        valuePaint.setColor(ContextCompat.getColor(context, R.color.ink));
        valuePaint.setTextSize(sp(11));
    }

    public void setPoints(List<FinanceAnalytics.TrendPoint> chartPoints) {
        points.clear();
        if (chartPoints != null) {
            points.addAll(chartPoints);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (points.isEmpty()) {
            return;
        }

        float width = getWidth();
        float height = getHeight();
        float bottomPadding = dp(28);
        float maxValue = 0f;
        for (FinanceAnalytics.TrendPoint point : points) {
            maxValue = Math.max(maxValue, (float) point.value);
        }
        if (maxValue == 0f) {
            maxValue = 1f;
        }

        float gap = dp(14);
        float totalGap = gap * (points.size() - 1);
        float barWidth = (width - totalGap) / points.size();
        float x = 0;

        for (FinanceAnalytics.TrendPoint point : points) {
            float barHeight = ((float) point.value / maxValue) * (height - bottomPadding - dp(16));
            float top = height - bottomPadding - barHeight;
            canvas.drawRoundRect(x, top, x + barWidth, height - bottomPadding, dp(10), dp(10), barPaint);
            canvas.drawText(point.label, x + dp(2), height - dp(8), labelPaint);
            x += barWidth + gap;
        }
    }

    private float dp(int value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    private float sp(int value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, getResources().getDisplayMetrics());
    }
}
