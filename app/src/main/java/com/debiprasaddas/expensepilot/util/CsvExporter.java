package com.debiprasaddas.expensepilot.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.debiprasaddas.expensepilot.data.entity.TransactionEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class CsvExporter {

    private CsvExporter() {
    }

    public static Intent createShareIntent(Context context, List<TransactionEntity> transactions) throws Exception {
        File exportDir = new File(context.getCacheDir(), "exports");
        if (!exportDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            exportDir.mkdirs();
        }

        String fileName = "expensepilot_export_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date()) + ".csv";
        File exportFile = new File(exportDir, fileName);

        StringBuilder csv = new StringBuilder();
        csv.append("Amount,Type,Category,Date,Note\n");
        for (TransactionEntity transaction : transactions) {
            csv.append(transaction.getAmount()).append(',')
                    .append(transaction.getType()).append(',')
                    .append(escape(transaction.getCategory())).append(',')
                    .append(escape(FormatterUtils.fullDate(transaction.getDate()))).append(',')
                    .append(escape(transaction.getNote()))
                    .append('\n');
        }

        try (FileOutputStream outputStream = new FileOutputStream(exportFile)) {
            outputStream.write(csv.toString().getBytes(StandardCharsets.UTF_8));
        }

        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", exportFile);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "ExpensePilot Export");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return Intent.createChooser(shareIntent, "Share finance export");
    }

    private static String escape(String value) {
        String safeValue = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + safeValue + "\"";
    }
}
