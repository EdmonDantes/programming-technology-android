package ru.loginov.chemistryapplication.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

public class PermissionUtils {

    public static boolean checkPermission(@NotNull final Activity activity, @NotNull final String permission, final int requestCode) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    showDialog("External storage", activity, permission, requestCode);
                } else {
                    ActivityCompat.requestPermissions(activity, new String[] {permission}, requestCode);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private static void showDialog(@NotNull final String msg, @NotNull final Context context, @NotNull final String permission, final int requestCode) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");

        alertBuilder.setPositiveButton(android.R.string.yes, (dialog, which) ->
                ActivityCompat.requestPermissions((Activity) context, new String[] { permission }, requestCode));

        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

}
