package ufsc.sisinf.brmodelo2all.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.w3c.dom.Document;

import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxConnectionHandler;
import com.mxgraph.swing.view.mxCellEditor;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUtils;
import com.mxgraph.util.png.mxPngImageEncoder;
import com.mxgraph.view.mxGraph;

import ufsc.sisinf.brmodelo2all.control.*;
import ufsc.sisinf.brmodelo2all.model.Modeling;
import ufsc.sisinf.brmodelo2all.model.ModelingComponent;
import ufsc.sisinf.brmodelo2all.model.objects.AssociativeEntityObject;
import ufsc.sisinf.brmodelo2all.model.objects.AssociativeRelationObject;
import ufsc.sisinf.brmodelo2all.model.objects.AttributeObject;
import ufsc.sisinf.brmodelo2all.model.objects.RelationObject;
import ufsc.sisinf.brmodelo2all.model.objects.TableObject;
import ufsc.sisinf.brmodelo2all.util.AppDefaultFileFilter;

/**
 *
 */
public class CommandActions {

	/**
	 *
	 * @param e
	 * @return Returns the window for the given action event.
	 */
	public static final AppMainWindow getEditor(ActionEvent e) {
		if (e.getSource() instanceof Component) {
			Component component = (Component) e.getSource();

			while (component != null && !(component instanceof AppMainWindow)) {
				component = component.getParent();
			}

			return (AppMainWindow) component;
		}

		return null;
	}

	/**
	 *
	 * @param e
	 * @return Returns the modeling for the given action event.
	 */
	public static final Modeling getModeling(ActionEvent e) {
		if (e.getSource() instanceof ModelingComponent) {
			ModelingComponent modelingComponent = (ModelingComponent) e.getSource();
			return (Modeling) modelingComponent.getGraph();
		}

		if (e.getSource() instanceof Component) {
			Component component = (Component) e.getSource();

			while (component != null && !(component instanceof AppMainWindow)) {
				component = component.getParent();
			}

			if (component instanceof AppMainWindow) {
				AppMainWindow mainWindow = (AppMainWindow) component;
				ModelingEditor modelingEditor = mainWindow.getCurrentEditor();
				if (modelingEditor != null) {
					return (Modeling) modelingEditor.getGraphComponent().getGraph();
				}

			}
		}

		return null;
	}

	/**
	 *
	 * @param e
	 * @return Returns the modeling component for the given action event.
	 */
	public static final ModelingComponent getModelingComponent(ActionEvent e) {
		if (e.getSource() instanceof ModelingComponent) {
			return (ModelingComponent) e.getSource();
		}

		if (e.getSource() instanceof Component) {
			Component component = (Component) e.getSource();

			while (component != null && !(component instanceof AppMainWindow)) {
				component = component.getParent();
			}

			if (component instanceof AppMainWindow) {
				AppMainWindow mainWindow = (AppMainWindow) component;
				ModelingEditor modelingEditor = mainWindow.getCurrentEditor();
				if (modelingEditor != null) {
					return (ModelingComponent) modelingEditor.getGraphComponent();
				}

			}
		}

		return null;
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ExitAction extends AbstractAction {

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			AppMainWindow editor = getEditor(e);

			if (editor != null) {
				editor.exit();
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class CloseAction extends AbstractAction {

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			AppMainWindow editor = getEditor(e);

			if (editor != null) {
				ModelingEditor currentEditor = editor.getCurrentEditor();
				if (currentEditor != null) {
					currentEditor.dispose();
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class StylesheetAction extends AbstractAction {

		/**
		 *
		 */
		protected String stylesheet;

		/**
		 *
		 */
		public StylesheetAction(String stylesheet) {
			this.stylesheet = stylesheet;
		}

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraphComponent graphComponent = (mxGraphComponent) getModelingComponent(e);
			if (graphComponent != null) {
				mxGraph graph = graphComponent.getGraph();
				mxCodec codec = new mxCodec();
				Document doc = mxUtils.loadDocument(CommandActions.class.getResource(stylesheet).toString());

				if (doc != null) {
					codec.decode(doc.getDocumentElement(), graph.getStylesheet());
					graph.refresh();
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class PageSetupAction extends AbstractAction {

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraphComponent graphComponent = (mxGraphComponent) getModelingComponent(e);
			if (graphComponent != null) {
				PrinterJob pj = PrinterJob.getPrinterJob();
				PageFormat format = pj.pageDialog(graphComponent.getPageFormat());

				if (format != null) {
					graphComponent.setPageFormat(format);
					// graphComponent.zoomAndCenter();
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ToggleGridItem extends JCheckBoxMenuItem {

		/**
		 *
		 */
		public ToggleGridItem(final AppMainWindow editor, String name) {
			super(name);
			setSelected(true);

			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mxGraphComponent graphComponent = editor.getCurrentEditor().getGraphComponent();
					if (graphComponent == null) {
						return;
					}
					mxGraph graph = graphComponent.getGraph();
					boolean enabled = !graph.isGridEnabled();

					graph.setGridEnabled(enabled);
					graphComponent.setGridVisible(enabled);
					graphComponent.repaint();
					setSelected(enabled);
				}
			});

		}
	}

	@SuppressWarnings("serial")
	public static class ScaleAction extends AbstractAction {

		/**
		 *
		 */
		protected double scale;

		/**
		 *
		 */
		public ScaleAction(double scale) {
			this.scale = scale;
		}

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraphComponent graphComponent = (mxGraphComponent) getModelingComponent(e);
			if (graphComponent == null) {
				return;
			}

			// ZOOM
			double scale = this.scale;

			if (scale == 0) {
				String value = (String) JOptionPane.showInputDialog(graphComponent, mxResources.get("value"),
						mxResources.get("scale") + " (%)", JOptionPane.PLAIN_MESSAGE, null, null, "");

				if (value != null) {
					scale = Double.parseDouble(value.replace("%", "")) / 100;
				}
			}

			if (scale > 0) {
				graphComponent.zoomTo(scale, graphComponent.isCenterZoom());
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class PrintAction extends AbstractAction {

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraphComponent graphComponent = (mxGraphComponent) getModelingComponent(e);
			if (graphComponent != null) {
				PrinterJob pj = PrinterJob.getPrinterJob();

				if (pj.printDialog()) {
					PageFormat pf = graphComponent.getPageFormat();
					Paper paper = new Paper();
					double margin = 36;
					paper.setImageableArea(margin, margin, paper.getWidth() - margin * 2,
							paper.getHeight() - margin * 2);
					pf.setPaper(paper);
					pj.setPrintable(graphComponent, pf);

					try {
						pj.print();
					} catch (PrinterException e2) {
						System.out.println(e2);
					}
				}
			}
		}
	}

	@SuppressWarnings("serial")
	public static class SaveConceitualAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
			ModelingEditor editor = getEditor(e).getCurrentEditor();

			if (editor != null) {
				ObjectOutputStream output = null;
				JFileChooser jFileChooser = new JFileChooser();
				int result = jFileChooser.showDialog(null, mxResources.get("save"));

				try {
					if (result == JFileChooser.APPROVE_OPTION) {
						ModelingComponent modelingComponent = getModelingComponent(e);
						FileOutputStream fileout = new FileOutputStream(
								jFileChooser.getSelectedFile().getAbsolutePath());
						output = new ObjectOutputStream(fileout);

						mxRectangle rect = modelingComponent.getGraph().getGraphBounds();

						int x = (int) rect.getX();
						int y = (int) rect.getY();

						Rectangle ret = new Rectangle(x + 60000, y + 60000);

						Object[] cells = modelingComponent.getCells(ret);
						output.writeObject(cells);
						output.close();
						fileout.close();
					}
				} catch (IOException ex) {
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class Old_OpenAction extends AbstractAction {

		/**
		 *
		 */
		protected String lastDir;

		/**
		 *
		 */
		protected void resetEditor(ModelingEditor editor) {
			editor.setModified(false);
			editor.getUndoManager().clear();
			editor.getGraphComponent().zoomAndCenter();
		}

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			ModelingEditor editor = getEditor(e).getCurrentEditor();

			if (editor != null) {
				if (!editor.isModified() || JOptionPane.showConfirmDialog(editor,
						mxResources.get("loseChanges")) == JOptionPane.YES_OPTION) {

					mxGraph graph = editor.getGraphComponent().getGraph();

					if (graph != null) {
						String wd = (lastDir != null) ? lastDir : System.getProperty("user.dir");

						JFileChooser fc = new JFileChooser(wd);

						// Adds file filter for supported file format
						AppDefaultFileFilter defaultFilter = new AppDefaultFileFilter(".mxe",
								mxResources.get("allSupportedFormats") + " (.mxe, .png, .vdx)") {
							public boolean accept(File file) {
								String lcase = file.getName().toLowerCase();

								return super.accept(file) || lcase.endsWith(".png") || lcase.endsWith(".vdx");
							}
						};
						fc.addChoosableFileFilter(defaultFilter);

						fc.addChoosableFileFilter(new AppDefaultFileFilter(".mxe",
								"mxGraph Editor " + mxResources.get("file") + " (.mxe)"));
						fc.addChoosableFileFilter(
								new AppDefaultFileFilter(".png", "PNG+XML  " + mxResources.get("file") + " (.png)"));

						// Adds file filter for VDX import
						fc.addChoosableFileFilter(new AppDefaultFileFilter(".vdx",
								"XML Drawing  " + mxResources.get("file") + " (.vdx)"));

						// Adds file filter for GD import
						fc.addChoosableFileFilter(new AppDefaultFileFilter(".txt",
								"Graph Drawing  " + mxResources.get("file") + " (.txt)"));

						fc.setFileFilter(defaultFilter);

						int rc = fc.showDialog(null, mxResources.get("openFile"));

						if (rc == JFileChooser.APPROVE_OPTION) {
							lastDir = fc.getSelectedFile().getParent();

							try {
								if (fc.getSelectedFile().getAbsolutePath().toLowerCase().endsWith(".png")) {
									// openXmlPng(editor, fc.getSelectedFile());
								} else if (fc.getSelectedFile().getAbsolutePath().toLowerCase().endsWith(".txt")) {
									// mxGdDocument document = new
									// mxGdDocument();
									// document.parse(mxUtils.readFile(fc
									// .getSelectedFile()
									// .getAbsolutePath()));
									// openGD(editor, fc.getSelectedFile(),
									// document);
								} else {
									Document document = mxUtils
											.parseXml(mxUtils.readFile(fc.getSelectedFile().getAbsolutePath()));

									if (fc.getSelectedFile().getAbsolutePath().toLowerCase().endsWith(".vdx")) {
										// openVdx(editor, fc.getSelectedFile(),
										// document);
									} else {
										mxCodec codec = new mxCodec(document);
										codec.decode(document.getDocumentElement(), graph.getModel());
										editor.setCurrentFile(fc.getSelectedFile());
									}

									resetEditor(editor);
								}
							} catch (IOException ex) {
								ex.printStackTrace();
								JOptionPane.showMessageDialog(editor.getGraphComponent(), ex.toString(),
										mxResources.get("error"), JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("serial")
	public static class OpenAction extends AbstractAction {
		protected void resetEditor(ModelingEditor editor) {
			editor.setModified(false);
			editor.getUndoManager().clear();
			editor.getGraphComponent().zoomAndCenter();
		}

		public void actionPerformed(ActionEvent e) {
			AppMainWindow mainWindow = CommandActions.getEditor(e);
			JFileChooser jFileChooser = new JFileChooser(mainWindow.getLastDiretory());

			AppDefaultFileFilter defaultFilter = new AppDefaultFileFilter(".brM",
					"brModeloNext " + mxResources.get("file") + " (.brM)");
			jFileChooser.setFileFilter(defaultFilter);

			ObjectInputStream input = null;
			int returnVal = jFileChooser.showOpenDialog(null);
			boolean isConceptual = true;
			boolean isRelational = false;
			boolean isNoSQL = false;
			try {
				if (returnVal == 0) {
					FileInputStream fileInputStream = new FileInputStream(
							jFileChooser.getSelectedFile().getAbsolutePath());
					input = new ObjectInputStream(fileInputStream);
					Object[] readObject = (Object[]) input.readObject();
					if (readObject.length > 0) {
						mxCell cell = (mxCell) readObject[0];
						isRelational = cell.getValue() instanceof TableObject;
						isNoSQL = cell.getValue() instanceof ufsc.sisinf.brmodelo2all.model.objects.Collection;
					}
					if ((isRelational) || (isNoSQL)) {
						isConceptual = false;
					}
					String title;
					if (isConceptual) {
						title = mxResources.get("newConceptual");
					} else {
						if (isRelational) {
							title = mxResources.get("newRelational");
						} else {
							title = mxResources.get("newNoSQL");
						}
					}
					ModelingEditor newModelingEditor = new ModelingEditor(title, mainWindow, isConceptual,
							isRelational);
					mainWindow.setCurrentEditor(newModelingEditor);
					mainWindow.getEditors().add(newModelingEditor);
					mainWindow.getDesktop().add(newModelingEditor, -1);
					mainWindow.setLastDiretory(jFileChooser.getSelectedFile().getParent());
					newModelingEditor.initialize();

					ModelingManager modelingManager = newModelingEditor.getModelingManager();
					Object[] arrayOfObject1;
					int j = (arrayOfObject1 = readObject).length;
					for (int i = 0; i < j; i++) {
						Object object = arrayOfObject1[i];
						if ((object instanceof mxCell)) {
							mxCell cell = (mxCell) object;
							modelingManager.insertObject(cell);
						}
					}
					newModelingEditor.setCurrentFile(jFileChooser.getSelectedFile());
					resetEditor(newModelingEditor);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static class SaveAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		/**
		 *
		 */
		protected boolean showDialog;

		public SaveAction(boolean showDialog) {
			this.showDialog = showDialog;
		}

		protected void addFilters(JFileChooser fc) {
			fc.addChoosableFileFilter(
					new AppDefaultFileFilter(".brM", "brModeloNext " + mxResources.get("file") + " (.brM)"));
			fc.addChoosableFileFilter(
					new AppDefaultFileFilter(".png", "PNG+XML " + mxResources.get("file") + " (.png)"));
			fc.addChoosableFileFilter(
					new AppDefaultFileFilter(".mxe", "mxGraph Editor " + mxResources.get("file") + " (.mxe)"));
			fc.addChoosableFileFilter(new AppDefaultFileFilter(".svg", "SVG " + mxResources.get("file") + " (.svg)"));
			fc.addChoosableFileFilter(new AppDefaultFileFilter(".html", "VML " + mxResources.get("file") + " (.html)"));
			fc.addChoosableFileFilter(
					new AppDefaultFileFilter(".html", "HTML " + mxResources.get("file") + " (.html)"));

			// Adds a filter for each supported image format
			Object[] imageFormats = ImageIO.getReaderFormatNames();
			HashSet<String> formats = new HashSet<String>();

			for (int i = 0; i < imageFormats.length; i++) {
				String ext = imageFormats[i].toString().toLowerCase();
				formats.add(ext);
			}

			imageFormats = formats.toArray();
			for (int i = 0; i < imageFormats.length; i++) {
				String ext = imageFormats[i].toString();
				fc.addChoosableFileFilter(new AppDefaultFileFilter("." + ext,
						ext.toUpperCase() + " " + mxResources.get("file") + " (." + ext + ")"));
			}

			// Adds filter that accepts all supported image formats
			fc.addChoosableFileFilter(new AppDefaultFileFilter.ImageFileFilter(mxResources.get("allImages")));
		}

		protected void saveXmlPng(ModelingEditor editor, String filename, Color bg) throws IOException {
			mxGraphComponent graphComponent = editor.getGraphComponent();
			mxGraph graph = graphComponent.getGraph();

			// Creates the image for the PNG file
			BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, bg, graphComponent.isAntiAlias(),
					null, graphComponent.getCanvas());

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

					editor.setModified(false);
					editor.setCurrentFile(new File(filename));
				} else {
					JOptionPane.showMessageDialog(graphComponent, mxResources.get("noImageData"));
				}
			} finally {
				outputStream.close();
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			AppMainWindow mainWindow = getEditor(e);
			ModelingEditor editor = mainWindow.getCurrentEditor();

			if (editor != null) {

				String filename = "";

				// Se arquivo nao existe
				if (editor.getCurrentFile() == null || showDialog) {
					JFileChooser jFileChooser = new JFileChooser(mainWindow.getLastDiretory());

					// add Filter
					AppDefaultFileFilter defaultFilter = new AppDefaultFileFilter(".brM",
							"brModeloNext " + mxResources.get("file") + " (.brM)");

					jFileChooser.setFileFilter(defaultFilter);

					// Se botao cancelar, retornar sem salvar
					if (jFileChooser.showDialog(null, mxResources.get("save")) != JFileChooser.APPROVE_OPTION) {
						return;
					}

					// Verificar se arquivo ja existe, se nao sobreescrever
					// retornar sem salvar
					filename = jFileChooser.getSelectedFile().getAbsolutePath();
					if (new File(filename).exists() && JOptionPane.showConfirmDialog(editor.getGraphComponent(),
							mxResources.get("overwriteExistingFile")) != JOptionPane.YES_OPTION) {
						return;
					}

					// Incluir extensao ao nome do arquivo
					AppDefaultFileFilter fileFilter = (AppDefaultFileFilter) jFileChooser.getFileFilter();
					filename += fileFilter.getExtension();
					mainWindow.setLastDiretory(jFileChooser.getSelectedFile().getParent());

				} else {
					// Arquivo ja foi salvo, so manter o mesmo nome
					filename = editor.getCurrentFile().getAbsolutePath();
				}

				// Salvar modelo .brM, filename deve ter sido definido.
				try {

					ObjectOutputStream output = null;
					ModelingComponent modelingComponent = getModelingComponent(e);

					FileOutputStream fileout = new FileOutputStream(filename);
					output = new ObjectOutputStream(fileout);

					mxRectangle rect = modelingComponent.getGraph().getGraphBounds();

					int x = (int) rect.getX();
					int y = (int) rect.getY();

					Rectangle ret = new Rectangle(x + 60000, y + 60000);

					Object[] cells = modelingComponent.getCells(ret);
					output.writeObject(cells);
					output.close();
					fileout.close();

					// Setar o current file do editor
					editor.setModified(false);
					editor.setCurrentFile(new File(filename));

				} catch (IOException ex) {
				}
			}
		}

		public void actionPerformed2(ModelingEditor m, AppMainWindow a) {
			AppMainWindow mainWindow = a;
			ModelingEditor editor = m;

			if (editor != null) {

				String filename = "";

				// Se arquivo nao existe
				if (editor.getCurrentFile() == null || showDialog) {
					JFileChooser jFileChooser = new JFileChooser(mainWindow.getLastDiretory());

					// add Filter
					AppDefaultFileFilter defaultFilter = new AppDefaultFileFilter(".brM",
							"brModeloNext " + mxResources.get("file") + " (.brM)");
					jFileChooser.setFileFilter(defaultFilter);

					// Se botao cancelar, retornar sem salvar
					if (jFileChooser.showDialog(null, mxResources.get("save")) != JFileChooser.APPROVE_OPTION) {
						return;
					}

					// Verificar se arquivo ja existe, se nao sobreescrever
					// retornar sem salvar
					filename = jFileChooser.getSelectedFile().getAbsolutePath();
					if (new File(filename).exists() && JOptionPane.showConfirmDialog(editor.getGraphComponent(),
							mxResources.get("overwriteExistingFile")) != JOptionPane.YES_OPTION) {
						return;
					}

					// Incluir extensao ao nome do arquivo
					AppDefaultFileFilter fileFilter = (AppDefaultFileFilter) jFileChooser.getFileFilter();
					filename += fileFilter.getExtension();
					mainWindow.setLastDiretory(jFileChooser.getSelectedFile().getParent());

				} else {
					// Arquivo ja foi salvo, so manter o mesmo nome
					filename = editor.getCurrentFile().getAbsolutePath();
				}

				// Salvar modelo .brM, filename deve ter sido definido.
				try {
					ObjectOutputStream output = null;
					ModelingComponent modelingComponent = (ModelingComponent) m.getModelingComponent();

					FileOutputStream fileout = new FileOutputStream(filename);
					output = new ObjectOutputStream(fileout);

					mxRectangle rect = modelingComponent.getGraph().getGraphBounds();

					int x = (int) rect.getX();
					int y = (int) rect.getY();

					Rectangle ret = new Rectangle(x + 60000, y + 60000);

					Object[] cells = modelingComponent.getCells(ret);
					output.writeObject(cells);
					output.close();
					fileout.close();

					// Setar o current file do editor
					editor.setModified(false);
					editor.setCurrentFile(new File(filename));

				} catch (IOException ex) {
				}
			}
		}

	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ToggleDirtyAction extends AbstractAction {

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraphComponent graphComponent = (mxGraphComponent) getModelingComponent(e);
			if (graphComponent != null) {
				graphComponent.showDirtyRectangle = !graphComponent.showDirtyRectangle;
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ToggleConnectModeAction extends AbstractAction {

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraphComponent graphComponent = (mxGraphComponent) getModelingComponent(e);
			if (graphComponent != null) {
				mxConnectionHandler handler = graphComponent.getConnectionHandler();
				handler.setHandleEnabled(!handler.isHandleEnabled());
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ToggleCreateTargetItem extends JCheckBoxMenuItem {

		/**
		 *
		 */
		public ToggleCreateTargetItem(final AppMainWindow editor, String name) {
			super(name);
			setSelected(true);

			addActionListener(new ActionListener() {
				/**
				 *
				 */
				public void actionPerformed(ActionEvent e) {
					mxGraphComponent graphComponent = editor.getCurrentEditor().getGraphComponent();

					if (graphComponent != null) {
						mxConnectionHandler handler = graphComponent.getConnectionHandler();
						handler.setCreateTarget(!handler.isCreateTarget());
						setSelected(handler.isCreateTarget());
					}
				}
			});
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class PromptPropertyAction extends AbstractAction {

		/**
		 *
		 */
		protected Object target;
		/**
		 *
		 */
		protected String fieldname, message;

		/**
		 *
		 */
		public PromptPropertyAction(Object target, String message) {
			this(target, message, message);
		}

		/**
		 *
		 */
		public PromptPropertyAction(Object target, String message, String fieldname) {
			this.target = target;
			this.message = message;
			this.fieldname = fieldname;
		}

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof Component) {
				try {
					Method getter = target.getClass().getMethod("get" + fieldname);
					Object current = getter.invoke(target);

					// TODO: Support other atomic types
					if (current instanceof Integer) {
						Method setter = target.getClass().getMethod("set" + fieldname, new Class[] { int.class });

						String value = (String) JOptionPane.showInputDialog((Component) e.getSource(), "Value", message,
								JOptionPane.PLAIN_MESSAGE, null, null, current);

						if (value != null) {
							setter.invoke(target, Integer.parseInt(value));
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			// Repaints the graph component
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				graphComponent.repaint();
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class TogglePropertyItem extends JCheckBoxMenuItem {

		/**
		 *
		 */
		public TogglePropertyItem(Object target, String name, String fieldname) {
			this(target, name, fieldname, false);
		}

		/**
		 *
		 */
		public TogglePropertyItem(Object target, String name, String fieldname, boolean refresh) {
			this(target, name, fieldname, refresh, null);
		}

		/**
		 *
		 */
		public TogglePropertyItem(final Object target, String name, final String fieldname, final boolean refresh,
				ActionListener listener) {
			super(name);

			// Since action listeners are processed last to first we add the
			// given
			// listener here which means it will be processed after the one
			// below
			if (listener != null) {
				addActionListener(listener);
			}

			addActionListener(new ActionListener() {
				/**
				 *
				 */
				public void actionPerformed(ActionEvent e) {
					execute(target, fieldname, refresh);
				}
			});

			PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * java.beans.PropertyChangeListener#propertyChange(java.beans
				 * .PropertyChangeEvent)
				 */
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName().equalsIgnoreCase(fieldname)) {
						update(target, fieldname);
					}
				}
			};

			if (target instanceof mxGraphComponent) {
				((mxGraphComponent) target).addPropertyChangeListener(propertyChangeListener);
			} else if (target instanceof mxGraph) {
				((mxGraph) target).addPropertyChangeListener(propertyChangeListener);
			}

			update(target, fieldname);
		}

		/**
		 *
		 */
		public void update(Object target, String fieldname) {
			if (target != null && fieldname != null) {
				try {
					Method getter = target.getClass().getMethod("is" + fieldname);

					if (getter != null) {
						Object current = getter.invoke(target);

						if (current instanceof Boolean) {
							setSelected(((Boolean) current).booleanValue());
						}
					}
				} catch (Exception e) {
					// ignore
				}
			}
		}

		/**
		 *
		 */
		public void execute(Object target, String fieldname, boolean refresh) {
			if (target != null && fieldname != null) {
				try {
					Method getter = target.getClass().getMethod("is" + fieldname);
					Method setter = target.getClass().getMethod("set" + fieldname, new Class[] { boolean.class });

					Object current = getter.invoke(target);

					if (current instanceof Boolean) {
						boolean value = !((Boolean) current).booleanValue();
						setter.invoke(target, value);
						setSelected(value);
					}

					if (refresh) {
						mxGraph graph = null;

						if (target instanceof mxGraph) {
							graph = (mxGraph) target;
						} else if (target instanceof mxGraphComponent) {
							graph = ((mxGraphComponent) target).getGraph();
						}

						graph.refresh();
					}
				} catch (Exception e) {
					// ignore
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class HistoryAction extends AbstractAction {

		/**
		 *
		 */
		protected boolean undo;

		/**
		 *
		 */
		public HistoryAction(boolean undo) {
			this.undo = undo;
		}

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			ModelingEditor editor = getEditor(e).getCurrentEditor();

			if (editor != null) {
				if (undo) {
					editor.getUndoManager().undo();
				} else {
					editor.getUndoManager().redo();
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class FontStyleAction extends AbstractAction {

		/**
		 *
		 */
		protected boolean bold;

		/**
		 *
		 */
		public FontStyleAction(boolean bold) {
			this.bold = bold;
		}

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraphComponent graphComponent = (mxGraphComponent) getModelingComponent(e);
			if (graphComponent != null) {
				Component editorComponent = null;

				if (graphComponent.getCellEditor() instanceof mxCellEditor) {
					editorComponent = ((mxCellEditor) graphComponent.getCellEditor()).getEditor();
				}

				if (editorComponent instanceof JEditorPane) {
					JEditorPane editorPane = (JEditorPane) editorComponent;
					int start = editorPane.getSelectionStart();
					int ende = editorPane.getSelectionEnd();
					String text = editorPane.getSelectedText();

					if (text == null) {
						text = "";
					}

					try {
						HTMLEditorKit editorKit = new HTMLEditorKit();
						HTMLDocument document = (HTMLDocument) editorPane.getDocument();
						document.remove(start, (ende - start));
						editorKit.insertHTML(document, start,
								((bold) ? "<b>" : "<i>") + text + ((bold) ? "</b>" : "</i>"), 0, 0,
								(bold) ? HTML.Tag.B : HTML.Tag.I);
					} catch (Exception ex) {
						ex.printStackTrace();
					}

					editorPane.requestFocus();
					editorPane.select(start, ende);
				} else {
					mxIGraphModel model = graphComponent.getGraph().getModel();
					model.beginUpdate();
					try {
						graphComponent.stopEditing(false);
						graphComponent.getGraph().toggleCellStyleFlags(mxConstants.STYLE_FONTSTYLE,
								(bold) ? mxConstants.FONT_BOLD : mxConstants.FONT_ITALIC);
					} finally {
						model.endUpdate();
					}
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class NewConceptualModelingAction extends AbstractAction {

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			AppMainWindow mainWindow = getEditor(e);

			ModelingEditor newModelingEditor = new ModelingEditor(mxResources.get("newConceptual"), mainWindow, true,
					false);
			mainWindow.setCurrentEditor(newModelingEditor);
			mainWindow.getEditors().add(newModelingEditor);
			mainWindow.getDesktop().add(newModelingEditor, -1);
			newModelingEditor.initialize();
		}
	}

	@SuppressWarnings("serial")
	public static class NewRelationalModelingAction extends AbstractAction {

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			AppMainWindow mainWindow = getEditor(e);

			ModelingEditor newModelingEditor = new ModelingEditor(mxResources.get("newRelational"), mainWindow, false,
					true);
			mainWindow.setCurrentEditor(newModelingEditor);
			mainWindow.getEditors().add(newModelingEditor);
			mainWindow.getDesktop().add(newModelingEditor, -1);
			newModelingEditor.initialize();
		}
	}

	@SuppressWarnings("serial")
	public static class NewNoSQLModelingAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			AppMainWindow mainWindow = getEditor(e);

			ModelingEditor newModelingEditor = new ModelingEditor(mxResources.get("newNoSQL"), mainWindow, false,
					false);
			mainWindow.setCurrentEditor(newModelingEditor);
			mainWindow.getEditors().add(newModelingEditor);
			mainWindow.getDesktop().add(newModelingEditor, -1);
			newModelingEditor.initialize();
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ToggleAction extends AbstractAction {

		/**
		 *
		 */
		protected String key;
		/**
		 *
		 */
		protected boolean defaultValue;

		/**
		 *
		 * @param key
		 */
		public ToggleAction(String key) {
			this(key, false);
		}

		/**
		 *
		 * @param key
		 */
		public ToggleAction(String key, boolean defaultValue) {
			this.key = key;
			this.defaultValue = defaultValue;
		}

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraph graph = getModeling(e);

			if (graph != null) {
				graph.toggleCellStyles(key, defaultValue);
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class SetLabelPositionAction extends AbstractAction {

		/**
		 *
		 */
		protected String labelPosition, alignment;

		/**
		 *
		 * @param key
		 */
		public SetLabelPositionAction(String labelPosition, String alignment) {
			this.labelPosition = labelPosition;
			this.alignment = alignment;
		}

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraph graph = getModeling(e);

			if (graph != null && !graph.isSelectionEmpty()) {
				graph.getModel().beginUpdate();
				try {
					// Checks the orientation of the alignment to use the
					// correct constants
					if (labelPosition.equals(mxConstants.ALIGN_LEFT) || labelPosition.equals(mxConstants.ALIGN_CENTER)
							|| labelPosition.equals(mxConstants.ALIGN_RIGHT)) {
						graph.setCellStyles(mxConstants.STYLE_LABEL_POSITION, labelPosition);
						graph.setCellStyles(mxConstants.STYLE_ALIGN, alignment);
					} else {
						graph.setCellStyles(mxConstants.STYLE_VERTICAL_LABEL_POSITION, labelPosition);
						graph.setCellStyles(mxConstants.STYLE_VERTICAL_ALIGN, alignment);
					}
				} finally {
					graph.getModel().endUpdate();
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class SetStyleAction extends AbstractAction {

		/**
		 *
		 */
		protected String value;

		/**
		 *
		 * @param key
		 */
		public SetStyleAction(String value) {
			this.value = value;
		}

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraph graph = getModeling(e);

			if (graph != null && !graph.isSelectionEmpty()) {
				graph.setCellStyle(value);
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class KeyValueAction extends AbstractAction {

		/**
		 *
		 */
		protected String key, value;

		/**
		 *
		 * @param key
		 */
		public KeyValueAction(String key) {
			this(key, null);
		}

		/**
		 *
		 * @param key
		 */
		public KeyValueAction(String key, String value) {
			this.key = key;
			this.value = value;
		}

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraph graph = getModeling(e);

			if (graph != null && !graph.isSelectionEmpty()) {
				graph.setCellStyles(key, value);
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class PromptValueAction extends AbstractAction {

		/**
		 *
		 */
		protected String key, message;

		/**
		 *
		 * @param key
		 */
		public PromptValueAction(String key, String message) {
			this.key = key;
			this.message = message;
		}

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof Component) {
				mxGraph graph = getModeling(e);

				if (graph != null && !graph.isSelectionEmpty()) {
					String value = (String) JOptionPane.showInputDialog((Component) e.getSource(),
							mxResources.get("value"), message, JOptionPane.PLAIN_MESSAGE, null, null, "");

					if (value != null) {
						if (value.equals(mxConstants.NONE)) {
							value = null;
						}

						graph.setCellStyles(key, value);
					}
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class AlignCellsAction extends AbstractAction {

		/**
		 *
		 */
		protected String align;

		/**
		 *
		 * @param key
		 */
		public AlignCellsAction(String align) {
			this.align = align;
		}

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraph graph = getModeling(e);

			if (graph != null && !graph.isSelectionEmpty()) {
				graph.alignCells(align);
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class AutosizeAction extends AbstractAction {

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraph graph = getModeling(e);

			if (graph != null && !graph.isSelectionEmpty()) {
				Object[] cells = graph.getSelectionCells();
				mxIGraphModel model = graph.getModel();

				model.beginUpdate();
				try {
					for (int i = 0; i < cells.length; i++) {
						graph.updateCellSize(cells[i]);

					}
				} finally {
					model.endUpdate();
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ColorAction extends AbstractAction {

		/**
		 *
		 */
		protected String name, key;

		/**
		 *
		 * @param key
		 */
		public ColorAction(String name, String key) {
			this.name = name;
			this.key = key;
		}

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraphComponent graphComponent = (mxGraphComponent) getModelingComponent(e);
			if (graphComponent != null) {
				mxGraph graph = graphComponent.getGraph();

				if (!graph.isSelectionEmpty()) {
					Color newColor = JColorChooser.showDialog(graphComponent, name, null);

					if (newColor != null) {
						graph.setCellStyles(key, mxUtils.hexString(newColor));
					}
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class BackgroundImageAction extends AbstractAction {

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraphComponent graphComponent = (mxGraphComponent) getModelingComponent(e);
			if (graphComponent != null) {
				String value = (String) JOptionPane.showInputDialog(graphComponent, mxResources.get("backgroundImage"),
						"URL", JOptionPane.PLAIN_MESSAGE, null, null,
						"http://www.callatecs.com/images/background2.JPG");

				if (value != null) {
					if (value.length() == 0) {
						graphComponent.setBackgroundImage(null);
					} else {
						Image background = mxUtils.loadImage(value);
						// Incorrect URLs will result in no image.
						// TODO provide feedback that the URL is not correct
						if (background != null) {
							graphComponent.setBackgroundImage(new ImageIcon(background));
						}
					}

					// Forces a repaint of the outline
					graphComponent.getGraph().repaint();
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class BackgroundAction extends AbstractAction {

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraphComponent graphComponent = (mxGraphComponent) getModelingComponent(e);
			if (graphComponent != null) {
				Color newColor = JColorChooser.showDialog(graphComponent, mxResources.get("background"), null);

				if (newColor != null) {
					graphComponent.getViewport().setOpaque(false);
					graphComponent.setBackground(newColor);
				}

				// Forces a repaint of the outline
				graphComponent.getGraph().repaint();
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class PageBackgroundAction extends AbstractAction {

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraphComponent graphComponent = (mxGraphComponent) getModelingComponent(e);
			if (graphComponent != null) {
				Color newColor = JColorChooser.showDialog(graphComponent, mxResources.get("pageBackground"), null);

				if (newColor != null) {
					graphComponent.setPageBackgroundColor(newColor);
				}

				// Forces a repaint of the component
				graphComponent.repaint();
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class StyleAction extends AbstractAction {

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraphComponent graphComponent = (mxGraphComponent) getModelingComponent(e);
			if (graphComponent != null) {
				mxGraph graph = graphComponent.getGraph();
				String initial = graph.getModel().getStyle(graph.getSelectionCell());
				String value = (String) JOptionPane.showInputDialog(graphComponent, mxResources.get("style"),
						mxResources.get("style"), JOptionPane.PLAIN_MESSAGE, null, null, initial);

				if (value != null) {
					graph.setCellStyle(value);
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class InsertConceptualObjectAction extends AbstractAction {

		/**
		 *
		 */
		protected String name;

		/**
		 *
		 * @param key
		 */
		public InsertConceptualObjectAction(String name) {
			this.name = name;
		}

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			AppMainWindow mainWindow = getEditor(e);

			// clear selection first, so it won't select and unselect the object
			// throught the menu bar
			mainWindow.conceptualObjects.clearSelection();

			int count = mainWindow.conceptualObjects.getComponentCount();
			int index = 0;
			boolean found = false;
			while (!found && index < count) {
				Component component = mainWindow.conceptualObjects.getComponent(index);
				if (component instanceof JLabel) {
					JButton label = (JButton) component;
					if (label.getToolTipText() == name) {
						mainWindow.conceptualObjects.setSelectionEntry(label,
								mainWindow.conceptualObjects.getTransferable(index));
						found = true;
					}
				}
				index++;
			}
		}
	}

	/**
	 *
	 */
	// @SuppressWarnings("serial")
	// public static class InsertLogicalObjectAction extends AbstractAction {
	//
	// /**
	// *
	// */
	// protected String name;
	//
	// /**
	// *
	// * @param key
	// */
	// public InsertLogicalObjectAction(String name) {
	// this.name = name;
	// }
	//
	// /**
	// *
	// */
	// public void actionPerformed(ActionEvent e) {
	// AppMainWindow mainWindow = getEditor(e);
	//
	// // clear selection first, so it won't select and unselect the object
	// // throught the menu bar
	// mainWindow.relationalObjects.clearSelection();
	//
	// int count = mainWindow.relationalObjects.getComponentCount();
	// int index = 0;
	// boolean found = false;
	// while (!found && index < count) {
	// Component component = mainWindow.relationalObjects
	// .getComponent(index);
	// if (component instanceof JLabel) {
	// JButton label = (JButton) component;
	// if (label.getToolTipText() == name) {
	// mainWindow.relationalObjects.setSelectionEntry(label,
	// mainWindow.relationalObjects
	// .getTransferable(index));
	// found = true;
	// }
	// }
	// index++;
	// }
	// }
	// }

	@SuppressWarnings("serial")
	public static class InsertNoSQLObjectAction extends AbstractAction {

		/**
		 *
		 */
		protected String name;

		/**
		 *
		 * @param key
		 */
		public InsertNoSQLObjectAction(String name) {
			this.name = name;
		}

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			AppMainWindow mainWindow = getEditor(e);

			// clear selection first, so it won't select and unselect the object
			// throught the menu bar
			mainWindow.noSQLObjects.clearSelection();

			int count = mainWindow.noSQLObjects.getComponentCount();
			int index = 0;
			boolean found = false;
			while (!found && index < count) {
				Component component = mainWindow.noSQLObjects.getComponent(index);
				if (component instanceof JLabel) {
					JButton label = (JButton) component;
					if (label.getToolTipText() == name) {
						mainWindow.noSQLObjects.setSelectionEntry(label,
								mainWindow.noSQLObjects.getTransferable(index));
						found = true;
					}
				}
				index++;
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ConvertConceptualToLogicalAction extends AbstractAction {

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {

			ModelingComponent modelingComponent = getModelingComponent(e);

			AppMainWindow mainWindow = getEditor(e);

			ModelingEditor newModelingEditor = new ModelingEditor(mxResources.get("newRelational"), mainWindow, false,
					true);
			mainWindow.setCurrentEditor(newModelingEditor);
			mainWindow.getEditors().add(newModelingEditor);
			mainWindow.getDesktop().add(newModelingEditor, -1);
			newModelingEditor.initialize();

			ConceptualConversor conversor = new ConceptualConversor(modelingComponent, newModelingEditor, mainWindow);
			conversor.convertModeling();
		}
	}

	/**
	*
	*/
	@SuppressWarnings("serial")
	public static class ConvertConceptualToNoSqlAction extends AbstractAction {

		/**
		*
		*/
		public void actionPerformed(ActionEvent e) {

			ModelingComponent modelingComponent = getModelingComponent(e);

			AppMainWindow mainWindow = getEditor(e);

			ModelingEditor newModelingEditor = new ModelingEditor(mxResources.get("newNoSQL"), mainWindow, false,
					false);
			mainWindow.setCurrentEditor(newModelingEditor);
			mainWindow.getEditors().add(newModelingEditor);
			mainWindow.getDesktop().add(newModelingEditor, -1);
			newModelingEditor.initialize();

			ConceptualToNoSql conversor = new ConceptualToNoSql(modelingComponent, newModelingEditor, mainWindow);
			conversor.convertModeling();
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ConvertLogicalToPhysicalAction extends AbstractAction {

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			ModelingComponent modelingComponent = getModelingComponent(e);
			AppMainWindow mainWindow = getEditor(e);
			JFrame frame = (JFrame) SwingUtilities.windowForComponent(mainWindow);
			SqlEditor sqlEditor = new SqlEditor(frame);

			LogicalConversor conversor = new LogicalConversor(modelingComponent, sqlEditor);
			conversor.convertModeling();

			mainWindow.openSqlEditor(sqlEditor);
		}
	}

	@SuppressWarnings("serial")
	public static class ConvertLogicalToPhysicalNoSQLAction extends AbstractAction {

		/**
		 * 
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			AppMainWindow mainWindow = getEditor(e);
			ModelingComponent modelingComponent = getModelingComponent(e);
			NoSqlEditor sqlEditor = new NoSqlEditor();
			sqlEditor.setTitle(mxResources.get("nosqlEditor"));
			mainWindow.getDesktop().add(sqlEditor, -1);
			LogicalConversorToNoSQL conversor = new LogicalConversorToNoSQL(modelingComponent, sqlEditor);
			conversor.convertModeling();
			mainWindow.openNoSqlEditor(sqlEditor);
		}

	}

	@SuppressWarnings("serial")
	public static class ConvertLogicalToMongoAction extends AbstractAction {

		/**
		 *
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			AppMainWindow mainWindow = getEditor(e);
			ModelingComponent modelingComponent = getModelingComponent(e);
			NoSqlEditor sqlEditor = new NoSqlEditor();
			sqlEditor.setTitle(mxResources.get("nosqlEditorMongodb"));
			mainWindow.getDesktop().add(sqlEditor, -1);
			LogicalConversorToDocument documentConversor = new LogicalConversorToDocument(modelingComponent, sqlEditor);
			documentConversor.convertModeling();
			mainWindow.openNoSqlEditor(sqlEditor);
		}

	}

	@SuppressWarnings("serial")
	public static class ConvertLogicalToCassandraAction extends AbstractAction {

		/**
		 *
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			AppMainWindow mainWindow = getEditor(e);
			ModelingComponent modelingComponent = getModelingComponent(e);
			NoSqlEditor sqlEditor = new NoSqlEditor();
			sqlEditor.setTitle(mxResources.get("nosqlEditor"));
			mainWindow.getDesktop().add(sqlEditor, -1);
			LogicalConversorToNoSQL conversor = new LogicalConversorToNoSQL(modelingComponent, sqlEditor);
			conversor.convertModeling();
			mainWindow.openNoSqlEditor(sqlEditor);
		}

	}

	@SuppressWarnings("serial")
	public static class ConvertLogicalToRedisAction extends AbstractAction {

		/**
		 *
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			AppMainWindow mainWindow = getEditor(e);
			ModelingComponent modelingComponent = getModelingComponent(e);
			NoSqlEditor sqlEditor = new NoSqlEditor();
			sqlEditor.setTitle(mxResources.get("nosqlEditor"));
			mainWindow.getDesktop().add(sqlEditor, -1);
			LogicalConversorToNoSQL conversor = new LogicalConversorToNoSQL(modelingComponent, sqlEditor);
			conversor.convertModeling();
			mainWindow.openNoSqlEditor(sqlEditor);
		}

	}


	@SuppressWarnings("serial")
	public static class NosqlConfigurationAction extends AbstractAction {

		/**
		 *
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			AppMainWindow mainWindow = getEditor(e);
			mainWindow.displayNosqlConvertOptions();
		}

	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class AssociativeEntityPromotionAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			mxGraphComponent graphComponent = (mxGraphComponent) getModelingComponent(e);
			if (graphComponent != null) {
				mxGraph graph = graphComponent.getGraph();

				if (!graph.isSelectionEmpty()) {
					promote(graph);
				}
			}
		}

		public void promote(mxGraph graph) {
			Object selectedCell = graph.getSelectionCell();
			graph.getModel().beginUpdate();
			try {
				mxCell relationCell = (mxCell) selectedCell;
				RelationObject relation = (RelationObject) relationCell.getValue();
				mxGeometry geometry = relationCell.getGeometry();
				mxRectangle newBounds = new mxRectangle(geometry.getX(), geometry.getY(), 150, 80);
				graph.getModel().setStyle(relationCell, "associativeEntity");
				graph.resizeCell(relationCell, newBounds);

				String relationName = relation.getName();
				AssociativeRelationObject associativeRelationObject = new AssociativeRelationObject(relationName); // $NON-NLS-1$
				AssociativeEntityObject entityObject = new AssociativeEntityObject(mxResources.get("associativeEntity"), //$NON-NLS-1$
						associativeRelationObject);
				relationCell.setValue(entityObject);
				Object newRelationObject = graph.insertVertex(relationCell, null, associativeRelationObject, 15, 20,
						120, 50, "associativeRelation");

				entityObject.addChildObject(newRelationObject); // $NON-NLS-1$
				Iterator<Object> iterator = relation.getChildObjects().iterator();
				while (iterator.hasNext()) {
					Object object = iterator.next();
					associativeRelationObject.addChildObject(object);
					((AttributeObject) ((mxCell) object).getValue()).setParentObject(newRelationObject);
				}
				relation.getChildObjects().clear();

				int count = relationCell.getEdgeCount();
				for (int i = 0; i < count; i++) {
					mxCell edge = (mxCell) relationCell.getEdgeAt(i);
					if (edge.getSource() == relationCell) {
						edge.setSource((mxICell) newRelationObject);
					} else {
						edge.setTarget((mxICell) newRelationObject);
					}
				}

			} finally {
				graph.getModel().endUpdate();
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class EntityPromotionAction extends AbstractAction {

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraphComponent graphComponent = (mxGraphComponent) getModelingComponent(e);
			if (graphComponent != null) {
				mxGraph graph = graphComponent.getGraph();

				if (!graph.isSelectionEmpty()) {
					Object selectedCell = graph.getSelectionCell();
					graph.getModel().beginUpdate();
					try {
						mxCell cell = (mxCell) selectedCell;

					} finally {
						graph.getModel().endUpdate();
					}
				}
			}
		}
	}

}
