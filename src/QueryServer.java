import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

public class QueryServer {
	
	private ResourceManager resourceManager;
	private ArrayList<Resource> relevantQueryResources = new ArrayList<>(); // store relevant files in memory via ArrayList
	private ArrayList<String> templateTags = null;
	private ArrayList<String> resourceTags = null;
	private boolean stillCandidate = true;
	private DataOutputStream output;
	private JSONObject clientCommand;
	private int clientID;
	private boolean relay;
	
	public QueryServer(JSONObject clientCommand, ResourceManager resourceManager, DataOutputStream output, int clientID, boolean relay) {
		this.clientCommand = clientCommand;
		this.resourceManager = resourceManager;
		this.output = output;
		this.clientID = clientID;
		this.relay = relay;
	}
	
	@SuppressWarnings("unchecked")
	public void query() {
		Logger logger = Logger.getLogger(FetchServer.class.getName());
		String loggerPrefix = "Client " + clientID + ": ";
		
		ArrayList<Resource> serverResources;
		JSONObject resourceTemplate = new JSONObject();
		
		logger.info(loggerPrefix + "relay " + (relay?"on":"off"));
				
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
		// pack matching queries into JSON format
		try {
			for (Resource r : relevantQueryResources) 
				output.writeUTF(r.toJSON().toJSONString());
			JSONObject resultSize = new JSONObject();
			resultSize.put("resultSize", relevantQueryResources.size());
			output.writeUTF(resultSize.toJSONString());
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	private ArrayList<String> parseTags(String stringResourceTags) {
        String tagsStringRep = stringResourceTags.substring(1, stringResourceTags.length()-1);
        return new ArrayList<String>(Arrays.asList(tagsStringRep.split(",")));
	}
}
