import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ShareServer {
	private ResourceManager resourceManager;
	
	
	public ShareServer(ResourceManager r) {
		resourceManager = r;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject share(JSONObject clientCmd, String secret) {
		JSONObject result = new JSONObject();
		JSONObject resource = new JSONObject();
		String name = "", description = "", channel = "", owner = "";
		URI uri = null;
		ArrayList<String> tags = new ArrayList<String>();
		if (!clientCmd.containsKey("resource") || !clientCmd.containsKey("secret")) 
			return returnErrorMsg("missing resource and\\/or secret");
		if (clientCmd.get("secret").toString().equals(secret))
			return returnErrorMsg("incorrect secret");
		
		//extract resource
		resource = (JSONObject) clientCmd.get("resource");
		if (resource.containsKey("name"))
	        name = resource.get("name").toString();
	    if (resource.containsKey("tags")) {
	        JSONArray tag_array = (JSONArray) resource.get("tags");
	        for (int i = 0; i < tag_array.size(); i++)
	            tags.add(tag_array.get(i).toString());
	    }
	    if (resource.containsKey("description")) {
	        description = resource.get("description").toString().trim();
	        description = description.replaceAll("\\u00", "");
	    }
	    if (resource.containsKey("channel"))
	        channel = resource.get("channel").toString();
	    if (resource.containsKey("owner") && !resource.get("owner").equals("*"))
	        owner = resource.get("owner").toString();	
		if (resource.containsKey("uri"))
			try {
				uri = new URI(resource.get("uri").toString());
				if (!uri.getScheme().equals("file"))
					return returnErrorMsg("invalid resource");
				File file = new File(uri);
				if (!file.exists())
					return returnErrorMsg("cannot share resource");
			} catch (URISyntaxException e) {
				return returnErrorMsg("invalid resource");
			}
		
		// login the resource
		Resource r = new Resource(name, description, tags, uri.toString(), channel, owner);
		// TODO store the resource
		
		result.put("response", "success");
		return null;
	}
	
	// private method for returning error msg
	@SuppressWarnings("unchecked")
	private JSONObject returnErrorMsg(String msg) {
		JSONObject result = new JSONObject();
		result.put("response", "error");
		result.put("errorMessage", msg);
		return result;
	}
}
