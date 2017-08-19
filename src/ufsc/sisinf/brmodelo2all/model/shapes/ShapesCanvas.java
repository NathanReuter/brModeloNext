package ufsc.sisinf.brmodelo2all.model.shapes;

import com.mxgraph.swing.view.mxInteractiveCanvas;

public class ShapesCanvas extends mxInteractiveCanvas {

	/**
	 * Static initializer.
	 */
	static {
		putShape("attribute", new AttributeShape());
		putShape("identifierAttribute", new AttributeShape());
		putShape("inheritance", new InheritanceShape());
		putShape("column", new ColumnShape());
		putShape("noSqlAttribute", new NoSqlAttributeShape());

	}

}
