package net.bobmandude9889.bot;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class StopCommand implements DiscordCommand{

	Main main;
	
	public StopCommand(Main main) {
		this.main = main;
	}
	
	@Override
	public void onCommand(String[] args, MessageReceivedEvent event) {
		GuildMusicManager musicManager = main.getGuildAudioPlayer(event.getGuild());
		musicManager.scheduler.stop();
		event.getTextChannel().sendMessage("Stopping music.").queue();
	}

}
