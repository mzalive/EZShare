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

	
	//private static int port = 3781;
	public static void main(String[] args) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		int port = 3781;
		String host = "sunrise.cis.unimelb.edu.au";
		for(int i=0; i<args.length;i++){
			if(args[i].equals("-host"))
			{host=args[i+1];}
			else if(args[i].equals("-port"))
			{port=Integer.parseInt(args[i+1]);}
			else{}
		}
		try(Socket socket = new Socket(host,port);){
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
			int len = args.length;
			switch (args[0]){
			case "-QUERY":
				QueryClient q = new QueryClient();
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
				while(i<args.length){
					Query = parse(args[i],args[i+1],Query);
					i+=2;
				}
				
				Query.put("command", "QUERY");
				System.out.println(Query.toJSONString());
				output.writeUTF(Query.toJSONString());
				output.flush();
				break;
			case "-EXCHANGE":
				ExchangeClient e = new ExchangeClient();
				i=1;
				JSONObject Exchange = new JSONObject();
				Exchange.put("serverList", new ArrayList<JSONObject>());
				while(i<args.length){
					Exchange = parse(args[i],args[i+1],Exchange);
					i+=2;
				}
				
				Exchange.put("command", "EXCHANGE");
				System.out.println(Exchange.toJSONString());
				output.writeUTF(Exchange.toJSONString());
				output.flush();
				break;
			case "-FETCH":
				i=1;
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
				while(i<args.length){
					Fetch = parse(args[i],args[i+1],Fetch);
					i+=2;
				}	
				Fetch.put("command", "FETCH");
				System.out.println(Fetch.toJSONString());

				output.writeUTF(Fetch.toJSONString());
				output.flush();	
				System.out.println("Received from server:");
				String result2 = input.readUTF();
				String result3 = input.readUTF();
			//	String result4  = input.readUTF();
				System.out.println(result2+result3);
				
				File file = new File("/Users/macbookair/Desktop/newFile");
				FileOutputStream fos = new FileOutputStream(file);
				byte[] inputBytes = new byte[3743507];
				int length =0;
				while((length=input.read(inputBytes,0,inputBytes.length))>0){
					fos.write(inputBytes,0,length);
					fos.flush();
				}
				break;
			case "-PUBLISH":
			//	PublishClient r = new PublishClient();
				i=1;
				resource  = new JSONObject();
				JSONObject Publish = new JSONObject();
				resource = new JSONObject();
				resource.put("name", "");
				resource.put("channel", "");
				resource.put("description", "");
				resource.put("uri", "");
				resource.put("owner", "");
				resource.put("ezserver", null);
				resource.put("tags", new ArrayList<String>());
				Publish.put("resource", resource);
				while(i<args.length){
					Publish = parse(args[i],args[i+1],Publish);
					i+=2;
				}	
				Publish.put("command", "PUBLISH");
				System.out.println(Publish.toJSONString());
				output.writeUTF(Publish.toJSONString());
				output.flush();
		
				break;
			case "-SHARE":
			//	ShareClient s = new ShareClient();
				i=1;
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
				while(i<args.length){
					Share = parse(args[i],args[i+1],Share);
					i+=2;
				}
				
				Share.put("command", "SHARE");
				System.out.println(Share.toJSONString());
				output.writeUTF(Share.toJSONString());
				output.flush();
				break;
			case "-REMOVE":
				RemoveClient r = new RemoveClient();
				i=1;
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
				while(i<args.length){
					Share = parse(args[i],args[i+1],Remove);
					i+=2;
				}	
				Remove.put("command", "REMOVE");
				//((JSONObject)Remove.get("resource")).put("channel", "");
				System.out.println(Remove.toJSONString());
				output.writeUTF(Remove.toJSONString());
				output.flush();
				break;
			
		
				
			default:
				System.out.println("Invalid args input!");
				break;
			}
			if(!args[0].equals("FETCH")){
			System.out.println("Received from server:");
			while(true){
				  if(input.available()>0){
						String result = input.readUTF();	
						System.out.println(result);
					}
				  
					}}
			
		}
	}
	private static JSONObject parse(String s1, String s2, JSONObject json) {
		// TODO Auto-generated method stub
		switch(s1){
		case "-ezserver":
			JSONObject r = (JSONObject) json.get("resourceTemplate");
			if(r==null){ r = (JSONObject) json.get("resource");}
			r.put("ezserver", s2);	
			break;
		
		case "-channel":
			 r = (JSONObject) json.get("resourceTemplate");
			if(r==null){ r = (JSONObject) json.get("resource");}
			r.put("channel", s2);
			break;
		case "-description":
			r = (JSONObject) json.get("resourceTemplate");
			if(r==null){ r = (JSONObject) json.get("resource");}
			r.put("description", s2);
			break;
		case "-host":
			r = (JSONObject) json.get("resourceTemplate");
			if(r==null){ r = (JSONObject) json.get("resource");}
			r.put("host", s2);
			break;
		case "-name":
			
			r = (JSONObject) json.get("resourceTemplate");
			if(r==null){ r = (JSONObject) json.get("resource");}
			r.put("name", s2);
			break;
		case "-owner":
			r = (JSONObject) json.get("resourceTemplate");
			if(r==null){ r = (JSONObject) json.get("resource");}
			r.put("owner", s2);
			break;
		case "-port":
			json.put("port", s2);
			break;
		case "-secret":
			json.put("secret", s2);
			break;
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
		case "-tags":
		    String[] bb = s2.split("\\,"); 
		    ArrayList<String> b = (ArrayList<String>) json.get("tags");
		    if(bb.length>0){
		    for (int i = 0 ; i <bb.length ; i++ ) {
		    	
		    		b.add(bb[i]);
		    	
		    }}
		    json.put("tags", b);
			break;
		case "-uri":
			r = (JSONObject) json.get("resourceTemplate");
			if(r==null){ r = (JSONObject) json.get("resource");}
			r.put("uri", s2);
			break;
		default:
			json.put("error", "incorrectargs");
			break;
		}
		return json;
	}


}
