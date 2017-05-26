
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.simple.JSONObject;

public class testclient {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int port = 3000;
	
			// TODO Auto-generated method stub
			try(Socket socket = new Socket("localhost",port);){
				DataInputStream input = new DataInputStream(socket.getInputStream());
				DataOutputStream output = new DataOutputStream(socket.getOutputStream());
				output.writeUTF("I want to connect!");
				output.flush();
				JSONObject newCommand = new JSONObject();
				newCommand.put("command_name", "Math");
				newCommand.put("method_name", "add");	
				newCommand.put("first_integer", 1);
				newCommand.put("second_integer", 2);
				System.out.println(newCommand.toString());
				String message = input.readUTF();
				System.out.println(message);
				output.writeUTF(newCommand.toJSONString());
				output.flush();
				String result = input.readUTF();
				System.out.println("Received from server:"+result);
			}
			catch(UnknownHostException e){
				e.printStackTrace();
			}catch (IOException e){	
			}
			
		}

	}
