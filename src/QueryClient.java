import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import org.json.simple.JSONObject;

public class QueryClient {

	private static int port = 3780;
	public static void main(String[] args) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		try(Socket socket = new Socket("sunrise.cis.unimelb.edu.au",port);){
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());

			JSONObject newCommand = new JSONObject();
			JSONObject resource = new JSONObject();
			newCommand.put("command", "QUERY");
			newCommand.put("relay", true);
			resource.put("tags", "[]");
			resource.put("description", "");	
			resource.put("uri", "");
			resource.put("channel", "");
			resource.put("owner", "");
			resource.put("ezserver",null);
			newCommand.put("resourceTemplate",resource);
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
