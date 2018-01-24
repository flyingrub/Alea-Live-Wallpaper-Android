package flying.grub.alea;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;


public class MainActivity extends Activity {

    private static final int MAX = 18;
    private WebView webview;
    private int alea;
    SharedPreferences settings;
    SharedPreferences.Editor editor;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = getSharedPreferences("my_prefs", MODE_PRIVATE);
        editor = settings.edit();

        boolean firstTime = settings.getBoolean("first_time", true);
        if (firstTime) {
            editor.putBoolean("first_time", false);
            editor.apply();
            Intent i = new Intent(this, TutoActivity.class);
            startActivity(i);
        }

        webview = new WebView(this);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {
            public void onSwipeRight() {
                alea = aleaMinus(alea);
                load();
                setPref();
            }

            public void onSwipeLeft() {
                alea = aleaPlus(alea);
                load();
                setPref();
            }

            public void onMyDoubleTap() {
                setWall();
            }
        });
        setContentView(webview);

        alea = 1 + (int) (Math.random() * MAX);
        setPref();
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        hideBar();
        load();
    }

    public int aleaPlus(int a) {
        a++;
        if (a > MAX) {
            a = 1;
        }
        return a;
    }

    public int aleaMinus(int a) {
        a--;
        if (a < 1) {
            a = MAX;
        }
        return a;
    }


    public void hideBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public void setPref() {
        editor.putInt("my_alea", alea);
        editor.commit();
    }

    public void setWall() {
        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(this, AleaService.class));
        startActivity(intent);
    }

    public void load() {
        webview.loadUrl("file:///android_asset/index.html?img=" + alea);
    }

}

