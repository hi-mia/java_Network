package network;

import java.io.Serializable;

enum Info {
	JOIN, EXIT, SEND
} 
//객체로 움직임
public class InfoDTO implements Serializable { //serializalbe -> 하나의 객체로 만들어서 한꺼번에 보냄
	private static final long serialVersionUID = 1L;
	
	private String nickName;
	private String message; //입장, 퇴장, 대화내용
	private Info command;
	
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Info getCommand() {
		return command;
	}
	public void setCommand(Info command) {
		this.command = command;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
	
}

/*채팅 프로그램 io만 바꾸기

ChatHandler

ObjectInputStream, ObjectOutputStream을 사용 - String X,
InfoDTO 단위로 데이터 송/수신

ois = new ObjectInputStream(socket.getInputStream());
oos = new ObjectOutputStream(socket.getOutputStream());

ois.readObject()
oos.writeObject()를 사용한다

ChatClientObject
ChatServerObject
ChatHandlerObject

InfoDTO

*/


