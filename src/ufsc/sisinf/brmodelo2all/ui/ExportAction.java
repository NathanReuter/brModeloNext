package ufsc.sisinf.brmodelo2all.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.png.mxPngImageEncoder;
import com.mxgraph.view.mxGraph;

import ufsc.sisinf.brmodelo2all.control.ModelingEditor;
import ufsc.sisinf.brmodelo2all.util.AppDefaultFileFilter;

public class ExportAction implements Action {

	AppMainWindow gra;

	public ExportAction(AppMainWindow gra) {

		this.gra = gra;

	}

	public void actionPerformed(ActionEvent e) {

		AppMainWindow mainWindow = gra;
		ModelingEditor editor = mainWindow.getCurrentEditor();

		String filename = "";

		// Se arquivo nao existe

		JFileChooser jFileChooser = new JFileChooser(mainWindow.getLastDiretory());

		// add Filter
		AppDefaultFileFilter defaultFilter = new AppDefaultFileFilter(".png",
				"PNG " + mxResources.get("file") + " (.png)");

		jFileChooser.setFileFilter(defaultFilter);

		// Se botao cancelar, retornar sem salvar
		if (jFileChooser.showDialog(null, mxResources.get("save")) != JFileChooser.APPROVE_OPTION) {
			return;
		}

		// Verificar se arquivo ja existe, se nao sobreescrever retornar sem
		// salvar
		filename = jFileChooser.getSelectedFile().getAbsolutePath();
		if (new File(filename).exists() && JOptionPane.showConfirmDialog(editor.getGraphComponent(),
				mxResources.get("overwriteExistingFile")) != JOptionPane.YES_OPTION) {
			return;
		}

		// Incluir extensao ao nome do arquivo
		AppDefaultFileFilter fileFilter = (AppDefaultFileFilter) jFileChooser.getFileFilter();
		filename += fileFilter.getExtension();
		mainWindow.setLastDiretory(jFileChooser.getSelectedFile().getParent());

		try {

			Color c = editor.getBackground();

			saveXmlPng(editor, filename, c);

		} catch (IOException ex) {
		}

	}

	protected void saveXmlPng(ModelingEditor editor, String filename, Color bg) throws IOException {
		mxGraphComponent graphComponent = editor.getGraphComponent();
		mxGraph graph = graphComponent.getGraph();

		// Creates the image for the PNG file
		BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, bg, graphComponent.isAntiAlias(), null,
				graphComponent.getCanvas());

		// Creates the URL-encoded XML data
		// mxCodec codec = new mxCodec();
		// String xml = URLEncoder.encode(
		// mxUtils.getXml(codec.encode(graph.getModel())), "UTF-8");
		// mxPngEncodeParam param = mxPngEncodeParam
		// .getDefaultEncodeParam(image);
		// param.setCompressedText(new String[]{"mxGraphModel", xml});

		// Saves as a PNG file
		FileOutputStream outputStream = new FileOutputStream(new File(filename));
		try {
			mxPngImageEncoder encoder = new mxPngImageEncoder(outputStream, /* param */null);

			if (image != null) {
				encoder.encode(image);

			} else {
				JOptionPane.showMessageDialog(graphComponent, mxResources.get("noImageData"));
			}
		} finally {
			outputStream.close();
		}

	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getValue(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void putValue(String arg0, Object arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setEnabled(boolean arg0) {
		// TODO Auto-generated method stub

	}

}
