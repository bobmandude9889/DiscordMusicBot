package net.bobmandude9889.bot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import javax.security.auth.login.LoginException;
import javax.swing.JOptionPane;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sun.jndi.toolkit.url.UrlUtil;

import jdk.nashorn.api.scripting.URLReader;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class Main extends ListenerAdapter {

	static String path;

	public static void main(String[] args) {
		path = "";
		for (String arg : args) {
			path += arg + " ";
		}
		path = path.substring(0, path.length() - 1);
		try {
			path = UrlUtil.decode(path);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		Config.loadConfig(path);

		try {
			JDA jda = new JDABuilder(AccountType.BOT).setToken(Config.botToken).buildBlocking();
			jda.setAutoReconnect(true);
			jda.addEventListener(new Main());

			System.out.println("Connected as " + jda.getSelfUser().getName() + " with token " + Config.botToken);
		} catch (LoginException | IllegalArgumentException | InterruptedException | RateLimitedException e) {
			e.printStackTrace();
		}
	}

	HashMap<String, DiscordCommand> commands;
	public final AudioPlayerManager playerManager;
	public final Map<Long, GuildMusicManager> musicManagers;
	public final List<String> whitelist;
	public final File whitelistFile;

	@SuppressWarnings("unchecked")
	public void saveWhitelist() {
		try {
			PrintWriter out = new PrintWriter(whitelistFile);
			JSONArray array = new JSONArray();
			array.addAll(whitelist);
			out.print(array.toJSONString());
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void loadWhitelist() {
		JSONParser parser = new JSONParser();
		try {
			JSONArray array = (JSONArray) parser.parse(new FileReader(whitelistFile));
			whitelist.clear();
			whitelist.addAll(array);
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}

	public Main() {
		whitelist = new ArrayList<String>();
		whitelistFile = new File(path, "whitelist.json");
		if (!whitelistFile.exists()) {
			whitelist.add("250137172496089088");
			whitelist.add("95036075466039296");
			try {
				whitelistFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			saveWhitelist();
		} else {
			loadWhitelist();
		}

		commands = new HashMap<String, DiscordCommand>();
		commands.put("ping", new DiscordCommand() {

			@Override
			public void onCommand(String[] args, MessageReceivedEvent event) {
				event.getTextChannel().sendMessage("pong").queue();
			}

		});
		commands.put("volume", new DiscordCommand() {

			@Override
			public void onCommand(String[] args, MessageReceivedEvent event) {
				try {
					getGuildAudioPlayer(event.getGuild()).player.setVolume(Integer.parseInt(args[0]));
					event.getTextChannel().sendMessage("Volume set to " + args[0]).queue();
				} catch (Exception e) {
					event.getTextChannel().sendMessage("Could not set volume.");
				}
			}

		});
		commands.put("queue", new DiscordCommand() {
			@Override
			public void onCommand(String[] args, MessageReceivedEvent event) {
				Object[] queue = getGuildAudioPlayer(event.getGuild()).scheduler.queue.toArray();
				String tracks = "";
				int i = 0;
				int max = 8;
				for (Object track : queue) {
					AudioTrack trackO = (AudioTrack) track;
					tracks += "\n" + trackO.getInfo().title;
					i++;
					if (i >= max)
						break;
				}
				if (queue.length > max)
					tracks += "\nand " + (queue.length - max) + " more...";
				event.getTextChannel().sendMessage("Songs currently in queue: " + tracks).queue();
			}
		});
		commands.put("play", new PlayCommand(this));
		commands.put("stop", new StopCommand(this));
		commands.put("skip", new SkipCommand(this));
		commands.put("pause", new DiscordCommand() {

			@Override
			public void onCommand(String[] args, MessageReceivedEvent event) {
				getGuildAudioPlayer(event.getGuild()).scheduler.pause();
				event.getTextChannel().sendMessage("Pausing music.").queue();
			}

		});
		commands.put("resume", new DiscordCommand() {

			@Override
			public void onCommand(String[] args, MessageReceivedEvent event) {
				getGuildAudioPlayer(event.getGuild()).scheduler.resume();
				event.getTextChannel().sendMessage("Resuming music.").queue();
			}

		});
		commands.put("nowplaying", new DiscordCommand() {

			@Override
			public void onCommand(String[] args, MessageReceivedEvent event) {
				AudioTrack current = getGuildAudioPlayer(event.getGuild()).player.getPlayingTrack();
				if (current != null)
					event.getTextChannel().sendMessage("Currently playing " + current.getInfo().title).queue();
				else
					event.getTextChannel().sendMessage("No songs playing").queue();
			}

		});
		commands.put("help", new DiscordCommand() {

			@Override
			public void onCommand(String[] args, MessageReceivedEvent event) {
				String message = "Commands:";
				for (String command : commands.keySet()) {
					message += "\n!" + command;
				}
				event.getTextChannel().sendMessage(message).queue();
			}

		});
		commands.put("cat", new DiscordCommand() {

			@Override
			public void onCommand(String[] args, MessageReceivedEvent event) {
				try {
					URL url = new URL("http://random.cat/meow");
					JSONParser parser = new JSONParser();
					JSONObject obj = (JSONObject) parser.parse(new URLReader(url));
					event.getTextChannel().sendMessage(obj.get("file").toString()).queue();
				} catch (IOException | ParseException e) {
					e.printStackTrace();
				}
			}

		});
		commands.put("dog", new DiscordCommand() {

			@Override
			public void onCommand(String[] args, MessageReceivedEvent event) {
				try {
					URL url = new URL("https://random.dog/woof");
					Scanner in = new Scanner(url.openStream());
					String dog = in.nextLine();
					in.close();
					event.getTextChannel().sendMessage("http://random.dog/" + dog).queue();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		});
		commands.put("8ball", new DiscordCommand() {

			String[] answers = { "It is certain", "It is decidedly so", "Without a doubt", "Yes definitely", "You may rely on it", "As I see it, yes", "Most likely", "Outlook good", "Yes", "Signs point to yes", "Reply hazy try again", "Ask again later", "Better not tell you now", "Cannot predict now", "Concentrate and ask again", "Don't count on it", "My reply is no", "My sources say no", "Outlook not so good", "Very doubtful" };

			@Override
			public void onCommand(String[] args, MessageReceivedEvent event) {

				String question = "";
				for (String s : args) {
					question += s + " ";
				}
				question = question.substring(0, question.length() - 1);
				int random = (int) Math.round(Math.random() * (answers.length - 1));
				event.getTextChannel().sendMessage(question + ":\n" + answers[random]).queue();
			}

		});
		commands.put("random", new DiscordCommand() {

			@Override
			public void onCommand(String[] args, MessageReceivedEvent event) {
				if (args.length > 0) {
					try {
						Integer i = Integer.parseInt(args[0]);
						Random r = new Random();
						event.getTextChannel().sendMessage(event.getAuthor().getAsMention() + " " + r.nextInt(i)).queue();
					} catch (NumberFormatException e) {
						event.getTextChannel().sendMessage("!random <max>").queue();
					}
				} else {
					event.getTextChannel().sendMessage("!random <max>").queue();
				}
			}

		});
		if (Config.useWhitelist)
			commands.put("whitelist", new WhitelistCommand(this));

		this.musicManagers = new HashMap<>();

		this.playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playerManager);
		AudioSourceManagers.registerLocalSource(playerManager);
	}

	public synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
		long guildId = Long.parseLong(guild.getId());
		GuildMusicManager musicManager = musicManagers.get(guildId);

		if (musicManager == null) {
			musicManager = new GuildMusicManager(playerManager, guild);
			musicManagers.put(guildId, musicManager);
		}

		guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

		return musicManager;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (!Config.useWhitelist || whitelist.contains(event.getAuthor().getId())) {
			for (String key : commands.keySet()) {
				if (event.getMessage().getContent().toLowerCase().startsWith("!" + key.toLowerCase())) {
					String message = event.getMessage().getContent();
					message = message.substring(key.length() + 1);
					if (message.startsWith(" "))
						message = message.substring(1);
					commands.get(key).onCommand(message.split(" "), event);
				}
			}
		}
	}

}
