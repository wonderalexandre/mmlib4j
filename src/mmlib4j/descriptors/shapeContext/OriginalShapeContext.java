package mmlib4j.descriptors.shapeContext;

import java.util.List;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.images.impl.MmlibImageFactory;
import mmlib4j.utils.Resampling;

public class OriginalShapeContext extends ShapeContext {
	private double histogram[][];

	public OriginalShapeContext(Point point) {
		super(point);
	}

	public void compute(List<Point> points, double median) {
		int i, j, radBin, angBin, angle;
		double dist;
		int total;

		histogram = new double[5][12];
		for (i = 0; i < 5; i++) {
			for (j = 0; j < 12; j++) {
				histogram[i][j] = 0;
				histogram[i][j] = 0;
			}
		}
		for (i = 0; i < points.size(); i++) {
			// Invariancia à escala
			dist = Math.hypot(points.get(i).x - point.x, points.get(i).y
					- point.y)
					/ median;
			angle = (int) (Math.atan2((point.y - points.get(i).y),
					(points.get(i).x - point.x)) * 180 / Math.PI);
			if (dist != 0) {
				if (angle >= 360)
					angle -= 360;
				if (angle < 0)
					angle += 360;
				angBin = angularBin(angle);
				radBin = logRadialBin(dist);
				histogram[radBin][angBin]++;
				histogram[radBin][angBin]++;
			}
		}

		// Normalização
		total = 0;
		for (i = 0; i < 5; i++)
			for (j = 0; j < 12; j++)
				total += histogram[i][j];

		for (i = 0; i < 5; i++) {
			for (j = 0; j < 12; j++) {
				histogram[i][j] = (double) histogram[i][j] / total;
			}
		}
	}

	public GrayScaleImage toBitmap() {
		int i, j;
		int value;
		double max = 0;
		int width = 12;
		int height = 5;
		GrayScaleImage sc = ImageFactory.instance.createGrayScaleImage(8, width, height);

		for (i = 0; i < height; i++) {
			for (j = 0; j < width; j++) {
				if (histogram[i][j] > max)
					max = histogram[i][j];
			}
		}

		for (i = 0; i < height; i++) {
			for (j = 0; j < width; j++) {
				value = (int) (255 - (histogram[i][j] * 255 / max));
				sc.setPixel(j, i, value);
			}
		}
		return Resampling.bilinear(sc, width * 20, height * 20);

	}

	public double distance(ShapeContext sc) {
		double distance = 0.0, g, h;
		OriginalShapeContext oSc = (OriginalShapeContext) sc;
		distance = 0.0;
		for (int k = 0; k < 5; k++) {
			for (int l = 0; l < 12; l++) {
				g = this.get(k, l);
				h = oSc.get(k, l);
				if (g + h > 0)
					distance += ((g - h) * (g - h)) / (g + h);
			}
		}

		return distance;

	}

	double get(int i, int j) {
		return histogram[i][j];
	}

}