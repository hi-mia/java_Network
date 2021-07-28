package network;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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

public class ChatClient extends JFrame implements ActionListener, Runnable {
	private JTextArea output;
	private JTextField input;
	private JButton sendBtn;
	private Socket socket;
	private BufferedReader br;
	private PrintWriter pw;
		
	public ChatClient() {
		
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
	    		pw.println("quit");
	    		pw.flush();
	    	}
	    });
	}
	
	public void service() {
	//	String serverIP = JOptionPane.showInputDialog(this, "서버IP를 입력하세요", "서버IP", JOptionPane.INFORMATION_MESSAGE);
	//this: 항상 프레임 위에 뜨게 함 - 메시지창 안 끄면 뒤에창도 못 끔 / 메시지 - 메시지타이틀 - i
		
		String serverIP = JOptionPane.showInputDialog(this, "서버IP를 입력하세요", "192.168.0");
		//default를 만들어줌
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
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			
		} catch (UnknownHostException e) {
			System.out.println("서버를 찾을 수 없습니다");
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			System.out.println("서버와 연결이 안 되었습니다");
			e.printStackTrace();
			System.exit(0);
		}
		
		//닉네임 서버로 보내기
		
		//pw.write(nickName + "\n");
		pw.println(nickName);
		pw.flush(); //버퍼 비워져야 그 다음 데이터가 넘어감
		
		Thread t = new Thread(this);//스레드 생성 - 내가 스레드가 되고 싶음
		t.start();//스레드 시작 - 스레드 실행(run())
		
		//이벤트
		input.addActionListener(this);
		sendBtn.addActionListener(this);
		
	}//service()
	

	@Override
	public void run() {
		//받는 쪽
		String line = null;
		
		while(true) {
			try {
				line = br.readLine(); //한줄씩 나에게 들어옴
				if(line == null || line.toLowerCase().equals("quit")) {
					//스레드는 종료되지 않고 백단에서 계속 돌고있기 때문에 line이 null값 나올 때가 있음
				
					br.close();
					pw.close();
					socket.close();
					
					System.exit(0);
				}
				
				output.append(line+"\n"); //메모장 / readLine은 엔터를 안 읽고 엔터 앞까지만 읽음
				int pos = output.getText().length();
				output.setCaretPosition(pos);
				
				
			} catch (IOException e) {
				e.printStackTrace();
			} 
			
		}//while
		
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		//보내는 쪽
		String msg = input.getText();//JTextField 값을 얻어오기 - 메모장 실습
		pw.println(msg);//서버로 보내기
		pw.flush();
		input.setText("");//비워주기 - JTextField 초기화

	}
//-------------------------
	public static void main(String[] args) {
		new ChatClient().service();
	}
	
}

//메인이 끝나도 스레드는 종료되지 않는다. 메인이 종료되어도 백그라운드에서 스레드는 돌아간다
