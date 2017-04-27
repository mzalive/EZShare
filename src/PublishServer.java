import org.json.simple.JSONObject;

public class PublishServer {

	JSONObject resource;
	
	// publish constructor
	public PublishServer(JSONObject r) {
		resource = r;
	}
	
	// method to publish the resource to the server
	public void publish() {
		
		String name = resource.get("name").toString();
		String description = resource.get("description").toString();
		String channel = resource.get("channel").toString();
		String owner = resource.get("description").toString();
		String uri = resource.get("uri").toString();
		
		System.out.println(name);
		System.out.println(description);
		System.out.println(channel);
		System.out.println(owner);
		System.out.println(uri);
		
		
	}
	
}
