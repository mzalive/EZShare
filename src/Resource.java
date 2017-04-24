import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Resource {
	private String name;
	private String description;
	private ArrayList<String> tags;
	private String uri;
	private String channel;
	private String owner;
	private String ezserver;
	
	public Resource(String name, String description, ArrayList<String> tags, String uri, String channel, String owner, 
			String ezserver) {
		//returns a resource object with full information 
		super();
		this.name = name;
		this.description = description;
		this.tags = tags;
		this.uri = uri;
		this.channel = channel;
		this.owner = owner;
		this.ezserver = ezserver;
	}
	
	public Resource(String name, String description, ArrayList<String> tags, String uri, String channel, String owner) {
		//returns a resource object with no server info 
		super();
		this.name = name;
		this.description = description;
		this.tags = tags;
		this.uri = uri;
		this.channel = channel;
		this.owner = owner;
		this.ezserver = "";
	}

	public Resource(String uri) {
		//returns a resource object with minimum information 
		super();
		this.name = "";
		this.description = "";
		this.tags = new ArrayList<String>();
		this.uri = uri;
		this.channel = "";
		this.owner = "";
		this.ezserver = null;
	}
	
	public Resource(){	
		//returns a empty resource object for resourceTemplate 
		super();
		this.name = "";
		this.description = "";
		this.tags = new ArrayList<String>();
		this.uri = "";
		this.channel = "";
		this.owner = "";
		this.ezserver = null;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		JSONArray tag_array = new JSONArray();
		for (String tag: tags) tag_array.add(tag);
		
		json.put("name", name);
		json.put("tags", tag_array);
		json.put("description", description);
		json.put("channel", channel);
		json.put("owner", owner);
		json.put("uri", uri);
//		json.put("ezserver", ezserver);
		
		return json;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ArrayList<String> getTags() {
		return tags;
	}

	public void setTags(ArrayList<String> tags) {
		this.tags = tags;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getEzserver() {
		return ezserver;
	}

	public void setEzserver(String ezserver) {
		this.ezserver = ezserver;
	}
	
	
}
	
		
