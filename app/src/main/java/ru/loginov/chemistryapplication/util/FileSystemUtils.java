package ru.loginov.chemistryapplication.util;

import android.content.Context;
import android.net.Uri;

import androidx.core.content.FileProvider;

import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import kotlin.Pair;

public class FileSystemUtils {

    private static final Map<Integer, Pair<Uri, File>> tmpFiles = new HashMap<>();
    private static final Object nextIdLock = new Object();
    private static Integer nextId = 1;


    public static Pair<Uri, File> createTmpFile(@NotNull Context context, int fileCode) throws IOException {
        synchronized (tmpFiles) {
            if (tmpFiles.containsKey(fileCode)) {
                return tmpFiles.get(fileCode);
            } else {
                synchronized (nextIdLock) {
                    nextId = Math.max(nextId, fileCode);
                }
            }

            File output = context.getCacheDir();
            try {
                File file = File.createTempFile(RandomStringUtils.randomAlphanumeric(20), RandomStringUtils.randomAlphanumeric(20), output);
                try {
                    Uri uri = FileProvider.getUriForFile(context, "ru.loginov.chemistryapplication", file);

                    Pair<Uri, File> pair = new Pair<>(uri, file);

                    tmpFiles.put(fileCode, pair);

                    return pair;
                } catch (IllegalArgumentException e) {
                    throw new IOException("Can not create uri for file", e);
                }
            } catch (IOException e) {
                throw new IOException("Can not create tmp file", e);
            }
        }
    }

    public static int getNextFileCode() {
        synchronized (nextIdLock) {
            return nextId++;
        }
    }

    public static void clearCache(@NotNull Context context) {
        tmpFiles.clear();

        for (File file : Objects.requireNonNull(context.getCacheDir().listFiles())) {
            file.delete();
        }
    }

}
