package name.vbraun.view.write;

import java.util.LinkedList;

import junit.framework.Assert;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

public class GraphicsLine extends GraphicsControlpoint {
	private static final String TAG = "GraphicsLine";
	
	private Controlpoint p0, p1;
	private final Paint pen = new Paint();
	private int pen_thickness;
	private int pen_color;
	
	/**
	 * @param transform The current transformation
	 * @param x Screen x coordinate 
	 * @param y Screen y coordinate 
	 * @param penThickness
	 * @param penColor
	 */
	protected GraphicsLine(Transformation transform, float x, float y, int penThickness, int penColor) {
		super(Tool.LINE);
		setTransform(transform);
		p0 = new Controlpoint(x,y);
		p1 = new Controlpoint(x,y);
		controlpoints.add(p0);
		controlpoints.add(p1);
		setPen(penThickness, penColor);
	}

	@Override
	protected Controlpoint initialControlpoint() {
		return p1;
	}

	void setPen(int new_pen_thickness, int new_pen_color) {
		pen_thickness = new_pen_thickness;
		pen_color = new_pen_color;
		pen.setARGB(Color.alpha(pen_color), Color.red(pen_color), 
					 Color.green(pen_color), Color.blue(pen_color));
		pen.setAntiAlias(true);
		pen.setStrokeCap(Paint.Cap.ROUND);
		recompute_bounding_box = true;
	}
	
	// this computes the argument to Paint.setStrokeWidth()
	public float getScaledPenThickness() {
		return Stroke.getScaledPenThickness(scale, pen_thickness);
	}
	
	protected float boundingBoxInset() { 
		return -getScaledPenThickness()/2 - 1;
	}
	
	
	@Override
	public boolean intersects(RectF screenRect) {
		float x0 = p0.screenX() + 0.1f;
		float x1 = p1.screenX();
		float y0 = p0.screenY();
		float y1 = p1.screenY();
		return lineIntersectsRectF(x0, y0, x1, y1, screenRect);
	}
	
	public static boolean lineIntersectsRectF(float x0, float y0, float x1, float y1, RectF rect) { 
		// F(x,y) = (y1-y0)x + (x0-x1)y + (x1*y0-x0*y1)
		float dx = x1-x0;
		float dy = y1-y0;
		float F0 = x1*y0-x0*y1;
		float F1 = dy * rect.left  + dx * rect.bottom + F0;
		float F2 = dy * rect.left  + dx * rect.top    + F0;
		float F3 = dy * rect.right + dx * rect.bottom + F0;
		float F4 = dy * rect.right + dx * rect.top    + F0;
		boolean allNegative = (F1<0) && (F2<0) && (F3<0) && (F4<0);
		boolean allPositive = (F1>0) && (F2>0) && (F3>0) && (F4>0);
		if (allNegative || allPositive) return false;
		// rect intersects the infinite line, check segment endpoints
		float xMin = Math.min(x0, x1);
		if (xMin > rect.right) return false;
		float xMax = Math.max(x0, x1);
		if (xMax < rect.left) return false;
		float yMin = Math.min(y0, y1);
		if (yMin > rect.top) return false;
		float yMax = Math.max(y0, y1);
		if (yMax < rect.bottom) return false;
		return true;
	}

	@Override
	public void draw(Canvas c, RectF bounding_box) {
		final float scaled_pen_thickness = getScaledPenThickness();
		pen.setStrokeWidth(scaled_pen_thickness);
		float x0, x1, y0, y1;
		// note: we offset the first point by 1/10 pixel since android does not draw lines with start=end
		x0 = p0.screenX() + 0.1f;
		x1 = p1.screenX();
		y0 = p0.screenY();
		y1 = p1.screenY();
		// Log.v(TAG, "Line ("+x0+","+y0+") -> ("+x1+","+y1+"), thickness="+scaled_pen_thickness);
		c.drawLine(x0, y0, x1, y1, pen);
	}

}