package tp.zync.display;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import tp.zync.RecvWorker;
//import tp.zync.SendWorker;
//import tp.zync.old.SyncRecv;
//import tp.zync.old.SyncSend;

public class OpenFrameRecv extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JTextField tf1;
	private JTextField tf2;
	private JTextField tf3;
	private JTextArea tf4;
	private JScrollPane tfScrollPane;
	private JScrollPane tfScrollPane4;
	private JScrollPane tfScrollPane3;
	private JScrollPane tfScrollPane2;
	private JButton startButton;
	private JButton stopButton;
	private JButton browse;
	private JFileChooser directorySelect;

	public OpenFrameRecv() throws HeadlessException {
		super();

		getContentPane().setLayout(null);

		tf1=new JTextField();
		tfScrollPane=new JScrollPane(tf1);
		tfScrollPane.setBounds(50, 10, 100, 30);
		getContentPane().add(tfScrollPane);
		tf1.setText("localhost");
		tf1.setVisible(false);

		tf2=new JTextField();
		tfScrollPane2=new JScrollPane(tf2);
		tfScrollPane2.setBounds(150, 10, 50, 30);
		getContentPane().add(tfScrollPane2);
		tf2.setText("27022");
		
		tf3=new JTextField();
		tfScrollPane3=new JScrollPane(tf3);
		tfScrollPane3.setBounds(50, 115, 500, 30);
		getContentPane().add(tfScrollPane3);
		tf3.setText("F:\\00 xtra OS data\\Downloads\\test");
		
		browse=new JButton("Browse");
		browse.setBounds(70, 70, 100, 30);
		getContentPane().add(browse);
		browse.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				directorySelect=new JFileChooser();
				directorySelect.setDialogTitle("Browse");
				directorySelect.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				directorySelect.setOpaque(false);
				int optionSelected=directorySelect.showOpenDialog(getContentPane());

				if(optionSelected==JFileChooser.APPROVE_OPTION)
				{
					setVisible(true);
					System.out.println(directorySelect.getSelectedFile().getAbsolutePath());
					tf3.setText(directorySelect.getSelectedFile().getAbsolutePath());
				}
				if(optionSelected==JFileChooser.CANCEL_OPTION)
				{
					setVisible(true);
					//System.out.println(directorySelect.getSelectedFile().getAbsolutePath());
				}
			}
		});


		startButton=new JButton("Start");
		startButton.setBounds(70, 180, 300, 40);
		getContentPane().add(startButton);
		startButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("start Button Selected");
				
				tfScrollPane.setVisible(false);
				tfScrollPane2.setVisible(false);
				tfScrollPane3.setVisible(false);
				browse.setVisible(false);
				startButton.setVisible(false);
				tfScrollPane4.setVisible(true);
				stopButton.setVisible(true);

				String currentFolder = tf3.getText();
				String ip = tf1.getText();
				String port = tf2.getText();
				
				tf4.append(currentFolder + '\t' + ip + '\t' + port + '\n');

				tf4.append("start kar ra");
				tf4.append("\n");
				
			    RecvWorker rw = new RecvWorker(currentFolder, port, tf4);
			    rw.execute();
				
			}
		});
		

		stopButton=new JButton("Stop");
		stopButton.setBounds(100, 5, 300, 30);
		getContentPane().add(stopButton);
		stopButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				System.out.println("stop Button Selected");
				stopButton.setText("stop ni hoga");
				
			}
		});
		stopButton.setVisible(false);
		
		
		tf4=new JTextArea();
		tfScrollPane4=new JScrollPane(tf4);
		tfScrollPane4.setBounds(5, 45, 550, 200);
		getContentPane().add(tfScrollPane4);
		tfScrollPane4.setVisible(false);
		

	}

}
