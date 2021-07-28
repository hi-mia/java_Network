package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ChatHandler extends Thread { //스레드
	private Socket socket;
	private List<ChatHandler> list;
	private BufferedReader br;
	private PrintWriter pw;
	
	//소켓과 리스트 보관
	public ChatHandler(Socket socket, List<ChatHandler> list) throws IOException {
		this.socket = socket;
		this.list = list;
		br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
	}

	
	@Override
	public void run() {
		String nickName;
		String line;
		
		try {
			nickName = br.readLine();
			
			broadcast(nickName + "님이 입장하였습니다");
			
			while(true) {
				//받는 쪽
				line = br.readLine(); //받는 쪽
				if(line == null || line.toLowerCase().equals("quit")) break; // system.exit로 종료X
				
				//보내는 쪽
				broadcast("[" + nickName + "] " + line);

			}//while
			
			// quit를 보낸 글라이언트에게 QIT를 보내고 종료(close()) 한다.
			
			pw.println("quit");
			pw.flush();
	
			// 남은 클라이언트에게 퇴장 메세지를 보낸다 
			
			list.remove(this);
			br.close();
			pw.close();
			socket.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}//run()


	private void broadcast(String msg) {
		for(ChatHandler handler : list) { //list안에 담긴 것: ChatHandler
			handler.pw.println(msg);
			handler.pw.flush();
		}//for
	}
	
		
}
