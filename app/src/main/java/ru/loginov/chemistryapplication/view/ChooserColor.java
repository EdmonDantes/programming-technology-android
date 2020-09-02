package ru.loginov.chemistryapplication.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;

import static ru.loginov.chemistryapplication.ChemistryApplication.TAG;

public class ChooserColor extends SubsamplingScaleImageView {

    private PointF preStartPoint;
    private PointF startPoint;
    private PointF endPoint;
    private Paint selectRectPaint = new Paint();
    private boolean useLongPress = false;
    private Bitmap bitmap;

    public ChooserColor(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChooserColor(Context context) {
        super(context);
        init();
    }

    private void init(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            selectRectPaint.setColor(Color.argb(0.5f, .4f, 0.8f, 0.8f));
        } else {
            selectRectPaint.setColor(Color.parseColor("#7F66CCCC"));
        }

        final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDown(MotionEvent e) {
                Log.d(TAG, "onDown: ");
                preStartPoint = viewToSourceCoord(e.getX(), e.getY());
                return false;
            }


            @Override
            public void onLongPress(MotionEvent e) {
                Log.d(TAG, "onLongPress: ");
                useLongPress = true;
                startPoint = preStartPoint;
                endPoint = viewToSourceCoord(e.getX(), e.getY());
                Toast.makeText(getContext(), "Choose color mode enabled", Toast.LENGTH_SHORT).show();
                invalidate();
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.d(TAG, "onSingleTapUp: ");
                if (useLongPress) {
                    useLongPress = false;
                    endPoint = viewToSourceCoord(e.getX(), e.getY());
                    invalidate();
                    return true;
                }
                return false;
            }

        });

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (useLongPress) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_MOVE:
                            endPoint = viewToSourceCoord(event.getX(), event.getY());
                            invalidate();
                            return true;
                        case MotionEvent.ACTION_UP:
                            endPoint = viewToSourceCoord(event.getX(), event.getY());
                            useLongPress = false;
                            invalidate();
                            return true;
                    }
                }
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (startPoint != null && endPoint != null) {
            PointF realStartPoint = sourceToViewCoord(startPoint);
            PointF realEndPoint = sourceToViewCoord(endPoint);
            canvas.drawRect(realStartPoint.x, realStartPoint.y, realEndPoint.x, realEndPoint.y, selectRectPaint);
        }
    }




    public int[] getChosenColors() {
        try {
            Bitmap bitmap = getSrcBitmap();
            Rect rect = getPointsRect();

            int[] pixels = new int[rect.width() * rect.height()];
            bitmap.getPixels(pixels, 0, rect.width(), rect.left, rect.top, rect.width(), rect.height());

            return pixels;
        } catch (NoSuchFieldException | IllegalAccessException | FileNotFoundException e) {
            throw new IllegalStateException("Can not get bitmap from this object");
        }
    }

    private Bitmap getSrcBitmap() throws NoSuchFieldException, IllegalAccessException, FileNotFoundException {
        Field uriField = SubsamplingScaleImageView.class.getDeclaredField("uri");
        uriField.setAccessible(true);
        return BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream((Uri) uriField.get(this)));
    }

    private Rect getPointsRect() {
        if (startPoint == null || endPoint == null) {
            return new Rect();
        }

        return new Rect((int) Math.min(startPoint.x, endPoint.x), (int) Math.min(startPoint.y, endPoint.y), (int) Math.max(startPoint.x, endPoint.x), (int) Math.max(startPoint.y, endPoint.y));
    }
}
