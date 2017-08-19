package ufsc.sisinf.brmodelo2all.ui;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;

public class CopyHandler extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int controle = 0;

	public CopyHandler(int i) {

		if (i == 1 && controle > 0) {

			controle++;
			ActionEvent cop = null;
			actionPerformed(cop);

		}

		controle++;

	}

	public void actionPerformed(ActionEvent cop) {
		Robot robot;

		try {
			robot = new Robot();
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_C);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			robot.keyRelease(KeyEvent.VK_C);
		} catch (AWTException d) {
			// TODO Auto-generated catch block
			d.printStackTrace();
		} catch (NullPointerException d) {
			// TODO Auto-generated catch block

		}

	}

}
