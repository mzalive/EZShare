import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class EZServer {

	private static String hostname = "localhost";
	private static int port = 3780;
	private static int connection_interval = 600;
	private static int exchange_interval = 600;
	private static String secret;
	private static int counter =0;
	private static ResourceManager resourceManager; // resource manager handles all server resources

	public static void main(String[] args) {

		// init logger
		System.setProperty("java.util.logging.config.file", "src/logging.properties");
		Logger logger = Logger.getLogger(EZServer.class.getName());

		// handle args
		Options options = new Options();
		options.addOption("advertisedhostname", true, "advertised hostname");
		options.addOption("connectionintervallimit", true,"connection interval limit in seconds");
		options.addOption("exchangeinterval", true,"exchange interval in seconds");
		options.addOption("port", true,"server port, an integer");
		options.addOption("secret", true,"secret");
		options.addOption("debug", false, "print debug information");
		options.addOption("h","help", false, "show usage");
		CommandLineParser parser = new DefaultParser();
		CommandLine cLine = null;
		try {
		    cLine = parser.parse(options, args);
		} catch (org.apache.commons.cli.ParseException e) {
		    System.out.println("Unexpected exception:" + e.getMessage());
		}
		if (cLine.hasOption("h")) {
			HelpFormatter hformatter = new HelpFormatter();
            hformatter.printHelp("EZServer", options);
            return;
		}
		if (cLine.hasOption("debug")) {
			logger.setLevel(Level.ALL);
			logger.info("debug mode on");
		} else logger.setLevel(Level.OFF);

		if (cLine.hasOption("advertisedhostname")) {
			hostname = cLine.getOptionValue("advertisedhostname");
			logger.info("set advertised hostname : " + hostname);
		} else logger.info("no assigned advertised hostname, using default : " + hostname);

		if (cLine.hasOption("connectionintervallimit")) {
			connection_interval = Integer.valueOf(cLine.getOptionValue("connectionintervallimit"));
			logger.info("set connection interval limit : " + connection_interval + "s");
		} else logger.info("no assigned connection interval limit, using default : " + connection_interval + "s");

		if (cLine.hasOption("exchangeinterval")) {
			exchange_interval = Integer.valueOf(cLine.getOptionValue("exchangeinterval"));
			logger.info("set exchange interval limit : " + exchange_interval + "s");
		} else logger.info("no assigned exchange interval limit, using default : " + exchange_interval + "s");

		if (cLine.hasOption("port")) {
			port = Integer.valueOf(cLine.getOptionValue("port"));
			logger.info("set port : " + port + "s");
		} else logger.info("no assigned port, using default : " + port);

		if (cLine.hasOption("secret")) {
			secret = cLine.getOptionValue("secret");
			logger.info("set secret : " + secret + "s");
		} else {
			secret = secretGen(32);
			logger.info("no assigned secret, auto generate : " + secret);
		}


		ServerSocketFactory factory = ServerSocketFactory.getDefault();
		try(ServerSocket server = factory.createServerSocket(port)){
			resourceManager = new ResourceManager(server.getInetAddress().toString(), Integer.toString(port));
			logger.fine("Server established, standing by.");
			while(true){
				Socket client = server.accept();
				counter++;
				logger.info("Incoming connection from client " + counter + ", starting new thread");
				Thread t = new Thread(()->serveClient(client, counter));
				t.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void serveClient(Socket client, int clientID){
		Timer timer = new Timer();
timer.schedule(new MyTask(resourceManager), 1000, 10000);
		Logger logger = Logger.getLogger(EZServer.class.getName());
		String loggerPrefix = "Client " + clientID + ": ";

		JSONObject results = new JSONObject();
		JSONParser parser = new JSONParser();

		JSONObject resourceTemplate = new JSONObject();


		try(Socket clientSocket = client){
			DataInputStream input = new DataInputStream(clientSocket.getInputStream());
			DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
			while (true) {
			if(input.available() > 0){
				JSONObject clientCommand = (JSONObject)parser.parse(input.readUTF());
				logger.info(loggerPrefix + "original request: " + clientCommand.toJSONString());

				String command = clientCommand.get("command").toString();
				logger.info(loggerPrefix + "command rcvd: " + command);

				switch (command) {
				case "PUBLISH":
					resourceTemplate = (JSONObject) clientCommand.get("resource");
					ArrayList<JSONObject> outcomeJSON;

					// query object to handle publish command
					PublishServer publishObject = new PublishServer(resourceTemplate, resourceManager);
					outcomeJSON = publishObject.publish();

					// respond with the outcome of the operation
					for (int i = 0; i < outcomeJSON.size(); i++) {
						results.put("result"+i, outcomeJSON.get(i));
					}
					output.writeUTF(results.toJSONString());
					break;

				case "REMOVE":
					RemoveServer removeServer = new RemoveServer(clientCommand, resourceManager, output, clientID, secret);
					removeServer.remove();
					break;

				case "SHARE":
					ShareServer shareServer = new ShareServer(clientCommand, resourceManager, output, clientID, secret);
					shareServer.share();
					break;

				case "QUERY":
					boolean relay = (boolean)clientCommand.get("relay");
					QueryServer queryServer = new QueryServer(clientCommand, resourceManager, output, clientID, relay);
					queryServer.query();
					break;

				case "FETCH":
					FetchServer fetchServer = new FetchServer(clientCommand, resourceManager, output, clientID);
					fetchServer.fetch();
					break;

				case "EXCHANGE":
				ArrayList<JSONObject> e = (ArrayList<JSONObject>) command.get("serverList");
				// get the serverlist and visit each JSONObject in them for exchanging
				ExchangeServer es = new ExchangeServer(resourceManager);
				JSONObject[] resultArray = new JSONObject[e.size()];
				int len = 0;
				JSONObject result = new JSONObject();
			//	result.put("result1", e.size());
				for (JSONObject j : e){
					// process the IP Address for exchanging and write the response into an array.
					JSONObject result1 = es.exchange(j);
					resultArray[len] = result1;
					len++;
				}
				result.put("response", resultArray);
				return result;
					break;
				default:
					logger.warning(loggerPrefix + "unknown command");
					clientSocket.close();
					break;
				}

			}
		}
		}
		catch(IOException | ParseException e){
			e.printStackTrace();
		}
	}


//	private static JSONObject parseCommand(JSONObject command){
//		JSONObject results = new JSONObject();
//        results.put("response", "success");
//
//		switch((String) command.get("command")){
//		case "REMOVE":{
//			JSONObject resource = (JSONObject) command.get("resource");
//			String name =resource.get("name").toString();
//			String tags = resource.get("tags").toString();
//			String description = resource.get("description").toString();
//			String uri = resource.get("uri").toString();
//			String channel = resource.get("channel").toString();
//			String owner = resource.get("owner").toString();
//			String ezserver = resource.get("ezserver").toString();
//			results.put("REMOVE RESPONSE","success");
//			return results;
//		}
//
//		case "SHARE":{
//			JSONObject resource = (JSONObject) command.get("resource");
//            String secret =(String)command.get("secret");
//			String name =resource.get("name").toString();
//			String tags = resource.get("tags").toString();
//			String description = resource.get("description").toString();
//			String uri = resource.get("uri").toString();
//			String channel = resource.get("channel").toString();
//			String owner = resource.get("owner").toString();
//			String ezserver = resource.get("ezserver").toString();
//			results.put("SHARE RESPONSE","success");
//			return results;
//		}
//
//		case "PUBLISH":{
//			JSONObject resource = (JSONObject) command.get("resource");
//			ArrayList<JSONObject> outcomeJSON;
//
//			// query object to handle publish command
//			PublishServer publishObject = new PublishServer(resource, resourceManager);
//			outcomeJSON = publishObject.publish();
//
//			// respond with the outcome of the operation
//			for (int i = 0; i < outcomeJSON.size(); i++) {
//				results.put("result"+i, outcomeJSON.get(i));
//			}
//
//			return results;
//		}
//
//		case "FETCH":{
//			System.out.println("Fetching!");
//			JSONObject resourceTemplate = (JSONObject) command.get("resourceTemplate");
//			String name =resourceTemplate.get("name").toString();
//			String tags = resourceTemplate.get("tags").toString();
//			String description = resourceTemplate.get("description").toString();
//			String uri = resourceTemplate.get("uri").toString();
//			String channel = resourceTemplate.get("channel").toString();
//			String owner = resourceTemplate.get("owner").toString();
//			String ezserver = resourceTemplate.get("ezserver").toString();
//			results.put("FETCH RESPONSE","success");
//			return results;
//		}
//
//		case "QUERY":{
//			JSONObject resource = (JSONObject) command.get("resourceTemplate");
//
//			// query server object to handle queries
//			QueryServer queryObject = new QueryServer(resourceManager);
//			ArrayList<JSONObject> resourcesJSONFormat;
//
//			boolean relay = (boolean)command.get("relay");
//
//			resourcesJSONFormat = queryObject.query(resource);
//
//			for (int i = 0; i < resourcesJSONFormat.size(); i++) {
//				results.put("result"+i, resourcesJSONFormat.get(i));
//			}
//
//			return results;
//		}
//
//		case "EXCHANGE":{
//			break;
//		}
//
//		default:			try{
//			throw new Exception();
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		}
//		return results;
//	}

	/*
	 * Large ramdon string generator
	 * for generate server secret
	 */
	private static String secretGen(int length) {
	    String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	    Random random = new Random();
	    StringBuffer buf = new StringBuffer();
	    for (int i = 0; i < length; i++) {
	        int num = random.nextInt(62);
	        buf.append(str.charAt(num));
	    }
	    return buf.toString();
	}

}



// define a timer thread class for exchanging periodically
class MyTask extends TimerTask {
	// define variables
	public ArrayList<JSONObject> serverList;
	public ServerSocket socket;
	public ResourceManager resourceManager;
	public DataOutputStream output;
	public DataInputStream input;
	public int i;
	// constructor method
	public MyTask(ResourceManager resourceManager) throws IOException{
		//this.socket = socket;
		this.resourceManager = resourceManager;
		try(Socket socket = new Socket("localhost",3780);){
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
		}
	}
    @Override
    public void run() {
    	int i;
    	// loop each serverresource in the resourceManager class and try to exchange it with current server record
    	int len = resourceManager.serverlist.size();
    	if(len>0){
    	for(i=0; i<len; i++){
 if(resourceManager.serverlist.size()==0){
	 break;
 }
    	JSONObject j1 =resourceManager.serverlist.get(i);

    		// get the hostname and port
        String hostname = j1.get("hostname").toString();
        int port = Integer.parseInt(j1.get("port").toString());

        // Use exchanger class to exchange
		Exchanger e = new Exchanger(hostname,port);
		try {

				try {
					if(!e.exchange(resourceManager,this)){i--;}
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				}
			catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
						}
    	}
    	// print information after finished
        System.out.println("Finished Exchanging!");
    	}
    	else{
    		System.out.println("No available server on the list");
    	}

    }

}
