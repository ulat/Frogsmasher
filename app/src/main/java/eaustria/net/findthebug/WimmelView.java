package eaustria.net.findthebug;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import java.util.Random;

/**
 * Created by uwe on 06.09.13.
 */
public class WimmelView extends View {

    private int imageCount;
    private Random rnd;
    private long randomSeed=1;
    private Paint paint = new Paint();

    private static final int[] images={R.drawable.distract1, R.drawable.distract2,
            R.drawable.distract3, R.drawable.distract4,
            R.drawable.distract5,R.drawable.distract6,
            R.drawable.distract7, R.drawable.distract8};

    public void setImageCount(double imageCount) {
        this.imageCount = (int) Math.round(imageCount);
        randomSeed = System.currentTimeMillis();
        invalidate();
    }

    public WimmelView(Context context) {
        super(context);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        rnd = new Random(randomSeed);
        for(int image : images) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), image);
            for(int i=0; i<imageCount/images.length; i++) {
                float left = (float) (rnd.nextFloat() * (getWidth() - bitmap.getWidth()));
                float top = (float) (rnd.nextFloat() * (getHeight() - bitmap.getHeight()));
                canvas.drawBitmap(bitmap, left, top, paint);
            }
            bitmap.recycle();
        }
    }
}
