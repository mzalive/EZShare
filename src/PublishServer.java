import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class PublishServer {

	JSONObject clientCommand;
	
	// publish constructor
	public PublishServer(JSONObject c) {
		clientCommand = c;
	}
	
	// method to publish the resource to the server
	public ArrayList<JSONObject> publish() {
		
		JSONObject resource = null;
		JSONObject outcomeJSON = null;
		ArrayList<JSONObject> result = new ArrayList<>();
		
		//extract resource
		resource = (JSONObject) clientCommand.get("resource");
		
		String name = null;
		String description = null;
		String channel = null;
		String owner = null;
		URI uri = null;
		ArrayList<String> tags = new ArrayList<String>();
		
		System.out.println(resource.get("name").toString());
		
		// publish server rules are below:
		if (resource.containsKey("name"))
			name = resource.get("name").toString();
		
		// if client command resource template has tags, add them to the resource
	    if (resource.containsKey("tags")) {
	        JSONArray tag_array = (JSONArray) resource.get("tags");
	        for (int i = 0; i < tag_array.size(); i++)
	            tags.add(tag_array.get(i).toString());
	    }
	    
	    // if client command resource template contains a description
	    if (resource.containsKey("description")) {
	        description = resource.get("description").toString().trim();
	        description = description.replaceAll("\\u00", "");
	    }
	    
	    // if there is a channel
	    if (resource.containsKey("channel"))
	        channel = resource.get("channel").toString();
	    
	    // if owner is not *
	    if (resource.containsKey("owner") && !resource.get("owner").equals("*"))
	        owner = resource.get("owner").toString();	
	    
	    // if the resource has a uri
		if (resource.containsKey("uri"))
			try {
				uri = new URI(resource.get("uri").toString());
				if (!uri.getScheme().equals("file"))
					result.add(returnErrorMsg("invalid resource"));
				File file = new File(uri);
				if (!file.exists())
					result.add((returnErrorMsg("cannot share resource")));
			} catch (URISyntaxException e) {
					result.add(returnErrorMsg("invalid resource"));
			}
				
		// finally, store the resource
		Resource r = new Resource(name, description, tags, uri.toString(), channel, owner);
		
		outcomeJSON.put("responsess", "success");
		result.add(outcomeJSON);
		return result;
		
	}

	private JSONObject returnErrorMsg(String msg) {
		JSONObject result = new JSONObject();
		result.put("response", "error");
		result.put("errorMessage", msg);
		return result;
	}
	
}
