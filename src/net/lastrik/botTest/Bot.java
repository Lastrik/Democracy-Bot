package net.lastrik.botTest;

import java.awt.Color;
import java.util.ArrayList;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.events.*;
import net.dv8tion.jda.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.hooks.EventListener;
import net.dv8tion.jda.managers.GuildManager;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.managers.ChannelManager;

import javax.security.auth.login.LoginException;
import java.util.*;
import java.util.regex.Pattern;
import net.dv8tion.jda.managers.RoleManager;

/**
 *
 * @author Lastrik
 */
public class Bot implements EventListener {

    public final static int DAYS_TO_VOTE = 2;

    private String tokenCommand;
    private GuildManager democracy;
    private JDA jda;
    private boolean stop = false;
    private ArrayList<Votation> votations;

    public Bot(String token) {
        try {
            jda = new JDABuilder().setBotToken(token).setBulkDeleteSplittingEnabled(false).buildBlocking();
            jda.addEventListener(this);
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("Un error append please check your internet connection and the bot token");
            return;
        }
        democracy = new GuildManager(jda.getGuilds().get(0));
        System.out.println("Connected with: " + jda.getSelfInfo().getUsername());
        int i;
        tokenCommand = "!";
        System.out.println("The bot is authorized on " + (i = jda.getGuilds().size()) + " server" + (i > 1 ? "s" : ""));
        if (jda.getGuilds().size() > 1) {
            System.err.println("This bot is not made to run on more than on server per . Please create multiple applications for multiple servers");
        }
        votations = new ArrayList<>();
        while (!stop) {
            Scanner scanner = new Scanner(System.in);
            String cmd = scanner.next();
            if (cmd.equalsIgnoreCase("stop")) {
                stop();
            }
        }
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof PrivateMessageReceivedEvent) {
            if (((PrivateMessageReceivedEvent) event).getMessage().getContent().equalsIgnoreCase("ping")) {
                ((PrivateMessageReceivedEvent) event).getChannel().sendTyping();
                ((PrivateMessageReceivedEvent) event).getChannel().sendMessage("pong");
            }
        }

        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent e = (MessageReceivedEvent) event;
            if (e.getMessage().getContent().startsWith(tokenCommand) && (!e.isPrivate())) {
                ArrayList<String> splittedCommand = splitter(e.getMessage().getContent());
                String commandNoArgs = splittedCommand.get(0).replaceFirst(tokenCommand, "");
                splittedCommand.remove(0);
                ArrayList<String> args = splittedCommand;

                switch (commandNoArgs) {
                    //Quelques commandes qui touchent directement au bot
                    case "stop":
                        stop();
                        break;
                    case "changetoken":
                        changeToken(e, args);
                        break;
                    default:
                        Command command = new Command(e, commandNoArgs, args);
                        command.process();
                }
            }
        }
        if (event instanceof GuildMemberJoinEvent) {
            User user = ((GuildMemberJoinEvent) event).getUser();
            democracy = democracy.addRoleToUser(user, democracy.getGuild().getRolesByName("Citizen").toArray(new Role[0]));
            democracy.update();
            democracy.getGuild().getPublicChannel().sendMessage("bienvenue " + ((GuildMemberJoinEvent) event).getUser().getAsMention());
        }
    }

    private void changeToken(MessageReceivedEvent e, ArrayList<String> args) {
        Command command = new Command(e, "changetoken", args);
        tokenCommand = command.changeToken();
    }

    private ArrayList<String> splitter(String args) {
        return new ArrayList<>(Arrays.asList(args.split(" ")));
    }

    private void stop() {
        democracy.getGuild().getPublicChannel().sendMessage("goodbye");
        jda.shutdown(true);
        stop = true;
        System.exit(0);
    }
    
    
//J'ai pas touché à ces trucs, tu en fait ce que tu veux
    public void startVotation(String name) {
        Votation vote = new Votation(name, democracy);
        votations.add(vote);
        TimerTask taskEndVote = new TimerTask() {
            @Override
            public void run() {
                endVote(vote);
            }
        };
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR, DAYS_TO_VOTE);
        Date time = cal.getTime();
        Timer timerUntilEnd = new Timer();
        timerUntilEnd.schedule(taskEndVote, time);
    }

    public boolean endVote(Votation votation) {
        democracy.getGuild().getPublicChannel().sendMessage("The votation has ended.");
        ChannelManager voteChan = votation.getChan();
        boolean result = votation.getResult();
        voteChan.delete();
        return result;
    }

}
