package ufsc.sisinf.brmodelo2all.model.shapes;

import java.awt.Rectangle;
import java.util.Map;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.shape.mxLabelShape;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;

public class ColumnShape extends mxLabelShape {

	public Rectangle getImageBounds(mxGraphics2DCanvas canvas, mxCellState state) {
		Rectangle bounds = super.getImageBounds(canvas, state);
		Map<String, Object> style = state.getStyle();
		double scale = canvas.getScale();
		int imgWidth = (int) (mxUtils.getInt(style, mxConstants.STYLE_IMAGE_WIDTH, mxConstants.DEFAULT_IMAGESIZE)
				* scale);
		int imgHeight = (int) (mxUtils.getInt(style, mxConstants.STYLE_IMAGE_HEIGHT, mxConstants.DEFAULT_IMAGESIZE)
				* scale);

		mxRectangle imageBounds = new mxRectangle(bounds.x, bounds.y, imgWidth, imgHeight);

		return imageBounds.getRectangle();
	}
}
