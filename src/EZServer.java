import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.net.ServerSocketFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class EZServer {

	private static int port = 3780;
	private static int counter =0;
	private static ResourceManager resourceManager; // resource manager handles all server resources
	
		public static void main(String[] args) {
			// TODO Auto-generated method stub
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
					JSONObject result = parseCommand(command);
			      //  results.put("responses", "success");
					
					// handle output for QUERY command
					JSONObject result2 = new JSONObject();
					if (command.get("command").equals("QUERY")) {
						//System.out.println(result.toJSONString());
						result2.put("response", result.get("response").toString());
						output.writeUTF(result2.toJSONString());
						for (int i = 0; i < result.size()-1; i++) {
							output.writeUTF(result.get("result"+i).toString());
						}
						result2.clear();
						result2.put("resultSize", result.size()-1);
						output.writeUTF(result2.toJSONString());
					}
					else if (command.get("command").equals("PUBLISH")) {
						//System.out.println(result.toJSONString());
						System.out.println(result.toJSONString());
						result.remove("response");
						System.out.println(result.toJSONString());
						for (int i = 0; i < result.size(); i++) {
							output.writeUTF(result.get("result"+i).toString());
						}
						for(Resource re1:resourceManager.getServerResources()) {
							System.out.println(re1.getName());
							System.out.println(re1.getDescription());
						}
					}
					else {
						output.writeUTF(result.toJSONString());
						output.flush();	
					}
				}
			}
		}
		catch(IOException | ParseException e){
			e.printStackTrace();
		}
	}
		
	private static JSONObject parseCommand(JSONObject command){
		JSONObject results = new JSONObject();
        results.put("response", "success");

		switch((String) command.get("command")){
		case "REMOVE":{
			JSONObject resource = (JSONObject) command.get("resource");
			String name =resource.get("name").toString();
			String tags = resource.get("tags").toString();
			String description = resource.get("description").toString();
			String uri = resource.get("uri").toString();
			String channel = resource.get("channel").toString();
			String owner = resource.get("owner").toString();
			String ezserver = resource.get("ezserver").toString();
			results.put("REMOVE RESPONSE","success");
			return results;
		}
		
		case "SHARE":{
			JSONObject resource = (JSONObject) command.get("resource");
            String secret =(String)command.get("secret");
			String name =resource.get("name").toString();
			String tags = resource.get("tags").toString();
			String description = resource.get("description").toString();
			String uri = resource.get("uri").toString();
			String channel = resource.get("channel").toString();
			String owner = resource.get("owner").toString();
			String ezserver = resource.get("ezserver").toString();
			results.put("SHARE RESPONSE","success");
			return results;
		}
		
		case "PUBLISH":{
			JSONObject resource = (JSONObject) command.get("resource");
			ArrayList<JSONObject> outcomeJSON;
			
			// query object to handle publish command
			PublishServer publishObject = new PublishServer(resource, resourceManager);
			outcomeJSON = publishObject.publish();
			
			// respond with the outcome of the operation
			for (int i = 0; i < outcomeJSON.size(); i++) {
				results.put("result"+i, outcomeJSON.get(i));
			}
			
			return results;
		}
		
		case "FETCH":{
			System.out.println("Fetching!");
			JSONObject resourceTemplate = (JSONObject) command.get("resourceTemplate");
			String name =resourceTemplate.get("name").toString();
			String tags = resourceTemplate.get("tags").toString();
			String description = resourceTemplate.get("description").toString();
			String uri = resourceTemplate.get("uri").toString();
			String channel = resourceTemplate.get("channel").toString();
			String owner = resourceTemplate.get("owner").toString();
			String ezserver = resourceTemplate.get("ezserver").toString();
			results.put("FETCH RESPONSE","success");
			return results;
		}
		
		case "QUERY":{
			JSONObject resource = (JSONObject) command.get("resourceTemplate");
			
			// query server object to handle queries
			QueryServer queryObject = new QueryServer(resourceManager);
			ArrayList<JSONObject> resourcesJSONFormat;
			
			boolean relay = (boolean)command.get("relay");
			
			resourcesJSONFormat = queryObject.query(resource);
			
			for (int i = 0; i < resourcesJSONFormat.size(); i++) {
				results.put("result"+i, resourcesJSONFormat.get(i));
			}
			
			return results;
		} 
		
		case "EXCHANGE":{
			break;
		}
		
		default:			try{
			throw new Exception();
		}catch(Exception e){
			e.printStackTrace();
		}
		}
		return results;	
	}}
