package com.tarun.expense;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    // The HTML is served from app assets under a fixed https origin, so
    // localStorage stays stable and "/assets/..." script paths resolve.
    private static final String HOST = "appassets.androidplatform.net";

    private WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);

        web.addJavascriptInterface(new Bridge(), "AndroidBridge");
        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                if (!HOST.equals(u.getHost())) return null; // e.g. Google Fonts
                String path = u.getPath() == null ? "" : u.getPath();
                String name;
                if (path.isEmpty() || path.equals("/")) name = "index.html";
                else if (path.startsWith("/assets/")) name = path.substring(8);
                else name = path.substring(1);
                try {
                    InputStream in = getAssets().open(name);
                    return new WebResourceResponse(mime(name), "UTF-8", in);
                } catch (IOException e) {
                    Map<String, String> h = new HashMap<>();
                    return new WebResourceResponse("text/plain", "UTF-8", 404, "Not Found",
                            h, new ByteArrayInputStream(new byte[0]));
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                if (HOST.equals(u.getHost())) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) {}
                return true;
            }

            @Override
            public void onPageFinished(WebView v, String url) {
                v.evaluateJavascript("window.print=function(){AndroidBridge.print();};", null);
            }
        });
        // Blob downloads are handled by the bridge instead
        web.setDownloadListener((url, ua, cd, mt, len) -> { });

        web.loadUrl("https://" + HOST + "/index.html");
    }

    private static String mime(String n) {
        n = n.toLowerCase();
        if (n.endsWith(".html")) return "text/html";
        if (n.endsWith(".js")) return "application/javascript";
        if (n.endsWith(".css")) return "text/css";
        if (n.endsWith(".json")) return "application/json";
        if (n.endsWith(".png")) return "image/png";
        if (n.endsWith(".svg")) return "image/svg+xml";
        if (n.endsWith(".woff2")) return "font/woff2";
        return "application/octet-stream";
    }

    private void toast(final String m) {
        runOnUiThread(() -> Toast.makeText(this, m, Toast.LENGTH_LONG).show());
    }

    private class Bridge {
        @JavascriptInterface
        public void saveFile(String b64, String filename, String mimeType) {
            try {
                byte[] data = Base64.decode(b64, Base64.DEFAULT);
                ContentValues v = new ContentValues();
                v.put(MediaStore.Downloads.DISPLAY_NAME, filename);
                v.put(MediaStore.Downloads.MIME_TYPE, mimeType);
                v.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
                if (uri == null) throw new IOException("Could not create file");
                try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                    os.write(data);
                }
                toast("Saved to Downloads: " + filename);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setDataAndType(uri, mimeType);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    startActivity(Intent.createChooser(i, "Open " + filename));
                } catch (ActivityNotFoundException e) {
                    toast("Saved to Downloads (no app found to open it)");
                }
            } catch (Exception e) {
                toast("Save failed: " + e.getMessage());
            }
        }

        @JavascriptInterface
        public void print() {
            runOnUiThread(() -> {
                PrintManager pm = (PrintManager) getSystemService(PRINT_SERVICE);
                pm.print("Expense Sheet", web.createPrintDocumentAdapter("Expense Sheet"),
                        new PrintAttributes.Builder().build());
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
