package alistar.app;

import alistar.app.brain.Emotion;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class AliEmotionsChart extends View
{
	float step = 0;
	float valueHeight = 0;
	int pointsOnScreen = 24;
	float width = 0;
	float height = 0;
	float halfWidth = 0;
	float halfHeight = 0;
	float padding = 0;
	Paint baseLinePaint = new Paint();
	Paint linePaint = new Paint();
	Paint blueLinePaint = new Paint();
	Paint chartPaint = new Paint();
	Paint fillPaint = new Paint();
	Paint shadowPaint = new Paint();
	Paint circlePaint = new Paint();
	List<Emotion> data = null;
	List<Point> points = null;

	public AliEmotionsChart(Context context) {
		super(context);
		init();
	}

	public AliEmotionsChart(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	protected void init() {
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		baseLinePaint.setColor(Color.parseColor("#AA4c4e73"));
		baseLinePaint.setStyle(Paint.Style.STROKE);
		baseLinePaint.setStrokeWidth(1);

		linePaint.setColor(Color.parseColor("#404c4e73"));
		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setStrokeWidth(1);

		blueLinePaint.setColor(ContextCompat.getColor(getContext(), R.color.blue_40));
		blueLinePaint.setStyle(Paint.Style.STROKE);
		blueLinePaint.setStrokeWidth(1);

		chartPaint.setColor(Color.parseColor("#6C91FA"));
		chartPaint.setStyle(Paint.Style.STROKE);
		chartPaint.setStrokeWidth(2);
		chartPaint.setAntiAlias(true);

		shadowPaint.setColor(Color.TRANSPARENT);
		shadowPaint.setStyle(Paint.Style.STROKE);
		shadowPaint.setStrokeWidth(2);
		shadowPaint.setAntiAlias(true);
		shadowPaint.setShadowLayer(4, 0, 6, Color.parseColor("#60000000"));

		fillPaint.setColor(Color.parseColor("#FFD869"));
		fillPaint.setStyle(Paint.Style.FILL);
		fillPaint.setStrokeWidth(3);
		fillPaint.setAntiAlias(true);

		circlePaint.setColor(Color.parseColor("#FFD969"));
		circlePaint.setStyle(Paint.Style.FILL);
		circlePaint.setAntiAlias(true);

		points = new ArrayList<Point>();
	}

	public void setData(List<Emotion> data) {
		this.data = data;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO: Implement this method
		super.onDraw(canvas);
		Shader chartShader = new LinearGradient(0, getHeight() / 2, getWidth(), getHeight() / 2, Color.parseColor("#6C91FA"), Color.parseColor("#9967F4"), Shader.TileMode.CLAMP);
		Shader fillShader = new LinearGradient(getWidth() / 2, 0, getWidth() / 2, getHeight(),
				new int[]{Color.parseColor("#156C91FA"), Color.parseColor("#156C91FA"), Color.TRANSPARENT},
				new float[]{0, .5f,1f}, Shader.TileMode.CLAMP);
		fillPaint.setShader(fillShader);
		chartPaint.setShader(chartShader);
		width = getWidth();
		height = getHeight();
		halfWidth = width / 2;
		halfHeight = height / 2;
		padding = getPaddingRight();
		step = (width - (padding * 2)) / (pointsOnScreen - 1);

		valueHeight = (height - (padding) - (chartPaint.getStrokeWidth() * 3f)) / 11f;
		float valueWidth = (width - (padding * 2)) / (pointsOnScreen - 1);

		//draw chart
		Emotion emotion;
		if (data != null) {
			for (int i = 0; i < data.size(); i++) {
				emotion = data.get(i);
				Point point = new Point();
				float x = 0;
				float y = 0;

				if (i == 0) {
					x = padding;
				}
				if (i > 0 & i != data.size() - 1 & i < data.size()) {
					x = padding + (((float) i) * step);
				}
				if (i == data.size() - 1) {
					x = padding + ((float) i * step);
				}

				y = -(emotion.getFeeling() * valueHeight) + halfHeight;

				point.set(x, y);
				point.isAnomaly = emotion.isAnomaly();
				point.isFirstPointOfDay = emotion.isDayFirstRecord();
				points.add(point);
			}
		}

		if(points.size() == 0)
			return;

		Path path = new Path();
		Path fillPath = new Path();
		Path shadowPath = new Path();
		path.setFillType(Path.FillType.EVEN_ODD);
		fillPath.setFillType(Path.FillType.EVEN_ODD);
		shadowPath.setFillType(Path.FillType.EVEN_ODD);
		Point point0 = points.get(0);
		path.moveTo(point0.x, point0.y);
		shadowPath.moveTo(point0.x, point0.y);
		fillPath.moveTo(point0.x, point0.y);

		drawChartGrid(canvas, valueWidth);

		for (int i = 0; i < points.size(); i++) {
			Point point = points.get(i);
			path.lineTo(point.x, point.y);
			shadowPath.lineTo(point.x, point.y);
			fillPath.lineTo(point.x, point.y);
		}

		for (int i = 0; i < points.size(); i++) {
			Point point = points.get(i);
			if (point.isFirstPointOfDay)
				canvas.drawLine(padding + (i * valueWidth), -(-5 * valueHeight) + halfHeight, padding + (i * valueWidth), -(5 * valueHeight) + halfHeight, blueLinePaint);
		}

		fillPath.lineTo((padding) + ((points.size() - 1) * step), height - padding);
		fillPath.lineTo(padding, height - padding);
		fillPath.lineTo(point0.x, point0.y);
		fillPath.close();
		canvas.drawPath(fillPath, fillPaint);
		canvas.drawPath(shadowPath, shadowPaint);
		canvas.drawPath(path, chartPaint);

		for (int i = 0; i < points.size(); i++) {
			Point point = points.get(i);
			if (point.isAnomaly)
				canvas.drawCircle(point.x, point.y, 3, circlePaint);
		}
	}

	private void drawChartGrid(Canvas canvas, float valueWidth) {
		for (int i = -5; i <= 5; i++)
			canvas.drawLine(padding, -(i * valueHeight) + halfHeight, width - padding, -(i * valueHeight) + halfHeight, i == -5 || i == 5 || i == 0 ? baseLinePaint : linePaint);

		for (int i = 0; i < pointsOnScreen; i++)
			canvas.drawLine(padding + (i * valueWidth), -(-5 * valueHeight) + halfHeight, padding + (i * valueWidth), -(5 * valueHeight) + halfHeight, i == 0 || i == pointsOnScreen - 1 ? baseLinePaint : linePaint);
	}

	class Point {

		float x;
		float y;
		boolean isAnomaly;
		boolean isFirstPointOfDay;

		void set(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}

}
