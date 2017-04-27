package Aaron1;

import java.util.ArrayList;

import org.json.simple.JSONObject;

public class ExchangeServer {
	private ResourceManager resourceManager;
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
		
		return result;
		
		
	}	
	
	
	
	
	
	@SuppressWarnings("unchecked")
	private JSONObject returnErrorMsg(String msg) {
		JSONObject result = new JSONObject();
		result.put("response", "error");
		result.put("errorMessage", msg);
		return result;
	}
}
