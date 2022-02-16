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
   
   // 한정적인 자원으로 안정적인 서버 운용을 위해 스레드 풀 사용
   public static ExecutorService threadPool;
   // 접속한 클라이언트들을 ArrayList 형태로 저장.
   public static ArrayList<Handler> clients = new ArrayList<Handler>();

   ServerSocket serverSocket;

   // 서버를 구동시켜 클라이언트의 연결을 기다리는 메소드.
   public void startServer(String IP, int port) {
      try {
         serverSocket = new ServerSocket();
         serverSocket.bind(new InetSocketAddress(IP, port)); // 바인딩
      } catch (Exception e) {
         e.printStackTrace();
         if (!serverSocket.isClosed()) {
            stopServer(); 
         }
         return;
      }
      
      // 클라이언트가 접속할 때 까지 기다리는 스레드
      Thread thread = new Thread() {

         @Override
         public void run() {
            while (true) {
               try {
                  Socket socket = serverSocket.accept();
                  clients.add(new Handler(socket)); // ArrayList에 socket 추가
                  System.out.println(" [클라이언트 접속] " + socket.getRemoteSocketAddress() + " :"
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
      // 스레드 풀을 초기화
      threadPool = Executors.newCachedThreadPool();
      // 클라이언트에 접속을 원하는 스레드를 넣어줍니다.
      threadPool.submit(thread);
   }

   // 서버의 작동을 중지시켜주는 메소드
   public void stopServer() {
      try {
         Iterator<Handler> iterator = clients.iterator(); // ArrayList
         // 각각의 클라이언트에게 접근하여 작업중인 모든 소켓 닫기
         while (iterator.hasNext()) {
            Handler client = iterator.next();
            client.socket.close();
            iterator.remove();
         }
         // 서버 소켓 객체 닫기
         if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
         }
         // 스레드풀 종료
         if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Override
   public void start(Stage stage) {
      
      BorderPane root = new BorderPane(); // 디자인 틀 생성
      root.setPadding(new Insets(5)); // 내부 패딩 5

      TextArea textArea = new TextArea();
      textArea.setEditable(false); // 수정 금지
      textArea.setFont(new Font("나눔고딕", 15));
      root.setCenter(textArea);

      // 시작버튼을 토글로 설정
      Button toggleButton = new Button("시작하기");
      toggleButton.setMaxWidth(Double.MAX_VALUE);
      BorderPane.setMargin(toggleButton, new Insets(1, 0, 0, 0));
      root.setBottom(toggleButton);

      // 서버 주소 초기화
      String IP = "192.168.0.97";
      int port = 5001;

      toggleButton.setOnAction(event -> {
         if (toggleButton.getText().equals("시작하기")) {
            startServer(IP, port);
            // 스레드 내에서의 UI변경 시 안정성을 위해
            Platform.runLater(() -> {
               String message = String.format("[서버 시작]\n", IP, port);
               textArea.appendText(message);
               toggleButton.setText("종료하기");
            });
         } else {
            stopServer();
            Platform.runLater(() -> {
               String message = String.format("[서버 종료]\n", IP, port);
               textArea.appendText(message);
               toggleButton.setText("시작하기");
            });
         }
      });
      
      // 크기
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