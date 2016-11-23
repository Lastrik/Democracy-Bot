package net.lastrik.botTest;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.managers.GuildManager;
import net.dv8tion.jda.managers.RoleManager;

/**
 *
 * @author Jordan
 */
public class Command {

    private HashMap<String, ArrayList<String>> config;
    private MessageReceivedEvent e;
    private String command;
    private ArrayList<String> args;
    private GuildManager democracy;
    private ArrayList<User> users;
    private ArrayList<Role> roles;
    private User author;

    public Command(HashMap<String, ArrayList<String>> config, MessageReceivedEvent e, String command, ArrayList<String> args) {
        this.config = config;
        this.e = e;
        this.command = command;
        this.args = args;
        this.democracy = e.getGuild().getManager();
        this.users = new ArrayList<>(e.getMessage().getMentionedUsers());
        this.roles = new ArrayList<>(e.getMessage().getMentionedRoles());
        this.author = e.getAuthor();
    }

    public void process() {
        switch (command) {
            case "say":
                say(argsAsString());
                break;
            case "newtextchannel":
                newTextChannel();
                break;
            case "role":
                role();
                break;
            case "unrole":
                unrole();
                break;
            case "createrole":
                createRole();
                break;
            case "deleterole":
                deleteRole();
                break;
            case "ban":
                ban();
                break;
            case "unban":
                unban();
                break;
            case "authorize":
                authorize();
                break;
            default:
                say("You can only do these commands :");
                listCommands();
        }

    }

    private void say(String sentence) {
        e.getChannel().sendMessage(sentence);
    }

    private void ban() {
        for (User user : users) {
            if (verifieRole(user, "Judge")) {
                say("You can't ban judges");
            } else {
                say("User " + user.getAsMention() + " has been banned.");
                user.getPrivateChannel().sendMessage("Hello, you have been banned from " + democracy.getGuild().getName() + ". You can send a Private message to " + e.getAuthor().getAsMention() + " who banned you in order to know why you were banned and what you can do to get unbanned. Have a nice day :)");
                democracy.ban(user, 1);
            }
        }

    }

    private void unban() {
        for (User user : users) {
            if (!(democracy.getBans().contains(user))) {
                say(user.getAsMention() + " is not banned");
            } else {
                say("User " + user.getAsMention() + " has been unbanned.");
                democracy.unBan(user);
            }
        }
    }

    private void newTextChannel() {
        String channelName = argsAsString();
        Pattern p = Pattern.compile("[^-a-zA-Z0-9_]");
        boolean hasSpecialChar = p.matcher(channelName).find();
        if (!hasSpecialChar) {
            democracy.getGuild().createTextChannel(channelName);
            say("The text channel " + channelName + " has been created");
        } else {
            say("Your name cannot contain non-aplhanumerical characacters.");
        }
    }

    private void role() {
        for (Role role : roles) {
            for (User user : users) {
                democracy.addRoleToUser(user, role);
                say(user.getAsMention() + " is now a " + role.getAsMention());
            }
        }
        democracy.update();
    }

    private void unrole() {
        for (Role role : roles) {

            for (User user : users) {
                democracy.removeRoleFromUser(user, role);
                say(user.getAsMention() + " is no longer a " + role.getAsMention());
            }

        }
        democracy.update();
    }

    private void createRole() {
        RoleManager manager = democracy.getGuild().createRole();
        String name = argsAsString();
        Pattern p = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
        for (String arg : args) {
            if (p.matcher(arg).matches()) {
                manager.setColor(Color.decode(arg));
                name = name.replace(arg, "");
                break;
            }
        }
        manager.setName(name);
        manager.setMentionable(true);
        manager.setGrouped(true);
        manager.update();
        democracy.update();
        say("The role " + manager.getRole().getAsMention() + " has been created");
    }

    private void deleteRole() {
        for (Role role : roles) {
            say("The role " + role.getAsMention() + " has been deleted");
            role.getManager().delete();
        }
        democracy.update();
    }

    public String changeToken() {
        return argsAsString();
    }

    private String argsAsString() {
        String res = "";
        for (String arg : args) {
            res += " " + arg;
        }
        return res.replaceFirst(" ", "");
    }

    private boolean verifieRole(User user, String roleString) {
        boolean result = false;
        return result;
    }

    private void listCommands() {
        say("The commands list is not available yet");
    }

    private void authorize() {
        ArrayList<String> rolesString = new ArrayList<>();
        for (Role role : roles) {
            rolesString.add(role.getId());
        }
        if(config.containsKey(command)){
            config.get(command).addAll(rolesString);
        }
    }
}
