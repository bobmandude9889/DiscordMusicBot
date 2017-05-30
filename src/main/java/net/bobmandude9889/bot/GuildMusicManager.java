package net.bobmandude9889.bot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import net.dv8tion.jda.core.entities.Guild;

public class GuildMusicManager {

	public final AudioPlayer player;
	public final TrackScheduler scheduler;
	public final Guild guild;

	public GuildMusicManager(AudioPlayerManager manager, Guild guild) {
		this.guild = guild;
		player = manager.createPlayer();
		scheduler = new TrackScheduler(player, guild);
		player.addListener(scheduler);
	}

	public AudioPlayerSendHandler getSendHandler() {
		return new AudioPlayerSendHandler(player);
	}
}
