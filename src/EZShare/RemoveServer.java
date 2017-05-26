package EZShare;

import java.io.DataOutputStream;
import java.net.URI;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

public class RemoveServer {

	int clientID;
	ResourceManager resourceManager;
	DataOutputStream output;
	JSONObject clientCommand;
	
	public RemoveServer(JSONObject clientCommand, ResourceManager resourceManager, DataOutputStream output, int clientID, String server_secret) {
		this.resourceManager = resourceManager;
		this.output = output;
		this.clientID = clientID;
		this.clientCommand = clientCommand;
	}
	
	public void remove() {
		Logger logger = Logger.getLogger(RemoveServer.class.getName());
		String loggerPrefix = "Client " + clientID + ": ";
		
		JSONObject resource = new JSONObject();
		String uri = "";
		String owner = "";
		String channel = "";
		
		if (!clientCommand.containsKey("resource")) {
			RespondUtil.returnErrorMsg(output, "missing resource");
			logger.warning(loggerPrefix + "missing resource");
			return;
		}
		resource = (JSONObject) clientCommand.get("resource");
		logger.info(loggerPrefix + resource.toJSONString());
		if (resource.containsKey("uri")) {
			uri = resource.get("uri").toString();
			logger.info(loggerPrefix + "uri: " + uri);
		} else {
			logger.warning(loggerPrefix + "no uri");
			RespondUtil.returnErrorMsg(output,"missing resource");
			return;
		}
		if (resource.containsKey("owner")) {
			owner = resource.get("owner").toString();
			logger.info(loggerPrefix + "owner: " + owner);
		} else {
			logger.warning(loggerPrefix + "no owner");
			RespondUtil.returnErrorMsg(output,"missing resource");
			return;
		}
		if (resource.containsKey("channel")) {
			channel = resource.get("channel").toString();
			logger.info(loggerPrefix + "channel: " + channel);
		} else {
			logger.warning(loggerPrefix + "no channel");
			RespondUtil.returnErrorMsg(output,"missing resource");
			return;
		}
		
		if (!isValidUri(uri)) {
			logger.warning(loggerPrefix + "invalid uri");
			RespondUtil.returnErrorMsg(output, "invalid resource");
			return;
		}
		
		if (resourceManager.removeResource(owner, channel, uri)) {
			RespondUtil.returnSuccessMsg(output);	
			logger.info(loggerPrefix + "removed");
		} else {
			logger.warning(loggerPrefix + "resource not exist");
			RespondUtil.returnErrorMsg(output, "cannot remove resource");
			return;
		}

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

