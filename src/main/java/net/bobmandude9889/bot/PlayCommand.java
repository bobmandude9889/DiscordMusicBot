package net.bobmandude9889.bot;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import jdk.nashorn.api.scripting.URLReader;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.managers.AudioManager;

public class PlayCommand implements DiscordCommand {

	Main main;

	public PlayCommand(Main main) {
		this.main = main;
	}

	@Override
	public void onCommand(String[] args, MessageReceivedEvent event) {
		String url = args[0];
		try {
			new URL(url);
		} catch (MalformedURLException e) {
			String search = "";
			for (String s : args) {
				search += s + "%20";
			}
			search = search.substring(0, search.length() - 3);
			try {
				URL youtubeApi = new URL("https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&key=AIzaSyBd6mUb34CibMuvWApStI4h5pBJsnjStW4&type=video&q=" + search);

				JSONParser parse = new JSONParser();
				JSONObject obj = (JSONObject) parse.parse(new URLReader(youtubeApi));
				System.out.println(obj);

				JSONArray items = (JSONArray) obj.get("items");
				if(items.size() > 0) {
					JSONObject id = (JSONObject) ((JSONObject) items.get(0)).get("id");
					String videoId = (String) id.get("videoId");
					url = "https://www.youtube.com/watch?v=" + videoId;
				} else {
					event.getTextChannel().sendMessage("No results found.").queue();
					return;
				}
			} catch (IOException | ParseException e1) {
				event.getTextChannel().sendMessage("Something went wrong!").queue();
				e1.printStackTrace();
				return;
			}
		}
		loadAndPlay(event.getTextChannel(), url, event.getAuthor());
	}

	private void loadAndPlay(final TextChannel channel, final String trackUrl, final User user) {
		GuildMusicManager musicManager = main.getGuildAudioPlayer(channel.getGuild());

		main.playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(AudioTrack track) {
				channel.sendMessage("Adding to queue: " + track.getInfo().title).queue();

				play(channel.getGuild(), musicManager, track, user);
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				for(AudioTrack track : playlist.getTracks()) {
					play(channel.getGuild(), musicManager, track, user);
				}

				channel.sendMessage("Adding playlist to queue: " + playlist.getName()).queue();
			}

			@Override
			public void noMatches() {
				channel.sendMessage("Nothing found by " + trackUrl).queue();
			}

			@Override
			public void loadFailed(FriendlyException exception) {
				channel.sendMessage("Could not play: " + exception.getMessage()).queue();
			}
		});
	}

	private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track, User user) {
		connectToDefaultChannel(guild.getAudioManager(), musicManager, user);

		musicManager.scheduler.queue(track);
	}

	private static void connectToDefaultChannel(AudioManager audioManager, GuildMusicManager musicManager, User user) {
		if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
			List<VoiceChannel> channels = audioManager.getGuild().getVoiceChannelsByName(Config.defaultChannel, true);
			if (channels.size() > 0) {
				audioManager.openAudioConnection(channels.get(0));
			} else {
				System.out.println("Could not find default channel. Connecting to users channel.");
				VoiceChannel connectTo = null;
				for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
					for (Member m : voiceChannel.getMembers()) {
						if (m.getUser().equals(user)) {
							connectTo = voiceChannel;
							break;
						}
					}
					if(connectTo != null)
						break;
				}
				if(connectTo != null)
					audioManager.openAudioConnection(connectTo);
				else {
					System.out.println("Could not find users channel. Connecting to first channel in server.");
					audioManager.openAudioConnection(audioManager.getGuild().getVoiceChannels().get(0));
				}
			}
			musicManager.player.setVolume(20);
		}
	}

}
