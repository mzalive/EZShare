import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import org.json.simple.JSONObject;

public class FetchClient {

	//private static String ip = "sunrise.cis.unimelb.edu.au";
	private static int port = 3780;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try(Socket socket = new Socket("sunrise.cis.unimelb.edu.au",port);){
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
			JSONObject newCommand = new JSONObject();
			JSONObject resource = new JSONObject();		
			resource.put("name", "Aaron");
			resource.put("tags", "['photo','jpeg','jpg']");
			resource.put("description", "Secret agent photo :-)");
			resource.put("uri", "file:///usr/local/share/ezshare/photo.jpg");
			resource.put("channel", "");
			resource.put("owner", "*");
			resource.put("ezserver", "sunrise.cis.unimelb.edu.au:3780");
			newCommand.put("command","FETCH");
			newCommand.put("resourceTemplate", resource);
			newCommand.put("command","FETCH");
			System.out.println(newCommand.toJSONString());		
			output.writeUTF(newCommand.toJSONString());
			output.flush();	
			System.out.println("Received from server:");
			String result2 = input.readUTF();
			String result3 = input.readUTF();
			System.out.println(result2+result3);
			File file = new File("/Users/leonchung/Desktop/aaron2.jpg");
			FileOutputStream fos = new FileOutputStream(file);
			byte[] inputBytes = new byte[1743506];
			int length =0;
			while((length=input.read(inputBytes,0,inputBytes.length))>0){
				fos.write(inputBytes,0,length);
				fos.flush();
			}
			System.out.println("read success");
		}
		catch(UnknownHostException e){
			e.printStackTrace();
		}catch (IOException e){	
		}
		
	}

}
