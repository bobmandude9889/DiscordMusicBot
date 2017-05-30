package net.bobmandude9889.bot;

import java.io.File;
import java.io.FileReader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Config {

	public static String botToken;
	public static boolean useWhitelist;
	public static String defaultChannel;
	
	public static void loadConfig(String path) {
		File config = new File(path, "config.json");
		if(config.exists()) {
		JSONParser parser = new JSONParser();
		try {
			JSONObject obj = (JSONObject) parser.parse(new FileReader(config));
			botToken = (String) obj.get("bot_token");
			useWhitelist = (Boolean) obj.get("use_whitelist");
			defaultChannel = (String) obj.get("default_channel");

			System.out.println("Loaded config.");
			System.out.println("Settings:");
			for(Object key : obj.keySet()) {
				System.out.println(key + " = " + obj.get(key));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		} else {
			System.out.println("Could not find config file in " + Main.path);
			System.exit(0);
		}
	}
	
}
