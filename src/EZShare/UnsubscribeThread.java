package EZShare;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class UnsubscribeThread extends Thread{
	
	public Socket s;
	
	public UnsubscribeThread(Socket s){
		this.s = s;
	}

	public void run(){
		BufferedReader commandPromptBufferedReader = new BufferedReader(new InputStreamReader(System.in));
		DataOutputStream dos;
		try {
			dos = new DataOutputStream(s.getOutputStream());
			DataInputStream dis = new DataInputStream(s.getInputStream());
			while(true){
				System.out.println("Enter Message:");
				String message;
			message = commandPromptBufferedReader.readLine();
			String s[] = message.split("\\s+");
			for(String t : s){
				System.out.println("Test: "+t);
			}
			if(s.length==3){
			if(s[0].equals("-unsubscribe") && s[1].equals("-id")){
				int unid = Integer.parseInt(s[2]);
				JSONObject Unsubscribe = new JSONObject();
				Unsubscribe.put("command", "UNSUBSCRIBE");
				Unsubscribe.put("id",unid);
				System.out.println(Unsubscribe.toJSONString());

					dos.writeUTF(Unsubscribe.toJSONString());
					dos.flush();
				/*	String result = dis.readUTF();
					JSONParser parser = new JSONParser();
					JSONObject json = (JSONObject) parser.parse(result);
					if(json.get("response")!=null){
						if(json.get("response").toString().equals("success")){
							break;
						}
					}*/
				//	break;
		}}}} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// catch (ParseException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
	//	}

			
			

			
			
		
	}	
}
