package model.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.*;

public class JsonLoader {
	
	public static JSONObject loadFile(InputStream inStream) throws IOException {
		String content = new String(inStream.readAllBytes(), StandardCharsets.UTF_8);
		JSONObject result = new JSONObject(content);
		return result;
	}
	
}
