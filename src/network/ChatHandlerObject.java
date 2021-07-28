package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ChatHandlerObject extends Thread { //스레드
	private Socket socket;
	private List<ChatHandlerObject> list;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	
	//소켓과 리스트 보관
	public ChatHandlerObject(Socket socket, List<ChatHandlerObject> list) throws IOException {
		this.socket = socket;
		this.list = list;
		
		// 출력 스트림을 먼저 생성해야 한다(안 그러면 입장 메세지가 안 뜸) = oos가 ois보다 먼저 생성
		oos = new ObjectOutputStream(socket.getOutputStream());
		ois = new ObjectInputStream(socket.getInputStream());
		//서버측에서 무조건 출력(생성)이 먼저 있어야 함 - 객체로 받을 때만 이럼
		//문자로 받을 때는 순서 상관 없음(br, pw)
	}

	
	@Override
	public void run() {
		String nickName = null;
		InfoDTO dto = null; //받는 InfoDTO / 받은 dto 보내는 dto 2개를 만들어줘야 함
		
		while(true) {	
			try {
				dto = (InfoDTO)ois.readObject(); //이거 자체가 InfoDTO를 주니 그냥 그대로 가져가면 됨: new할 필요X
				
				if(dto.getCommand() == Info.JOIN) {
					nickName = dto.getNickName();
					
					//나를 포함한 모든 클라이언트에게 입장메세지를 보내기 ==> send
					InfoDTO sendDTO = new InfoDTO(); // 보내는 InfoDTO
					sendDTO.setCommand(Info.SEND);
					//~님이 입장했습니다 + 답장 ==> Send O, JoinX
					//받는 쪽은 run
					sendDTO.setMessage(nickName + "님이 입장하였습니다");
					broadcast(sendDTO);
					
				}else if(dto.getCommand() == Info.EXIT) {
					InfoDTO sendDTO = new InfoDTO(); //보내는 InfoDTO
					
					//quit를 보낸 클라이언트엑 quit를 보내고 종료(close()) 한다.
					sendDTO.setCommand(Info.EXIT);
					oos.writeObject(sendDTO);
					oos.flush();
					
					ois.close();
					oos.close();
					socket.close();
					//브로드캐스트 안 탄다! -> 나한테만 보내주기 때문
					
					//남은 클라이언트에게 퇴장 메세지를 보낸다
					list.remove(this); //퇴장메세지는 브로드캐스트 타야함
					
					sendDTO.setCommand(Info.SEND);
					sendDTO.setMessage(nickName + "님이 퇴장하였습니다.");
					broadcast(sendDTO);
					
					break; //빠져 나와야 함!
					
				}else if(dto.getCommand() == Info.SEND) {
					InfoDTO sendDTO = new InfoDTO(); //보내는 InfoDTO
					sendDTO.setCommand(Info.SEND);
					sendDTO.setMessage("[" + nickName + "]" + dto.getMessage());
					broadcast(sendDTO);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}//while
	}//run()


	private void broadcast(InfoDTO sendDTO) { //이름은 상관없으나 sendDTO로 하면 안 헷갈림
		for(ChatHandlerObject handler : list) { //list안에 담긴 것: ChatHandler
		
			try {
				handler.oos.writeObject(sendDTO);;
				handler.oos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}//for
	}
	
		
}
