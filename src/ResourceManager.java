import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

public class ResourceManager {

	private File folder = new File("resources");
	private File[] serverResourceFiles = folder.listFiles();
	private ArrayList<Resource> serverResources = new ArrayList<>(); // store files in memory via ArrayList
	
	// construct Resource objects for each file
	public ResourceManager(String ezServer, String serverPort) {
		for (int i = 0; i < serverResourceFiles.length; i++) {	
			
			Resource r = new Resource(serverResourceFiles[i].toURI().toString());
			r.setName(serverResourceFiles[i].getName());
			r.setEzserver(ezServer+":"+serverPort);
			serverResources.add(r);
		}
	}
	
	public ArrayList<Resource> getServerResources() {
		return serverResources;
	}
	
}
