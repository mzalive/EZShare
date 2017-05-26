package EZShare;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.sf.json.JSONArray;

public class Client {
	private static Logger logger;

	private static String host = "localhost";
	private static int port = 3780;
	private static boolean secure = false;

	private static String channel = "";
	private static String description = "";
	private static String name = "";
	private static String owner = "";
	private static String secret = "";
	private static JSONArray servers = new JSONArray();
	private static ArrayList<String> tags = new ArrayList<String>();
	private static String uri = "";


	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		
		Socket socket = null;

		JSONObject clientCommand = new JSONObject();
		JSONParser p = new JSONParser();

		// init logger
		try {
			LogManager.getLogManager().readConfiguration(Client.class.getClassLoader().getResourceAsStream("logging.properties"));
		} catch (SecurityException | IOException e1) { e1.printStackTrace(); }
		logger = Logger.getLogger(Client.class.getName());

		// handle args
		Options options = new Options();
		options.addOption("channel", true, "channel");
		options.addOption("debug", false, "print debug information");
		options.addOption("description", true, "resource description");
		options.addOption("exchange", false, "exchange server list with server");
		options.addOption("fetch", false, "fetch resources from server");
		options.addOption("subscribe", false, "subscribe resources from server");
		options.addOption("host", true, "server host, a domain name or IP address");
		options.addOption("name", true, "resource name");
		options.addOption("owner", true, "owner");
		options.addOption("port", true, "server port, an integer");
		options.addOption("publish", false, "publish resource on server");
		options.addOption("query", false, "query for resources from server");
		options.addOption("remove", false, "remove resource from server");
		options.addOption("secret", true, "secret");
		options.addOption("servers", true, "server list, host1:port1,host2:port2,...");
		options.addOption("share", false, "share resource on server");
		options.addOption("tags", true, "resource tags, tag1,tag2,tag3,...");
		options.addOption("uri", true, "resource URI");
		options.addOption("secure", false, "use secure socket");
		options.addOption("h","help", false, "show usage");
		CommandLineParser parser = new DefaultParser();
		CommandLine cLine = null;

		try {
			cLine = parser.parse(options, args);
		} catch (org.apache.commons.cli.ParseException e) {
			System.out.println(e.getMessage());
			logger.warning(e.getMessage());

			HelpFormatter hFormatter = new HelpFormatter();
			hFormatter.printHelp("EZShare Client", options);
			return;
		}

		if (args.length == 0 || cLine.hasOption("h")) {
			HelpFormatter hFormatter = new HelpFormatter();
			hFormatter.printHelp("EZShare Client", options);
			return;
		}

		// set debug mode
		if (cLine.hasOption("debug")) {
			logger.setLevel(Level.ALL);
			logger.info("debug mode on");
		} else logger.setLevel(Level.OFF);

		// set secure mode
		if (cLine.hasOption("secure")) {
			secure = true;
			logger.info("secure mode on");
		} else logger.info("unsecure mode");

		// check if the host and port is set manually
		if (cLine.hasOption("host")) {
			host = cLine.getOptionValue("host");
			logger.info("host : " + host);
		} else logger.info("no assigned host, using default : " + host);
		if (cLine.hasOption("port")) {
			port = Integer.valueOf(cLine.getOptionValue("port"));
			logger.info("set port : " + port);
		} else logger.info("no assigned port, using default : " + port);

		//create socket for connection
		try {
			if (secure) {
				logger.info("[SECURE] try connecting " + host + ":" + port);
//				System.setProperty("javax.net.ssl.keyStore", "keystore/clientKeystore/myClient");
//				System.setProperty("javax.net.ssl.keyStorePassword","comp90015");
//				System.setProperty("javax.net.ssl.trustStore","keystore/clientKeystore/myClient");
//				System.setProperty("javax.net.debug","all");
				String keyStorePwd = "comp90015";
				SSLContext ctx = SSLContext.getInstance("SSL");  
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");  
		        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");  
		        KeyStore ks = KeyStore.getInstance("JKS");
		        KeyStore tks = KeyStore.getInstance("JKS");
		        ks.load(Server.class.getClassLoader().getResourceAsStream("keystore/clientKeystore/myClient"), keyStorePwd.toCharArray());  
		        tks.load(Server.class.getClassLoader().getResourceAsStream("keystore/clientKeystore/myClient"), keyStorePwd.toCharArray());  
		        kmf.init(ks, keyStorePwd.toCharArray());  
		        tmf.init(tks);  
		        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		        
//				SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
				socket = (SSLSocket) ctx.getSocketFactory().createSocket(host, port);
			} else {
				logger.info("[UNSECURE] try connecting " + host + ":" + port);
				socket = new Socket(host, port);
			}
			// get the input and output stream
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());

			/*
			 * QUERY
			 */
			if (cLine.hasOption("query")) {
				logger.info("command: QUERY");
				extractResourceTemplate(cLine);
				Resource template = new Resource(name, description, tags, uri, channel, owner);

				// compose request
				clientCommand.put("command", "QUERY");
				clientCommand.put("resourceTemplate", template.toJSON());
				clientCommand.put("relay", true);

				// send request
				output.writeUTF(clientCommand.toJSONString());
				output.flush();

				// get response
				System.out.println("waiting for server respond...");
				JSONObject response = (JSONObject) p.parse(input.readUTF());
				System.out.println(response);
				if (response.get("response").equals("success")) {
					boolean hasMoreData = true;
					while (hasMoreData) {
						JSONObject result = (JSONObject) p.parse(input.readUTF());
						if (result.containsKey("resultSize")) {
							System.out.println("Total: " + result.get("resultSize") + " results");
							hasMoreData = false;
						} else System.out.println(result);
					}
				}			

			} 

			/*
			 * EXCHANGE
			 */
			else if (cLine.hasOption("exchange")) {
				logger.info("command: EXCHANGE");

				// extract server list
				if (cLine.hasOption("servers")) {
					String serverArgs = cLine.getOptionValue("servers");
					ArrayList<String> serverlist = new ArrayList<String>(Arrays.asList(serverArgs.split(",")));
					for (String server: serverlist) {
						String[] serverPar = server.split(":");
						if (serverPar.length == 2 || serverPar[0] != "" || serverPar[1] != "") {
							JSONObject serverObj = new JSONObject();
							serverObj.put("hostname", serverPar[0]);
							serverObj.put("port", Integer.valueOf(serverPar[1]));
							servers.add(serverObj);
						} else logger.warning("invalid server: " + server);
					}
					logger.info("servers: " + servers.toString());	

					// compose request
					clientCommand.put("command", "EXCHANGE");
					clientCommand.put("serverList", servers);

					// send request
					output.writeUTF(clientCommand.toJSONString());
					output.flush();

					// get response
					System.out.println("waiting for server respond...");
					System.out.println(input.readUTF());

				} else {
					System.out.println("No server list");
				}
			}

			/*
			 * FETCH
			 */
			else if (cLine.hasOption("fetch")) {
				logger.info("command: FETCH");
				extractResourceTemplate(cLine);
				Resource template = new Resource(name, description, tags, uri, channel, owner);

				// compose request
				clientCommand.put("command", "FETCH");
				clientCommand.put("resourceTemplate", template.toJSON());

				// send request
				output.writeUTF(clientCommand.toJSONString());
				output.flush();

				// get response
				System.out.println("waiting for server respond...");
				JSONObject response = (JSONObject) p.parse(input.readUTF());
				System.out.println(response);

				if (response.get("response").equals("success")) {
					boolean hasMoreData = true;

					while (hasMoreData) {
						JSONObject result = (JSONObject) p.parse(input.readUTF());

						if (result.containsKey("resultSize")) {
							System.out.println("Total: " + result.get("resultSize") + " results");
							hasMoreData = false;
						} else {
							// get filename
							String uri = result.get("uri").toString();
							String fileName = uri.substring( uri.lastIndexOf('/')+1, uri.length() );

							// get length
							long fileSizeRemaining =(long) (result.get("resourceSize"));

							// create file
							RandomAccessFile downloadingFile = new RandomAccessFile(fileName, "rw");

							// Receive file from server
							int chunkSize = 1024*1024;
							chunkSize = (fileSizeRemaining < chunkSize) ? (int)fileSizeRemaining : 1024*1024;
							byte[] receiveBuffer = new byte[chunkSize];
							int num;
							System.out.println("Downloading "+fileName+" of size "+fileSizeRemaining);
							System.out.println("Writing file: " + System.getProperty("user.dir") + "/" + fileName);
							while((num=input.read(receiveBuffer))>0){
								// Write the received bytes into the RandomAccessFile
								downloadingFile.write(Arrays.copyOf(receiveBuffer, num));

								// Reduce the file size left to read..
								fileSizeRemaining-=num;

								// Set the chunkSize again
								chunkSize = (fileSizeRemaining < chunkSize) ? (int)fileSizeRemaining : 1024*1024;
								receiveBuffer = new byte[chunkSize];

								System.out.print(".");

								// If you're done then break
								if(fileSizeRemaining==0){
									break;
								}
							}
							System.out.print("\n");
							downloadingFile.close();
							System.out.println("Transmission complete: File: "+ fileName);
						}
					}
				} 
			}


			/*
			 * PUBLISH
			 */
			else if (cLine.hasOption("publish")) {
				logger.info("command: PUBLISH");
				extractResourceTemplate(cLine);
				Resource resource = new Resource(name, description, tags, uri, channel, owner);

				// compose request
				clientCommand.put("command", "PUBLISH");
				clientCommand.put("resource", resource.toJSON());

				// send request
				output.writeUTF(clientCommand.toJSONString());
				output.flush();

				// get response
				System.out.println("waiting for server respond...");
				JSONObject response = (JSONObject) p.parse(input.readUTF());
				System.out.println(response);			
			} 


			/*
			 * SHARE
			 */
			else if (cLine.hasOption("share")) {
				logger.info("command: SHARE");
				extractResourceTemplate(cLine);
				Resource resource = new Resource(name, description, tags, uri, channel, owner);

				if (cLine.hasOption("secret")) {
					secret = cLine.getOptionValue("secret");
					logger.info("secret: " + secret);
				} else logger.warning("no assigned resource secret");

				// compose request
				clientCommand.put("command", "SHARE");
				clientCommand.put("secret", secret);
				clientCommand.put("resource", resource.toJSON());

				// send request
				output.writeUTF(clientCommand.toJSONString());
				output.flush();

				// get response
				System.out.println("waiting for server respond...");
				JSONObject response = (JSONObject) p.parse(input.readUTF());
				System.out.println(response);			
			} 


			/*
			 * REMOVE
			 */
			else if (cLine.hasOption("remove")) {
				logger.info("command: REMOVE");
				extractResourceTemplate(cLine);
				Resource resource = new Resource(uri, channel, owner);

				// compose request
				clientCommand.put("command", "REMOVE");
				clientCommand.put("resource", resource.toJSON());

				// send request
				output.writeUTF(clientCommand.toJSONString());
				output.flush();

				// get response
				System.out.println("waiting for server respond...");
				JSONObject response = (JSONObject) p.parse(input.readUTF());
				System.out.println(response);			
			} 
			
			
			/*
			 * SUBSCRIBE
			 */
			else if (cLine.hasOption("subscribe")) {
				logger.info("command: SUBSCRIBE");
				extractResourceTemplate(cLine);
				Resource template = new Resource(name, description, tags, uri, channel, owner);
				String id = idGen(10);

				// compose request
				clientCommand.put("command", "SUBSCRIBE");
				clientCommand.put("resourceTemplate", template.toJSON());
				clientCommand.put("id", id);
				clientCommand.put("relay", true);

				// send request
				output.writeUTF(clientCommand.toJSONString());
				output.flush();

				// get response
				System.out.println("waiting for server respond...");
				JSONObject response = (JSONObject) p.parse(input.readUTF());
				System.out.println(response);		
				if (response.containsKey("response") && "success".equals(response.get("response").toString())) {
					Thread longConnection = new Thread( () -> inputAgent(output, id));
					longConnection.start();
						
					while(true) {
						JSONObject resource = (JSONObject) p.parse(input.readUTF().toString());
						System.out.println(resource);
						if(resource.containsKey("resultSize")) break;
					}
				}
			} 

			socket.close();

		} catch (UnknownHostException e) {
			System.out.println("Unknown server, exit");
			return;
		} catch (IOException e) {
			System.out.println("Failed to connect, exit");
			return;
		} catch (ParseException e) {
			System.out.println("Unexpected response from server");
		} catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | UnrecoverableKeyException | KeyManagementException e) {
			e.printStackTrace();
		}
	}

	private static boolean extractResourceTemplate(CommandLine cLine) {
		if (cLine.hasOption("name")) {
			name = cLine.getOptionValue("name");
			logger.info("name: " + name);
		} else logger.warning("no assigned resource name");
		if (cLine.hasOption("channel")) {
			channel = cLine.getOptionValue("channel");
			logger.info("channel: " + channel);
		} else logger.warning("no assigned resource channel");
		if (cLine.hasOption("description")) {
			description = cLine.getOptionValue("description");
			logger.info("description: " + description);
		} else logger.warning("no assigned resource description");
		if (cLine.hasOption("owner")) {
			owner = cLine.getOptionValue("owner");
			logger.info("owner: " + owner);
		} else logger.warning("no assigned resource owner");
		if (cLine.hasOption("tags")) {
			String tagArgs = cLine.getOptionValue("tags");
			tags = new ArrayList<String>(Arrays.asList(tagArgs.split(",")));
			logger.info("tags: " + tags);
		} else logger.warning("no assigned resource tags");
		if (cLine.hasOption("uri")) {
			uri = cLine.getOptionValue("uri");
			logger.info("uri: " + uri);
		} else logger.warning("no assigned resource uri");
		return true;
	}
	
	private static void inputAgent(DataOutputStream output, String id) {
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		scanner.close();
		String unsubscribe = "{\"command\":\"UNSUBSCRIBE\",\"id\":\"" + id + "\"}";
		
		try{
			output.writeUTF(unsubscribe);
			output.flush();
			logger.info(unsubscribe);
		}catch(IOException e){
			System.out.println(e.toString());
		}
	}
	
	private static String idGen(int length) {
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
