package EZShare;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.util.ArrayList;

import org.json.simple.JSONObject;

public class ResourceManager {

	private File folder = new File("resources");
	private File[] serverResourceFiles = folder.listFiles();
	private ArrayList<Resource> serverResources = new ArrayList<>(); // store files in memory via ArrayList
	public ArrayList<JSONObject> serverlist = new ArrayList<JSONObject>();
	// construct Resource objects for each file
	public ResourceManager(String ezServer, String serverPort) {
		if(serverResourceFiles!=null){
		for (int i = 0; i < serverResourceFiles.length; i++) {	
			
			Resource r = new Resource("file://" + serverResourceFiles[i].toURI().getRawPath());
			r.setName(serverResourceFiles[i].getName());
			r.setEzserver(ezServer+":"+serverPort);
			serverResources.add(r);
		}}
		JSONObject self = new JSONObject();
		self.put("hostname","192.168.1.1");
		self.put("port", 3780);
		JSONObject self2 = new JSONObject();
		self2.put("hostname","localhost");
		self2.put("port", 3780);
		serverlist.add(self);

	//	serverlist.add(self2);
	}
	
	public ArrayList<Resource> getServerResources() {
		return serverResources;
	}
	
	public void addResource(Resource r) {
		serverResources.add(r);
	}
	public Resource getServerResource(String channel, String uri) {
		for (Resource r: serverResources) {
			if (r.getChannel().equals(channel) && r.getUri().equals(uri))
				return r;
		}
		return null;
	}
	
}
