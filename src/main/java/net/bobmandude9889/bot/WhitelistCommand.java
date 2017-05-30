package net.bobmandude9889.bot;

import java.util.List;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class WhitelistCommand implements DiscordCommand {

	Main main;
	
	public WhitelistCommand(Main main) {
		this.main = main;
	}
	
	@Override
	public void onCommand(String[] args, MessageReceivedEvent event) {
		if(args.length > 1) {
			List<Member> members = event.getGuild().getMembers();
			Member member = null;
			for(Member m : members) {
				if(m.getUser().getName().toLowerCase().contains(args[1])) {
					member = m;
					break;
				}
			}
			if(member == null) {
				event.getTextChannel().sendMessage(event.getAuthor().getAsMention() + " Could not find user.").queue();
			}
			String id = member.getUser().getId();
			if(args[0].equalsIgnoreCase("add")) {
				if(!main.whitelist.contains(id))
					main.whitelist.add(id);
				event.getTextChannel().sendMessage(event.getAuthor().getAsMention() + " Added " + member.getUser().getName() + " to whitelist.").queue();
				main.saveWhitelist();
			} else if (args[0].equalsIgnoreCase("remove")) {
				if(main.whitelist.contains(id))
					main.whitelist.remove(id);
				event.getTextChannel().sendMessage(event.getAuthor().getAsMention() + " Removed " + member.getUser().getName() + " from whitelist.").queue();
				main.saveWhitelist();
			} else {
				event.getTextChannel().sendMessage(event.getAuthor().getAsMention() + " Usage: !whitelist <add, remove> <name>").queue();
			}
		} else {
			event.getTextChannel().sendMessage(event.getAuthor().getAsMention() + " Usage: !whitelist <add, remove> <name>").queue();
		}
	}

}
