package com.pixeldiagnostic;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.DecimalFormat;

public class MainActivity extends Activity {
    private static final int CREATE_REPORT = 71;
    private String latestReport = "";
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showScreen();
    }

    private void showScreen() {
        int pad = (int) (18 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        root.setBackgroundColor(0xFF101820);

        TextView title = new TextView(this);
        title.setText("PIXEL DIAGNOSTIC");
        title.setTextColor(0xFF65E6C4);
        title.setTextSize(25);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, pad, 0, pad / 2);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("فحص معلومات الجهاز وأدائه");
        subtitle.setTextColor(0xFFB8C7D1);
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, pad);
        root.addView(subtitle);

        TextView report = new TextView(this);
        report.setTextColor(0xFFEAF2F5);
        report.setTextSize(15);
        report.setTextIsSelectable(true);
        report.setPadding(pad / 2, pad, pad / 2, pad);
        report.setText(buildReport());
        latestReport = report.getText().toString();

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(report);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        root.addView(scroll, scrollParams);

        Button refresh = new Button(this);
        refresh.setText("إعادة الفحص");
        refresh.setOnClickListener(v -> {
            report.setText(buildReport());
            latestReport = report.getText().toString();
        });
        root.addView(refresh);

        Button save = new Button(this);
        save.setText("حفظ التقرير TXT");
        save.setOnClickListener(v -> saveReport());
        root.addView(save);

        TextView note = new TextView(this);
        note.setText("ملاحظة: أندرويد يمنع التطبيقات العادية من قراءة كثير من سجلات النظام المحمية. هذا الفحص لا يغيّر إعدادات الجهاز ولا يحذف ملفات.");
        note.setTextColor(0xFF91A5B2);
        note.setTextSize(12);
        note.setPadding(0, pad / 2, 0, 0);
        root.addView(note);

        setContentView(root);
    }

    private String buildReport() {
        StringBuilder s = new StringBuilder();
        s.append("PIXEL DIAGNOSTIC REPORT\n");
        s.append("=======================\n\n");
        s.append("الشركة: ").append(Build.MANUFACTURER).append("\n");
        s.append("الجهاز: ").append(Build.MODEL).append("\n");
        s.append("إصدار أندرويد: ").append(Build.VERSION.RELEASE).append("\n");
        s.append("SDK: ").append(Build.VERSION.SDK_INT).append("\n");
        s.append("إصدار النظام: ").append(Build.DISPLAY).append("\n\n");

        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        s.append("RAM الكلية التقريبية: ").append(formatBytes(mi.totalMem)).append("\n");
        s.append("RAM المتاحة حالياً: ").append(formatBytes(mi.availMem)).append("\n");
        s.append("ضغط الذاكرة: ").append(mi.lowMemory ? "النظام أبلغ عن انخفاض الذاكرة" : "لا يوجد تحذير انخفاض ذاكرة الآن").append("\n\n");

        try {
            StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
            long total = stat.getTotalBytes();
            long free = stat.getAvailableBytes();
            s.append("مساحة التخزين الكلية: ").append(formatBytes(total)).append("\n");
            s.append("المساحة المتاحة: ").append(formatBytes(free)).append("\n");
        } catch (Exception e) {
            s.append("تعذر قراءة مساحة التخزين: ").append(e.getClass().getSimpleName()).append("\n");
        }

        s.append("\nمدة تشغيل الجهاز: ").append(android.os.SystemClock.elapsedRealtime() / 60000).append(" دقيقة\n");
        s.append("\nملاحظات مهمة:\n");
        s.append("- هذه قراءة لحالة الجهاز وقت الفحص فقط.\n");
        s.append("- لا يمكن لهذا التطبيق قراءة كل أخطاء Android أو سجلات التطبيقات المحمية.\n");
        s.append("- إذا كان الهاتف يعلق، أعد الفحص أثناء أو بعد حدوث البطء وسجّل وقت المشكلة.\n");
        s.append("- لا يتم حذف أو تعديل أي ملف أو إعداد في الجهاز.\n");
        return s.toString();
    }

    private String formatBytes(long bytes) {
        if (bytes <= 0) return "غير متاح";
        double mb = bytes / 1024.0 / 1024.0;
        if (mb >= 1024) return df.format(mb / 1024.0) + " GB";
        return df.format(mb) + " MB";
    }

    private void saveReport() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, "PixelDiagnostic_Report.txt");
        startActivityForResult(intent, CREATE_REPORT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_REPORT && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try (java.io.OutputStream out = getContentResolver().openOutputStream(data.getData())) {
                out.write(latestReport.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            } catch (Exception e) {
                android.widget.Toast.makeText(this, "تعذر حفظ التقرير", android.widget.Toast.LENGTH_LONG).show();
            }
        }
    }
}
