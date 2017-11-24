package src;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import pt.lsts.imc.EstimatedState;
import pt.lsts.imc.lsf.batch.LsfBatch;
import pt.lsts.imc.net.Consume;

public class MenuActionListener implements ActionListener {

	
	@Consume
	public void on(EstimatedState msg) {
		System.out.println(msg.getDate() + " "+ msg.getSourceName());
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		System.out.println("Selected: " + e.getActionCommand());
		LsfBatch batch = LsfBatch.selectFolders();
		
		
		batch.process(new MenuActionListener());
		
		
	}

}
