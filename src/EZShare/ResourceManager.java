package EZShare;
import java.io.File;
import java.util.ArrayList;

import org.json.simple.JSONObject;

public class ResourceManager {

	private File folder = new File("resources");
	private File[] serverResourceFiles = folder.listFiles();
	private ArrayList<Resource> serverResources = new ArrayList<>(); // store files in memory via ArrayList
	public ArrayList<JSONObject> serverlist = new ArrayList<JSONObject>();

	public static ArrayList<Host> hostList = new ArrayList<Host>();
	public static ArrayList<Host> hostList_secure = new ArrayList<Host>();
	// construct Resource objects for each file
	public ResourceManager(String ezServer, String serverPort) {
		if(serverResourceFiles!=null){
		for (int i = 0; i < serverResourceFiles.length; i++) {	
			
			Resource r = new Resource("file://" + serverResourceFiles[i].toURI().getRawPath());
			r.setName(serverResourceFiles[i].getName());
			r.setEzserver(ezServer+":"+serverPort);
			serverResources.add(r);
		}}

	}
	
	public ArrayList<Resource> getServerResources() {
		return serverResources;
	}

	public void addResource(Resource r) {
		removeResource(r.getOwner(), r.getChannel(), r.getUri());
		serverResources.add(r);
	}
	
	public Resource getServerResource(String channel, String uri) {
		for (Resource r: serverResources) {
			if (r.getChannel().equals(channel) && r.getUri().equals(uri))
				return r;
		}
		return null;
	}
	
	public Resource getServerResource(String owner, String channel, String uri) {
		for (Resource r: serverResources) {
			if (r.getOwner().equals(owner) && r.getChannel().equals(channel) && r.getUri().equals(uri))
				return r;
		}
		return null;
	}
	
	public boolean isResourceExist(String owner, String channel, String uri) {
		for (Resource r: serverResources) {
			if (r.getOwner().equals(owner) && r.getChannel().equals(channel) && r.getUri().equals(uri))
				return true;
		}
		return false;
	}

	public boolean removeResource(String owner, String channel, String uri) {
		if (!isResourceExist(owner, channel, uri))
			return false;
		else
		for (int i=0; i<serverResources.size(); i++) {
			Resource r = serverResources.get(i);
			if (r.getOwner().equals(owner) && r.getChannel().equals(channel) && r.getUri().equals(uri)) {
				serverResources.remove(i);
				return true;
			}
		}
		return false;
	}
	
}
