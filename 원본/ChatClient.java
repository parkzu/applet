import java.net.*;
import java.io.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class ChatClient extends Applet
  implements ActionListener, Runnable
{
  Socket mySocket= null;
  PrintWriter out= null;
  BufferedReader in= null;

  TextField serverIp;
  Button connect;
  Thread clock;
  TextArea memo;
  TextField name;
  TextField input;
  Panel upPanel, downPanel;
  
  public void init()
  {
    // GUI
    setLayout(new BorderLayout());
    
    // �ؽ�Ʈ ����� �������̾ƿ��� �߾ӿ� ��ġ
    memo= new TextArea(10, 55);
    add("Center", memo);

    //�г� �����Ͽ� �гο� IP �ּ� �Է��� ���� �ؽ�Ʈ�ʵ�� ���� ��ư �߰�
    upPanel = new Panel();
    
    serverIp = new TextField(12);
    serverIp.setText("���� IP �ּ� �Է�");
    upPanel.add(serverIp);
    
    connect = new Button("����");
    connect.addActionListener(this);
    upPanel.add(connect);
    
    // ������ �г��� �������̾ƿ��� ���ʿ� ��ġ
    add("North", upPanel);
    
    //�г� �����Ͽ� ��ȭ���� ���� �ؽ�Ʈ�ʵ�� �Է��� ���� �ؽ�Ʈ�ʵ� �߰�
    
    downPanel= new Panel();
    name= new TextField(8);
    name.setText("��ȭ��");
    downPanel.add(name);
    input= new TextField(32);
    input.addActionListener(this);	// ����ڰ� ����Ű�� ������ �޽����� ���۵ǵ��� �̺�Ʈ ����
    downPanel.add(input);
    add("South", downPanel);		// �������̾ƿ��� �Ʒ��ʿ� �г� ��ġ
  }

  public void run()		// ������ �κ����� ������ ������ �޽����� �ޱ� ���� while�� ���鼭 ��ٸ� 
  {
    out.println("LOGIN|"+ mySocket);		// �ʱ� ������ ������ �� ���� �޽����� ����
    memo.append("[�����Ͽ����ϴ�]" +"\n");	// �� ȭ���� �ؽ�Ʈ����� memo��  ���Ӹ޽��� ���

    try{
      while(true){							// �ݺ���
        String msg= in.readLine();			// ������ ���� �޽����� �о����
        if(!msg.equals("") && !msg.equals(null)){
          memo.append(msg +"\n");			// �� ȭ���� memo�� ���� �޽��� ���
        }
      }
    }catch(IOException e){
      memo.append(e.toString() +"\n");
    }
  }

  public void actionPerformed(ActionEvent e)	// connect ��ư�� ���� ���� input �ؽ�Ʈ�ʵ忡 
  {												// ���Ͱ� ������ ��� ����

	  if(e.getSource() == connect) {			// connect ��ư�� ������ ��� ������ ����
		 try{
			 mySocket= new Socket(serverIp.getText(),10129);	// �Է¹��� ���� IP �ּҿ� ��Ʈ��ȣ
		     
			 // ������ ������ �̿��� �������� ����� ��Ʈ���� ����
			 out= new PrintWriter(new OutputStreamWriter(mySocket.getOutputStream()));
		     in= new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
		     
		     // �����带 ���۽�Ŵ
		     if(clock==null){
		         clock= new Thread(this);
		         clock.start();
		     }
		     
		 }catch(UnknownHostException ie){
			 System.out.println(ie.toString());
		 }catch(IOException ie){
			 System.out.println(ie.toString());
		 }
  
	  }
	  else if(e.getSource() == input){	// input �ؽ�Ʈ�ʵ忡 ���Ͱ� �Էµ� ���
		  String data= input.getText();	// input �ؽ����ʵ��� ���� �о
		  input.setText("");
		  
		  // ���Ŀ� ���� ������ �޽����� ����
		  out.println("TALK|"+ "[" + name.getText() + "]" + " : "+ data);
		  out.flush();	// ���ۿ� �ִ� ��� �޽����� ���濡�� ������ ����
    }
  }

  public void stop()	// �����带 �����Ű�� ���� �޽����� ������ �����ϰ� ��� ������ ����
  {
    if((clock!=null)&&(clock.isAlive())){
      clock= null;		// ������ ����
    }

    // ������ ���� �޽��� ����
    out.println("LOGOUT|"+ name.getText());
    out.flush();

    // ��� ��Ʈ���� ���� ������ ����
    try{
      out.close();
      in.close();
      mySocket.close();
    }catch(IOException e){
      memo.append(e.toString() +"\n");
    }
  }
}
