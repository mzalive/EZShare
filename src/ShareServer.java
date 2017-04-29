import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ShareServer {
	
	private static int port = 3780;
	private static int counter =0;
	private static ResourceManager resourceManager;
	private static final String secret = "this_is_the_server_secret";
	
	
	public static void main(String[] args) {
		System.setProperty("java.util.logging.config.file", "src/logging.properties");
		Logger logger = Logger.getLogger(FetchServer.class.getName());
		logger.info("Init");
		
		
		ServerSocketFactory factory = ServerSocketFactory.getDefault();
		try(ServerSocket server = factory.createServerSocket(port)){
			resourceManager = new ResourceManager(server.getInetAddress().toString(), Integer.toString(port));
			System.out.println("Waiting for client connection.");
			while(true){
				Socket client = server.accept();
				counter++;
				System.out.println("Client "+counter+": "+"Applying for connection!");
				Thread t = new Thread(()->serveClient(client));
				t.start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void serveClient(Socket client){
		try(Socket clientSocket = client){
			JSONParser parser = new JSONParser();
			DataInputStream input = new DataInputStream(clientSocket.getInputStream());
			DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
			//System.out.println("CLIENT: "+input.readUTF());
			//output.writeUTF("Server: Hi Client "+counter+" hah!!!");
			JSONObject results = new JSONObject();
			
			//results.put("response", "success");
			//output.writeUTF(results.toJSONString());
			//Receive more data
			while(true){
				if(input.available()>0){
					JSONObject command = (JSONObject)parser.parse(input.readUTF());
		
					System.out.println("COMMAND RECEIVED: " + command.toJSONString());
					// JSONObject result1 = parseCommand(command);
					results = share(command, secret);
					output.writeUTF(results.toString());
				}
			}
		}
		catch(IOException | ParseException e){
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject share(JSONObject clientCmd, String secret) {
		System.setProperty("java.util.logging.config.file", "src/logging.properties");
		Logger logger = Logger.getLogger(FetchServer.class.getName());
		JSONObject result = new JSONObject();
		JSONObject resource = new JSONObject();
		String name = "", description = "", channel = "", owner = "";
		URI uri = null;
		ArrayList<String> tags = new ArrayList<String>();
		if (!clientCmd.containsKey("resource") || !clientCmd.containsKey("secret")) 
			return returnErrorMsg("missing resource and\\/or secret");
		if (!clientCmd.get("secret").toString().equals(secret)) {
			logger.info("Genuien Secret : " + secret);
			logger.info("Rcvd Secrest : " + clientCmd.get("secret").toString());
			return returnErrorMsg("incorrect secret");
		}
		
		//extract resource
		resource = (JSONObject) clientCmd.get("resource");
		if (resource.containsKey("name"))
	        name = resource.get("name").toString();
	    if (resource.containsKey("tags")) {
	        JSONArray tag_array = (JSONArray) resource.get("tags");
	        for (int i = 0; i < tag_array.size(); i++)
	            tags.add(tag_array.get(i).toString());
	    }
	    if (resource.containsKey("description")) {
	        description = resource.get("description").toString().trim();
//	        description = description.replaceAll("\\u00", "");
	    }
	    if (resource.containsKey("channel"))
	        channel = resource.get("channel").toString();
	    if (resource.containsKey("owner") && !resource.get("owner").equals("*"))
	        owner = resource.get("owner").toString();	
		if (resource.containsKey("uri"))
			try {
				uri = new URI(resource.get("uri").toString());
				if (!uri.getScheme().equals("file"))
					return returnErrorMsg("invalid resource");
				File file = new File(uri);
				if (!file.exists())
					return returnErrorMsg("cannot share resource");
			} catch (URISyntaxException e) {
				return returnErrorMsg("invalid resource");
			}
		
		// login the resource
		Resource r = new Resource(name, description, tags, uri.toString(), channel, owner);
		resourceManager.addResource(r);
		// TODO store the resource
		
		result.put("response", "success");
		for (Resource res: resourceManager.getServerResources()) {
			logger.fine(res.toJSON().toString());
		}
		return result;
	}
	
	// private method for returning error msg
	@SuppressWarnings("unchecked")
	private static JSONObject returnErrorMsg(String msg) {
		JSONObject result = new JSONObject();
		result.put("response", "error");
		result.put("errorMessage", msg);
		return result;
	}
}
