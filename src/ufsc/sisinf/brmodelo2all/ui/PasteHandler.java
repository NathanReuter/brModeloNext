package ufsc.sisinf.brmodelo2all.ui;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;

public class PasteHandler extends AbstractAction {

	private int controle = 0;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PasteHandler(int i) {
		if (i == 1 && controle > 0) {

			ActionEvent pas = null;
			actionPerformed(pas);

		}

		controle++;

	}

	public void actionPerformed(ActionEvent pas) {
		Robot robot;

		try {
			robot = new Robot();

			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			robot.keyRelease(KeyEvent.VK_V);

		} catch (AWTException d) {
			// TODO Auto-generated catch block
			d.printStackTrace();
		} catch (NullPointerException d) {
			// TODO Auto-generated catch block

		}

	}

}
