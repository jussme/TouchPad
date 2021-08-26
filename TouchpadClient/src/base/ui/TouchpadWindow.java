package base.ui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import base.web.LogInHandler.UserServerAddressConsumer;

public class TouchpadWindow extends JFrame{
  private static final long serialVersionUID = 1L;
  public static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
  public static final int FRAME_WIDTH = (int) (SCREEN_SIZE.width * 0.26);
  public static final int FRAME_HEIGHT = (int) (SCREEN_SIZE.height * 0.23);
  public static final int MARGIN = (int) (FRAME_WIDTH * 0.025);
  private JPanel currentPanel;
  
  public TouchpadWindow() {
    setSize(FRAME_WIDTH, FRAME_HEIGHT);
    setLocationRelativeTo(null);
    setTitle("TouchPad");
    setLayout(new GridLayout(1,1));
    setResizable(false);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    addWindowListener(new WindowListener() {

      @Override
      public void windowOpened(WindowEvent e) {}

      @Override
      public void windowClosing(WindowEvent e) {
        //inputReceiver.shutdown();
      }

      @Override
      public void windowClosed(WindowEvent e) {windowClosing(e);}

      @Override
      public void windowIconified(WindowEvent e) {}

      @Override
      public void windowDeiconified(WindowEvent e) {}

      @Override
      public void windowActivated(WindowEvent e) {}

      @Override
      public void windowDeactivated(WindowEvent e) {}
    });
    
    setVisible(true);
  }
  
  public void promptServerAddress(UserServerAddressConsumer consumer) {
    if(this.currentPanel != null) {
      getContentPane().remove(currentPanel);
    }
    currentPanel = new LogInPanel(this, consumer);
    getContentPane().add(currentPanel);
    revalidate();
    repaint();
  }
  
  public void returnToIdle() {
    if(this.currentPanel != null) {
      getContentPane().remove(currentPanel);
    }
  }
}
