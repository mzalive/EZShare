import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.simple.JSONObject;

public class QueryServer {
	
	private ResourceManager resourceManager;
	private ArrayList<Resource> relevantQueryResources = new ArrayList<>(); // store relevant files in memory via ArrayList
	private ArrayList<String> templateTags = null;
	private ArrayList<String> resourceTags = null;
	boolean stillCandidate = true;
	
	public QueryServer(ResourceManager r) {
		resourceManager = r;
	}
	
	public ArrayList<JSONObject> query(JSONObject resourceTemplate) {
		
		ArrayList<Resource> serverResources;
		ArrayList<JSONObject> results = new ArrayList<>();
		JSONObject candidateResourceTemplate;
		
		// get the stored resources on server-side
		serverResources = resourceManager.getServerResources();
		
		//System.out.println(resourceTemplate);
		
		// enforce server query rules to find matching candidates
		templateTags = parseTags(resourceTemplate.get("tags").toString());
		
		for (Resource r : serverResources) {
			stillCandidate = true;
			resourceTags = parseTags(r.getTags().toString());
			
			// if template provides a channel
			if (!resourceTemplate.get("owner").equals("")){
				if(!resourceTemplate.get("owner").equals(r.getOwner())) 
					stillCandidate = false;
			}
				
			// if template provides owner, check if we still have a candidate
			if (!resourceTemplate.get("owner").equals("")){
				if(!resourceTemplate.get("owner").equals(r.getOwner())) 
					stillCandidate = false;
			}

			// if template provides tags, check if we still have a candidate
			if (!templateTags.toString().equals("[]")) {
				String tempStr;
				for (int i = 0; i < templateTags.size(); i++) {
					for (int k = 0; k < resourceTags.size(); k++) {
						
						tempStr = templateTags.get(i).substring(1, templateTags.get(i).length()-1);
						if (tempStr.equals(resourceTags.get(k).toString())) {
							break;
						}
						if(k + 1 == resourceTags.size()) {
							stillCandidate = false;
						}
					}
				}
			}
			
			// if template provides owner, check if we still have a candidate
			if (!resourceTemplate.get("uri").equals("")){
				if(!resourceTemplate.get("uri").equals(r.getUri())) 
					stillCandidate = false;
			}

			//OR conditions
			// if template provides a name, check if we still have a candidate
			if (!resourceTemplate.get("name").equals("")){
				if( !r.getName().contains(resourceTemplate.get("name").toString() ))
						stillCandidate = false;
			}
			// if template provides a description, check if we still have a candidate
			else if (!resourceTemplate.get("description").equals("") ){
				if(!r.getDescription().contains(resourceTemplate.get("description").toString()))
					stillCandidate = false;
			}
			// if template name and description are both "", we still have a candidate
			else if (!resourceTemplate.get("name").equals("")){
				if (!resourceTemplate.get("description").equals(""))
					stillCandidate = false;
			}
			
			// add the candidate if found
			if (stillCandidate) {
				relevantQueryResources.add(r);
			}
			
		} // end finding candidates
		
		
		// pack matching queries into JSON format
		for (Resource r : relevantQueryResources) {
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
	
	private ArrayList<String> parseTags(String stringResourceTags) {
        String tagsStringRep = stringResourceTags.substring(1, stringResourceTags.length()-1);
        return new ArrayList<String>(Arrays.asList(tagsStringRep.split(",")));
	}
}
