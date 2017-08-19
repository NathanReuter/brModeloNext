package ufsc.sisinf.brmodelo2all.model.shapes;

import java.awt.Rectangle;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.shape.mxLabelShape;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxCellState;

public class NoSqlAttributeShape extends mxLabelShape {

	public Rectangle getImageBounds(mxGraphics2DCanvas canvas, mxCellState state) {
		Rectangle temp = state.getRectangle();
		int x = temp.x;
		int y = temp.y;

		mxRectangle imageBounds = new mxRectangle(x, y, 150, 20);

		return imageBounds.getRectangle();
	}

}
