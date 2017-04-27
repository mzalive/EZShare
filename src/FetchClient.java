import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.sf.json.JSON;

public class FetchClient {

	//private static String ip = "sunrise.cis.unimelb.edu.au";
	private static int port = 3780;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try(Socket socket = new Socket("localhost",port);){
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
			JSONObject newCommand = new JSONObject();
			JSONObject resource = new JSONObject();		
			resource.put("name", "Aaron");
			resource.put("tags", "['photo','jpeg','jpg']");
			resource.put("description", "Secret agent photo :-)");
			resource.put("uri", "file:///Users/mzalive/GitHub/EZShare/resources/mountains.jpg");
			resource.put("channel", "");
			resource.put("owner", "*");
			resource.put("ezserver", "localhost:3780");
			newCommand.put("command","FETCH");
			newCommand.put("resourceTemplate", resource);
			System.out.println(newCommand.toJSONString());		
			output.writeUTF(newCommand.toJSONString());
			output.flush();	
			System.out.println("Received from server:");
			String result2 = input.readUTF();
			System.out.println(result2);
			result2 = input.readUTF();
			System.out.println(result2);
			result2 = input.readUTF();
			System.out.println(result2);
			File file = new File("/Users/mzalive/Desktop/test.jpg");
			FileOutputStream fos = new FileOutputStream(file);
			JSONParser parser = new JSONParser();
			int size =Integer.valueOf(((JSONObject)parser.parse(result2)).get("resourceSize").toString());
			byte[] inputBytes = new byte[size];
			int length =0;
			System.out.println("Start Tramsmission");
			while((length=input.read(inputBytes,0,inputBytes.length))>0){
				fos.write(inputBytes,0,length);
				fos.flush();
			}
			System.out.println("read success");
		}
		catch(UnknownHostException e){
			e.printStackTrace();
		}catch (IOException e){	
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
