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

	
	private static int port = 3781;
	public static void main(String[] args) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub

		try(Socket socket = new Socket("sunrise.cis.unimelb.edu.au",port);){
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
				try(Socket esocket = new Socket("sunrise.cis.unimelb.edu.au",port);){
					JSONObject newCommand = new JSONObject();
					JSONObject server1 = new JSONObject();
					server1.put("hostname", "115.146.85.165");
					server1.put("port", 3780);
					JSONObject server2 = new JSONObject();
					server2.put("hostname", "115.146.85.24");
					server2.put("port", 3780);
					JSONObject server3 = new JSONObject();
					server3.put("hostname", "115.146.85.25");
					server3.put("port", 3780);
					ArrayList<JSONObject> l = new ArrayList<JSONObject>();
					l.add(server1);
					l.add(server2);
					l.add(server3);
					newCommand.put("serverList", l);
					
					newCommand.put("command", "EXCHANGE");

					output.writeUTF(newCommand.toJSONString());
					output.flush();
					System.out.println("Received from server:");
					while(true){
				  if(input.available()>0){
						String result = input.readUTF();	
						System.out.println(result);
					}
				  else{
					  break;
				  }
					}
					break;
					}

			case "-FETCH":
				try(Socket fsocket = new Socket("sunrise.cis.unimelb.edu.au",port);){
					//DataInputStream input = new DataInputStream(socket.getInputStream());
					//DataOutputStream output = new DataOutputStream(socket.getOutputStream());
					JSONObject newCommand = new JSONObject();
					JSONObject resource = new JSONObject();		
					resource.put("name", "Aaron");
					resource.put("tags", "['photo','jpeg','jpg']");
					resource.put("description", "Secret agent photo :-)");
					resource.put("uri", "file:///usr/local/share/ezshare/photo.jpg");
					resource.put("channel", "");
					resource.put("owner", "*");
					resource.put("ezserver", "sunrise.cis.unimelb.edu.au:3780");
					newCommand.put("command","FETCH");
					newCommand.put("resourceTemplate", resource);
					newCommand.put("command","FETCH");
					System.out.println(newCommand.toJSONString());		
					output.writeUTF(newCommand.toJSONString());
					output.flush();	
					System.out.println("Received from server:");
					String result2 = input.readUTF();
					String result3 = input.readUTF();
					System.out.println(result2+result3);
					File file = new File("/Users/macbookair/Desktop/aaron2.jpg");
					FileOutputStream fos = new FileOutputStream(file);
					byte[] inputBytes = new byte[1743506];
					int length =0;
					while((length=input.read(inputBytes,0,inputBytes.length))>0){
						fos.write(inputBytes,0,length);
						fos.flush();
					}
					System.out.println("read success");
				}
				break;
			case "-PUBLISH":
			//	PublishClient r = new PublishClient();
				i=1;
				JSONObject resource  = new JSONObject();
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
				System.out.println(Remove.toJSONString());
				output.writeUTF(Remove.toJSONString());
				output.flush();
				break;
			
		
				
			default:
				System.out.println("Invalid args input!");
				break;
			}
			
			System.out.println("Received from server:");
		//	while(true){
				  //if(input.available()>0){
						String result = input.readUTF();	
						System.out.println(result);
					//}
				  
					//}
			
		}
	}
	private static JSONObject parse(String s1, String s2, JSONObject json) {
		// TODO Auto-generated method stub
		switch(s1){
		case "-ezserver":
			JSONObject r = (JSONObject) json.get("resourceTemplate");
			if(r==null){ r = (JSONObject) json.get("resource");}
			r.put("ezserver", s2);	
		
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
			
			break;
		case "-tags":
			
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
