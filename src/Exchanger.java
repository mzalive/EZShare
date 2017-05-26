
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import EZShare.ResourceManager;

public class Exchanger {
	private static int port = 3780;
	public String host = "localhost";
	private Logger logger;
	public Exchanger(String host,int port){
		this.host = host;
		this.port = port;
		this.logger = Logger.getLogger(Exchanger.class.getName());
	}
	public boolean exchange(ResourceManager r,MyTask m) throws  InterruptedException, ParseException {
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
			this.logger.info("Exchaning..."+ l.toString());
			output.writeUTF(newCommand.toJSONString());
			output.flush();
			
			// print information from server
			this.logger.info("Received from server:");
			
			// if server's response interval exceed 1sec then disconnect automatically, else print response
			while(true){
				Thread.sleep(2000);
				if(input.available()>0){
			//	String result = input.readUTF();
				JSONParser parser = new JSONParser();
				JSONObject j = (JSONObject) parser.parse(input.readUTF());
				if(j.get("response").toString().equals("duplicated")){
					this.logger.info(j.toJSONString());
				return false;
				}
				this.logger.info(j.toJSONString());
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
			this.logger.info(host+ " :" + j.get("port").toString()+ " has been removed!");
		}
	}
	e.printStackTrace();
}
		catch (ConnectException e){
			if(r.serverlist.size()!=0){
			Iterator i1 = r.serverlist.iterator();
			while(i1.hasNext()){
			JSONObject j = (JSONObject) i1.next();
			if(j.get("hostname").toString().equals(host)){
				i1.remove();
				this.logger.info(host+ " :" + j.get("port").toString()+ " has been removed!");
				return false;
			}
			}
		}
		}
	catch (IOException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
		return true; 
	

}
}
