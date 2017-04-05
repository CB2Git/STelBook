package com.jay.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

    public static Toast show(Context context, int msgId) {
        return show(context, msgId, Toast.LENGTH_SHORT);
    }

    public static Toast show(Context context, String msg) {
        return show(context, msg, Toast.LENGTH_SHORT);
    }

    public static Toast show(Context context, int msgId, int duration) {
        Toast toast = Toast.makeText(context, msgId, duration);
        toast.show();
        return toast;
    }

    public static Toast show(Context context, String msg, int duration) {
        Toast toast = Toast.makeText(context, msg, duration);
        toast.show();
        return toast;
    }
}
