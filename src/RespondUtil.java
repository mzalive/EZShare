import java.io.DataOutputStream;
import java.io.IOException;

import org.json.simple.JSONObject;

public class RespondUtil {
	
	/*
	 *  method for returning error msg
	 */
	@SuppressWarnings("unchecked")
	public static void returnErrorMsg(DataOutputStream output, String msg) {
		JSONObject result = new JSONObject();
		result.put("response", "error");
		result.put("errorMessage", msg);
		try {
			output.writeUTF(result.toJSONString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}	
	
	/*
	 *  method for returning success msg
	 */
	@SuppressWarnings("unchecked")
	public static void returnSuccessMsg(DataOutputStream output) {
		JSONObject result = new JSONObject();
		result.put("response", "success");
		try {
			output.writeUTF(result.toJSONString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}

	
}
