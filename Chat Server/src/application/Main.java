package application;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {
   
   // �������� �ڿ����� �������� ���� ����� ���� ������ Ǯ ���
   public static ExecutorService threadPool;
   // ������ Ŭ���̾�Ʈ���� ArrayList ���·� ����.
   public static ArrayList<Handler> clients = new ArrayList<Handler>();

   ServerSocket serverSocket;

   // ������ �������� Ŭ���̾�Ʈ�� ������ ��ٸ��� �޼ҵ�.
   public void startServer(String IP, int port) {
      try {
         serverSocket = new ServerSocket();
         serverSocket.bind(new InetSocketAddress(IP, port)); // ���ε�
      } catch (Exception e) {
         e.printStackTrace();
         if (!serverSocket.isClosed()) {
            stopServer(); 
         }
         return;
      }
      
      // Ŭ���̾�Ʈ�� ������ �� ���� ��ٸ��� ������
      Thread thread = new Thread() {

         @Override
         public void run() {
            while (true) {
               try {
                  Socket socket = serverSocket.accept();
                  clients.add(new Handler(socket)); // ArrayList�� socket �߰�
                  System.out.println(" [Ŭ���̾�Ʈ ����] " + socket.getRemoteSocketAddress() + " :"
                        + Thread.currentThread().getName()); 
               } catch (Exception e) {
                  if (!serverSocket.isClosed()) {
                     stopServer();
                  }
                  break;
               }
            }
         }

      };
      // ������ Ǯ�� �ʱ�ȭ
      threadPool = Executors.newCachedThreadPool();
      // Ŭ���̾�Ʈ�� ������ ���ϴ� �����带 �־��ݴϴ�.
      threadPool.submit(thread);
   }

   // ������ �۵��� ���������ִ� �޼ҵ�
   public void stopServer() {
      try {
         Iterator<Handler> iterator = clients.iterator(); // ArrayList
         // ������ Ŭ���̾�Ʈ���� �����Ͽ� �۾����� ��� ���� �ݱ�
         while (iterator.hasNext()) {
            Handler client = iterator.next();
            client.socket.close();
            iterator.remove();
         }
         // ���� ���� ��ü �ݱ�
         if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
         }
         // ������Ǯ ����
         if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Override
   public void start(Stage stage) {
      
      BorderPane root = new BorderPane(); // ������ Ʋ ����
      root.setPadding(new Insets(5)); // ���� �е� 5

      TextArea textArea = new TextArea();
      textArea.setEditable(false); // ���� ����
      textArea.setFont(new Font("�������", 15));
      root.setCenter(textArea);

      // ���۹�ư�� ��۷� ����
      Button toggleButton = new Button("�����ϱ�");
      toggleButton.setMaxWidth(Double.MAX_VALUE);
      BorderPane.setMargin(toggleButton, new Insets(1, 0, 0, 0));
      root.setBottom(toggleButton);

      // ���� �ּ� �ʱ�ȭ
      String IP = "192.168.0.97";
      int port = 5001;

      toggleButton.setOnAction(event -> {
         if (toggleButton.getText().equals("�����ϱ�")) {
            startServer(IP, port);
            // ������ �������� UI���� �� �������� ����
            Platform.runLater(() -> {
               String message = String.format("[���� ����]\n", IP, port);
               textArea.appendText(message);
               toggleButton.setText("�����ϱ�");
            });
         } else {
            stopServer();
            Platform.runLater(() -> {
               String message = String.format("[���� ����]\n", IP, port);
               textArea.appendText(message);
               toggleButton.setText("�����ϱ�");
            });
         }
      });
      
      // ũ��
      Scene scene = new Scene(root, 300, 200);
      stage.setTitle("Server");
      stage.setOnCloseRequest(event -> stopServer());
      stage.setScene(scene);
      stage.show();
   }

   public static void main(String[] args) {
      launch(args);
   }
}