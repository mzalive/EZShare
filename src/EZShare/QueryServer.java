package EZShare;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class QueryServer {

	private ResourceManager resourceManager;
	private ArrayList<Resource> relevantQueryResources = new ArrayList<>(); // store relevant files in memory via ArrayList
	private ArrayList<String> templateTags = null;
	private ArrayList<String> resourceTags = null;
	private boolean stillCandidate = true;
	private DataOutputStream output;
	private JSONObject clientCommand;
	private int clientID;
	private boolean isSecure;

	private static Logger logger = Logger.getLogger(QueryServer.class.getName());
	private static JSONParser p = new JSONParser();
	private static ArrayList<Host> hostList = ResourceManager.hostList;
	private static ArrayList<Host> hostList_secure = ResourceManager.hostList_secure;

	public QueryServer(JSONObject clientCommand, ResourceManager resourceManager, DataOutputStream output, int clientID, boolean isSecure) {
		this.clientCommand = clientCommand;
		this.resourceManager = resourceManager;
		this.output = output;
		this.clientID = clientID;
		this.isSecure = isSecure;
	}

	@SuppressWarnings("unchecked")
	public void query() {
		String loggerPrefix = "Client " + clientID + ": ";

		boolean relay = (boolean)clientCommand.get("relay");
		logger.info(loggerPrefix + "relay " + (relay?"on":"off"));

		ArrayList<Resource> serverResources;
		JSONObject resourceTemplate = new JSONObject();

		// validate resourceTemplate
		logger.info(loggerPrefix + "validating resourceTemplate");
		if (clientCommand.get("resourceTemplate") == null) {
			RespondUtil.returnErrorMsg(output, "missing resourceTemplate");
			logger.warning(loggerPrefix + "resourceTemplate does not exist, exit.");
			return;
		}
		resourceTemplate = (JSONObject) clientCommand.get("resourceTemplate");

		// get the stored resources on server-side
		serverResources = resourceManager.getServerResources();

		//System.out.println(resourceTemplate);

		// enforce server query rules to find matching candidates
		templateTags = parseTags(resourceTemplate.get("tags").toString());
		// if relay field is set to true, send a query request to each server on server list


		for (Resource r : serverResources) {
			logger.fine(loggerPrefix + "validating resource: " + r.toJSON().toString());
			stillCandidate = true;
			resourceTags = parseTags(r.getTags().toString());

			// if template provides a channel
			if (!resourceTemplate.get("owner").equals("")){
				if(!resourceTemplate.get("owner").equals(r.getOwner())) 
					stillCandidate = false;
			}

			// if template provides owner, check if we still have a candidate
			if (!resourceTemplate.get("owner").equals("")){
				if(!resourceTemplate.get("owner").equals(r.getOwner())) 
					stillCandidate = false;
			}

			// if template provides tags, check if we still have a candidate
			if (!templateTags.toString().equals("[]")) {
				String tempStr;
				for (int i = 0; i < templateTags.size(); i++) {
					for (int k = 0; k < resourceTags.size(); k++) {

						tempStr = templateTags.get(i).substring(1, templateTags.get(i).length()-1);
						if (tempStr.equals(resourceTags.get(k).toString())) {
							break;
						}
						if(k + 1 == resourceTags.size()) {
							stillCandidate = false;
						}
					}
				}
			}

			// if template provides owner, check if we still have a candidate
			if (!resourceTemplate.get("uri").equals("")){
				if(!resourceTemplate.get("uri").equals(r.getUri())) 
					stillCandidate = false;
			}

			//OR conditions
			// if template provides a name, check if we still have a candidate
			if (!resourceTemplate.get("name").equals("")){
				if( !r.getName().contains(resourceTemplate.get("name").toString() ))
					stillCandidate = false;
			}
			// if template provides a description, check if we still have a candidate
			else if (!resourceTemplate.get("description").equals("") ){
				if(!r.getDescription().contains(resourceTemplate.get("description").toString()))
					stillCandidate = false;
			}
			// if template name and description are both "", we still have a candidate
			else if (!resourceTemplate.get("name").equals("")){
				if (!resourceTemplate.get("description").equals(""))
					stillCandidate = false;
			}

			// add the candidate if found
			if (stillCandidate) {
				logger.fine(loggerPrefix + "candidate resource found.");
				relevantQueryResources.add(r);
			}

		} // end finding candidates

		RespondUtil.returnSuccessMsg(output);
		
		int relaySize = 0;
		if (relay) relaySize = serverQuery(output, clientCommand, isSecure);
		// pack matching queries into JSON format
		try {
			for (Resource r : relevantQueryResources) 
				output.writeUTF(r.toJSON().toJSONString());
			JSONObject resultSize = new JSONObject();

			// we only want to show resultSize once after relay
			resultSize.put("resultSize", relevantQueryResources.size() + relaySize);
			output.writeUTF(resultSize.toJSONString());
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	@SuppressWarnings("unchecked")
	private static int serverQuery(DataOutputStream clientOutput, JSONObject originalRequest, boolean isSecure) {

		JSONObject forwardCommand = new JSONObject();

		ArrayList<Host> targetHostList = isSecure ? hostList_secure : hostList;

		String loggerPrefix = isSecure ? "[SECURE]" : "[UNSECURE]";

		// forwarded command must set relay field to false and channel and owner to ""
		forwardCommand = originalRequest;
		forwardCommand.put("relay", false);
		forwardCommand.put("channel", "");
		forwardCommand.put("owner", "");

		logger.info(loggerPrefix + "forwarding request: " + forwardCommand.toJSONString());

		int totalResultSize = 0; 

		for(Host h: targetHostList){
			if (h == null) {
				logger.warning(loggerPrefix + "Empty Serverlist! Skip Relay");
				return 0;
			}
			if (h.equals(Server.self) || h.equals(Server.self_secure)) 
				continue;
			logger.info(loggerPrefix + "Hit: " + h.toString());

			Socket socket = null;
			SSLSocket sslSocket = null;
			DataInputStream input;
			DataOutputStream output;

			try{
				if (isSecure) {
					sslSocket = (SSLSocket) Server.ctx.getSocketFactory().createSocket(h.getHostname(), h.getPort());  
					input = new DataInputStream(sslSocket.getInputStream());
					output = new DataOutputStream(sslSocket.getOutputStream());
				} else {
					socket = new Socket(h.getHostname(), h.getPort());
					input = new DataInputStream(socket.getInputStream());
					output = new DataOutputStream(socket.getOutputStream());
				}
				output.writeUTF(forwardCommand.toJSONString());
				output.flush();

				// get response
				System.out.println("RECEIVE(relay query):");
				JSONObject response = (JSONObject) p.parse(input.readUTF());
				System.out.println(response);
				if (response.get("response").equals("success")) {
					boolean hasMoreData = true;
					while (hasMoreData) {
						JSONObject result = (JSONObject) p.parse(input.readUTF());
						if (result.containsKey("resultSize")) {
							logger.info(loggerPrefix + "Total: " + result.get("resultSize") + " results");
							hasMoreData = false;
						} else {
							logger.info(loggerPrefix + result.toString());
							clientOutput.writeUTF(result.toJSONString());
							totalResultSize++;
						}
					}
				}

				if (socket != null) socket.close();
				if (sslSocket != null) sslSocket.close();
			} catch (IOException | ParseException e) { e.printStackTrace(); }

		}	
		return totalResultSize;
	}

	private ArrayList<String> parseTags(String stringResourceTags) {
		String tagsStringRep = stringResourceTags.substring(1, stringResourceTags.length()-1);
		return new ArrayList<String>(Arrays.asList(tagsStringRep.split(",")));
	}
}
