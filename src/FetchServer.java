import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



public class FetchServer {
	
	private static int port = 3780;
	private static int counter =0;
	private static ResourceManager resourceManager; // resource manager handles all server resources
	
	
	
	public static void main(String[] args) {
		
		// 
		// this segment shows the initialization of Logger
		//
		// src/logging.properties records the homemade logging rules including logging format
		// this should be done only once ahead of creating the first logger instance
		System.setProperty("java.util.logging.config.file", "src/logging.properties");
		Logger logger = Logger.getLogger(FetchServer.class.getName());
		// use setlevel function to control logging level
		// set level to Level.ALL/Level.OFF to turn on/off debug mode
		//logger.setLevel(Level.ALL);
		logger.fine("Init");
		logger.warning("Init");
		
		
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
			output.writeUTF("Server: Hi Client "+counter+" hah!!!");
			JSONObject results = new JSONObject();
			
			//results.put("response", "success");
			//output.writeUTF(results.toJSONString());
			//Receive more data
			while(true){
				if(input.available()>0){
					JSONObject command = (JSONObject)parser.parse(input.readUTF());
		
					System.out.println("COMMAND RECEIVED: " + command.toJSONString());
					// JSONObject result1 = parseCommand(command);
					fetch((JSONObject)command.get("resourceTemplate"), output);
				}
			}
		}
		catch(IOException | ParseException e){
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void fetch(JSONObject resourceTemplate, DataOutputStream output) {
		Logger logger = Logger.getLogger(FetchServer.class.getName());
		
		JSONObject result = new JSONObject();
		ArrayList<String> tags = new ArrayList<String>();
		URI uri = null;
	    String channel = "";
	    
		// validate resourceTemplate
	    logger.info("validating resourceTemplate");
		if (resourceTemplate == null) {
			returnErrorMsg(result, output, "missing resourceTemplate");
			logger.warning("resourceTemplate does not exist, exit.");
			return;
		}
		logger.info(resourceTemplate.toString());
		// extract key from resourceTemplate
		// only channel & uri relevant
		logger.info("extracting key from resourceTemplate");
		if (resourceTemplate.containsKey("channel"))
			channel = resourceTemplate.get("channel").toString();
		
		try {
			// TODO handle invalid uri
			if (resourceTemplate.containsKey("uri")) {
				uri = new URI(resourceTemplate.get("uri").toString());
				String scheme = uri.getScheme();
				if (!scheme.equals("file")) {
					returnErrorMsg(result, output, "invalid resourceTemplate");
					logger.warning("invalid resourceTemplate, exit.");
					return;
				}
			}
		} catch (URISyntaxException e) {
			returnErrorMsg(result, output, "missing resourceTemplate");
			logger.warning("resourceTemplate does not exist, exit.");
			return;
		}
		
		// fetch resource
		
		logger.info("fetching resource");
		Resource resource = resourceManager.getServerResource(channel, uri.toString());
		File f = new File(uri.getPath());
		if (resource == null || !f.exists()) {
			System.out.println("no match resource");
		} else try {
			// respond
			result.put("response", "success");
			output.writeUTF(result.toJSONString());
			
			
			JSONObject resourceJson = resource.toJSON();
			resourceJson.put("resourceSize", f.length());
			output.writeUTF(resourceJson.toJSONString());
			
			// Start transmission
			logger.fine("Start transmission");
			RandomAccessFile byteFile = new RandomAccessFile(f, "r");
			byte[] sendingBuffer = new byte[1024*1024];
			int num;
			while ((num = byteFile.read(sendingBuffer)) > 0) {
				output.write(Arrays.copyOf(sendingBuffer, num));
			}
			byteFile.close();
			
			// resultSize
			// TODO resultSize always be 1?
			JSONObject resultSize = new JSONObject();
			resultSize.put("resultSize", 1);
			output.writeUTF(resultSize.toJSONString());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}
	
	// private method for returning error msg
	@SuppressWarnings("unchecked")
	private static void returnErrorMsg(JSONObject result, DataOutputStream output, String msg) {
		result.put("response", "error");
		result.put("errorMessage", msg);
		try {
			output.writeUTF(result.toJSONString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}

}
