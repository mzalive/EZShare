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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import org.json.simple.JSONObject;

public class RemoveServer {
	@SuppressWarnings("unused")
	private ResourceManager resourceManager;
	public RemoveServer(ResourceManager r) {
		resourceManager = r;
	}
	@SuppressWarnings("unchecked")
	public JSONObject remove(JSONObject clientCmd) {
		JSONObject result = new JSONObject();
		JSONObject resource = new JSONObject();

		URI uri = null;
		if (!clientCmd.containsKey("resource")) 
			return returnErrorMsg("missing resource");
		resource = (JSONObject) clientCmd.get("resource");
		
		if (resource.containsKey("uri"))
			try {
				uri = new URI(resource.get("uri").toString());
				if (!uri.getScheme().equals("file"))
					return returnErrorMsg("invalid resource");
				File file = new File(uri);
				if (!file.exists())
					return returnErrorMsg("cannot remove resource");
				else file.delete();
				@SuppressWarnings("unused")
				Resource r = new Resource(uri.toString());
				
				result.put("response", "success");		
			} catch (URISyntaxException e) {
				return returnErrorMsg("invalid resource");
			}

		return null;	
	}
	@SuppressWarnings("unchecked")
	private JSONObject returnErrorMsg(String msg) {
		JSONObject result = new JSONObject();
		result.put("response", "error");
		result.put("errorMessage", msg);
		return result;
	}
	
}
//>>>>>>> Stashed changes
