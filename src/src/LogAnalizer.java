package src;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import pt.lsts.imc.EstimatedState;
import pt.lsts.imc.lsf.batch.LsfBatch;
import pt.lsts.imc.net.Consume;

public class LogAnalizer extends JLabel{
	
	private static final long serialVersionUID = 1L;
	static JFrame frame = null;
	static int widhtFrame = 640;
	static int heightFrame = 480;
	static JMenuBar menuBar;
	static JMenu menu, submenu;
	static JMenuItem menuItem;
	static LsfBatch batch;
	
	public LogAnalizer(){
	      super();
	    } 
	
	private static void layoutInit() {
		frame = new JFrame("Filter Loader");
    	frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    	frame.setResizable(true);
    	frame.setSize(widhtFrame + 10, heightFrame + 35);
    	frame.addComponentListener(new ComponentAdapter() {  
    		public void componentResized(ComponentEvent evt) {
    			Component c = evt.getComponent();
    		    widhtFrame = c.getSize().width;
    		    heightFrame = c.getSize().height;
    		    //System.out.println(c.getSize());  		    
    		}
    	});
    	frame.setVisible(true);
    	frame.setFocusable(true);
    	frame.addWindowListener(new java.awt.event.WindowAdapter() {
    	    @Override
    	    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
    	    	if (JOptionPane.showConfirmDialog(frame,"Are you sure to close this window?", "Really Closing?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
    	    		System.out.println("DONE...");
    	    		System.exit(0);
    	    	}
    	    	else {
    	    		frame.setVisible(true);
    	        }
    	    }
    	});
    	frame.getContentPane().setBackground(Color.DARK_GRAY);
    	
    	//Create the menu bar.
    	menuBar = new JMenuBar();

    	//Build the first menu.
    	menu = new JMenu("File");
    	menu.setMnemonic(KeyEvent.VK_F);
    	menu.getAccessibleContext().setAccessibleDescription(
    	        "The only menu in this program that has menu items");
    	menuBar.add(menu);
    	
		menuItem = new JMenuItem("Open folder");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				System.out.println("Selected: " + e.getActionCommand());
				batch = LsfBatch.selectFolders();
				batch.process(new LogAnalizer());
			}
		});
		menu.add(menuItem);
    	
    	frame.setJMenuBar(menuBar);
	}
	
	@Consume
	public void on(EstimatedState msg) {
		System.out.println("EstimatedState: "+msg.getDate() + " "+ msg.getSourceName());
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		layoutInit();
		while(true){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}