import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
	
public class ChatClient extends Applet implements ActionListener, Runnable {
    Socket mySocket = null;
    PrintWriter out = null;
    BufferedReader in = null;
    
    TextField serverIp;
    Button connect;
    Thread clock;
    TextArea memo;
    TextField name;
    TextField input;
    Panel upPanel, downPanel;
    Button nickname, changenickname;
    
    public void init() {

        setLayout(new BorderLayout());
        
        // 텍스트 에어리어 보더레이아웃의 중앙에 위치
        memo = new TextArea(10, 55);
        add("Center", memo);
        memo.setBackground(new Color(255,255,240));
        
        //패널 생성하여 패널에 IP 주소 입력을 위한 텍스트필드와 연결 버튼 추가
        upPanel = new Panel();

        serverIp = new TextField(12);
        serverIp.setText("서버 IP 주소 입력");
        upPanel.add(serverIp);
        connect = new Button("연결");
        connect.addActionListener(this);
        upPanel.add(connect);
        upPanel.setBackground(new Color(150,130,110));

        // 생성된 패널을 보더레이아웃의 위쪽에 위치
        add("North", upPanel);     
        
        List list = new List(10, false);
        add("East",list); 
        
        //패널 생성하여 대화명을 위한 텍스트필드와 입력을 위한 텍스트필드 추가
        downPanel = new Panel();
        changenickname = new Button("닉네임 변경");
        changenickname.addActionListener(this);
        downPanel.add(changenickname);
        
        name = new TextField(8);
        name.setText("대화명");
        
        downPanel.add(name);
        nickname = new Button("닉네임 설정");
        nickname.addActionListener(this);
        nickname.setName("닉네임 설정");
        downPanel.add(nickname); 
        input = new TextField(32);
        input.addActionListener(this); // 사용자가 엔터키를 누르면 메시지가 전송되도록 이벤트 연결
        downPanel.add(input);
        add("South", downPanel);        // 보더레이아웃의 아래쪽에 패널 위치
        downPanel.setBackground(new Color(150,130,110));
        changenickname.disable();
        connect.disable();
        input.disable();
    }

    public void run()        // 쓰레드 부분으로 상대방이 보내는 메시지를 받기 위해 while을 돌면서 기다림
    {
        // LOGIN|용정킴|ip----
        out.println("LOGIN|" + name.getText() + "|" + mySocket);        // 초기 서버에 접속한 후 접속 메시지를 보냄
        memo.append("[접속하였습니다]" + "\n");    // 내 화면의 텍스트에어리어 memo에  접속메시지 출력

        try {
            while (true) {                            // 반복문
                String msg = in.readLine();            // 상대방이 보낸 메시지를 읽어들임
                if (msg != null && !msg.equals("") && !msg.equals(null)) {
                    memo.append(msg + "\n");            // 내 화면의 memo에 받은 메시지 출력
                }
            }
        } catch (IOException e) {
            memo.append(e.toString() + "\n");
        }
    }

    public void actionPerformed(ActionEvent e)    // connect 버튼이 눌린 경우와 input 텍스트필드에
    {                                                // 엔터가 들어왔을 경우 실행
    	if(e.getSource() == nickname && nickname.getName()=="닉네임 설정") {
    		nickname.disable();
    		connect.enable();
    		changenickname.enable();
    		name.disable();
    		nickname.setName("닉네임 변경");
    	}
    	else if(e.getSource() == nickname && nickname.getName()=="닉네임 변경") {
        		nickname.disable();
        		connect.disable();
        	}
    	else if(e.getSource() == changenickname) {
    		nickname.enable();
    		changenickname.disable();
    		name.enable();
    	}
    	
    else if (e.getSource() == connect) {            // connect 버튼이 눌렸을 경우 서버에 연결
            try {
                mySocket = new Socket(serverIp.getText(), 10129);    // 입력받은 서버 IP 주소와 포트번호

                // 생성된 소켓을 이용해 서버와의 입출력 스트림을 생성
                out = new PrintWriter(new OutputStreamWriter(mySocket.getOutputStream()));
                in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));

                // 쓰레드를 동작시킴
                if (clock == null) {
                    clock = new Thread(this);
                    clock.start();
                }

            } catch (UnknownHostException ie) {
                System.out.println(ie.toString());
            } catch (IOException ie) {
                System.out.println(ie.toString());
            }
            	connect.disable();
            	input.enable();
        }
        else if (e.getSource() == input) {    // input 텍스트필드에 엔터가 입력될 경우
            String data = input.getText();    // input 텍스프필드의 값을 읽어서
            input.setText("");

            if (data.startsWith("/귓속말 ")) {
            	data = data.replace("/귓속말 ", "");
                if(data.isEmpty() || !data.contains("|")) {
                	return;
                }            
                
            	out.println("WHISPER|" + data);     
                out.flush();
                return;
            }

            // 형식에 맞춰 서버에 메시지를 전송
            out.println("TALK|" + "[" + name.getText() + "]" + " : " + data);
            out.flush();    // 버퍼에 있는 출력 메시지를 상대방에게 강제로 전송
        }
        
    }

    public void stop()    // 쓰레드를 종료시키고 종료 메시지를 서버에 전송하고 모든 연결을 닫음
    {
        if ((clock != null) && (clock.isAlive())) {
            clock = null;        // 쓰레드 종료
        }

        // 서버에 종료 메시지 보냄
        out.println("LOGOUT|" + name.getText());
        out.flush();

        // 모든 스트림과 소켓 연결을 끊음
        try {
            out.close();
            in.close();
            mySocket.close();
        } catch (IOException e) {
            memo.append(e.toString() + "\n");
        }
    }
}