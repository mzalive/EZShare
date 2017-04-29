import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONObject;

public class Exchanger {
	private static int port = 3780;
	public String host = "localhost";
	public Exchanger(String host,int port){
		this.host = host;
		this.port = port;
	}
	public void exchange(ResourceManager r) throws InterruptedException  {
		// TODO Auto-generated method stub
			ArrayList<JSONObject> l = r.serverlist; // get the serverlist in resourceManager
		try(Socket socket = new Socket(host,port);){ // create socket for connection
			// create input stream and output steam
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
			
			// Generate JSONObject for exchanging
			JSONObject newCommand = new JSONObject();
			newCommand.put("serverList", l);
			newCommand.put("command", "EXCHANGE");
			
			// send JSONObject command to server in the serverlist
			System.out.println("Exchaning..."+ l.toString());
			output.writeUTF(newCommand.toJSONString());
			output.flush();
			
			// print information from server
			System.out.println("Received from server:");
			
			// if server's response interval exceed 1sec then disconnect automatically, else print response
			while(true){
				Thread.sleep(1000);
				if(input.available()>0){
				String result = input.readUTF();	
				System.out.println(result);
				}
				else{
					break;
				}
						}
			// close socket;
			socket.close();
} 
		// if connection refused then remove it from serverlist.
	catch (UnknownHostException e) {
	// TODO Auto-generated catch block
		Iterator i = l.iterator();
		while(i.hasNext()){
		JSONObject j = (JSONObject) i.next();
		if(j.get("hostname").toString().equals(host)){
			i.remove();
			System.out.println(host+ " :" + j.get("port").toString()+ " has been removed!");
		}
	}
	e.printStackTrace();
} 
	catch (IOException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
} 
	

}
}
