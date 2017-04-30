package EZShare;
////<<<<<<< Updated upstream
////package Aaron1;
//
//import java.io.File;
//import java.net.URI;
//import java.net.URISyntaxException;
//import org.json.simple.JSONObject;
//
//public class RemoveServer {
//	@SuppressWarnings("unused")
//	private ResourceManager resourceManager;
//	public RemoveServer(ResourceManager r) {
//		resourceManager = r;
//	}
//	@SuppressWarnings("unchecked")
//	public JSONObject remove(JSONObject clientCmd) {
//		JSONObject result = new JSONObject();
//		JSONObject resource = new JSONObject();
//
//		URI uri = null;
//		if (!clientCmd.containsKey("resource")) 
//			return returnErrorMsg("missing resource");
//		resource = (JSONObject) clientCmd.get("resource");
//		
//		if (resource.containsKey("uri"))
//			try {
//				uri = new URI(resource.get("uri").toString());
//				if (!uri.getScheme().equals("file"))
//					return returnErrorMsg("invalid resource");
//				File file = new File(uri);
//				if (!file.exists())
//					return returnErrorMsg("cannot remove resource");
//				else file.delete();
//				@SuppressWarnings("unused")
//				Resource r = new Resource(uri.toString());
//				
//				result.put("response", "success");		
//			} catch (URISyntaxException e) {
//				return returnErrorMsg("invalid resource");
//			}
//
//		return null;	
//	}
//	@SuppressWarnings("unchecked")
//	private JSONObject returnErrorMsg(String msg) {
//		JSONObject result = new JSONObject();
//		result.put("response", "error");
//		result.put("errorMessage", msg);
//		return result;
//	}
//	
//}
//=======
////package Aaron1;

import java.io.DataOutputStream;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
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
		Logger logger = Logger.getLogger(FetchServer.class.getName());
		String loggerPrefix = "Client " + clientID + ": ";
		
		JSONObject resource = new JSONObject();
		URI uri = null;
		
		if (!clientCommand.containsKey("resource")) {
			RespondUtil.returnErrorMsg(output, "missing resource");
			return;
		}
		resource = (JSONObject) clientCommand.get("resource");
		
		if (resource.containsKey("uri"))
			try {
				uri = new URI(resource.get("uri").toString());
				if (!uri.getScheme().equals("file")) {
					{if(!uri.getScheme().equals("http")){
					//	System.out.println("Scheme:"+uri.getScheme());
						RespondUtil.returnErrorMsg(output,"invalid resource");}
					else{
						ArrayList<Resource> a = this.resourceManager.getServerResources();
						Iterator<Resource> iterator = a.iterator();
						int a1 = 0;
						while(iterator.hasNext()){
							Resource re = iterator.next();
							if(re.getUri().equals(resource.get("uri"))){
								iterator.remove();
								a1=1;
								break;
							}}
							if(a1==0){RespondUtil.returnErrorMsg(output,"missing resource");}
							if(a1==1){RespondUtil.returnSuccessMsg(output);}
				}}}
				File file = new File(uri);
				if (!file.exists()) {
					RespondUtil.returnErrorMsg(output, "cannot remove resource");
					return;
				}
				else file.delete();
				Resource r = new Resource(uri.toString());
				
				RespondUtil.returnSuccessMsg(output);		
			} catch (URISyntaxException e) {
				RespondUtil.returnErrorMsg(output, "invalid resource");
			}
	}
}
//>>>>>>> Stashed changes
