import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;



public class FetchServer {
	
	private ResourceManager resourceManager;
	
	
	public FetchServer(ResourceManager r) {
		resourceManager = r;
	}
	
	@SuppressWarnings("unchecked")
	public void fetch(JSONObject resourceTemplate, DataOutputStream output) {
		JSONObject result = new JSONObject();
		ArrayList<String> tags = new ArrayList<String>();
		URI uri = null;
	    String channel = "";
	    
		// validate resourceTemplate
		if (resourceTemplate == null) {
			returnErrorMsg(result, output, "missing resourceTemplate");
			return;
		}
		
		// extract key from resourceTemplate
		// only channel & uri relevant
		if (resourceTemplate.containsKey("channel"))
			channel = resourceTemplate.get("channel").toString();
		try {
			// TODO handle invalid uri
			if (resourceTemplate.containsKey("uri")) {
				uri = new URI(resourceTemplate.get("uri").toString());
				String scheme = uri.getScheme();
				if (!scheme.equals("file")) {
					returnErrorMsg(result, output, "invalid resourceTemplate");
					return;
				}
			}
		} catch (URISyntaxException e) {
			returnErrorMsg(result, output, "missing resourceTemplate");
			return;
		}
		
		// fetch resource
		Resource resource = resourceManager.getServerResource(channel, uri.toString());
		File f = new File(uri);
		if (resource == null || !f.exists()) {
			// TODO no match. have no idea how to solve
		} else try {
			// respond
			result.put("response", "success");
			output.writeUTF(result.toJSONString());
			
			
			JSONObject resourceJson = resource.toJSON();
			resourceJson.put("resourceSize", f.length());
			output.writeUTF(resourceJson.toJSONString());
			
			// Start transmission
			RandomAccessFile byteFile = new RandomAccessFile(f, "r");
			byte[] sendingBuffer = new byte[1024*1024];
			int num;
			while ((num = byteFile.read(sendingBuffer)) > 0) {
				output.write(Arrays.copyOf(sendingBuffer, num));
			}
			byteFile.close();
			
			// resultSize
			// TODO resultSize always be 1?
			JSONObject resultSize = new JSONObject();
			resultSize.put("resultSize", 1);
			output.writeUTF(resultSize.toJSONString());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}
	
	// private method for returning error msg
	@SuppressWarnings("unchecked")
	private void returnErrorMsg(JSONObject result, DataOutputStream output, String msg) {
		result.put("response", "error");
		result.put("errorMessage", msg);
		try {
			output.writeUTF(result.toJSONString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}

}
