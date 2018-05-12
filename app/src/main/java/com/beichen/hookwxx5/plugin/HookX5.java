package com.beichen.hookwxx5.plugin;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;

import java.lang.reflect.Field;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookX5 implements IXposedHookLoadPackage {
    private final static String TAG = "beichen";
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if (!loadPackageParam.packageName.equals("com.tencent.mm")){
            return;
        }
        ClassLoader loader = loadPackageParam.classLoader;
        Log.e(TAG, "开始Hook微信");
        hookLog(loadPackageParam.classLoader);
        /*************** Hook x5内核框架层 *******************/
        // 因Android使用的是x5内核,因此最终都会执行到这个地方
        Class<?> webView = XposedHelpers.findClass("com.tencent.smtt.sdk.WebView", loadPackageParam.classLoader);
        XposedHelpers.findAndHookMethod(webView, "loadUrl", String.class,x5WebViewCallback);
        XposedHelpers.findAndHookMethod(webView, "loadUrl", String.class, Map.class, x5WebViewCallback);
        Class<?> valueCallBackClass = loader.loadClass("com.tencent.smtt.sdk.ab");
        XposedHelpers.findAndHookMethod(webView, "evaluateJavascript", String.class, valueCallBackClass, x5WebViewCallback);
        XposedHelpers.findAndHookMethod(webView, "loadData", String.class, String.class, String.class, x5WebViewCallback);
        XposedHelpers.findAndHookMethod(webView, "loadDataWithBaseURL", String.class, String.class, String.class, String.class, String.class, x5WebViewCallback);

        // 下面是具体的子类,可以不用管
        // Lcom/tencent/mm/plugin/appbrand/game/page/b loadUrl(Ljava/lang/String;)V
        // Lcom/tencent/mm/plugin/appbrand/page/s loadUrl(Ljava/lang/String;)V
        // Lcom/tencent/mm/plugin/appbrand/page/e loadUrl(Ljava/lang/String;)V
        // Lcom/tencent/xweb/WebView;->loadUrl(Ljava/lang/String;)V
        // Lcom/tencent/mm/plugin/appbrand/page/l;->loadUrl(Ljava/lang/String;)V        抽象类
        // com.tencent.mm.ui.widget.MMWebView.loadUrl(Ljava/lang/String;)V0

//        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.appbrand.game.page.b", loader, "loadUrl", String.class, callback);
//        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.appbrand.page.s", loader, "loadUrl", String.class, callback);
//        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.appbrand.page.e", loader, "loadUrl", String.class, callback);
//        XposedHelpers.findAndHookMethod("com.tencent.xweb.WebView", loader, "loadUrl", String.class, callback);
//        XposedHelpers.findAndHookMethod("com.tencent.mm.ui.widget.MMWebView", loader, "loadUrl", String.class, callback);

        // 这个类是在读取JS文件为字符串
        Class<?> appbrandEClass = loader.loadClass("com.tencent.mm.plugin.appbrand.e");
        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.appbrand.appcache.ap", loader, "a", appbrandEClass, String.class, callback);

        // amd方法可以获取软件小程序
//        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.appbrand.page.t", loader, "amd", callback);

    }

    private XC_MethodHook x5WebViewCallback = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            String name = param.method.getDeclaringClass().getName() + "." + param.method.getName();
            switch (name){
                case "com.tencent.smtt.sdk.WebView.loadUrl":
                    if (param.args.length == 1){
                        Log.e(TAG, "X5 Hook loadUrl(String)方法之前", new Exception());
                        String url = (String) param.args[0];
                        Log.e(TAG, "loadUrl: " + url);
                    } else if (param.args.length == 2){
                        Log.e(TAG, "X5 loadUrl(String, Map): " + (String) param.args[0]);
                    }
                    break;
                case "com.tencent.smtt.sdk.WebView.evaluateJavascript":
                    Log.e(TAG, "evaluateJavascript 堆栈", new Exception());
                    resolveJS(param);
                    break;
                case "com.tencent.smtt.sdk.WebView.loadData":
                    Log.e(TAG, "X5 Hook loadData(String, String, String) 方法之前", new Exception());
                    String str1 = (String) param.args[0];
                    String str2 = (String) param.args[1];
                    String str3 = (String) param.args[2];
                    Log.e(TAG, "data=" + str1 + " mineType=" + str2 + " encoding=" + str3);
                    break;
                case "com.tencent.smtt.sdk.WebView.loadDataWithBaseURL":
                    //String baseUrl, String data, String mimeType, String encoding, String historyUrl
                    String baseUrl = (String) param.args[0];
                    String data = (String) param.args[1];
                    String mimeType = (String) param.args[2];
                    String encoding = (String) param.args[3];
                    String historyUrl = (String) param.args[4];
                    Log.e(TAG, "loadDataWithBaseURL 堆栈: ", new Exception());
                    Log.e(TAG, "loadDataWithBaseURL: baseUrl=" + baseUrl + " data=" + data + " mimeType=" + mimeType + " encoding=" + encoding + " historyUrl=" + historyUrl);
                    break;
            }
        }
    };

    public void hookXWeb(ClassLoader loader) throws Exception{
        Class<?> cls = loader.loadClass("com.tencent.xweb.j");
        XposedHelpers.findAndHookMethod(cls, "onConsoleMessage", ConsoleMessage.class, webCallback);
        XposedHelpers.findAndHookMethod("com.tencent.smtt.export.external.proxy.ProxyWebChromeClient", loader, "onConsoleMessage", String.class, int.class, String.class, webCallback);
        XposedHelpers.findAndHookMethod("com.tencent.smtt.export.external.proxy.ProxyWebChromeClient", loader, "onConsoleMessage", loader.loadClass("com.tencent.smtt.export.external.interfaces.ConsoleMessage"), webCallback);
        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.game.gamewebview.e.b$a", loader, "onConsoleMessage", ConsoleMessage.class, webCallback);
        XposedHelpers.findAndHookMethod("com.tencent.xweb.sys.SysWebView$1", loader, "onConsoleMessage", ConsoleMessage.class, webCallback);
    }

    private XC_MethodHook webCallback = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            super.beforeHookedMethod(param);
        }
    };

    public void hookLog(ClassLoader loader) throws Exception{
        Class<?> logClass = loader.loadClass("com.tencent.mm.sdk.platformtools.w");
        XposedHelpers.findAndHookMethod(logClass, "f", String.class, String.class, Object[].class, logCallback);
        XposedHelpers.findAndHookMethod(logClass, "e", String.class, String.class, Object[].class, logCallback);
        XposedHelpers.findAndHookMethod(logClass, "w", String.class, String.class, Object[].class, logCallback);
        XposedHelpers.findAndHookMethod(logClass, "i", String.class, String.class, Object[].class, logCallback);
        XposedHelpers.findAndHookMethod(logClass, "d", String.class, String.class, Object[].class, logCallback);
        XposedHelpers.findAndHookMethod(logClass, "v", String.class, String.class, Object[].class, logCallback);
        XposedHelpers.findAndHookMethod(logClass, "k", String.class, String.class, Object[].class, logCallback);
        XposedHelpers.findAndHookMethod(logClass, "l", String.class, String.class, Object[].class, logCallback);
    }


    private XC_MethodHook callback = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

            switch (param.method.getDeclaringClass().getName() + "." + param.method.getName()){
                case "com.tencent.mm.plugin.appbrand.game.page.b.loadUrl":
                    break;
                case "com.tencent.mm.plugin.appbrand.page.s.loadUrl":
                    break;
                case "com.tencent.mm.plugin.appbrand.page.e.loadUrl":
                    break;
                case "com.tencent.xweb.WebView.loadUrl":
                    break;
                case "com.tencent.mm.ui.widget.MMWebView.loadUrl":
                    break;
                    // appbrand 的webview
                case "com.tencent.mm.plugin.appbrand.page.t.amd":
                    break;
                case "com.tencent.mm.plugin.appbrand.appcache.ap.a":
                    String name = (String) param.args[1];
                    //Log.e(TAG, "ap.a 堆栈:  " + name, new Exception());
                    break;
            }
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            Log.e(TAG, "afterHookedMethod: " + param.method.getDeclaringClass().getName() + "." + param.method.getName());
            String name = param.method.getDeclaringClass().getName() + "." + param.method.getName();
            switch (name){
                case "com.tencent.mm.plugin.appbrand.page.t.amd":
                    String result = (String) param.getResult();
                    Log.e(TAG, "amd: " + result);
                    try {
                        Field mAppIdField = param.thisObject.getClass().getDeclaredField("mAppId");
                        mAppIdField.setAccessible(true);
                        String appid = (String) mAppIdField.get(param.thisObject);
                        if (!TextUtils.isEmpty(appid)){
                            Log.e(TAG, "appid: " + appid + " hashCode: " + appid.hashCode());
                        }
                    }catch (Exception e){
                        Log.e(TAG, "异常", e);
                    }
                    break;
                case "com.tencent.mm.plugin.appbrand.appcache.ap.a":
                    String name1 = (String) param.args[1];
                    String s = (String) param.getResult();
                    Log.e(TAG, "脚本名: " + name1 + " 脚本内容: " + s);
                    break;
            }

        }
    };

    private void resolveJS(XC_MethodHook.MethodHookParam param){
        String js = (String) param.args[0];
        ValueCallback callback = (ValueCallback) param.args[1];
        Log.e(TAG, "evaluateJavascript JS : " + js);
    }


    private XC_MethodHook logCallback = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            boolean log = false;
            Throwable ex = new Throwable();
            StackTraceElement[] elements = ex.getStackTrace();
            for (StackTraceElement element : elements){
                if (element.getClassName().contains("com.tencent.mm.plugin.appbrand")){
                    log = true;
                    break;
                }
            }
            if (!log){
                return;
            }
            int level = 0;
            String name = param.method.getName();
            String arg0 = (String) param.args[0];
            String arg1 = (String) param.args[1];
            Object[] arg2 = (Object[]) param.args[2];
            String format = arg2 == null ? arg1 : String.format(arg1, arg2);
            switch (name){
                case "f":
                    level = 0;
                    break;
                case "e":
                    level = 2;
                    break;
                case "w":
                    level = 2;
                    break;
                case "i":
                    level = 0;
                    break;
                case "d":
                    level = 1;
                    break;
                case "v":
                    level = 1;
                    break;
                case "k":
                    level = 1;
                    break;
                case "l":
                    level = 1;
                    break;
            }
            switch (level){
                case 0:
                    Log.i("beichen " + arg0, format);
                    break;
                case 1:
                    Log.d("beichen " + arg0, format);
                    break;
                case 2:
                    Log.e("beichen " + arg0, format);
                    break;
            }
        }
    };
}
