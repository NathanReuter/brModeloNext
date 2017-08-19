package ufsc.sisinf.brmodelo2all.model.shapes;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.GeneralPath;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.shape.mxBasicShape;
import com.mxgraph.view.mxCellState;

public class AttributeShape extends mxBasicShape {

	/**
	 * 
	 */
	public Shape createShape(mxGraphics2DCanvas canvas, mxCellState state) {
		Rectangle temp = state.getRectangle();
		int x = temp.x;
		int y = temp.y;
		int w = temp.width;
		int h = temp.height;

		GeneralPath path = new GeneralPath();

		path.moveTo(x + w / 2, y + h);
		path.curveTo(x - (w * 0.165), y + h, x - (w * 0.165), y, x + w / 2, y);
		path.curveTo(x + w + (w * 0.165), y, x + w + (w * 0.165), y + h, x + w / 2, y + h);
		path.closePath();

		return path;
	}

}
