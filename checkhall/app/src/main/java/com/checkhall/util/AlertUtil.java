package com.checkhall.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * Created by machorom on 2017-11-21.
 */

public class AlertUtil {
    public static void showAlert(Context c, String message){
        AlertDialog.Builder alert = new AlertDialog.Builder(c);
        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();     //닫기
            }
        });
        alert.setMessage("message");
        alert.show();

    }

}
