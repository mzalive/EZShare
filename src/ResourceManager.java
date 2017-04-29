import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.util.ArrayList;

public class ResourceManager {

	private File folder = new File("resources");
	private File[] serverResourceFiles = folder.listFiles();
	private ArrayList<Resource> serverResources = new ArrayList<>(); // store files in memory via ArrayList
	public ArrayList<JSONObject> serverlist = new ArrayList<JSONObject>();
	private ArrayList<String> tagsArray = new ArrayList<>();

	// construct Resource objects for each file
	public ResourceManager(String ezServer, String serverPort) {
		for (int i = 0; i < serverResourceFiles.length; i++) {

			Resource r = new Resource("file://" + serverResourceFiles[i].toURI().getRawPath());
			r.setName(serverResourceFiles[i].getName());
			r.setEzserver(ezServer+":"+serverPort);
			tagsArray.add("html");
			r.setTags(tagsArray);
			serverResources.add(r);
		}
		JSONObject self = new JSONObject();
		self.put("hostname","192.168.1.250");
		self.put("port", 3780);
		serverlist.add(self);
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
