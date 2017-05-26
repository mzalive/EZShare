package EZShare;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ShareServer {
	
	int clientID;
	DataOutputStream output;
	JSONObject clientCommand;
	ResourceManager resourceManager;
	String server_secret;
	int hit;
	HashMap<Socket,HashMap> map1;
	public ShareServer (JSONObject clientCommand, ResourceManager resourceManager, DataOutputStream output, int clientID, String server_secret,HashMap map1, int hit) {
		this.clientCommand = clientCommand;
		this.resourceManager = resourceManager;
		this.output = output;
		this.clientID = clientID;
		this.server_secret = server_secret;
		this.hit = hit;
		this.map1 = map1;
	}
	private static boolean canSubscribe(JSONObject rTemplate1, JSONObject rTemplate2){
		// rTemplate1 is client resource template and rTemplate2 is any server resource template
		// determine if a client resource template matches the given server resource template

		// check name of the resource
		if ( !(rTemplate1.get("name").toString().equals(""))&& !rTemplate1.get("name").equals(rTemplate2.get("name")) )
		{System.out.println("name not match"); return false;}
	//	ArrayList<String> ta = (ArrayList<String>) rTemplate1.get("tags");
		// check tags are the same
		else if (!(((ArrayList<String>) rTemplate1.get("tags")).size()!=0 )&& !rTemplate1.get("tags").equals(rTemplate2.get("tags")) )
			{System.out.println("tag not match"); return false;}
		
		// check if descriptions match
		else if (! (rTemplate1.get("description").toString().equals(""))&&!rTemplate1.get("description").equals(rTemplate2.get("description")) )
		{System.out.println("description not match"); return false;}
		
		// check if the uri's match
		else if ( !(rTemplate1.get("uri").toString().equals(""))&&!rTemplate1.get("uri").equals(rTemplate2.get("uri")) )
		{System.out.println("uri not match"); return false;}
		
		// check if the channels match
		else if (!(rTemplate1.get("channel").toString().equals(""))&& !rTemplate1.get("channel").equals(rTemplate2.get("channel")) )
		{System.out.println("channel not match"); return false;}
		
		// check if the owner's are the same
		else if ( !(rTemplate1.get("owner").toString().equals(""))&&!rTemplate1.get("owner").equals(rTemplate2.get("owner")) )
		{System.out.println("owner not match"); return false;}
		
		// check if ezServer matches
		else if ( (rTemplate1.get("ezServer")!=null)&&!rTemplate1.get("ezServer").equals(rTemplate2.get("ezServer")) )
		{System.out.println("ezServer not match"); return false;}
		
		return true;
	}
	public int share() throws IOException {
		Logger logger = Logger.getLogger(ShareServer.class.getName());
		String loggerPrefix = "Client " + clientID + ": ";
		
		JSONObject resource = new JSONObject();
		String name = "", description = "", channel = "", owner = "";
		URI uri = null;
		ArrayList<String> tags = new ArrayList<String>();
		
		if (!clientCommand.containsKey("resource") || !clientCommand.containsKey("secret"))  {
			RespondUtil.returnErrorMsg(output, "missing resource and\\/or secret");
	
		}
		if (!clientCommand.get("secret").toString().equals(server_secret)) {
			logger.info(loggerPrefix + "Genuien Secret : " + server_secret);
			logger.info(loggerPrefix + "Rcvd Secrest : " + clientCommand.get("secret").toString());
			RespondUtil.returnErrorMsg(output, "incorrect secret");
		
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

				}
				File file = new File(uri);
				if (!file.exists()) {
					RespondUtil.returnErrorMsg(output, "cannot share resource");
			
				}
			} catch (URISyntaxException e) {
				RespondUtil.returnErrorMsg(output, "invalid resource");
			}
		
		// login the resource
		Resource r = new Resource(name, description, tags, uri.toString(), channel, owner);
		resourceManager.addResource(r);
		Iterator iter = map1.entrySet().iterator();
		while (iter.hasNext()) {
		HashMap.Entry entry = (HashMap.Entry) iter.next();
		Socket soc = (Socket) entry.getKey();
		HashMap hash = (HashMap) entry.getValue();
			Iterator iter1 = hash.entrySet().iterator();
				while (iter1.hasNext()) {
				HashMap.Entry entry1 = (HashMap.Entry) iter1.next();
				String key =  (entry1.getKey().toString());
				JSONObject val = (JSONObject) entry1.getValue();


					if(canSubscribe(val,resource)){
					hit++;
			//		System.out.println("Publish:"+hit);
					DataOutputStream outsoc = new DataOutputStream (soc.getOutputStream());
					outsoc.writeUTF(resource.toJSONString());
					outsoc.flush();
				}
				}
		}
		logger.fine(loggerPrefix + "resource stored");
		
		RespondUtil.returnSuccessMsg(output);
		
		for (Resource res: resourceManager.getServerResources()) {
			logger.finest(res.toJSON().toString());
		}
		return hit;
	}

}
