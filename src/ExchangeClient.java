
import java.awt.List;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.json.simple.JSONObject;

public class ExchangeClient {

	private static int port = 3780;
	public static void main(String[] args) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		try(Socket socket = new Socket("sunrise.cis.unimelb.edu.au",port);){
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());

			JSONObject newCommand = new JSONObject();
			JSONObject server1 = new JSONObject();
			server1.put("hostname", "115.146.85.165");
			server1.put("port", 3780);
			JSONObject server2 = new JSONObject();
			server2.put("hostname", "115.146.85.24");
			server2.put("port", 3780);
			JSONObject server3 = new JSONObject();
			server3.put("hostname", "115.146.85.25");
			server3.put("port", 3780);
			ArrayList<JSONObject> l = new ArrayList<JSONObject>();
			l.add(server1);
			l.add(server2);
			l.add(server3);
			newCommand.put("serverList", l);
			
			newCommand.put("command", "EXCHANGE");

			output.writeUTF(newCommand.toJSONString());
			output.flush();
			System.out.println("Received from server:");
			while(true){
		  if(input.available()>0){
				String result = input.readUTF();	
				System.out.println(result);
			}
			}
}
	}
}
