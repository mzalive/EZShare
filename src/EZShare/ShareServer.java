package EZShare;
import java.io.DataOutputStream;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ShareServer {
	
	int clientID;
	DataOutputStream output;
	JSONObject clientCommand;
	ResourceManager resourceManager;
	String server_secret;
	
	public ShareServer (JSONObject clientCommand, ResourceManager resourceManager, DataOutputStream output, int clientID, String server_secret) {
		this.clientCommand = clientCommand;
		this.resourceManager = resourceManager;
		this.output = output;
		this.clientID = clientID;
		this.server_secret = server_secret;
	}

	public void share() {
		Logger logger = Logger.getLogger(ShareServer.class.getName());
		String loggerPrefix = "Client " + clientID + ": ";
		
		JSONObject resource = new JSONObject();
		String name = "", description = "", channel = "", owner = "";
		URI uri = null;
		ArrayList<String> tags = new ArrayList<String>();
		
		if (!clientCommand.containsKey("resource") || !clientCommand.containsKey("secret"))  {
			RespondUtil.returnErrorMsg(output, "missing resource and\\/or secret");
			return;
		}
		if (!clientCommand.get("secret").toString().equals(server_secret)) {
			logger.info(loggerPrefix + "Genuien Secret : " + server_secret);
			logger.info(loggerPrefix + "Rcvd Secrest : " + clientCommand.get("secret").toString());
			RespondUtil.returnErrorMsg(output, "incorrect secret");
			return;
		}
		
		//extract resource
		resource = (JSONObject) clientCommand.get("resource");
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
				if (!uri.getScheme().equals("file")) {
					logger.warning(loggerPrefix + "invalid scheme: " + uri.getScheme());
					RespondUtil.returnErrorMsg(output, "cannot share resource");
					return;
				}
				File file = new File(uri);
				if (!file.exists()) {
					RespondUtil.returnErrorMsg(output, "cannot share resource");
					return;
				}
			} catch (URISyntaxException e) {
				RespondUtil.returnErrorMsg(output, "invalid resource");
			}
		
		// login the resource
		Resource r = new Resource(name, description, tags, uri.toString(), channel, owner);
		resourceManager.addResource(r);
		logger.fine(loggerPrefix + "resource stored");
		
		RespondUtil.returnSuccessMsg(output);
		
		for (Resource res: resourceManager.getServerResources()) {
			logger.finest(res.toJSON().toString());
		}
	}
	
}
