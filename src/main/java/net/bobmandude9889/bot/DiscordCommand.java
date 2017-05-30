package net.bobmandude9889.bot;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public interface DiscordCommand {

	public void onCommand(String[] args, MessageReceivedEvent event);
	
}
