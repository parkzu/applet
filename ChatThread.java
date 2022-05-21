import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

public class ChatThread extends Thread {
    final static String WHISPER_COMMAND = "WHISPER";

    ChatServer myServer;    // ChatServer ��ü
    Socket mySocket;        // Ŭ���̾�Ʈ ����
    String clientName;
    ChatClient chatClient;

    PrintWriter out;        // ����� ��Ʈ��
    BufferedReader in;

    public ChatThread(ChatServer server, Socket socket)    // ������
    {
        super("ChatThread");

        myServer = server;
        mySocket = socket;

        out = null;
        in = null;
    }

    public void sendMessage(String msg) throws IOException    // �޽����� ����
    {
        out.println(msg);
        out.flush();
    }

    public void disconnect()    // ������ ����
    {
        try {
            out.flush();
            in.close();
            out.close();
            mySocket.close();
            myServer.removeClient(this.clientName);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    public void run()    // ������ ����
    {
        try {
            // ������ �̿��Ͽ� ����� ����� ��Ʈ���� ����
            out = new PrintWriter(new OutputStreamWriter(mySocket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));

            while (true) {    // Ŭ���̾�Ʈ ���� �޽����� ó���ϱ� ���� ��ٸ�

                String inLine = in.readLine();    // ��ٸ��� �ִٰ� Ŭ���̾�Ʈ�� ���� �޽����� �ִ� ��� �о����
                if (!inLine.equals("") && !inLine.equals(null)) {
                    messageProcess(inLine);        // Ŭ���̾�Ʈ�� ���� �޽����� Ȯ���Ͽ� ���� ������ ��� Ŭ���̾�Ʈ���� ��ε�ĳ��Ʈ
                }
            }
        } catch (Exception e) {
            disconnect();
        }
    }

    // Ŭ���̾�Ʈ�� ���� �޽����� Ȯ���� �� ó��
    public void messageProcess(String msg) {
        System.out.println(msg);    // ȭ�鿡 ���

        StringTokenizer st = new StringTokenizer(msg, "|");    // ��Ģ�� ���� ���� �޽����� �и��Ͽ� Ȯ��
        String command = st.nextToken();        // �޽����� ��� command �κ�

        
        
        if (command.equals(WHISPER_COMMAND)) {
            // WHISPER|{name}|{msg}
            String name = st.nextToken();
            String message = st.nextToken();

            if(myServer.clientTable.containsKey(name)) {
                try {
                    this.myServer.unicast("from. "+this.clientName + ": " +message, name);
                    this.myServer.unicast("To. "+name+": "+message, this.clientName);
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
            else {
                return;
            }
        } else if (command.equals("LOGIN")) {        // ���� �޽����� LOGIN �̸� ó�� ���� �޽����̱� ������ ���� ó��
            System.out.println("[����] " + mySocket);
            String name = st.nextToken();

            this.clientName = name;
            // clientVector �� Ŭ���̾�Ʈ ��ü �߰�
            myServer.addClient(this, name);
 
            try {                                // ���ο� Ŭ���̾�Ʈ�� �����Ͽ� �߰��� Ŭ���̾�Ʈ ���� ��ε�ĳ��Ʈ
                myServer.broadcast("[���� �����ڼ�] " + myServer.clientNum + "��");
            } catch (IOException e) {
                System.out.println(e.toString());
                
            }
        }
        
        else if (command.equals("CHANGE")) {
        	myServer.clientNum--; // �г��� ����� Ŭ���̾�Ʈ �����ڼ� ����(�ߺ�����)
        }
        
        else if (command.equals("LOGOUT")) {    // ���� �޽����� LOGOUT �̸� ���� �޽����̹Ƿ� ���ŵ� Ŭ���̾�Ʈ�� ����
            String name = st.nextToken();
            myServer.clientNum--;
            try {                              // ��ε�ĳ��Ʈ
                myServer.broadcast("[���� ����] " + name);
                myServer.broadcast("[���� �����ڼ�] " + myServer.clientNum + "��");
            } catch (IOException e) {
                System.out.println(e.toString());
            }
            disconnect();                        // ���� ����
        }else if(command.equals("TALKS")) {
        	String talk = st.nextToken();
        	String prenm = st.nextToken();
        	String curnm = st.nextToken();
        	myServer.removeClient(prenm);
        	myServer.addClient(this, curnm);
        	clientName = curnm;
        	
        	try {  
                myServer.broadcast(talk);
            } catch (IOException e) {
                System.out.println(e.toString());
            }
        	
        }
        else {
            String talk = st.nextToken();        // �޽����� ��ȭ talk �κ�
            try {      // LOGIN, LOGOUT �̿��� ���� �Ϲ� �޽����� ��� Ŭ���̾�Ʈ���� ���� �޽��� ����
                myServer.broadcast(talk);
            } catch (IOException e) {
                System.out.println(e.toString());
            }
        }
    }
}