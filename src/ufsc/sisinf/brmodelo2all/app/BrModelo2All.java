package ufsc.sisinf.brmodelo2all.app;

import javax.swing.UIManager;

import ufsc.sisinf.brmodelo2all.ui.AppMainWindow;
import ufsc.sisinf.brmodelo2all.ui.MenuBar;

public class BrModelo2All {
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		AppMainWindow app = new AppMainWindow();
		app.createFrame(new MenuBar(app)).setVisible(true);
	}
}
