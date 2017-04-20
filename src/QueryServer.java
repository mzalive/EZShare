import java.io.File;
import java.util.ArrayList;

import org.json.simple.JSONObject;

public class QueryServer {
	
	private ResourceManager resourceManager;
	
	public QueryServer(ResourceManager r) {
		resourceManager = r;
	}
	
	public ArrayList<JSONObject> query(JSONObject resourceTemplate) {
		
		ArrayList<Resource> serverResources;
		ArrayList<JSONObject> results = new ArrayList<>();
		JSONObject candidateResourceTemplate;
		
		// unpack resource template
		
		// enforce server query rules to find matching candidates
		serverResources = resourceManager.getServerResources();
		
		// pack matching queries into JSON format
		for (Resource r : serverResources) {
			candidateResourceTemplate = new JSONObject();
			candidateResourceTemplate.put("name", r.getName());
			candidateResourceTemplate.put("description", r.getDescription());
			candidateResourceTemplate.put("tags", r.getTags());
			candidateResourceTemplate.put("uri", r.getUri());
			candidateResourceTemplate.put("channel", r.getChannel());
			candidateResourceTemplate.put("owner", r.getOwner());
			candidateResourceTemplate.put("ezserver", r.getEzserver());
			results.add(candidateResourceTemplate);
			//System.out.println(candidateResourceTemplate.toString());
		}
		return results;
	}

}
