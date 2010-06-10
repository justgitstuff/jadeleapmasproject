import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;

/**
   @author Ancuta Iordache , Lorena Bacanu, Andrei Avram
 */
class AWTtraficGui extends Frame implements traficGui {
	private Vehicle myAgent;
	private TextField writeTf;
	private TextArea allTa;
	private ParticipantsFrame participantsFrame;
	
	AWTtraficGui(Vehicle a) {
		myAgent = a;
		
		setTitle("Chat: "+myAgent.getLocalName());
		setSize(getProperSize(256, 320));
		Panel p = new Panel();
		p.setLayout(new BorderLayout());
		writeTf = new TextField();
		p.add(writeTf, BorderLayout.CENTER);
		Button b = new Button("Send");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		  	String s = writeTf.getText();
		  	if (s != null && !s.equals("")) {
			  	myAgent.handleSpoken(s);
			  	writeTf.setText("");
		  	}
			} 
		} );
		p.add(b, BorderLayout.EAST);
		add(p, BorderLayout.NORTH);
		
		allTa = new TextArea();
		allTa.setEditable(false);
		allTa.setBackground(Color.white);
		add(allTa, BorderLayout.CENTER);
		
		b = new Button("Participants");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!participantsFrame.isVisible()) {
					participantsFrame.setVisible(true);
				}	
			} 
		} );
		add(b, BorderLayout.SOUTH);
		
		participantsFrame = new ParticipantsFrame(this, myAgent.getLocalName());
		
		addWindowListener(new	WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myAgent.doDelete();
			}
		} );
		
		show();
	}
	
	public void notifyParticipantsChanged(String[] names) {
		if (participantsFrame != null) {
			participantsFrame.refresh(names);
		}
	}
	
	public void notifySpoken(String speaker, String sentence) {
		allTa.append(speaker+": "+sentence+"\n");
	}
	
	Dimension getProperSize(int maxX, int maxY) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screenSize.width < maxX ? screenSize.width : maxX);
		int y = (screenSize.height < maxY ? screenSize.height : maxY);
		return new Dimension(x, y);
	}
	
	public void dispose() {
		participantsFrame.dispose();
		super.dispose();
	}
	
	
}

