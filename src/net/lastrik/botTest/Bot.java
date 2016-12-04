package net.lastrik.botTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
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

/**
 *
 * @author Lastrik
 */
public class Bot implements EventListener {

    public final static int DAYS_TO_VOTE = 2;

    private String savePath;
    private GuildManager democracy;
    private JDA jda;
    private boolean stop = false;
    private Config config;
    private long time;

    public Bot(String token) {
        try {
            jda = new JDABuilder().setBotToken(token).setBulkDeleteSplittingEnabled(false).buildBlocking();
            jda.addEventListener(this);
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("Un error append please check your internet connection and the bot token");
            return;
        }
        savePath = "save.ser";
        democracy = new GuildManager(jda.getGuilds().get(0));
        System.out.println("Connected with: " + jda.getSelfInfo().getUsername());
        int i;
        config = new Config();
        load();
        time = System.currentTimeMillis();
        for (Votation votation : config.getVotations().values()) {
            votation.getSubject().setVotation(votation);
        }
        for (Votation votation : config.getVotations().values()) {
            if (votation.getSubject().getCal().getTimeInMillis() < time) {
                votation.getSubject().end();
            }
        }
        System.out.println("The bot is authorized on " + (i = jda.getGuilds().size()) + " server" + (i > 1 ? "s" : ""));
        if (jda.getGuilds().size() > 1) {
            System.err.println("This bot is not made to run on more than on server per . Please create multiple applications for multiple servers");
        }
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
            PrivateMessageReceivedEvent p = (PrivateMessageReceivedEvent) event;
            if (p.getMessage().getContent().startsWith(config.getTokenCommand())) {
                ArrayList<String> splittedCommand = splitter(p.getMessage().getContent());
                String commandNoArgs = splittedCommand.get(0).replaceFirst(config.getTokenCommand(), "");
                splittedCommand.remove(0);
                ArrayList<String> args = splittedCommand;
                MpCommand mpCommand = new MpCommand(democracy, p, commandNoArgs, args, config);
                mpCommand.process();
            }
        }

        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent e = (MessageReceivedEvent) event;
            if (e.getMessage().getContent().startsWith(config.getTokenCommand()) && (!e.isPrivate())) {
                ArrayList<String> splittedCommand = splitter(e.getMessage().getContent());
                String commandNoArgs = splittedCommand.get(0).replaceFirst(config.getTokenCommand(), "");
                splittedCommand.remove(0);
                ArrayList<String> args = splittedCommand;
                if (!e.getAuthor().isBot()) {
                    if (authorized(e, commandNoArgs)) {
                        if (System.currentTimeMillis() - time > 1000000) {
                            silentSave();
                            time = System.currentTimeMillis();
                        }
                        switch (commandNoArgs) {
                            //Quelques commandes qui touchent directement au bot
                            case "stop":
                                stop();
                                break;
                            case "save":
                                save();
                                break;
                            case "reload":
                                reload(e);
                                break;
                            default:
                                Command command = new Command(config, e, commandNoArgs, args);
                                command.process();
                        }
                    } else {
                        e.getChannel().sendMessage("you're not authorized to do this command");
                    }
                }
            }
        }
        if (event instanceof GuildMemberJoinEvent) {
            User user = ((GuildMemberJoinEvent) event).getUser();
            democracy.getGuild().getPublicChannel().sendMessage("Welcome " + ((GuildMemberJoinEvent) event).getUser().getAsMention());
        }
    }

    private ArrayList<String> splitter(String args) {
        return new ArrayList<>(Arrays.asList(args.split(" ")));
    }

    private void stop() {
        save();
        democracy.getGuild().getPublicChannel().sendMessage("goodbye");
        jda.shutdown(true);
        stop = true;
        System.exit(0);
    }

//J'ai pas touché à ces trucs, tu en fait ce que tu veux
//    public void startVotation(String name) {
//        Votation vote = new Votation(name, democracy);
//        votations.add(vote);
//        TimerTask taskEndVote = new TimerTask() {
//            @Override
//            public void run() {
//                endVote(vote);
//            }
//        };
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(new Date());
//        cal.add(Calendar.DAY_OF_YEAR, DAYS_TO_VOTE);
//        Date time = cal.getTime();
//        Timer timerUntilEnd = new Timer();
//        timerUntilEnd.schedule(taskEndVote, time);
//    }
//
//    public boolean endVote(Votation votation) {
//        democracy.getGuild().getPublicChannel().sendMessage("The votation has ended.");
//        ChannelManager voteChan = votation.getChan();
//        boolean result = votation.getResult();
//        voteChan.delete();
//        return result;
//    }
    public void save() {
        try {
            config.serialize(democracy);
            FileOutputStream fos = new FileOutputStream(savePath);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(config);
            oos.close();
            fos.close();
            democracy.getGuild().getPublicChannel().sendMessage("Config saved");
            load();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void load() {
        try {
            System.out.println("Loading save file");
            FileInputStream fis = new FileInputStream(savePath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            config = (Config) ois.readObject();
            ois.close();
            fis.close();
            config.unserialize(democracy);
            System.out.println("Save file loaded");
        } catch (IOException ioe) {
            System.out.println("No save file found");
            return;
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
            return;
        }
    }

    private boolean authorized(MessageReceivedEvent e, String commandNoArgs) {
        boolean res = false;
        if (config.getAuthorization().containsKey(commandNoArgs)) {
            ArrayList<Role> roles = new ArrayList<>(democracy.getGuild().getRolesForUser(e.getAuthor()));
            for (Role role : roles) {
                if (config.getAuthorization().get(commandNoArgs).contains(role.getId())) {
                    res = true;
                    break;
                }
            }
        } else {
            res = true;
        }
        return res;
    }

    private void reload(MessageReceivedEvent e) {
        config = new Config();
        save();
        e.getChannel().sendMessage("Config reloaded");
    }

    private void silentSave() {
        try {
            config.serialize(democracy);
            FileOutputStream fos = new FileOutputStream(savePath);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(config);
            oos.close();
            fos.close();
            load();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
