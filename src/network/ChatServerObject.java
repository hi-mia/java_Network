package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServerObject {
	private ServerSocket serverSocket;
	private List<ChatHandlerObject> list; //챗 핸들러랑 연결고리가 만들어짐
	private Socket socket;
	
	
	public ChatServerObject() {
		try {
			serverSocket = new ServerSocket(9500);
			System.out.println("서버준비완료..");
			
			list = new ArrayList<ChatHandlerObject>();
			//List는 인터페이스라 new List불가, 생성은 ArrayList
			
			while(true) { 
				
				Socket socket = serverSocket.accept(); //낚아채서 소켓만들기
				
				//ChatHandler 생성해서 생성자로 소켓과 list 보내기
				ChatHandlerObject handler = new ChatHandlerObject(socket, list);
				//스레드 시작 - 스레드 실행(run());
				handler.start(); // 스레드 걸어주기
				list.add(handler);

				//list에 담기
				list.add(handler);
				
			}//while
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		new ChatServer();
	}
	
}
