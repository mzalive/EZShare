package EZShare;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RelayThread extends Thread{
	public DataInputStream input;
	public DataOutputStream output;
	public RelayThread(DataInputStream input, DataOutputStream output){
		this.input = input;
		this.output = output;
	}
	public void run(){
		try {
			while(input.available()>0){
				String result = input.readUTF();
				output.writeUTF(result);
				output.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
