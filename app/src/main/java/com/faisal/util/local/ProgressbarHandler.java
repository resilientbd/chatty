package com.faisal.util.local;
/**
 * Create by Sk. Faisal
 * purpose: display default progress
 * email:faisal.hossain.pk@gmail.com
 * github:https://github.com/resilientbd
 * Date: 03/07/2020
 */

import android.app.ProgressDialog;
import android.content.Context;

public class ProgressbarHandler {
    static ProgressDialog progressDialog;

    /**
     * Show dailog progress loading
     *
     * @param context context
     */
    public static void ShowLoadingProgress(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    /**
     * Dismiss progress of loading
     *
     * @param context context
     */
    public static void DismissProgress(Context context) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
        }
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
}
