package Aaron1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import java.lang.Math;
import org.json.simple.JSONObject;

public class ExchangeServer {
	private static ResourceManager resourceManager;
	public ExchangeServer(ResourceManager r) {
		resourceManager = r;
	}


	
	@SuppressWarnings({ "unchecked", "null" })
	public  JSONObject exchange(JSONObject serverlist) {
		JSONObject result = new JSONObject();
		JSONObject serverrecord = new JSONObject();
		ArrayList<JSONObject> ll = new ArrayList<JSONObject>();
		
		
		
		ArrayList<String> recordhostname = new ArrayList<String>();
		ArrayList<String> recordport = new ArrayList<String>();
		String list = "";
		String list1 = ""; 
		String[] slist = null;
		String[] slist1 = null;
		
 	    if (serverlist == null) {
			returnErrorMsg("missing or invalid server list"); 	    
	    	if(serverlist.containsKey("hostname")){
		    	list = serverlist.get("hostname").toString();
		          slist = list.split("\\s");
		          for(String s : slist)
		             {
                       recordhostname.add(s);		        	  
		                }		        	  

		                     for(int a= 1;a<recordhostname.size();a=a+2)
		                       {
		    	                String n = recordhostname.get(a);
		    	                     if(n.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")){
		    		                    String s[] = n.split("\\.");  
		                                if(Integer.parseInt(s[0])<255)  
		                                  if(Integer.parseInt(s[1])<255)  
		                                    if(Integer.parseInt(s[2])<255)  
		                                      if(Integer.parseInt(s[3])<255); 
		    	                              }
		    	                      returnErrorMsg("missing resourceTemplate"); 	
		                              }
		             if(serverlist.containsKey("port"))
			            list1 = serverlist.get("port").toString();
		                 slist1 = list1.split("\\s");
				          for(String s : slist1)
				             {
		                       recordport.add(s);		        	  
				                }		
		                    for(int a= 1;a<recordport.size();a=a+2)
		                     {
		    	               String n = recordport.get(a);
		    	                int port = Integer.parseInt(n);
                                 if (port<0 && port>65535)
   		    	                   returnErrorMsg("missing resourceTemplate"); 	    	   
		                           }			} }
 	    
		result.put("response", "success");
		ll.add(serverlist);	
		serverrecord.put("serverlist",ll);
		
		
		
		return null;	
	}	
	
	@SuppressWarnings({ "unchecked", "null" })
	
	public static void exchange() throws UnknownHostException, IOException, InterruptedException {
	
	ArrayList<JSONObject> record = new ArrayList<JSONObject>();
	ArrayList<String> part = new ArrayList<String>();
    record = null;    
	JSONObject server = new JSONObject();
	server.put("115.146.85.16 ", 3780);
	server.put("115.14.83.16 ", 3780);
	server.put("11.142.83.11 ", 3780);	
	record.add(server);
	while(true){
		Thread.sleep(100000);
	int x=(int)(Math.random()*record.size());
	record.get(x);
	String rec = record.get(x).toString();
	String[] recc = null;
	recc = rec.split("//s");
	for(String s : recc)
    {
      part.add(s);		        	  
       }		

	try(Socket socket = new Socket(part.get(0),Integer.parseInt(part.get(1)));){
		DataInputStream input = new DataInputStream(socket.getInputStream());
		DataOutputStream output = new DataOutputStream(socket.getOutputStream());
		JSONObject newCommand = new JSONObject();
		newCommand.put("command", "EXCHANGE");
		newCommand.put("serverList", record.get(x));
		
		output.writeUTF(newCommand.toJSONString());
		output.flush();
		System.out.println("Received from server:");
		while(true){
	  if(input.available()>0){
			String result = input.readUTF();	
			System.out.println(result);
		}
	  else record.remove(x);
	
		}
		
	}
		
	}
	
	
		}
		

	
	
	
	
	
	
	
	
	
	
	
	
	@SuppressWarnings("unchecked")
	private JSONObject returnErrorMsg(String msg) {
		JSONObject result = new JSONObject();
		result.put("response", "error");
		result.put("errorMessage", msg);
		return result;
	}
}
