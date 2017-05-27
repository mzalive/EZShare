package EZShare;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

public class PublishServer {

	private DataOutputStream output;
	private JSONObject clientCommand;
	private int clientID;
	private ResourceManager resourceManager;
	boolean failed = false;
	int hit;
	static HashMap <Socket,HashMap> map1 = new HashMap<Socket,HashMap>();
	// publish constructor
	public PublishServer(JSONObject c, ResourceManager reManager, DataOutputStream output, int clientID, HashMap map1, int hit) {
		this.clientCommand = c;
		this.resourceManager = reManager;
		this.output = output;
		this.clientID = clientID;
		this.map1 = map1;
		this.hit = hit;
	}
	private static boolean canSubscribe(JSONObject rTemplate1, JSONObject rTemplate2){
		// rTemplate1 is client resource template and rTemplate2 is any server resource template
		// determine if a client resource template matches the given server resource template

		// check name of the resource
		if ( !(rTemplate1.get("name").toString().equals(""))&& !rTemplate1.get("name").equals(rTemplate2.get("name")) )
		 return false;
	//	ArrayList<String> ta = (ArrayList<String>) rTemplate1.get("tags");
		// check tags are the same
		else if ((((ArrayList<String>) rTemplate1.get("tags")).size()!=0 )&& !rTemplate1.get("tags").equals(rTemplate2.get("tags")) )
			 return false;
		
		// check if descriptions match
		else if (! (rTemplate1.get("description").toString().equals(""))&&!rTemplate1.get("description").equals(rTemplate2.get("description")) )
		 return false;
		
		// check if the uri's match
		else if ( !(rTemplate1.get("uri").toString().equals(""))&&!rTemplate1.get("uri").equals(rTemplate2.get("uri")) )
		 return false;
		
		// check if the channels match
		else if (!(rTemplate1.get("channel").toString().equals(""))&& !rTemplate1.get("channel").equals(rTemplate2.get("channel")) )
		 return false;
		
		// check if the owner's are the same
		else if ( !(rTemplate1.get("owner").toString().equals(""))&&!rTemplate1.get("owner").equals(rTemplate2.get("owner")) )
	 return false;
		
		// check if ezServer matches
		else if ( (rTemplate1.get("ezServer")!=null)&&!rTemplate1.get("ezServer").equals(rTemplate2.get("ezServer")) )
		 return false;
		
		return true;
	}
	// method to publish the resource to the server
	public int publish() throws IOException {
		Logger logger = Logger.getLogger(PublishServer.class.getName());
		String loggerPrefix = "Client " + clientID + ": ";
		JSONObject resource = null;
		
		//extract resource
		resource = (JSONObject) clientCommand.get("resource");
		System.out.println(resource);
		
		String name = null;
		String description = null;
		String channel = null;
		String owner = null;
		String tagsStringRep;
		URI uri = null;
		ArrayList<String> tags = new ArrayList<String>();
		
		// publish server rules are below:
		if (resource.containsKey("name"))
			name = resource.get("name").toString();
		
		// if client command resource template has tags, add them to the resource
	    if (resource.containsKey("tags")) {
	        tagsStringRep = resource.get("tags").toString();
	        tagsStringRep = tagsStringRep.substring(1, tagsStringRep.length()-1);
	        tags = new ArrayList<String>(Arrays.asList(tagsStringRep.split(",")));	        
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
					RespondUtil.returnErrorMsg(output, "cannot publish resource");
				
			} catch (URISyntaxException e) {
				RespondUtil.returnErrorMsg(output, "invalid resource");
			}
		}
				
		// finally, store the resource
		if (!failed) {
			Resource r = new Resource(name, description, tags, uri.toString(), channel, owner);
			resourceManager.addResource(r);
			logger.fine(loggerPrefix + "resource: " + r.toJSON().toString() + " published");
			RespondUtil.returnSuccessMsg(output);
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
			

		//	out1.close();
		//	socket.close();
		}
	
	return hit;

		
	}
	
}
