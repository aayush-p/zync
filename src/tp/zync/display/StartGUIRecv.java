package tp.zync.display;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class StartGUIRecv {

	public static void main(String[] args) {

	    // schedule this for the event dispatch thread (edt)
	    SwingUtilities.invokeLater(new Runnable()
	    {
	      public void run()
	      {
	    	  JFrame jf = new OpenFrameRecv();
	    	  jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	    	  // set the jframe size and location, and make it visible
	    	  jf.setPreferredSize(new Dimension(600, 300));
	    	  jf.pack();
	    	  jf.setLocationRelativeTo(null);
	    	  jf.setTitle("Zync Receiver 0.2");
	    	  jf.setVisible(true);
//	    	  displayJFrame();
	      }
	    });
	    
	}

}
