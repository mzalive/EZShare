import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.util.ArrayList;

public class ResourceManager {

	private File folder = new File("resources");
	private File[] serverResourceFiles = folder.listFiles();
	private ArrayList<Resource> serverResources = new ArrayList<>(); // store files in memory via ArrayList
	private ArrayList<String> tagsArray = new ArrayList<>();
	private ArrayList<String> tagsArray2 = new ArrayList<>();
	
	// construct Resource objects for each file
	public ResourceManager(String ezServer, String serverPort) {
		tagsArray.add("html");
		tagsArray2.add("py,css");
		for (int i = 0; i < serverResourceFiles.length; i++) {	
			
			Resource r = new Resource("file://" + serverResourceFiles[i].toURI().getRawPath());
			r.setName(serverResourceFiles[i].getName());
			r.setEzserver(ezServer+":"+serverPort);
			serverResources.add(r);
		}
		serverResources.get(0).setTags(tagsArray);
		serverResources.get(1).setTags(tagsArray2);
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
