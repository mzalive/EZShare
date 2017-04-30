package EZShare;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.sf.json.JSON;



public class FetchServer {
	int clientID;
	DataOutputStream output;
	JSONObject clientCommand;
	ResourceManager resourceManager; // resource manager handles all server resources

	public FetchServer (JSONObject clientCommand, ResourceManager resourceManager, DataOutputStream output, int clientID) {
		this.clientCommand = clientCommand;
		this.resourceManager = resourceManager;
		this.output = output;
		this.clientID = clientID;
	}

	
	@SuppressWarnings("unchecked")
	public void fetch() {
		Logger logger = Logger.getLogger(FetchServer.class.getName());
		String loggerPrefix = "Client " + clientID + ": ";
		
		JSONObject resourceTemplate = new JSONObject();
		JSONObject result = new JSONObject();
		ArrayList<String> tags = new ArrayList<String>();
		URI uri = null;
	    String channel = "";
	    
		// validate resourceTemplate
	    logger.info(loggerPrefix + "validating resourceTemplate");
		if (clientCommand.get("resourceTemplate") == null) {
			RespondUtil.returnErrorMsg(output, "missing resourceTemplate");
			logger.warning(loggerPrefix + "resourceTemplate does not exist, exit.");
			return;
		}
		resourceTemplate = (JSONObject) clientCommand.get("resourceTemplate");
		logger.info(loggerPrefix + resourceTemplate.toString());
		// extract key from resourceTemplate
		// only channel & uri relevant
		logger.info(loggerPrefix + "extracting key from resourceTemplate");
		if (resourceTemplate.containsKey("channel"))
			channel = resourceTemplate.get("channel").toString();
		
		try {
			// TODO handle invalid uri
			if (resourceTemplate.containsKey("uri")) {
				uri = new URI(resourceTemplate.get("uri").toString());
				String scheme = uri.getScheme();
				if (!scheme.equals("file")) {
					RespondUtil.returnErrorMsg(output, "invalid resourceTemplate");
					logger.warning(loggerPrefix + "invalid resourceTemplate, exit.");
					return;
				}
			}
		} catch (URISyntaxException e) {
			RespondUtil.returnErrorMsg(output, "missing resourceTemplate");
			logger.warning(loggerPrefix + "resourceTemplate does not exist, exit.");
			return;
		}
		
		// fetch resource
		
		logger.info(loggerPrefix + "fetching resource");
		Resource resource = resourceManager.getServerResource(channel, uri.toString());
		File f = new File(uri.getPath());
		if (resource == null || !f.exists()) {
			System.out.println("no match resource");
		} else try {
			// respond
			result.put("response", "success");
			output.writeUTF(result.toJSONString());
			
			
			JSONObject resourceJson = resource.toJSON();
			resourceJson.put("resourceSize", f.length());
			output.writeUTF(resourceJson.toJSONString());
			
			// Start transmission
			logger.fine(loggerPrefix + "Start transmission");
			RandomAccessFile byteFile = new RandomAccessFile(f, "r");
			byte[] sendingBuffer = new byte[1024*1024];
			int num;
			while ((num = byteFile.read(sendingBuffer)) > 0) {
				output.write(Arrays.copyOf(sendingBuffer, num));
			}
			byteFile.close();
			logger.fine(loggerPrefix + "Transmission finished");
			
			// resultSize
			// TODO resultSize always be 1?
			JSONObject resultSize = new JSONObject();
			resultSize.put("resultSize", 1);
			output.writeUTF(resultSize.toJSONString());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}
	
	// 
	// this segment shows the initialization of Logger
	//
	// src/logging.properties records the homemade logging rules including logging format
	// this should be done only once ahead of creating the first logger instance
	// System.setProperty("java.util.logging.config.file", "src/logging.properties");
	// Logger logger = Logger.getLogger(FetchServer.class.getName());
	// use setlevel function to control logging level
	// set level to Level.ALL/Level.OFF to turn on/off debug mode
	// logger.setLevel(Level.ALL);
	// logger.fine("Init");
	// logger.warning("Init");

}
