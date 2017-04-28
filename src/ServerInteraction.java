package Aaron1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.lang.Math;

import org.json.simple.JSONObject;

public class ServerInteraction {
	
	@SuppressWarnings({ "unchecked", "null" })
	public static void exchange() throws UnknownHostException, IOException, InterruptedException {	
	ArrayList<JSONObject> record = new ArrayList<JSONObject>();
	ArrayList<String> part = new ArrayList<String>();
    record = null;    
	JSONObject server = new JSONObject();
	server.put("115.146.85.16 ", 3780);
	server.put("115.14.83.16 ", 3780);
	server.put("11.142.83.11 ", 3780);	
	record.add(server);
	while(true){
		Thread.sleep(100000);
	int x=(int)(Math.random()*record.size());
	record.get(x);
	String rec = record.get(x).toString();
	String[] recc = null;
	recc = rec.split("//s");
	for(String s : recc)
    {
      part.add(s);		        	  
       }		

	try(Socket socket = new Socket(part.get(0),Integer.parseInt(part.get(1)));){
		DataInputStream input = new DataInputStream(socket.getInputStream());
		DataOutputStream output = new DataOutputStream(socket.getOutputStream());
		JSONObject newCommand = new JSONObject();
		newCommand.put("command", "EXCHANGE");
		newCommand.put("serverList", record.get(x));	
		output.writeUTF(newCommand.toJSONString());
		output.flush();

		while(true){
	       if(input.available()>0){
			String result = input.readUTF();	
			System.out.println(result);
		}
	
		}	
	}
	catch(UnknownHostException e){
		JSONObject response = new JSONObject();
		response.put("response", "error");
		response.put("errorMessage", "invalid server record");
		System.out.println(response.toJSONString());
		record.remove(x);
		
	}
		
	}
	
	
		}
		

	
	
	
	
	
	
	
	
	
	
}
