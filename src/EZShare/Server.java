package EZShare;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Server {

	public static Host self;
	public static Host self_secure;

	private static int port = 3780;
	private static int sport = 3781;
	private static int connection_interval = 60 * 10;
	private static int exchange_interval = 60 * 10;
	private static String hostname;
	private static String secret;
	private static int counter =0;
	private static ResourceManager resourceManager; // resource manager handles all server resources
	
	private static Logger logger = null;
	public static SSLContext ctx;
	
	static ArrayList<JSONObject> subscriptionResources = new ArrayList<JSONObject>();
	static HashMap <JSONObject,Integer> resourceMap = new HashMap<JSONObject,Integer>();
	static HashMap <Integer,JSONObject>unsubscribeMap = new HashMap<Integer,JSONObject>();
	static HashMap <Integer,Socket> subscribeMap = new HashMap<Integer,Socket>();
	static HashMap <String,RelayThread> relayMap = new HashMap<String,RelayThread>();
	static HashMap <String, Socket> relayIDMap = new HashMap<String,Socket>();
	static HashMap <Socket,HashMap> map1 = new HashMap<Socket,HashMap>();
	static HashMap <Integer,JSONObject> map2 = new HashMap<Integer,JSONObject>();
	Object[][] o = new Object[100][2];
	int len = 0;
	static int hit = 0;
	public static void main(String[] args) {

		// init logger
		try {
			LogManager.getLogManager().readConfiguration(Server.class.getClassLoader().getResourceAsStream("logging.properties"));

			String keyStorePwd = "comp90015";
			ctx = SSLContext.getInstance("TLS");  
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");  
	        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");  
	        KeyStore ks = KeyStore.getInstance("JKS");
	        KeyStore tks = KeyStore.getInstance("JKS");
	        ks.load(Server.class.getClassLoader().getResourceAsStream("keystore/serverKeystore/myServer"), keyStorePwd.toCharArray());  
	        tks.load(Server.class.getClassLoader().getResourceAsStream("keystore/serverKeystore/myServer"), keyStorePwd.toCharArray());
	        kmf.init(ks, keyStorePwd.toCharArray());  
	        tmf.init(tks);  
	        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
	        
		} catch (SecurityException | IOException e1) { 
			e1.printStackTrace(); 
		} catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | UnrecoverableKeyException | KeyManagementException e) {
				e.printStackTrace();
		}
		logger = Logger.getLogger(Server.class.getName());
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
		} else try {
			hostname = InetAddress.getLocalHost().getHostAddress().toString();
			logger.info("no assigned advertised hostname, using default : " + hostname);
		} catch (IOException e) {
			System.out.println("Cannot resolve local address! exit.");
			return;
		}

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
			SSLServerSocket serverSSLSocket = (SSLServerSocket) ctx.getServerSocketFactory().createServerSocket(sport);
//			SSLServerSocket serverSSLSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(sport);
			serverSSLSocket.setNeedClientAuth(true);

			
			resourceManager = new ResourceManager(serverSocket.getInetAddress().toString(), Integer.toString(port));
			
			self = new Host(hostname, port);
			self_secure = new Host(hostname, sport);
			
			ExchangeServerNG.addHost(false, self);
			ExchangeServerNG.addHost(true, self_secure);
			
			logger.fine("Server established, standing by.");
			
			Thread serverExchange = new Thread(() -> ExchangeServerNG.serverExchange(exchange_interval * 1000, false));
			serverExchange.start();
			Thread serverExchangeSecure = new Thread(() -> ExchangeServerNG.serverExchange(exchange_interval * 1000, true));
			serverExchangeSecure.start();
			
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

	private static boolean canSubscribe(JSONObject rTemplate1, JSONObject rTemplate2){
		// rTemplate1 is client resource template and rTemplate2 is any server resource template
		// determine if a client resource template matches the given server resource template

		// check name of the resource
		if ( !(rTemplate1.get("name").toString().equals(""))&& !rTemplate1.get("name").equals(rTemplate2.get("name")) )
		 return false;
	//	ArrayList<String> ta = (ArrayList<String>) rTemplate1.get("tags");
		// check tags are the same
		else if ((((ArrayList<String>) rTemplate1.get("tags")).size()!=0 )&& !rTemplate1.get("tags").equals(rTemplate2.get("tags")) )
			 return false;
		
		// check if descriptions match
		else if (! (rTemplate1.get("description").toString().equals(""))&&!rTemplate1.get("description").equals(rTemplate2.get("description")) )
		 return false;
		
		// check if the uri's match
		else if ( !(rTemplate1.get("uri").toString().equals(""))&&!rTemplate1.get("uri").equals(rTemplate2.get("uri")) )
		 return false;
		
		// check if the channels match
		else if (!(rTemplate1.get("channel").toString().equals(""))&& !rTemplate1.get("channel").equals(rTemplate2.get("channel")) )
		 return false;
		
		// check if the owner's are the same
		else if ( !(rTemplate1.get("owner").toString().equals(""))&&!rTemplate1.get("owner").equals(rTemplate2.get("owner")) )
		 return false;
		
		// check if ezServer matches
		else if ( (rTemplate1.get("ezServer")!=null)&&!rTemplate1.get("ezServer").equals(rTemplate2.get("ezServer")) )
		 return false;
		
		return true;
	}
	
	private static void serveClient(Socket clientSocket, int clientID, boolean isSecure){

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
						JSONObject resourceTemplate = (JSONObject) clientCommand.get("resource");
						ArrayList<JSONObject> outcomeJSON;

						// query object to handle publish command
					    PublishServer publishServer = new PublishServer(clientCommand, resourceManager, output, clientID,map1, hit);
					    hit = publishServer.publish();
					//	System.out.println("Server:"+hit);
//						PublishServer publishObject = new PublishServer(resourceTemplate, resourceManager);
//						outcomeJSON = publishObject.publish();
//						for(JSONObject j : outcomeJSON){
//							if(j.get("response").equals("success")){
//
//										Iterator iter = map1.entrySet().iterator();
//										while (iter.hasNext()) {
//										HashMap.Entry entry = (HashMap.Entry) iter.next();
//										Socket soc = (Socket) entry.getKey();
//										HashMap hash = (HashMap) entry.getValue();
//											Iterator iter1 = hash.entrySet().iterator();
//												while (iter1.hasNext()) {
//												HashMap.Entry entry1 = (HashMap.Entry) iter1.next();
//												int key = Integer.parseInt( (entry1.getKey().toString()));
//												JSONObject val = (JSONObject) entry1.getValue();
//												if(val.equals(resourceTemplate)){
//													DataOutputStream outsoc = new DataOutputStream (soc.getOutputStream());
//													outsoc.writeUTF(val.toJSONString());
//													outsoc.flush();
//												}
//												}
//										}
//										
//
//									//	out1.close();
//									//	socket.close();
//									}
//								
//							
//						}
//						// respond with the outcome of the operation
//						for (int i = 0; i < outcomeJSON.size(); i++) {
//							results.put("result"+i, outcomeJSON.get(i));
//						}
//						output.writeUTF(results.toJSONString());
						
						keepAlive = false;
						break;

					case "REMOVE":
						RemoveServer removeServer = new RemoveServer(clientCommand, resourceManager, output, clientID, secret);
						removeServer.remove();
						keepAlive = false;
						break;

					case "SHARE":
						ShareServer shareServer = new ShareServer(clientCommand, resourceManager, output, clientID, secret,map1, hit);
						hit = shareServer.share();
						keepAlive = false;
						break;

					case "QUERY":
						QueryServer queryServer = new QueryServer(clientCommand, resourceManager, output, clientID, isSecure);
						queryServer.query();
						keepAlive = false;
						break;

					case "FETCH":
						FetchServer fetchServer = new FetchServer(clientCommand, resourceManager, output, clientID);
						fetchServer.fetch();
						keepAlive = false;
						break;

					case "EXCHANGE":
						ExchangeServerNG.exchange(clientCommand, output, clientID, isSecure);
						keepAlive = false;
						break;
						
					case "SUBSCRIBE":

						
						JSONObject resource = (JSONObject) clientCommand.get("resourceTemplate");

					/*	for(Resource r : resourceManager.getServerResources()){
							if(canSubscribe(resource,r.toJSON()))
									{
								hit++;
								output.writeUTF(r.toJSON().toJSONString());
									}*/
						/*	else{
								output.writeUTF(resource.toJSONString());
							//	output.flush();
								System.out.println(resource.get("name").toString().equals(""));
								System.out.println(resource.get("channel").toString()=="");
								System.out.println(resource.get("owner").toString()=="");
								System.out.println(resource.get("uri").toString()=="");
								System.out.println(resource.get("description").toString()=="");
								System.out.println(resource.get("ezServer")==null);
								System.out.println(((ArrayList<String>)(resource.get("tags"))).size());
								output.writeUTF(r.toJSON().toJSONString());
								output.flush();
							}*/
							
						//}
						subscriptionResources.add(resource);
			//			resourceMap.put(resource, Integer.parseInt(clientCommand.get("id").toString()));
						//SubscribeServer(server.accept().start());
						String id = (clientCommand.get("id").toString());
			//			subscribeMap.put(id, client);
			//			unsubscribeMap.put(id, resource);


						if(map1.get(clientSocket)==null){
							HashMap <String,JSONObject> newmap = new HashMap<String,JSONObject>();
							newmap.put(id, resource);
							map1.put(clientSocket, newmap);
						}
						else{
							HashMap map = map1.get(clientSocket);
							map.put(id, resource);
							
						}
						JSONObject result = new JSONObject();
						result.put("response", "success");
						result.put("id", id);
						try {
	
							output.writeUTF(result.toJSONString());
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				/*			ArrayList<JSONObject> j1 = new ArrayList<JSONObject>();
							JSONObject j2 =  new JSONObject();
							PrintWriter printwriter = new PrintWriter(output,true);
							Iterator it = resourceManager.getServerResources().iterator();
							while(it.hasNext()){
								Resource r1 = (Resource) it.next();
								if(canSubscribe(resource,r1.toJSON()))
								{
							hit++;
						//	j1.add(r1.toJSON());
						//	j2 = r1.toJSON();
							output.writeUTF(r1.toJSON().toJSONString());
						//	output.flush();
								}
							}*/
							
							if((boolean)clientCommand.get("relay")){
								ArrayList<Host> hostList = new ArrayList<Host>();
								if(isSecure){	
									hostList = resourceManager.hostList;

								}
								else{
									hostList = resourceManager.hostList_secure;
								}
								Iterator hostit = hostList.iterator();
								while(hostit.hasNext()){
									Host h = (Host) hostit.next();
									String hname = h.getHostname();
									int hport = h.getPort();
									JSONObject serverCommand = new JSONObject();
									serverCommand.put("command", "SUBSCRIBE");
									serverCommand.put("resourceTemplate", resource);
									serverCommand.put("id", id);
									serverCommand.put("relay", true);
									Socket subsocket;
									if(isSecure){
									 subsocket = (SSLSocket) ctx.getSocketFactory().createSocket(hname, hport);}
									else{
										subsocket = new Socket(hname,hport);
									}
									DataOutputStream subout = new DataOutputStream(subsocket.getOutputStream());
									DataInputStream subint = new DataInputStream(subsocket.getInputStream());
									subout.writeUTF(serverCommand.toJSONString());
									subout.flush();
									JSONParser p = new JSONParser();
									JSONObject response = (JSONObject) p.parse(subint.readUTF());
									System.out.println(response);		
									if (response.containsKey("response") && "success".equals(response.get("response").toString())) {
										RelayThread rt = new RelayThread(subint,output);
										rt.start();
										relayMap.put(id, rt);
										relayIDMap.put(id, subsocket);
									}

								}
							}
							
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						break;
						
					case "UNSUBSCRIBE":
						 id = clientCommand.get("id").toString();
					/*	JSONObject j = unsubscribeMap.get(id);
						if(subscriptionResources.contains(j)){
							subscriptionResources.remove(j);
							Socket socket = subscribeMap.get(id);
							DataOutputStream out2 = new DataOutputStream(socket.getOutputStream());
							result = new JSONObject();
							result.put("response","success");
							out2.writeUTF(result.toJSONString());
							out2.flush();
						//	out2.close();
						}*/
						
						if(map1.get(clientSocket) != null){
							HashMap unmap = map1.get(clientSocket);
							if(unmap.get(id)!=null){
								RelayThread tr = relayMap.get(id);
								if(tr!=null){
									tr.stop();
									Socket s = relayIDMap.get(id);
								DataOutputStream relayout1 = new DataOutputStream(s.getOutputStream());
								relayout1.writeUTF(clientCommand.toJSONString());
								relayout1.flush();
								relayout1.close();
								relayIDMap.remove(id);
								relayMap.remove(id);
								}
								unmap.remove(id);
							//	RespondUtil.returnSuccessMsg(output);
								if(unmap.size()==0){
									map1.remove(clientSocket);
									if(map1.size()==0){
										JSONObject resultsize = new JSONObject();
										resultsize.put("resultSize",hit);
										hit=0;
										output.writeUTF(resultsize.toJSONString());
										output.flush();
									}
								}
								
							}
						//	else{
							//	RespondUtil.returnErrorMsg(output, "missing resourceTemplate");
						//	}
						}
						

					//	else{
						//	RespondUtil.returnErrorMsg(output, "missing resourceTemplate");
					//	}
						keepAlive = false;
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
	private static boolean isValidUri(String uri) {
		final URI u;
		String scheme = "";
		try {
			u = URI.create(uri);
			scheme = u.getScheme();
		} catch (Exception e) { 
			return false; 
		}
		return ("http".equals(scheme) || "file".equals(scheme));
	}



}


