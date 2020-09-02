package ru.loginov.chemistryapplication.activity.impl;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import kotlin.Pair;
import ru.loginov.chemistryapplication.R;
import ru.loginov.chemistryapplication.util.FileSystemUtils;
import ru.loginov.chemistryapplication.util.PermissionUtils;
import ru.loginov.chemistryapplication.view.ChooserColor;

import static ru.loginov.chemistryapplication.ChemistryApplication.DEFAULT_BUFFER_LENGTH;
import static ru.loginov.chemistryapplication.ChemistryApplication.EXECUTOR;
import static ru.loginov.chemistryapplication.ChemistryApplication.TAG;

public class ChooseColorsActivity extends AppCompatActivity {

    public static final String KEY_COLOR_URI = "colors";
    private static final int REQUEST_CODE_GET_PERMISSION = 1;
    private static final int REQUEST_CODE_GET_TMP_FILE = FileSystemUtils.getNextFileCode();

    private Uri image = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_choose_colors_activity);

        image = (Uri) getIntent().getExtras().get("image");

        if (PermissionUtils.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_CODE_GET_PERMISSION)) {
            setImageToView();
        }
    }

    public void chosenColor(View view) {

        Dialog dialog = new MaterialAlertDialogBuilder(this).setTitle("Loading").setView(R.layout.layout_choose_color_activity_progress_dialog).setCancelable(false).show();

        EXECUTOR.submit(() -> {
            try {
                Intent result = new Intent();
                Uri file = getColorsFromPixels();

                if (file == null) {
                    Toast.makeText(this, "Can not save result", Toast.LENGTH_SHORT).show();
                    return;
                }

                result.putExtra(KEY_COLOR_URI, file);
                setResult(RESULT_OK, result);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                dialog.dismiss();
            }
        });

    }

    private Uri getColorsFromPixels() {
        final ChooserColor chooser = findViewById(R.id.chooser);
        final int[] chosenColors = chooser.getChosenColors();

        Log.d(TAG, "getColorsFromPixels: chosen colors size = " + chosenColors.length * 4 + " bytes. Useful information = " + chosenColors.length * 3 + " bytes");

        Set<Integer> integers = new HashSet<>(chosenColors.length / 50, 0.95f);
        int prevValue = -1;

        long countBytes = 0;

        ByteBuffer buffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_LENGTH);

        try {
            Pair<Uri, File> file = FileSystemUtils.createTmpFile(this, REQUEST_CODE_GET_TMP_FILE);

            try (FileOutputStream fileOutputStream = new FileOutputStream(file.getSecond())) {
                try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, 8192)) {
                    try (Base64OutputStream base64OutputStream = new Base64OutputStream(bufferedOutputStream, Base64.NO_WRAP | Base64.NO_PADDING)) {
                        try (DeflaterOutputStream out = new DeflaterOutputStream(base64OutputStream, new Deflater(Deflater.BEST_COMPRESSION, true))) {

                            for (int chosenColor : chosenColors) {

                                chosenColor &= 0xffffff;

                                if (chosenColor == prevValue) {
                                    continue;
                                }

                                prevValue = chosenColor;

                                if (!integers.add(chosenColor)) {
                                    continue;
                                }

                                byte r = (byte) ((chosenColor >> 16) & 0xff);
                                byte g = (byte) ((chosenColor >> 8) & 0xff);
                                byte b = (byte) (chosenColor & 0xff);


                                buffer.put(r);
                                buffer.put(g);
                                buffer.put(b);

                                if (buffer.capacity() - buffer.position() < 3) {
                                    out.write(buffer.array(), buffer.arrayOffset(), buffer.position());
                                    countBytes += buffer.position();
                                    buffer.position(0);
                                }
                            }

                            if (buffer.position() > 0) {
                                out.write(buffer.array(), buffer.arrayOffset(), buffer.position());
                                countBytes += buffer.position();
                            }

                        } catch (IOException ex) {
                            throw new IOException("Can not use deflater stream", ex);
                        }
                    } catch (IOException ex) {
                        throw new IOException("Can not use base64 stream", ex);
                    }
                } catch (IOException ex) {
                    throw new IOException("Can not use buffered stream", ex);
                }
            } catch (FileNotFoundException ex) {
                Log.e(TAG, "getColorsFromPixels: Can not found file", ex);
                return null;
            } catch (IOException ex) {
                Log.e(TAG, "getColorsFromPixels: Can not use file stream", ex);
                return null;
            }

            Log.d(TAG, "getColorsFromPixels: wrote " + countBytes + " bytes");

            return file.getFirst();

        } catch (IOException ex) {
            Log.e(TAG, "getColorsFromPixels: Can not create tmp file", ex);
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_GET_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setImageToView();
            } else {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void setImageToView() {
        ((ChooserColor) findViewById(R.id.chooser)).setImage(ImageSource.uri(Objects.requireNonNull(image, "Uri in intent can not be null")));
    }
}
