import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class PublishServer {

	JSONObject clientCommand;
	ResourceManager resourceManager;
	boolean failed = false;
	
	// publish constructor
	public PublishServer(JSONObject c, ResourceManager reManager) {
		clientCommand = c;
		resourceManager = reManager;
	}
	
	// method to publish the resource to the server
	public ArrayList<JSONObject> publish() {
		
		JSONObject resource = null;
		JSONObject outcomeJSON = new JSONObject();
		ArrayList<JSONObject> jsonList = new ArrayList<>();
		String[] tag_array;
		
		//extract resource
		resource = clientCommand;
		System.out.println(clientCommand.get("tags").getClass());
		
		String name = null;
		String description = null;
		String channel = null;
		String owner = null;
		String tagsStringRep;
		URI uri = null;
		ArrayList<String> tags = null;
		
		// publish server rules are below:
		if (resource.containsKey("name"))
			name = resource.get("name").toString();
		
		// if client command resource template has tags, add them to the resource
	    if (resource.containsKey("tags")) {
	        tagsStringRep = resource.get("tags").toString();
	        tagsStringRep = tagsStringRep.substring(1, tagsStringRep.length()-1);
	        tags = new ArrayList<String>(Arrays.asList(tagsStringRep.split(",")));
	        for (String s : tags) {
	        	System.out.println(s);
	        }
	        
	    }
	    
	    // if client command resource template contains a description
	    if (resource.containsKey("description")) {
	        description = resource.get("description").toString().trim();
	        //description = description.replaceAll("\\u00", "");
	    }
	    
	    // if there is a channel
	    if (resource.containsKey("channel"))
	        channel = resource.get("channel").toString();
	    
	    // if owner is not *
	    if (resource.containsKey("owner") && !resource.get("owner").equals("*"))
	        owner = resource.get("owner").toString();	
	    
	    // if the resource has a uri
		if (resource.containsKey("uri"))
		{
			try {
				// get the uri of the resource
				uri = new URI(resource.get("uri").toString());
				
				// if the uri is a file scheme, we cannot publish the resource
				if (uri.getScheme().equals("file"))
					jsonList.add(returnErrorMsg("cannot publish resource"));
				
			} catch (URISyntaxException e) {
					jsonList.add(returnErrorMsg("invalid resource"));
			}
		}
				
		// finally, store the resource
		if (!failed) {
			Resource r = new Resource(name, description, tags, uri.toString(), channel, owner);
			resourceManager.addResource(r);
			JSONObject jsonResult = new JSONObject();
			jsonResult.put("response", "success");
			jsonList.add(jsonResult); 
		}
		
		
		// pack the response messages to be sent back to the client
		//outcomeJSON.put("response", "success");
		//jsonList.add(outcomeJSON);
		return jsonList;
		
	}

	private JSONObject returnErrorMsg(String msg) {
		JSONObject jsonResult = new JSONObject();
		jsonResult.put("response", "error");
		jsonResult.put("errorMessage", msg);
		failed = true;
		return jsonResult;
	}
	
}
