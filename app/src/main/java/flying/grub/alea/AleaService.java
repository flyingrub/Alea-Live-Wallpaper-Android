package flying.grub.alea;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.io.InputStream;

@TargetApi(Build.VERSION_CODES.ECLAIR_MR1)
public class AleaService extends WallpaperService {

    private static final String TAG = "LIVE_WALLPAPER";
    private static final Handler liveHandler = new Handler();
    SharedPreferences settings;
    int alea;

    @Override
    public Engine onCreateEngine() {
        settings = getSharedPreferences("my_prefs", MODE_PRIVATE);
        alea = settings.getInt("my_alea", 1);

        try {
            return new WallPaperEngine();
        } catch (IOException e) {
            Log.w(TAG, "Error creating WallPaperEngine", e);
            stopSelf();
            return null;
        }
    }

    class WallPaperEngine extends Engine {

        private Movie liveMovie;
        private int duration;
        private Runnable runnable;
        float scaleFactor;
        int timeElapsed;
        long startTime;

        public WallPaperEngine() throws IOException {
            InputStream is = getAssets().open(alea + ".gif");
            initWall(is);
        }

        public void initWall(InputStream is) throws IOException {
            if (is != null) {
                try {
                    liveMovie = Movie.decodeStream(is);
                    duration = liveMovie.duration();

                } finally {
                    is.close();
                }
            } else {
                throw new IOException("Unable to open gif");
            }
            timeElapsed = -1;
            runnable = new Runnable() {
                public void run() {
                    update();
                }
            };
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            liveHandler.removeCallbacks(runnable);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                update();
            } else {
                liveHandler.removeCallbacks(runnable);
            }

            try {
                InputStream is = getAssets().open(alea + ".gif");
                initWall(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format,
                                     int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            scaleFactor = (float) width / (liveMovie.width()) * 0.8f;
            Log.d("test", width + "|" +liveMovie.width() +"|" + scaleFactor);
            update();
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                                     float xOffsetStep, float yOffsetStep, int xPixelOffset,
                                     int yPixelOffset) {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep,
                    xPixelOffset, yPixelOffset);
            update();
        }

        void update() {
            tick();
            SurfaceHolder surfaceHolder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    drawGif(canvas);
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
            liveHandler.removeCallbacks(runnable);
            if (isVisible()) {
                liveHandler.postDelayed(runnable, 1000L / 25L);
            }
        }

        void tick() {
            if (timeElapsed == -1L) {
                timeElapsed = 0;
                startTime = SystemClock.uptimeMillis();
            } else {
                long mDiff = SystemClock.uptimeMillis() - startTime;
                timeElapsed = (int) (mDiff % duration);
            }
        }

        void drawGif(Canvas canvas) {
            float xPos = (canvas.getWidth() / 2 - liveMovie.width() * scaleFactor / 2) / scaleFactor;
            float yPos = (canvas.getHeight() / 2 - liveMovie.height() * scaleFactor / 2) / scaleFactor;
            canvas.drawColor(Color.BLACK);
            canvas.save();
            canvas.scale(scaleFactor, scaleFactor);
            liveMovie.setTime(timeElapsed);
            liveMovie.draw(canvas, xPos, yPos);
            canvas.restore();
        }
    }
}