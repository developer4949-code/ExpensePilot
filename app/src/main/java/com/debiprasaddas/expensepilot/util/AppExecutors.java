package com.debiprasaddas.expensepilot.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppExecutors {

    private static final ExecutorService DATABASE_EXECUTOR = Executors.newSingleThreadExecutor();

    private AppExecutors() {
    }

    public static ExecutorService database() {
        return DATABASE_EXECUTOR;
    }
}
