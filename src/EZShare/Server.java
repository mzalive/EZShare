package EZShare;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Server {

	private static String hostname = "localhost";
	private static int port = 3780;
	private static int sport = 3781;
	private static int connection_interval = 600;
	private static int exchange_interval = 600;
	private static String secret;
	private static int counter =0;
	private static ResourceManager resourceManager; // resource manager handles all server resources
	
	private static Logger logger = null;

	public static void main(String[] args) {

		// init logger
		try {
			LogManager.getLogManager().readConfiguration(Server.class.getClassLoader().getResourceAsStream("logging.properties"));
		} catch (SecurityException | IOException e1) { e1.printStackTrace(); }
		logger = Logger.getLogger(Server.class.getName());
		
		System.setProperty("javax.net.ssl.keyStore","serverKeystore/aGreatName");
		System.setProperty("javax.net.ssl.keyStorePassword","comp90015");
//		System.setProperty("javax.net.debug","all");
		
		// handle args
		Options options = new Options();
		options.addOption("advertisedhostname", true, "advertised hostname");
		options.addOption("connectionintervallimit", true,"connection interval limit in seconds");
		options.addOption("exchangeinterval", true,"exchange interval in seconds");
		options.addOption("port", true,"server port, an integer");
		options.addOption("sport", true,"secure server port, an integer");
		options.addOption("secret", true,"secret");
		options.addOption("debug", false, "print debug information");
		options.addOption("h","help", false, "show usage");
		CommandLineParser parser = new DefaultParser();
		CommandLine cLine = null;
		try {
			cLine = parser.parse(options, args);
		} catch (org.apache.commons.cli.ParseException e) {
			System.out.println(e.getMessage());
			HelpFormatter hformatter = new HelpFormatter();
			hformatter.printHelp("EZShare Server", options);
			return;
		}
		if (cLine.hasOption("h")) {
			HelpFormatter hformatter = new HelpFormatter();
			hformatter.printHelp("EZShare Server", options);
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
			logger.info("set port : " + port);
		} else logger.info("no assigned port, using default : " + port);
		
		if (cLine.hasOption("sport")) {
			sport = Integer.valueOf(cLine.getOptionValue("sport"));
			logger.info("set secure port : " + sport);
		} else logger.info("no assigned secure port, using default : " + sport);

		if (cLine.hasOption("secret")) {
			secret = cLine.getOptionValue("secret");
			logger.info("set secret : " + secret);
		} else {
			secret = secretGen(32);
			logger.info("no assigned secret, auto generate : " + secret);
		}

		
		try{
			ServerSocketFactory serverSocketFactory = ServerSocketFactory.getDefault();
			ServerSocket serverSocket = serverSocketFactory.createServerSocket(port);
			
			SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			SSLServerSocket serverSSLSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(sport);
			
			resourceManager = new ResourceManager(serverSocket.getInetAddress().toString(), Integer.toString(port));
			logger.fine("Server established, standing by.");
			Timer timer = new Timer();
			timer.schedule(new MyTask(resourceManager), 1000, exchange_interval*1000);
			
			Thread server = new Thread(()->serverStarter(serverSocket));
			server.start();
			Thread serverSecure = new Thread(()->serverSecureStarter(serverSSLSocket));
			serverSecure.start();
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void serverStarter(ServerSocket serverSocket) {
		while(true){
			try {
				Socket clientSocket = serverSocket.accept();
				counter++;
				logger.info("[unsecure] Incoming connection from client " + counter + ", starting new thread");
				Thread t = new Thread(()->serveClient(clientSocket, counter, false));
				t.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void serverSecureStarter(ServerSocket serverSSLSocket) {
		while(true){
			try {
				SSLSocket clientSecureSocket = (SSLSocket) serverSSLSocket.accept();
				counter++;
				logger.info("[secure] Incoming connection from client " + counter + ", starting new thread");
				Thread t = new Thread(()->serveClient(clientSecureSocket, counter, true));
				t.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void serveClient(Socket clientSocket, int clientID, boolean secure){

		Logger logger = Logger.getLogger(Server.class.getName());
		String loggerPrefix = "Client " + clientID + ": ";

		JSONParser parser = new JSONParser();

		boolean keepAlive = true;

		try {

			DataInputStream input = new DataInputStream(clientSocket.getInputStream());
			DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
			while (keepAlive) {
					JSONObject clientCommand = (JSONObject)parser.parse(input.readUTF());
					logger.info(loggerPrefix + "original request: " + clientCommand.toJSONString());

					String command = clientCommand.get("command").toString();
					logger.info(loggerPrefix + "command rcvd: " + command);

					switch (command) {
					case "PUBLISH":

						// query object to handle publish command
						PublishServer publishServer = new PublishServer(clientCommand, resourceManager, output, clientID);
						publishServer.publish();
						keepAlive = false;
						break;

					case "REMOVE":
						RemoveServer removeServer = new RemoveServer(clientCommand, resourceManager, output, clientID, secret);
						removeServer.remove();
						keepAlive = false;
						break;

					case "SHARE":
						ShareServer shareServer = new ShareServer(clientCommand, resourceManager, output, clientID, secret);
						shareServer.share();
						keepAlive = false;
						break;

					case "QUERY":
						boolean relay = (boolean)clientCommand.get("relay");
						QueryServer queryServer = new QueryServer(clientCommand, resourceManager, output, clientID, relay);
						queryServer.query();
						keepAlive = false;
						break;

					case "FETCH":
						FetchServer fetchServer = new FetchServer(clientCommand, resourceManager, output, clientID);
						fetchServer.fetch();
						keepAlive = false;
						break;

					case "EXCHANGE":
						JSONArray jsonArray = (JSONArray) clientCommand.get("serverList");
						ArrayList<JSONObject> e = new ArrayList<JSONObject>();
						if (jsonArray != null) { 
							for (int i=0; i<jsonArray.size(); i++){ 
								e.add((JSONObject) jsonArray.get(i));
							} 
						} 

						// get the serverlist and visit each JSONObject in them for exchanging
						ExchangeServer es = new ExchangeServer(resourceManager);
						JSONObject[] resultArray = new JSONObject[e.size()];
						int len = 0;
						JSONObject result = new JSONObject();
						//	result.put("result1", e.size());
						for (JSONObject j : e){
							// process the IP Address for exchanging and write the response into an array.
							JSONObject result1 = es.exchange(j,output);
							resultArray[len] = result1;
							len++;
						}
						result.put("response", resultArray);
						for(JSONObject j : resultArray){
							logger.info(j.toJSONString());
						}
						break;
					default:
						logger.warning(loggerPrefix + "unknown command");
						clientSocket.close();
						break;
					}

				}
			
			
			clientSocket.close();
			logger.info(loggerPrefix + "Disconnect.");;
		} catch (IOException | ParseException e) {
			logger.warning(loggerPrefix + "Unexpected exception");
		}
	}




	

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
	}
	@Override
	public void run() {
		Logger logger = Logger.getLogger(MyTask.class.getName());
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
						e1.printStackTrace();
					}

				}
				catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			// print information after finished
			logger.fine("Finished Exchanging!");
		} else {
			logger.warning("No available server on the list");
		}

	}

}
