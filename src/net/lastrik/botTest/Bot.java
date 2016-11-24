package net.lastrik.botTest;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import static jdk.nashorn.internal.objects.NativeArray.map;

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
    private HashMap<String, ArrayList<String>> config;

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
        config = new HashMap<>();
        charge();
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
            PrivateMessageReceivedEvent p = (PrivateMessageReceivedEvent) event;
            if (p.getMessage().getContent().startsWith(tokenCommand)) {
                ArrayList<String> splittedCommand = splitter(p.getMessage().getContent());
                String commandNoArgs = splittedCommand.get(0).replaceFirst(tokenCommand, "");
                splittedCommand.remove(0);
                ArrayList<String> args = splittedCommand;
                MpCommand mpCommand = new MpCommand(democracy, p, commandNoArgs, args);
                mpCommand.process();
            }
        }

        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent e = (MessageReceivedEvent) event;
            if (e.getMessage().getContent().startsWith(tokenCommand) && (!e.isPrivate())) {
                ArrayList<String> splittedCommand = splitter(e.getMessage().getContent());
                String commandNoArgs = splittedCommand.get(0).replaceFirst(tokenCommand, "");
                splittedCommand.remove(0);
                ArrayList<String> args = splittedCommand;
                if (authorized(e, commandNoArgs)) {
                    switch (commandNoArgs) {
                        //Quelques commandes qui touchent directement au bot
                        case "stop":
                            stop();
                            break;
                        case "changetoken":
                            changeToken(e, args);
                            break;
                        case "save":
                            save();
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
        if (event instanceof GuildMemberJoinEvent) {
            User user = ((GuildMemberJoinEvent) event).getUser();
            democracy = democracy.addRoleToUser(user, democracy.getGuild().getRolesByName("Citizen").toArray(new Role[0]));
            democracy.update();
            democracy.getGuild().getPublicChannel().sendMessage("bienvenue " + ((GuildMemberJoinEvent) event).getUser().getAsMention());
        }
    }

    private void changeToken(MessageReceivedEvent e, ArrayList<String> args) {
        Command command = new Command(config, e, "changetoken", args);
        tokenCommand = command.changeToken();
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

    public void save() {
        try {
            FileOutputStream fos = new FileOutputStream("save.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(config);
            oos.close();
            fos.close();
            democracy.getGuild().getPublicChannel().sendMessage("Config saved");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void charge() {
        try {
            FileInputStream fis = new FileInputStream("save.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            config = (HashMap) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
            return;
        }
        System.out.println("Config charging...");
        // Display content using Iterator
        Set set = config.entrySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry mentry = (Map.Entry) iterator.next();
            System.out.println("key: " + mentry.getKey() + " & Value: ");
            System.out.println(mentry.getValue());

        }
    }

    private boolean authorized(MessageReceivedEvent e, String commandNoArgs) {
        boolean res = false;
        if (config.containsKey(commandNoArgs)) {
            ArrayList<Role> roles = new ArrayList<>(democracy.getGuild().getRolesForUser(e.getAuthor()));
            for (Role role : roles) {
                if (config.get(commandNoArgs).contains(role.getId())) {
                    res = true;
                    break;
                }
            }
        } else {
            res = true;
        }
        return res;
    }
}
