package base.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import base.web.LogInHandler.UserServerAddressConsumer;

public class LogInPanel extends JPanel{
  private static final long serialVersionUID = 1L;
  private static final String INVALID_ARGS = "Argument format error";
  private static final String INVALID_ADDRESS = "Invalid server address";
  
  private JButton logInB;
  private JTextField hostnameTF;
  private JTextField portTF;
  private JTextArea errorMessageTA;
  
  public LogInPanel(TouchpadWindow touchpadWindow, UserServerAddressConsumer consumer) {
    prepareComponents();
    
    logInB.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          boolean validAddress = consumer.consume(hostnameTF.getText().strip(), portTF.getText().strip());
          
          if(validAddress) {
            touchpadWindow.returnToIdle();
          } else {
            errorMessageTA.setText(INVALID_ADDRESS);
          }
          
        } catch (IllegalArgumentException ex) {
          errorMessageTA.setText(INVALID_ARGS);
        }
      }
    });
    
    setVisible(true);
  }
  
  private void prepareComponents() {
    var inputPanel = new JPanel();
    var buttonPanel = new JPanel();
    var feedbackPanel = new JPanel();
    var flowLayout = new FlowLayout(FlowLayout.CENTER,
        TouchpadWindow.MARGIN,
        TouchpadWindow.MARGIN);
    
    inputPanel.setLayout(flowLayout);
    buttonPanel.setLayout(flowLayout);
    feedbackPanel.setLayout(flowLayout);
    setLayout(new GridLayout(3,1));
    
    var loweredborder = BorderFactory.createLoweredSoftBevelBorder();
    var serverAddressBorder = BorderFactory.createTitledBorder(loweredborder, "Server address");
    var serverPortBorder = BorderFactory.createTitledBorder(loweredborder, "Server port");
    
    hostnameTF = new JTextField();
      hostnameTF.setHorizontalAlignment(JTextField.CENTER);
      int hostnameTFWidth = (int)(TouchpadWindow.FRAME_WIDTH * 0.45);
      int hostnameTFHeight = (int)(TouchpadWindow.FRAME_HEIGHT * 0.2);
      hostnameTF.setPreferredSize(new Dimension(hostnameTFWidth, hostnameTFHeight));
      hostnameTF.setBorder(serverAddressBorder);
      hostnameTF.setBackground(getBackground());
      
      float fontSizeFactor = 0.36f;
      float fontSize = hostnameTFHeight * fontSizeFactor;
      
      hostnameTF.setFont(hostnameTF.getFont().deriveFont(fontSize));
      inputPanel.add(hostnameTF);
    JTextArea colonSignTA = new JTextArea(":");
      colonSignTA.setEditable(false);
      colonSignTA.setBackground(inputPanel.getBackground());
      colonSignTA.setFont(colonSignTA.getFont().deriveFont(fontSize));
      inputPanel.add(colonSignTA);
    portTF = new JTextField();
      portTF.setHorizontalAlignment(JTextField.CENTER);
      portTF.setPreferredSize(new Dimension((int)(TouchpadWindow.FRAME_WIDTH * 0.3),
          (int)(TouchpadWindow.FRAME_HEIGHT * 0.2)));
      portTF.setBorder(serverPortBorder);
      portTF.setBackground(getBackground());
      portTF.setFont(portTF.getFont().deriveFont(fontSize));
      inputPanel.add(portTF);
    add(inputPanel);
      
    logInB = new JButton("connect");
      buttonPanel.add(logInB);
      logInB.setFont(logInB.getFont().deriveFont(fontSize));
    add(buttonPanel);
    
    errorMessageTA = new JTextArea();
      errorMessageTA.setEditable(false);
      errorMessageTA.setBackground(feedbackPanel.getBackground());
      errorMessageTA.setFont(errorMessageTA.getFont().deriveFont(fontSize));
      feedbackPanel.add(errorMessageTA);
    add(feedbackPanel);
  }
}
