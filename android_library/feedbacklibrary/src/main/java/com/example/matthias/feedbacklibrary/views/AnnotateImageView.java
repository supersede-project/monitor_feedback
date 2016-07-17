/**
 * @license CanvasView
 * Android Application Library
 * https://github.com/Korilakkuma/CanvasView
 * <p/>
 * The MIT License
 * <p/>
 * Copyright (c) 2014 Tomohiro IKEDA (Korilakkuma)
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.example.matthias.feedbacklibrary.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.example.matthias.feedbacklibrary.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for the image annotation.
 * <p/>
 * This class is a modification of:
 * CanvasView (see license at the top)
 * Android Application Library
 * https://github.com/Korilakkuma/CanvasView
 */
public class AnnotateImageView extends View {
    private final Paint emptyPaint = new Paint();
    private Bitmap bitmap = null;
    private List<Path> pathLists = new ArrayList<>();
    private List<Paint> paintLists = new ArrayList<>();
    // Eraser
    private int baseColor = Color.WHITE;
    // Undo, Redo
    private int historyPointer = 0;
    // Flags
    private Mode mode = Mode.DRAW;
    private Drawer drawer = Drawer.PEN;
    private boolean isDown = false;
    // Paint
    private Paint.Style paintStyle = Paint.Style.STROKE;
    private int paintStrokeColor = Color.RED;
    private int paintFillColor = Color.RED;
    private float paintStrokeWidth = 12F;
    private int opacity = 255;
    private float blur = 0F;
    private Paint.Cap lineCap = Paint.Cap.ROUND;
    private Paint.Join lineJoin = Paint.Join.ROUND;
    // Text
    private String text = "";
    private Typeface fontFamily = Typeface.DEFAULT;
    private float fontSize = 32F;
    private Paint.Align textAlign = Paint.Align.RIGHT;
    private Paint textPaint = new Paint();
    private float textX = 0F;
    private float textY = 0F;
    // Drawer
    private float startX = 0F;
    private float startY = 0F;
    private float controlX = 0F;
    private float controlY = 0F;
    // Parent activity
    private boolean noActionExecuted = true;
    private ImageButton undoButton = null;
    private ImageButton redoButton = null;
    private PointF startPoint;
    private PointF endPoint;

    /**
     * @param context  the context
     * @param attrs    the attrs
     * @param defStyle the defStyle
     */
    public AnnotateImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setup(context);
    }

    /**
     * @param context the context
     * @param attrs   the attrs
     */
    public AnnotateImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setup(context);
    }

    /**
     * @param context the context
     */
    public AnnotateImageView(Context context) {
        super(context);
        this.setup(context);
    }

    /**
     * This method initializes the canvas.
     */
    public void clear() {
        Path path = new Path();
        path.moveTo(0F, 0F);
        path.addRect(0F, 0F, 1000F, 1000F, Path.Direction.CCW);
        path.close();

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);

        if (this.historyPointer == this.pathLists.size()) {
            this.pathLists.add(path);
            this.paintLists.add(paint);
            this.historyPointer++;
        } else {
            // On the way of Undo or Redo
            this.pathLists.set(this.historyPointer, path);
            this.paintLists.set(this.historyPointer, paint);
            this.historyPointer++;

            for (int i = this.historyPointer, size = this.paintLists.size(); i < size; i++) {
                this.pathLists.remove(this.historyPointer);
                this.paintLists.remove(this.historyPointer);
            }
        }

        this.text = "";

        // Clear
        this.invalidate();
    }

    /**
     * This method creates the instance of Paint and sets the styles for Paint.
     *
     * @return paint this returned as the instance of Paint
     */
    private Paint createPaint() {
        Paint paint = new Paint();

        paint.setAntiAlias(true);
        paint.setStyle(this.paintStyle);
        paint.setStrokeWidth(this.paintStrokeWidth);
        paint.setStrokeCap(this.lineCap);
        paint.setStrokeJoin(this.lineJoin);

        // Text
        if (this.mode == Mode.TEXT) {
            paint.setTypeface(this.fontFamily);
            paint.setTextSize(this.fontSize);
            paint.setTextAlign(this.textAlign);
            paint.setStrokeWidth(0F);
        }

        // Eraser
        if (this.mode == Mode.ERASER) {
            paint.setColor(this.baseColor);
            paint.setShadowLayer(this.blur, 0F, 0F, this.baseColor);
        } else {
            // Otherwise
            paint.setColor(this.paintStrokeColor);
            paint.setShadowLayer(this.blur, 0F, 0F, this.paintStrokeColor);
            paint.setAlpha(this.opacity);
        }

        return paint;
    }

    /**
     * This method initializes Path.
     * Namely, this method creates the instance of Path and moves to the current position.
     *
     * @param event onTouchEvent event
     * @return path this  returned as the instance of Path
     */
    private Path createPath(MotionEvent event) {
        Path path = new Path();

        // Save for ACTION_MOVE
        this.startX = event.getX();
        this.startY = event.getY();

        path.moveTo(this.startX, this.startY);

        if (this.drawer == Drawer.ARROW) {
            this.startPoint = new PointF(this.startX, this.startY);
            this.endPoint = new PointF();
        }

        return path;
    }

    /**
     * This method draws the designated bitmap to the canvas.
     *
     * @param bitmap the bitmap
     */
    public void drawBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.invalidate();
    }

    /**
     * This method draws the designated byte array of bitmap to the canvas.
     *
     * @param byteArray This is returned as byte array of bitmap.
     */
    public void drawBitmap(byte[] byteArray) {
        this.drawBitmap(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length));
    }

    /**
     * This method draws the text.
     *
     * @param canvas the canvas
     */
    private void drawText(Canvas canvas) {
        if (this.text.length() <= 0) {
            return;
        }

        if (this.mode == Mode.TEXT) {
            this.textX = this.startX;
            this.textY = this.startY;

            this.textPaint = this.createPaint();
        }

        float textX = this.textX;
        float textY = this.textY;

        Paint paintForMeasureText = new Paint();

        // Line break automatically
        float textLength = paintForMeasureText.measureText(this.text);
        float lengthOfChar = textLength / (float) this.text.length();
        // text-align : right
        float restWidth = canvas.getWidth() - textX;
        // The number of characters at 1 line
        int numChars = (lengthOfChar <= 0) ? 1 : (int) Math.floor((double) (restWidth / lengthOfChar));
        int modNumChars = (numChars < 1) ? 1 : numChars;
        float y = textY;

        for (int i = 0, len = this.text.length(); i < len; i += modNumChars) {
            String substring = "";

            if ((i + modNumChars) < len) {
                substring = this.text.substring(i, (i + modNumChars));
            } else {
                substring = this.text.substring(i, len);
            }

            y += this.fontSize;

            canvas.drawText(substring, textX, y, this.textPaint);
        }
    }

    public int getBaseColor() {
        return this.baseColor;
    }

    /**
     * This method returns the part of current canvas which represents the image, i.e., the 'bitmap part' of the whole view as a bitmap.
     *
     * @return This is returned as bitmap.
     */
    public Bitmap getBitmap() {
        this.setDrawingCacheEnabled(false);
        this.setDrawingCacheEnabled(true);

        return Bitmap.createBitmap(this.getDrawingCache(), 0, 0, this.bitmap.getWidth(), this.bitmap.getHeight());
    }

    /**
     * This method returns the bitmap as byte array.
     *
     * @param format  the format
     * @param quality the quality
     * @return the bitmap as byte array
     */
    public byte[] getBitmapAsByteArray(CompressFormat format, int quality) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        this.getWholeViewBitmap().compress(format, quality, byteArrayOutputStream);

        return byteArrayOutputStream.toByteArray();
    }

    /**
     * This method returns the bitmap as a byte array.
     * Bitmap format is PNG and quality is 100.
     *
     * @return the bitmap as byte array
     */
    public byte[] getBitmapAsByteArray() {
        return this.getBitmapAsByteArray(CompressFormat.PNG, 100);
    }

    public float getBlur() {
        return this.blur;
    }

    /**
     * This method gets the instance of Path that the pointer indicates.
     *
     * @return the instance of Path
     */
    private Path getCurrentPath() {
        return this.pathLists.get(this.historyPointer - 1);
    }

    public Drawer getDrawer() {
        return this.drawer;
    }

    public Typeface getFontFamily() {
        return this.fontFamily;
    }

    public float getFontSize() {
        return this.fontSize;
    }

    public Paint.Cap getLineCap() {
        return this.lineCap;
    }

    public Paint.Join getLineJoin() {
        return lineJoin;
    }

    public Mode getMode() {
        return this.mode;
    }

    public int getOpacity() {
        return this.opacity;
    }

    public int getPaintFillColor() {
        return this.paintFillColor;
    }

    public int getPaintStrokeColor() {
        return this.paintStrokeColor;
    }

    public float getPaintStrokeWidth() {
        return this.paintStrokeWidth;
    }

    public Paint.Style getPaintStyle() {
        return this.paintStyle;
    }

    /**
     * This method gets the current canvas as a scaled bitmap.
     *
     * @return the scaled bitmap.
     */
    public Bitmap getScaleBitmap(int w, int h) {
        this.setDrawingCacheEnabled(false);
        this.setDrawingCacheEnabled(true);

        return Bitmap.createScaledBitmap(this.getDrawingCache(), w, h, true);
    }

    public String getText() {
        return this.text;
    }

    /**
     * This method returns the current canvas, i.e., the whole view as a bitmap.
     *
     * @return This is returned as bitmap.
     */
    public Bitmap getWholeViewBitmap() {
        this.setDrawingCacheEnabled(false);
        this.setDrawingCacheEnabled(true);

        return Bitmap.createBitmap(this.getDrawingCache());
    }

    /**
     * This method indicates if a redo operation is possible.
     *
     * @return true if a redo operation is possible, false otherwise
     */
    public boolean isRedoable() {
        return this.historyPointer < this.pathLists.size();
    }

    /**
     * This method indicates if an undo operation is possible.
     *
     * @return true if an undo operation is possible, false otherwise
     */
    public boolean isUndoable() {
        return this.historyPointer > 1;
    }

    /**
     * This method defines the action on MotionEvent.ACTION_DOWN.
     *
     * @param event MotionEvent even, i.e., the argument of onTouchEvent method
     */
    private void onActionDown(MotionEvent event) {
        switch (this.mode) {
            case DRAW:
            case ERASER:
                if ((this.drawer != Drawer.QUADRATIC_BEZIER) && (this.drawer != Drawer.QUBIC_BEZIER)) {
                    // Otherwise
                    this.updateHistory(this.createPath(event));
                    this.isDown = true;
                } else {
                    // Bezier
                    if ((this.startX == 0F) && (this.startY == 0F)) {
                        // The 1st tap
                        this.updateHistory(this.createPath(event));
                    } else {
                        // The 2nd tap
                        this.controlX = event.getX();
                        this.controlY = event.getY();

                        this.isDown = true;
                    }
                }

                break;
            case TEXT:
                this.startX = event.getX();
                this.startY = event.getY();

                break;
            default:
                break;
        }
    }

    /**
     * This method defines the action on MotionEvent.ACTION_MOVE.
     *
     * @param event MotionEvent even, i.e., the argument of onTouchEvent method
     */
    private void onActionMove(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (this.mode) {
            case DRAW:
            case ERASER:
                if ((this.drawer != Drawer.QUADRATIC_BEZIER) && (this.drawer != Drawer.QUBIC_BEZIER)) {
                    if (!isDown) {
                        return;
                    }

                    Path path = this.getCurrentPath();

                    switch (this.drawer) {
                        case PEN:
                            path.lineTo(x, y);
                            break;
                        case LINE:
                            path.reset();
                            path.moveTo(this.startX, this.startY);
                            path.lineTo(x, y);
                            break;
                        case ARROW:
                            path.reset();
                            path.moveTo(this.startX, this.startY);
                            path.lineTo(x, y);

                            this.endPoint.x = x;
                            this.endPoint.y = y;

                            float deltaX = endPoint.x - startPoint.x;
                            float deltaY = endPoint.y - startPoint.y;
                            float frac = (float) 0.1;
                            float point_x_1 = startPoint.x + ((1 - frac) * deltaX + frac * deltaY);
                            float point_y_1 = startPoint.y + ((1 - frac) * deltaY - frac * deltaX);
                            float point_x_2 = endPoint.x;
                            float point_y_2 = endPoint.y;
                            float point_x_3 = startPoint.x + ((1 - frac) * deltaX - frac * deltaY);
                            float point_y_3 = startPoint.y + ((1 - frac) * deltaY + frac * deltaX);

                            path.moveTo(point_x_1, point_y_1);
                            path.lineTo(point_x_2, point_y_2);
                            path.lineTo(point_x_3, point_y_3);
                            break;
                        case RECTANGLE:
                            path.reset();
                            path.addRect(this.startX, this.startY, x, y, Path.Direction.CCW);
                            break;
                        case CIRCLE:
                            double distanceX = Math.abs((double) (this.startX - x));
                            double distanceY = Math.abs((double) (this.startX - y));
                            double radius = Math.sqrt(Math.pow(distanceX, 2.0) + Math.pow(distanceY, 2.0));

                            path.reset();
                            path.addCircle(this.startX, this.startY, (float) radius, Path.Direction.CCW);
                            break;
                        case ELLIPSE:
                            RectF rect = new RectF(this.startX, this.startY, x, y);

                            path.reset();
                            path.addOval(rect, Path.Direction.CCW);
                            break;
                        default:
                            break;
                    }
                } else {
                    if (!isDown) {
                        return;
                    }

                    Path path = this.getCurrentPath();

                    path.reset();
                    path.moveTo(this.startX, this.startY);
                    path.quadTo(this.controlX, this.controlY, x, y);
                }

                break;
            case TEXT:
                this.startX = x;
                this.startY = y;

                break;
            default:
                break;
        }
    }

    /**
     * This method defines the action on MotionEvent.ACTION_DOWN.
     *
     * @param event MotionEvent even, i.e., the argument of onTouchEvent method
     */
    private void onActionUp(MotionEvent event) {
        if (isDown) {
            this.startX = 0F;
            this.startY = 0F;
            this.isDown = false;
        }
    }

    /**
     * This method updates the instance of the canvas, i.e., the view
     *
     * @param canvas the new instance of Canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Before "drawPath"
        canvas.drawColor(this.baseColor);

        if (this.bitmap != null) {
            canvas.drawBitmap(this.bitmap, 0F, 0F, emptyPaint);
        }

        for (int i = 0; i < this.historyPointer; i++) {
            Path path = this.pathLists.get(i);
            Paint paint = this.paintLists.get(i);

            canvas.drawPath(path, paint);
        }

        this.drawText(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.bitmap = Utils.scaleBitmap(this.bitmap, w, h);
        RelativeLayout relativeLayout = (RelativeLayout) getParent();
        ViewGroup.LayoutParams relativeLayoutLayoutParams = relativeLayout.getLayoutParams();
        if (relativeLayoutLayoutParams != null) {
            relativeLayoutLayoutParams.height = this.bitmap.getHeight();
            relativeLayoutLayoutParams.width = this.bitmap.getWidth();
        }

        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * This method sets the event listener for drawing.
     *
     * @param event the instance of MotionEvent
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.onActionDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                this.onActionMove(event);
                break;
            case MotionEvent.ACTION_UP:
                this.onActionUp(event);
                break;
            default:
                break;
        }

        // Redraw
        this.invalidate();

        return true;
    }

    /**
     * This method draws the canvas again for Redo.
     *
     * @return true if Redo is enabled, false otherwise
     */
    public boolean redo() {
        if (this.historyPointer < this.pathLists.size()) {
            this.historyPointer++;
            this.invalidate();

            return true;
        } else {
            return false;
        }
    }

    /**
     * This method sets the canvas background color.
     *
     * @param color the background color
     */
    public void setBaseColor(int color) {
        this.baseColor = color;
    }

    /**
     * This method sets the amount of blur.
     *
     * @param blur the blur
     */
    public void setBlur(float blur) {
        if (!(blur < 0)) {
            this.blur = blur;
        } else {
            this.blur = 0F;
        }
    }

    /**
     * This method sets the drawer.
     *
     * @param drawer the drawer (PEN, LINE, RECTANGLE, CIRCLE, ELLIPSE, QUADRATIC_BEZIER or QUBIC_BEZIER)
     */
    public void setDrawer(Drawer drawer) {
        this.drawer = drawer;
    }

    /**
     * This method sets font-family of the text to be drawn.
     *
     * @param face the face
     */
    public void setFontFamily(Typeface face) {
        this.fontFamily = face;
    }

    /**
     * This method sets the font size of the text to be drawn.
     *
     * @param size the font size
     */
    public void setFontSize(float size) {
        if (!(size < 0F)) {
            this.fontSize = size;
        } else {
            this.fontSize = 32F;
        }
    }

    /**
     * This method sets the line cap.
     *
     * @param cap the cap
     */
    public void setLineCap(Paint.Cap cap) {
        this.lineCap = cap;
    }

    // TODO: observe?

    public void setLineJoin(Paint.Join lineJoin) {
        this.lineJoin = lineJoin;
    }

    /**
     * This method sets the mode.
     *
     * @param mode the mode (DRAW, ERASER or TEXT)
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void setNoActionExecuted(boolean noActionExecuted) {
        this.noActionExecuted = noActionExecuted;
    }

    /**
     * This method sets the alpha value.
     * The 1st argument must be between 0 and 255.
     *
     * @param opacity the opacity
     */
    public void setOpacity(int opacity) {
        if ((opacity >= 0) && (opacity <= 255)) {
            this.opacity = opacity;
        } else {
            this.opacity = 255;
        }
    }

    /**
     * This method sets the fill color.
     * But, current Android API cannot set fill color (?).
     *
     * @param color the fill color
     */
    public void setPaintFillColor(int color) {
        this.paintFillColor = color;
    }

    /**
     * This method sets the stroke color.
     *
     * @param color the stroke color
     */
    public void setPaintStrokeColor(int color) {
        this.paintStrokeColor = color;
    }

    /**
     * This method sets the stroke width.
     *
     * @param width the width
     */
    public void setPaintStrokeWidth(float width) {
        if (!(width < 0)) {
            this.paintStrokeWidth = width;
        } else {
            this.paintStrokeWidth = 3F;
        }
    }

    /**
     * This method sets the paint style.
     *
     * @param style the style (stroke or fill)
     */
    public void setPaintStyle(Paint.Style style) {
        this.paintStyle = style;
    }

    public void setRedoButton(ImageButton redoButton) {
        this.redoButton = redoButton;
    }

    /**
     * This method sets the text to be drawn.
     *
     * @param text the text to be drawn
     */
    public void setText(String text) {
        this.text = text;
    }

    public void setUndoButton(ImageButton undoButton) {
        this.undoButton = undoButton;
    }

    /**
     * @param context the context
     */
    private void setup(Context context) {
        this.pathLists.add(new Path());
        this.paintLists.add(this.createPaint());
        this.historyPointer++;

        this.textPaint.setARGB(0, 255, 255, 255);
    }

    /**
     * This method draws the canvas again for Undo.
     *
     * @return true if Undo is enabled, false otherwise
     */
    public boolean undo() {
        if (this.historyPointer > 1) {
            this.historyPointer--;
            invalidate();

            return true;
        } else {
            return false;
        }
    }

    /**
     * This method updates the lists for the instance of Path and Paint.
     * "Undo" and "Redo" are enabled by this method.
     *
     * @param path the instance of Path
     */
    private void updateHistory(Path path) {
        if (this.noActionExecuted) {
            this.undoButton.setEnabled(true);
            this.undoButton.setAlpha(1.0F);
            this.redoButton.setEnabled(false);
            this.redoButton.setAlpha(0.4F);
            this.noActionExecuted = false;
        }

        if (this.historyPointer == this.pathLists.size()) {
            this.pathLists.add(path);
            this.paintLists.add(this.createPaint());
            this.historyPointer++;
        } else {
            // On the way of Undo or Redo
            this.pathLists.set(this.historyPointer, path);
            this.paintLists.set(this.historyPointer, this.createPaint());
            this.historyPointer++;

            for (int i = this.historyPointer, size = this.paintLists.size(); i < size; i++) {
                this.pathLists.remove(this.historyPointer);
                this.paintLists.remove(this.historyPointer);
            }
        }
    }

    // Enumeration for Mode
    public enum Mode {
        DRAW,
        TEXT,
        ERASER
    }

    // Enumeration for Drawer
    public enum Drawer {
        PEN,
        LINE,
        ARROW,
        RECTANGLE,
        CIRCLE,
        ELLIPSE,
        QUADRATIC_BEZIER,
        QUBIC_BEZIER
    }

}