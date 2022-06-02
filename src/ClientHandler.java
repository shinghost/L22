import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private MyServer myServer;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String name;
    private int id;
    private boolean isAdmin;

    public String getName() {
        return name;
    }

    public ClientHandler(MyServer myServer, Socket socket) {
        try {
            this.myServer = myServer;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.name = "";
            new Thread(() -> {
                try {
                    authentication();
                    readMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("Проблемы при создании обработчика клиента");
        }
    }

    public void authentication() throws IOException {
        while (true) {
            String str = in.readUTF();
            if (str.startsWith("/auth")) {
                String[] parts = str.split("\\s");
                String nick = myServer.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                id = myServer.getAuthService().getUserIDByLoginPass(parts[1], parts[2]);
                isAdmin = parts[1].equals("admin");
                if (nick != null) {
                    if (!myServer.isNickBusy(nick)) {
                        sendMsg("/authok " + nick);
                        name = nick;
                        myServer.broadcastMsg(name + (isAdmin ? "(админ)" : "") + " зашел в чат");
                        myServer.subscribe(this);
                        return;
                    } else {
                        sendMsg("Учетная запиись уже используется");
                    }
                } else {
                    sendMsg("Неверный пароль/логин");
                }
            }
        }
    }

    public void readMessage() throws IOException {
        while (true) {
            String strFromClient = in.readUTF();
            if (strFromClient.equals("/end")) {
                return;
            }
            if (strFromClient.startsWith("/w")) {
                String[] parts = strFromClient.split("\\s", 3);
                if (parts.length == 3) {
                    myServer.sendMessage(parts[1], name + ": " + parts[2]);
                    System.out.println("от " + name + "(только " + parts[1] + ")" + ": " + parts[2]);
                    continue;
                }
            }
            else if (strFromClient.startsWith("/changenick")) {
                String[] parts = strFromClient.split("\\s", 2);
                if (parts.length == 2) {
                    if (!myServer.getAuthService().nicknameIsUsed(parts[1])) {
                        String newName = myServer.getAuthService().ChangeNick(id, parts[1]);
                        if (newName != null) {
                            name = newName;
                        }
                        else {
                            myServer.sendMessage(name, "Unknown error");
                        }
                    }
                    else {
                        myServer.sendMessage(name, "Nick is busy");
                    }
                    continue;
                }
            }
            else if (isAdmin) {
                if (strFromClient.startsWith("/adduser")) {
                    String[] parts = strFromClient.split("\\s", 4);
                    if (parts.length == 4) {
                        myServer.getAuthService().addUser(parts[1], parts[2], parts[3]);
                        continue;
                    }
                }
                else if (strFromClient.startsWith("/deluser")) {
                    String[] parts = strFromClient.split("\\s", 2);
                    if (parts.length == 2) {
                        myServer.kickUser(parts[1]);
                        myServer.getAuthService().deleteUser(parts[1]);
                        continue;
                    }
                }
            }

            myServer.broadcastMsg(name + ": " + strFromClient);
            System.out.println("от " + name + ": " + strFromClient);
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void closeConnection() {
        myServer.unsubscribe(this);
        myServer.broadcastMsg(name + " вышел из чата");
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}