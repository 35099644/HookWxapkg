package com.beichen.hookwxx5;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

public class MainActivity extends Activity {
    private final String TAG = "beichen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        a();
    }

    public void a(){
        b();
    }
    public void b(){
        c();
    }
    public void c(){
        d();
    }
    public void d(){
        Throwable ex = new Throwable();
        StackTraceElement[] elements = ex.getStackTrace();
        for (int i = 0; i < elements.length; i++){
            Log.e(TAG, elements[i].getClassName());
            Log.e(TAG, elements[i].getFileName());
            Log.e(TAG, String.valueOf(elements[i].getLineNumber()));
            Log.e(TAG, elements[i].getMethodName());
        }
    }
}
