package net.bobmandude9889.bot;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class SkipCommand implements DiscordCommand{

	Main main;
	
	public SkipCommand(Main main) {
		this.main = main;
	}
	
	@Override
	public void onCommand(String[] args, MessageReceivedEvent event) {
		main.getGuildAudioPlayer(event.getGuild()).scheduler.nextTrack();
		event.getTextChannel().sendMessage("Skipping track!").queue();
	}

}
