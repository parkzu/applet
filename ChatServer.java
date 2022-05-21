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
    // HTTP socket ���Ḹ �Ǿ��ִ� ����
    Vector<ChatThread> connectedSockets = new Vector();
    // map {key(����ڸ�): value(ChatThread)}
    // LOGIN| �޽����� ���� �����
    Hashtable<String, ChatThread> clientTable = new Hashtable<>();
    int clientNum = 0;    // ���ӵ� Ŭ���̾�Ʈ�� ��
    ChatClient chatClient;
    
    // ���ӵ� ��� Ŭ���̾�Ʈ���� �޽��� msg �� ����
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
    
    
    // ���� Ŭ���̾�Ʈ���� �޽��� ����
    public void unicast(String msg, String name) throws IOException {
        ChatThread chatThread = clientTable.get(name);
        if (chatThread != null) {
            synchronized (chatThread) {
               chatThread.sendMessage(msg);          
            }
        }
    }

    // ����� clientVector�� ����Ǿ� �ִ� Ŭ���̾�Ʈ ������ ����
    public void removeClient(String name) {
        synchronized (this.clientTable) {
            clientTable.remove(name);
        }
    }

    // ó�� ����Ǿ��� �� clientVector�� �ش� Ŭ���̾�Ʈ�� ������ �߰�
    public void addClient(ChatThread client, String name) {
        synchronized (this.clientTable) {
            // name ��ĥ ���
            Set<String> keys = this.clientTable.keySet();
            boolean contains = keys.contains(name);
            if (contains) {
            	dialog(); // ���̾�α� ����
            	client.stop(); //Ŭ���̾�Ʈ ���� ����
            }
            else {
            this.clientNum++;

            this.clientTable.put(name, client);         
            }
        }
    }  
    public static void dialog() {
        Frame f = new Frame("ä��");
        f.setSize(300,200);

        Dialog info = new Dialog(f, "�ߺ� �˸�", true);
        info.setSize(140,90);
        info.setLocation(50,50);
        info.setLayout(new FlowLayout());

        Label msg = new Label("�г����� �ߺ� �Ǿ����ϴ�. ������ ���ּ���.",Label.CENTER);
        Button ok = new Button("OK");

        ok.addActionListener(new ActionListener(){

            //OK��ư�� ������ �����.

            public void actionPerformed(ActionEvent e){

                   //info.setVisible(false);  //Dialog�� �Ⱥ��̰� �Ѵ�.
                    f.dispose();
                   info.dispose();     //Dialog�� �޸𸮿��� ���ش�.
            }

     });
        info.add(msg);
        info.add(ok);

        f.setVisible(true);
        info.setVisible(true);


    }
    // ������ ���� ���� �޼ҵ�
    public static void main(String[] args) {
        // ���� ����
        ServerSocket myServerSocket = null;

        // ChatServer ��ü ����
        ChatServer myServer = new ChatServer();

        try {
            // ���� ��Ʈ 10129�� ������ ���� ���� ����
            myServerSocket = new ServerSocket(10129);
        } catch (IOException e) {
            System.out.println(e.toString());
            System.exit(-1);
        }

        System.out.println("[���� ��� ����] " + myServerSocket);

        try {
            // �ټ��� Ŭ���̾�Ʈ ������ ó���ϱ� ���� �ݺ������� ����
            while (true) {
                // Ŭ���̾�Ʈ�� ���ӵǾ��� ��� �� Ŭ���̾�Ʈ�� ó���ϱ� ���� ChatThread ��ü ����
                ChatThread client = new ChatThread(myServer, myServerSocket.accept());

                // Ŭ���̾�Ʈ���� ���񽺸� �����ϱ� ���� ������ ����
                client.start();
                // clientVector �� Ŭ���̾�Ʈ ��ü �߰�
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