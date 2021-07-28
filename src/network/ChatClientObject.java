package network;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class ChatClientObject extends JFrame implements ActionListener, Runnable {
	private JTextArea output;
	private JTextField input;
	private JButton sendBtn;
	private Socket socket;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
		
	public ChatClientObject() {
		
		output = new JTextArea();
		output.setFont(new Font("나눔고딕", Font.BOLD, 20));
		JScrollPane scroll = new JScrollPane(output);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		output.setEditable(false);
		
		input = new JTextField();
		sendBtn = new JButton("보내기");
		
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout()); //창크기 커지면 같이 커짐
		p.add("Center", input);
		p.add("East", sendBtn);
		
		Container c = getContentPane();
		c.add("Center", scroll);
		c.add("South", p);
		
		setBounds(700,200,300,300);
	    setVisible(true);
	    //setDefaultCloseOperation(EXIT_ON_CLOSE); - 그냥 끊어버리는 거
	    addWindowListener(new WindowAdapter(){
	    	@Override
	    	public void windowClosing(WindowEvent e) {
	    		//dto싣기
	    		InfoDTO dto = new InfoDTO();
	    		dto.setCommand(Info.EXIT);
	    		try {
					oos.writeObject(dto);
		    		oos.flush();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

	    	}
	    });
	}
	
	public void service() {

		String serverIP = JOptionPane.showInputDialog(this, "서버IP를 입력하세요", "192.168.0");
	
		if(serverIP == null || serverIP.length() == 0) {
			System.out.println("서버IP가 입력되지 않았습니다");
			System.exit(0);
		}
		
		String nickName = JOptionPane.showInputDialog(this, "닉네임을 입력하세요", "닉네임", JOptionPane.INFORMATION_MESSAGE);
		if(nickName==null || nickName.length()==0) {
			nickName = "guest";
		}
		
		try {
			socket = new Socket(serverIP, 9500); //소켓 생성 
			
			//IO 생성
			ois = new ObjectInputStream(socket.getInputStream());
			oos = new ObjectOutputStream(socket.getOutputStream());
			
			//닉네임 서버로 보내기
			
			//dto가 있어야 그걸 담아서 보냄, dto로 받는 거
			InfoDTO dto = new InfoDTO();
			dto.setCommand(Info.JOIN); //나는 입장이다 라는거 알림
			dto.setNickName(nickName);
			oos.writeObject(dto); //dto 가라
			oos.flush();
			
		} catch (UnknownHostException e) {
			System.out.println("서버를 찾을 수 없습니다");
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			System.out.println("서버와 연결이 안 되었습니다");
			e.printStackTrace();
			System.exit(0);
		}
		
		Thread t = new Thread(this);//스레드 생성 - 내가 스레드가 되고 싶음
		t.start();//스레드 시작 - 스레드 실행(run())
		
		//이벤트
		input.addActionListener(this);
		sendBtn.addActionListener(this);
		
	}//service()
	

	@Override
	public void run() {
		//서버로부터 받는 쪽
		InfoDTO dto = null;
		
		while(true) {
			try {
				dto = (InfoDTO)ois.readObject(); // 자식 = (자식)부모

				if(dto.getCommand() == Info.EXIT) {	//exit: 끊자	
					ois.close();
					oos.close();
					socket.close();
					
					System.exit(0);
				}else if(dto.getCommand() == Info.SEND) //send: 대화 주고받겠다
				
				output.append(dto.getMessage()+"\n"); //메모장 / readLine은 엔터를 안 읽고 엔터 앞까지만 읽음
				int pos = output.getText().length();
				output.setCaretPosition(pos);
							
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} 
			
		}//while
		
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		//보내는 쪽
		String msg = input.getText();//JTextField 값을 얻어오기 - 메모장 실습
		
		InfoDTO dto = new InfoDTO();
		if(msg.toLowerCase().equals("quit")) { //quit인가 일반 메세지인가 구분
			dto.setCommand(Info.EXIT); //퇴장메시지 보냄 -> 저쪽에서 끊겠다는 거 알아서 퇴장준비 함
		}else { //계속 데이터 진행
			dto.setCommand(Info.SEND);
			dto.setMessage(msg);
		}
		
		try {
			oos.writeObject(dto);
			oos.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		input.setText("");//비워주기 - JTextField 초기화

	}
//-------------------------
	public static void main(String[] args) {
		new ChatClientObject().service();
	}
	
}

//메인이 끝나도 스레드는 종료되지 않는다. 메인이 종료되어도 백그라운드에서 스레드는 돌아간다
