package dev.keith.bots;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static dev.keith.bots.DiscordBot.JDA;
import static java.util.concurrent.TimeUnit.SECONDS;

public class JDAListener extends ListenerAdapter {
    public static Map<Guild, Map<Button, Role>> map = new HashMap<>();
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        DiscordBot.LOGGER.info("Slash Command: /" + event.getName() + " is called");
        switch (event.getName()) {
            case "help": {
                event.deferReply().queue();
                event.getHook().sendMessage(
                        """
                        <--------------- Br.uh Bot Helping menu --------------->
                        /help -> Ask bot to send this menu.
                        /say -> Send message though the bot.
                        /ping -> Get Bots Ping.
                        /role -> Send a message that can get role from a button.
                        /mute -> Timeout(mute) someone.
                        <------------------------------------------------------>
                        """).queue();
                break;
            }
            case "say": {
                event.deferReply().queue();
                selfDestruct(event.getHook(), event.getOption("content").getAsString()).queue();
                break;
            }
            case "ping": {
                event.deferReply().queue();
                event.getHook().sendMessage("Current Ping: " + JDA.getGatewayPing() + " (ms)").queue();
                break;
            }
            case "invite": {
                event.deferReply().queue();
                event.getHook().sendMessage(JDA.getInviteUrl(Permission.ADMINISTRATOR).replace("scope=bot", "scope=bot+applications.commands")).queue();
                break;
            }
            case "role": {
                event.deferReply().queue();
                Button button = Button.primary(event.getOption("role").getAsRole().getId(), "Get Role");
                Map<Button, Role> map1 = new HashMap<>();
                map1.put(button, event.getOption("role").getAsRole());
                map.put(event.getGuild(), map1);
                String message;
                try {
                    message = Objects.requireNonNull(event.getOption("message")).getAsString();
                } catch (NullPointerException e) {
                    message = "Click the below button to get your Role!";
                }
                event.getHook().sendMessage(message)
                        .addActionRow(button)
                        .queue();
                break;
            }
            case "mute": {
                event.deferReply().queue();
                Member member = event.getOption("member").getAsMember();
                boolean isNotified = event.getOption("notified").getAsBoolean();

                member.timeoutFor(event.getOption("time").getAsLong(), TimeUnit.MINUTES).queue();
                if(isNotified) {
                    member.getUser().openPrivateChannel().queue(
                            privateChannel -> privateChannel.sendMessage(
                                    "You have been timeout in Server: " +
                                            event.getGuild().getName() +
                                            " for " +
                                            event.getOption("time").getAsLong() +
                                            " minutes by Mod / Admin " +
                                            event.getUser().getName() +
                                            ".")
                                    .queue()
                    );
                }
                event.getHook().sendMessage("Member " + member.getUser().getName() + " has been timeout.").queue();
                break;
            }
            case "kick": {
                event.deferReply().queue();

                Member member = event.getOption("member").getAsMember();
                boolean isNotified = event.getOption("notified").getAsBoolean();

                member.kick().queue();

                if (isNotified) {
                    member.getUser().openPrivateChannel()
                            .queue(privateChannel -> privateChannel.sendMessage(
                                            "You have been kicked out in Server: " +
                                                    event.getGuild().getName() +
                                                    " by Mod / Admin " +
                                                    event.getUser().getName() +
                                                    ".")
                                    .queue()
                            );
                }

                event.getHook().sendMessage("Member " + member.getUser().getName() + " has been kicked.").queue();
                break;
            }
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        Guild guild = event.getGuild();

        if(map.get(guild) != null & map.get(guild).get(event.getButton()) != null) {
            Role role = map.get(guild).get(event.getButton());
            Member member = event.getMember();

            assert guild != null;
            assert member != null;

            guild.addRoleToMember(member, role).queue();
            event.reply("You have got the Role!").setEphemeral(true).queue();
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        updateSlashCommand(event.getGuild());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getMessage().getContentRaw().equals("!slash")) {
            DiscordBot.LOGGER.info("Guild Id: " + event.getGuild().getId() + " . Someone is needed to reload slash command");
            event.getMessage().reply("Reloading Slash Commands").queue();
            updateSlashCommand(event.getGuild());
        }
    }


    private void updateSlashCommand(Guild guild) {
        guild.updateCommands().addCommands(
                Commands.slash("say", "Say messages though the bot. It will destroyed after 15 secs")
                        .addOption(OptionType.STRING, "content", "The message content which bot will say.", true),
                Commands.slash("help", "Get the helping menu."),
                Commands.slash("ping", "Get the Bot ping"),
                Commands.slash("invite", "Get the invite link."),
                Commands.slash("role", "Send a message. When one reaction it, ones get a role")
                        .addOption(OptionType.ROLE, "role", "The role that added to one.", true)
                        .addOption(OptionType.STRING, "message", "The String of message.")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES, Permission.ADMINISTRATOR)),
                Commands.slash("mute", "Timeout(muting) someone.")
                        .addOption(OptionType.MENTIONABLE, "member", "The member that you want to timeout", true)
                        .addOption(OptionType.INTEGER, "time", "The time you want to timeout him. (minutes)", true)
                        .addOption(OptionType.BOOLEAN, "notified", "Notified the member that you timeout?", true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_PERMISSIONS, Permission.ADMINISTRATOR))
                        .setGuildOnly(true),
                Commands.slash("kick", "Kick someone.")
                        .addOption(OptionType.MENTIONABLE, "member", "The member that you want to kick", true)
                        .addOption(OptionType.BOOLEAN, "notified", "Notified the member that you kick?", true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_PERMISSIONS, Permission.ADMINISTRATOR))
                        .setGuildOnly(true)
        ).queue();
    }

    private RestAction<Void> selfDestruct(InteractionHook hook, String content) {
        return hook.sendMessage("The following message will destroy itself in 5 seconds!")
                .delay(3, SECONDS) // after sending, wait 3 seconds
                .flatMap((it) -> it.editMessage(content)) // then edit the message
                .delay(5, SECONDS) // wait 5 secs
                .flatMap(Message::delete); // then delete
    }
}
