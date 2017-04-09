import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import org.json.simple.JSONObject;

public class Client {

	//private static String ip = "sunrise.cis.unimelb.edu.au";
	private static int port = 3780;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try(Socket socket = new Socket("localhost",port);){
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
		//	output.writeUTF("I want to connect!");
		  //output.flush();
			JSONObject newCommand = new JSONObject();
			JSONObject resource = new JSONObject();
			
			resource.put("name", "Aaron");
			resource.put("tags", "['photo','jpeg','jpg']");
			resource.put("description", "Secret agent photo :-)");
			resource.put("uri", "file:///usr/local/share/ezshare/photo.jpg");
			resource.put("channel", "");
			resource.put("owner", "*");
			resource.put("ezserver", "sunrise.cis.unimelb.edu.au:3780");
			//newCommand.put("resource",resource);
			newCommand.put("command","FETCH");
			//newCommand.put("relay", true);
			newCommand.put("resourceTemplate", resource);
		    //newCommand.put("secret", "2os41f58vkd9e1q4ua6ov5emlv");
			//JSONObject server1 = new JSONObject();
			//JSONObject server2 = new JSONObject();
			//server1.put("hostname", "115.146.85.165");
			//server2.put("hostname", "115.146.85.24");
			//server1.put("port", 3780);
			//server2.put("port", 3780);			
			/*resource.put("name", "");
			resource.put("tags", "[]");
			resource.put("description", "");
			resource.put("uri", "file:///home/aaron/EZShare/ezshare.jar");
			resource.put("channel", "my_private_channel");
			resource.put("owner", "");
			resource.put("ezserver", null);	*/
			//resource.put(“);
			
			//newCommand.put("serverList",'[server1,server2]');		
			System.out.println(newCommand.toJSONString());
			
			//String message = input.readUTF();
			//System.out.println(message);
			
			output.writeUTF(newCommand.toJSONString());
			output.flush();
			
			
			System.out.println("Received from server:");
			while(input.available()>0){
				String result = input.readUTF();	
				System.out.println(result);
			}
			//String result2 = input.readUTF();
		//	String result3 = input.readUTF();
	      //String result4 = input.readUTF();

		/*	File file = new File("/Users/macbookair/Desktop/new.jpg");
			FileOutputStream fos = new FileOutputStream(file);
			byte[] inputBytes = new byte[1743506];
			int length =0;
			while((length=input.read(inputBytes,0,inputBytes.length))>0){
				fos.write(inputBytes,0,length);
				fos.flush();
			}
			System.out.println("read success");*/
		//	finally{if(output!=null)output.close();}
		}
		catch(UnknownHostException e){
			e.printStackTrace();
		}catch (IOException e){	
		}
		
	}

}
