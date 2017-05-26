package EZShare;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ExchangeServerNG {
	private static ArrayList<Host> hostList = ResourceManager.hostList;
	private static ArrayList<Host> hostList_secure = ResourceManager.hostList_secure;
	private static Logger logger = Logger.getLogger(ExchangeServerNG.class.getName());
	
	public static void exchange(JSONObject clientCommand,DataOutputStream output, int clientID, boolean isSecure) {
		
		String loggerPrefix = "Client" + clientID + ": ";	
		
		if (clientCommand.containsKey("serverList")) {
			JSONArray serverlist = (JSONArray) clientCommand.get("serverList");
			logger.info(loggerPrefix + serverlist.toJSONString());
			for (int i = 0; i < serverlist.size(); i++) {
				JSONObject host = (JSONObject) serverlist.get(i);
				logger.info(loggerPrefix + host.toJSONString());
				if (host.containsKey("hostname") && host.containsKey("port")) {
					Host hostObj = new Host(
							host.get("hostname").toString(), 
							Integer.parseInt(host.get("port").toString()));
					addHost(isSecure, hostObj, loggerPrefix);
				} else {
					RespondUtil.returnErrorMsg(output, "invalid server record");
					return;
				}
			}

		} else {
			RespondUtil.returnErrorMsg(output, "missing or invalid server list");
			return;
		}
		RespondUtil.returnSuccessMsg(output);
	}
	
	@SuppressWarnings("unchecked")
	public static void serverExchange(long interval, boolean isSecure) {
		
		ArrayList<Host> targetHostList = isSecure ? hostList_secure : hostList;
		
		String loggerPrefix = isSecure ? "[SECURE]" : "[UNSECURE]";

		Host target = null;
		JSONObject exchangeCommand = new JSONObject();

		while (true) {
			JSONArray serverList = new JSONArray();
			synchronized (targetHostList) {
				logger.info(loggerPrefix + "Server Exchange Task Initiated");
				target = getRandomHost(targetHostList);
				if (target == null) 
					logger.warning(loggerPrefix + "Empty Serverlist! Skip Exchange");
				else if (!target.equals(Server.self) && !target.equals(Server.self_secure)) {
					logger.info(loggerPrefix + "Hit: " + target.toString());
					for (Host h : targetHostList) {
						serverList.add(h.toJSON());
					}
					exchangeCommand.put("command", "EXCHANGE");
					exchangeCommand.put("serverList", serverList);
					logger.info(loggerPrefix + exchangeCommand.toJSONString());
					Socket socket = null;
					SSLSocket sslSocket = null;
					DataInputStream input;
					DataOutputStream output;
					try {
						if (isSecure) {
//							SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();  
							sslSocket = (SSLSocket) Server.ctx.getSocketFactory().createSocket(target.getHostname(), target.getPort());  
							input = new DataInputStream(sslSocket.getInputStream());
							output = new DataOutputStream(sslSocket.getOutputStream());
						} else {
							socket = new Socket(target.getHostname(), target.getPort());
							input = new DataInputStream(socket.getInputStream());
							output = new DataOutputStream(socket.getOutputStream());
						}
						output.writeUTF(exchangeCommand.toJSONString());
						output.flush();
						logger.info(loggerPrefix + input.readUTF().toString());
						if (socket != null) socket.close();
						if (sslSocket != null) sslSocket.close();
						
					} catch (IOException e) {
						// remove
						logger.warning("Err: " + target.toString());
						if (targetHostList.contains(target)) {
							targetHostList.remove(target);
							logger.info(loggerPrefix + target.toString() + "Removed");
						}
					}
				}
			}
			try {
				logger.warning(loggerPrefix + "Server Exchange Task Finished, Hibernating...");
				Thread.sleep(interval);
			} catch (InterruptedException e) {}
		}
	}
	
	private static Host getRandomHost(ArrayList<Host> hostList) {
		Random random = new Random();
		int size = hostList.size();
		return (size > 0) ? hostList.get(random.nextInt(size)) : null;
	}
	
	public static boolean addHost(boolean isSecure, Host host, String loggerPrefix) {
		ArrayList<Host> targetHostList = isSecure ? hostList_secure : hostList;
		for (Host h: targetHostList) 
			if (host.equals(h)) {
				logger.warning(loggerPrefix + host.toString() + " Duplicated!");
				return false;
			}
		targetHostList.add(host);
		logger.info(loggerPrefix + host.toString() + " Added!");
		return true;
	}
	
	public static boolean addHost(boolean isSecure, Host host) {
		return addHost(isSecure, host, "");
	}
}
