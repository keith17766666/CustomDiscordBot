package dev.keith.bots;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

public class DiscordBot {
    public static JDA JDA;
    public static Logger LOGGER = LoggerFactory.getLogger(DiscordBot.class);
    public static void startBot(String token) {
        JDA = JDABuilder.createLight(token, EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT))
                .addEventListeners(new JDAListener())
                .setActivity(Activity.of(Activity.ActivityType.PLAYING, "Use /help to get help menu!"))
                .build();
        LOGGER.info("Invite Link: " + JDA.getInviteUrl(Permission.ADMINISTRATOR));
    }
}