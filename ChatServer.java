import java.awt.Button;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

public class ChatServer {
    // HTTP socket 연결만 되어있는 정보
    Vector<ChatThread> connectedSockets = new Vector();
    // map {key(사용자명): value(ChatThread)}
    // LOGIN| 메시지를 보낸 사용자
    Hashtable<String, ChatThread> clientTable = new Hashtable<>();
    int clientNum = 0;    // 접속된 클라이언트의 수
    ChatClient chatClient;
    
    // 접속된 모든 클라이언트에게 메시지 msg 를 보냄
    public void broadcast(String msg) throws IOException {
        Set<String> keySet = clientTable.keySet();
        System.out.println(this.connectedSockets);
        System.out.println(this.clientTable);
        for (String key : keySet) {
            ChatThread client = clientTable.get(key);
            synchronized (client) {
                client.sendMessage(msg);
            }
        }
    }   
    
    
    // 단일 클라이언트에게 메시지 보냄
    public void unicast(String msg, String name) throws IOException {
        ChatThread chatThread = clientTable.get(name);
        if (chatThread != null) {
            synchronized (chatThread) {
               chatThread.sendMessage(msg);          
            }
        }
    }

    // 종료시 clientVector에 저장되어 있는 클라이언트 정보를 제거
    public void removeClient(String name) {
        synchronized (this.clientTable) {
            clientTable.remove(name);
        }
    }

    // 처음 연결되었을 때 clientVector에 해당 클라이언트의 정보를 추가
    public void addClient(ChatThread client, String name) {
        synchronized (this.clientTable) {
            // name 겹칠 경우
            Set<String> keys = this.clientTable.keySet();
            boolean contains = keys.contains(name);
            if (contains) {
            	dialog(); // 다이얼로그 실행
            	client.stop(); //클라이언트 접속 제한
            }
            else {
            this.clientNum++;

            this.clientTable.put(name, client);         
            }
        }
    }  
    public static void dialog() {
        Frame f = new Frame("채팅");
        f.setSize(300,200);

        Dialog info = new Dialog(f, "중복 알림", true);
        info.setSize(140,90);
        info.setLocation(50,50);
        info.setLayout(new FlowLayout());

        Label msg = new Label("닉네임이 중복 되었습니다. 재접속 해주세요.",Label.CENTER);
        Button ok = new Button("OK");

        ok.addActionListener(new ActionListener(){

            //OK버튼을 누르면 수행됨.

            public void actionPerformed(ActionEvent e){

                   //info.setVisible(false);  //Dialog를 안보이게 한다.
                    f.dispose();
                   info.dispose();     //Dialog를 메모리에서 없앤다.
            }

     });
        info.add(msg);
        info.add(ok);

        f.setVisible(true);
        info.setVisible(true);


    }
    // 서버의 시작 메인 메소드
    public static void main(String[] args) {
        // 서버 소켓
        ServerSocket myServerSocket = null;

        // ChatServer 객체 생성
        ChatServer myServer = new ChatServer();

        try {
            // 서버 포트 10129를 가지는 서버 소켓 생성
            myServerSocket = new ServerSocket(10129);
        } catch (IOException e) {
            System.out.println(e.toString());
            System.exit(-1);
        }

        System.out.println("[서버 대기 상태] " + myServerSocket);

        try {
            // 다수의 클라이언트 접속을 처리하기 위해 반복문으로 구현
            while (true) {
                // 클라이언트가 접속되었을 경우 이 클라이언트를 처리하기 위한 ChatThread 객체 생성
                ChatThread client = new ChatThread(myServer, myServerSocket.accept());

                // 클라이언트에게 서비스를 제공하기 위한 쓰레드 동작
                client.start();
                // clientVector 에 클라이언트 객체 추가
                myServer.addConnectedScoket(client);

            }
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    private void addConnectedScoket(ChatThread client) {
        synchronized (this.connectedSockets) {
            this.connectedSockets.add(client);
        }
    }
}