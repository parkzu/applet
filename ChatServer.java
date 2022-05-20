import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {
    // HTTP socket ���Ḹ �Ǿ��ִ� ����
    Vector<ChatThread> connectedSockets = new Vector();
    // map {key(����ڸ�): value(ChatThread)}
    // LOGIN| �޽����� ���� �����
    Hashtable<String, ChatThread> clientTable = new Hashtable<>();
    int clientNum = 0;    // ���ӵ� Ŭ���̾�Ʈ�� ��


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
                // �̹� �־��~~ msg
                return;
            }
            this.clientNum++;

            this.clientTable.put(name, client);
        }
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