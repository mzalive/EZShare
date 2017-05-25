package EZShare;

import org.json.simple.JSONObject;

public class Host {
	private String hostname = "";
	private int port = 0;
	
	public Host(String hostname,int port){
		setHostname(hostname);
		setPort(port);
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("hostname", hostname);
		json.put("port", port);
		return json;
	}
	
	@Override
	public String toString() {
		return hostname + ":" + port;
	}
	
	public boolean equals(Host host){
		if(this.hostname.equals(host.getHostname())&&(this.getPort()== host.getPort())){
			return true;
		}else{
			return false;
		}
		
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	
}
