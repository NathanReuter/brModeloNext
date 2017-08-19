package ufsc.sisinf.brmodelo2all.model.objects;

import java.util.ArrayList;
import java.util.List;

import com.mxgraph.model.mxICell;

import ufsc.sisinf.brmodelo2all.app.BrModelo2All;

public class DisjunctionObject extends ModelingObject {

	private static final long serialVersionUID = 1L;
	private List<Collection> childList = new ArrayList<Collection>();

	public DisjunctionObject(String name, Object collection) {
		super(name);
		setParentObject(collection);
	}

	@Override
	public String getStyle() {
		return "shape=image;image="
				+ BrModelo2All.class.getResource("/ufsc/sisinf/brmodelo2all/ui/images/nosql/bracket_icon.png");
	}

	public void addToChildList(Object selectedObject) {
		childList.add((Collection) ((mxICell) selectedObject).getValue());
	}

	public void removeFromChildList(Object removeChild) {
		childList.remove(removeChild);
	}

	public List<Collection> getChildList() {
		return childList;
	}
}
