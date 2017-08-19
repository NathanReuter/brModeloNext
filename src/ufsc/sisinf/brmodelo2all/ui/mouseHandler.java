package ufsc.sisinf.brmodelo2all.ui;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

public class mouseHandler extends AbstractAction {

	private int controle = 0;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public mouseHandler(int i) {
		if (i == 1 && controle > 0) {

			ActionEvent cut = null;
			actionPerformed(cut);

		}

		controle++;

	}

	public void actionPerformed(ActionEvent cut) {
		Robot robot;

		try {
			robot = new Robot();
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_X);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			robot.keyRelease(KeyEvent.VK_X);
		} catch (AWTException d) {
			// TODO Auto-generated catch block
			d.printStackTrace();
		} catch (NullPointerException d) {
			// TODO Auto-generated catch block

		}

	}

	public static Action getCopyAction() {
		// TODO Auto-generated method stub
		return null;
	}

	public static Action getDeleteAction() {
		// TODO Auto-generated method stub
		return null;
	}

	public static Action getPasteAction() {
		return null;

	}

	// public static Action getCutAction() {
	//
	//
	// Robot robot;
	//
	// try {
	// robot = new Robot();
	// robot.keyPress(KeyEvent.VK_CONTROL);
	// robot.keyPress(KeyEvent.VK_X);
	// robot.keyRelease(KeyEvent.VK_CONTROL);
	// robot.keyRelease(KeyEvent.VK_X);
	// } catch (AWTException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return null;
	// }
	//
	//

}
