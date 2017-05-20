package EZShare;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.simple.JSONObject;

public class SubscribeServer {
	Socket socket = new Socket();
	int a;
	boolean close;
	int id;
	JSONObject resourceObject;
	SubscribeServer(Socket socket,int id){
		this.id = id;
		this.socket = socket;
		a =0;
		this.close=false;
		this.resourceObject=null;
	}
	public void run(){
		try {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			PrintWriter printwriter = new PrintWriter(socket.getOutputStream(),true);
			DataInputStream i1 = new DataInputStream(socket.getInputStream()); 
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			System.out.println("Client"+a+" "+"is connected to server now.");
			a++;
			while(true){
				if(this.close==true){
					System.out.println("Client has closed the connection.");
					break;		
				}
				if(resourceObject!=null){
					out.writeUTF(resourceObject.toJSONString());
					out.flush();
					System.out.println("New resource "+resourceObject.toJSONString()+" has been written to client");
				}
			/*	String a = bufferedReader.readLine();
				if(a.equals("Q")){
					System.out.println("Client has closed the connection.");
					break;
				}
				
				System.out.println("Received from client:"+a);
				printwriter.println(a+"(SERVER RECEIVED)");*/
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}


