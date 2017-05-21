package EZShare;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

public class ExchangeServer {
	public ResourceManager r;
	public Logger logger;
	public ExchangeServer(ResourceManager r) {
		this.r = r;
		this.logger = Logger.getLogger(Exchanger.class.getName());
	}	
	@SuppressWarnings({ "unchecked", "null" })
	public  JSONObject exchange(JSONObject serverlist,DataOutputStream output) {
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
 	    	RespondUtil.returnErrorMsg(output, "missing or invalid server list"); 	    
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
		    	                     RespondUtil.returnErrorMsg(output, "missing resourceTemplate"); 	
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
                                	 RespondUtil.returnErrorMsg(output, "missing resourceTemplate"); 	    	   
		                           }			} }
 	    
		RespondUtil.returnSuccessMsg(output);
		Iterator i1 = r.serverlist.iterator();
		while(i1.hasNext()){
			JSONObject j = (JSONObject) i1.next();
			System.out.println(j.get("hostname")+ " | "+serverlist.get("hostname")+"   "+j.get("port")+ " | "+serverlist.get("port"));
		if(j.get("hostname").toString().equals(serverlist.get("hostname").toString() )&& j.get("port").toString().equals(serverlist.get("port").toString())){
			this.logger.info("Duplicated...");
			//i1.remove();
			result.put("response", "duplicated");
			return result;
		}
		}
		r.serverlist.add(serverlist);
		return result;	
	}
}
