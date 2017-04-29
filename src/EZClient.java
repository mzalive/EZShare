import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.json.simple.JSONObject;

public class EZClient {

	public static void main(String[] args) throws UnknownHostException, IOException {

		// set default host and ip address
		int port = 3780;
		String host = "localhost";
		
		// check if the host and port is set manually
		for(int i=0; i<args.length;i++){
			if(args[i].equals("-host"))
			{host=args[i+1];}
			else if(args[i].equals("-port"))
			{port=Integer.parseInt(args[i+1]);}
		}
		
		//create socket for connection
		try(Socket socket = new Socket(host,port);){
			
			// get the input and output stream
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
			int len = args.length;
			
			// judge the type of command
			switch (args[0]){
			case "-QUERY":
				// Create Template of JSONObject for query
				int i=1;
				JSONObject Query = new JSONObject();
				JSONObject resourceTemplate = new JSONObject();
				resourceTemplate.put("name", "");
				resourceTemplate.put("channel", "");
				resourceTemplate.put("description", "");
				resourceTemplate.put("uri", "");
				resourceTemplate.put("owner", "");
				resourceTemplate.put("ezserver", null);
				resourceTemplate.put("tags", new ArrayList<String>());
				Query.put("relay",false);
				Query.put("resourceTemplate", resourceTemplate);
				
				// check if any args are set manually
				while(i<args.length){
					Query = parse(args[i],args[i+1],Query);
					i+=2;
				}
				
				// put command into JSONObject and send it to the server
				Query.put("command", "QUERY");
				System.out.println(Query.toJSONString());
				output.writeUTF(Query.toJSONString());
				output.flush();
				break;
			case "-EXCHANGE":
				ExchangeClient e = new ExchangeClient();
				i=1;
				JSONObject Exchange = new JSONObject();
				
				// pass the serverlist variable to the exchange client object
				Exchange.put("serverList", new ArrayList<JSONObject>());
				
				// check if any args are set manually
				while(i<args.length){
					Exchange = parse(args[i],args[i+1],Exchange);
					i+=2;
				}
				
				// send the JSONObject to server
				Exchange.put("command", "EXCHANGE");
				System.out.println(Exchange.toJSONString());
				output.writeUTF(Exchange.toJSONString());
				output.flush();
				break;
			case "-FETCH":
				i=1;
				
				// Create Template of JSONObject for fetch
				JSONObject Fetch = new JSONObject();
				JSONObject resource = new JSONObject();
				resource.put("name", "");
				resource.put("channel", "");
				resource.put("description", "");
				resource.put("uri", "");
				resource.put("owner", "");
				resource.put("ezserver", null);
				resource.put("tags", new ArrayList<String>());
				Fetch.put("resourceTemplate", resource);
				
				// check if any args are set manually
				while(i<args.length){
					Fetch = parse(args[i],args[i+1],Fetch);
					i+=2;
				}	
				Fetch.put("command", "FETCH");
				
				// print information and send JSONObject to server
				System.out.println("SENT: "+Fetch.toJSONString());
				output.writeUTF(Fetch.toJSONString());
				output.flush();	
				
				// get message from server
				System.out.println("RECEIVE:");
				String result2 = input.readUTF();
				String result3 = input.readUTF();
				System.out.println(result2+result3);
				
				// Receive file from server
				File file = new File("/Users/macbookair/Desktop/newFile");
				FileOutputStream fos = new FileOutputStream(file);
				byte[] inputBytes = new byte[1024*1024];
				int length =0;
				
				// write bytes into the file
				while((length=input.read(inputBytes,0,inputBytes.length))>0){
					fos.write(inputBytes,0,length);
					fos.flush();
				}
				break;
				
			case "-PUBLISH":
				i=1;
				
				// Create resource template for Publish command
				resource  = new JSONObject();
				JSONObject Publish = new JSONObject();
				ArrayList<String> tags = new ArrayList<String>();
				resource = new JSONObject();
				resource.put("name", "");
				resource.put("channel", "");
				resource.put("description", "");
				resource.put("uri", "");
				resource.put("owner", "");
				resource.put("ezserver", null);
				resource.put("tags", tags);
				Publish.put("resource", resource);
				
				// check if any args are set manually
				while(i<args.length){
					Publish = parse(args[i],args[i+1],Publish);
					i+=2;
				}	
				
				// send the command to server
				Publish.put("command", "PUBLISH");
				System.out.println(Publish.toJSONString());
				output.writeUTF(Publish.toJSONString());
				output.flush();
				break;
			case "-SHARE":
				i=1;
				
				// Create JSONObject template for Sharing
				JSONObject Share = new JSONObject();
				 resource = new JSONObject();
				resource.put("name", "");
				resource.put("channel", "");
				resource.put("description", "");
				resource.put("uri", "");
				resource.put("owner", "");
				resource.put("ezserver", null);
				resource.put("tags", new ArrayList<String>());
				Share.put("resource", resource);
				Share.put("secret", "");
				
				// check if any args are set manually
				while(i<args.length){
					Share = parse(args[i],args[i+1],Share);
					i+=2;
				}
				
				// send the share command to server
				Share.put("command", "SHARE");
				System.out.println(Share.toJSONString());
				output.writeUTF(Share.toJSONString());
				output.flush();
				break;
			case "-REMOVE":
				i=1;
				
				// Create Remove template for Removing
				JSONObject Remove = new JSONObject();
				resource = new JSONObject();
				resource.put("name", "");
				resource.put("channel", "");
				resource.put("description", "");
				resource.put("uri", "");
				resource.put("owner", "");
				resource.put("ezserver", null);
				resource.put("tags", new ArrayList<String>());
				Remove.put("resource", resource);
				
				//check if any args are set manually
				while(i<args.length){
					Share = parse(args[i],args[i+1],Remove);
					i+=2;
				}	
				// send the remove command to server
				Remove.put("command", "REMOVE");
				System.out.println(Remove.toJSONString());
				output.writeUTF(Remove.toJSONString());
				output.flush();
				break;
					
			default:
				// don't send any command to server
				System.out.println("Invalid args input!");
				break;
			}
			/* 
			if(!args[0].equals("FETCH")){
			System.out.println("Received from server:");
			while(true){
				  if(input.available()>0){
						String result = input.readUTF();	
						System.out.println(result);
					}
				  
					}}*/
			
		}
	}
	private static JSONObject parse(String s1, String s2, JSONObject json) {
		// TODO Auto-generated method stub
		switch(s1){
		
		// put ezserver JSONObject
		case "-ezserver":
			JSONObject r = (JSONObject) json.get("resourceTemplate");
			if(r==null){ r = (JSONObject) json.get("resource");}
			r.put("ezserver", s2);	
			break;
		
		// put channel into JSONObject
		case "-channel":
			 r = (JSONObject) json.get("resourceTemplate");
			if(r==null){ r = (JSONObject) json.get("resource");}
			r.put("channel", s2);
			break;
			
		// put description into JSONObject	
		case "-description":
			r = (JSONObject) json.get("resourceTemplate");
			if(r==null){ r = (JSONObject) json.get("resource");}
			r.put("description", s2);
			break;
			
		// put name into JSONObject
		case "-name":		
			r = (JSONObject) json.get("resourceTemplate");
			if(r==null){ r = (JSONObject) json.get("resource");}
			r.put("name", s2);
			break;
			
		// put owner into JSONObject
		case "-owner":
			r = (JSONObject) json.get("resourceTemplate");
			if(r==null){ r = (JSONObject) json.get("resource");}
			r.put("owner", s2);
			break;
	
		// put secret into JSONObject			
		case "-secret":
			json.put("secret", s2);
			break;
			
		// put serverlist into JSONObject
		case "-servers":
		    String[] aa = s2.split("\\,"); 
		    ArrayList<JSONObject> a = (ArrayList<JSONObject>) json.get("serverList");
		    for (int i = 0 ; i <aa.length ; i++ ) {
		    	String[] aaa = aa[i].split("\\:");
		    	if(aaa.length==2){
		    		JSONObject j = new JSONObject();
		    		j.put("hostname", aaa[0]);
		    		j.put("port", Integer.parseInt(aaa[1]));
		    		a.add(j);
		    	}
		    }
		    json.put("serverList", a);
			break;
			
		// put tags into JSONObject
		case "-tags":
			r = (JSONObject) json.get("resourceTemplate");
			if(r==null){ r = (JSONObject) json.get("resource");}
		    String[] bb = s2.split("\\,"); 
		    ArrayList<String> b = (ArrayList<String>) r.get("tags");
		    if(bb.length>0){
		    for (int i = 0 ; i <bb.length ; i++ ) {
		    	
		    		b.add(bb[i]);
		    	
		    }}
		    json.put("tags", b);
			break;
			
		// put uri into JSONObject
		case "-uri":
			r = (JSONObject) json.get("resourceTemplate");
			if(r==null){ r = (JSONObject) json.get("resource");}
			r.put("uri", s2);
			break;
			
		// ignore invalid argument name
		default:
			break;
		}
		return json;
	}


}
