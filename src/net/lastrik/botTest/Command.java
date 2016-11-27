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

    private Config config;
    private MessageReceivedEvent e;
    private String command;
    private ArrayList<String> args;
    private GuildManager democracy;
    private ArrayList<User> users;
    private ArrayList<Role> roles;
    private User author;

    public Command(Config config, MessageReceivedEvent e, String command, ArrayList<String> args) {
        this.config = config;
        this.e = e;
        this.command = command;
        this.args = args;
        this.democracy = e.getGuild().getManager();
        this.users = new ArrayList<>(e.getMessage().getMentionedUsers());
        this.roles = new ArrayList<>(e.getMessage().getMentionedRoles());
        this.author = e.getAuthor();
    }

    public Command(GuildManager democracy, Config config, String command, ArrayList<String> args, ArrayList<String> usersID, ArrayList<String> rolesID) {
        this.config = config;
        this.e = null;
        this.command = command;
        this.args = args;
        this.democracy = democracy;
        this.users = new ArrayList<>();
        this.roles = new ArrayList<>();
        for (String roleID : rolesID) {
            roles.add(democracy.getGuild().getRoleById(roleID));
        }
        for (String userID : usersID) {
            users.add(democracy.getGuild().getUserById(userID));
        }
        this.author = null;
    }

    public void process() {
        switch (command) {
            case "say":
                say(argsAsString());
                break;
            case "newtextchannel":
                newTextChannel();
                break;
            case "newvoicechannel":
                newVoiceChannel();
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
            case "unauthorize":
                unauthorize();
                break;
            case "shconfig":
                shconfig();
                break;
            case "shroles":
                shroles();
                break;
            case "vote":
                vote();
                break;
            case "end":
                end();
                break;
            default:
                say("You can only do these commands :");
                listCommands();
        }

    }

    public boolean check() {
        Pattern p;
        boolean res = false;
        switch (command) {
            case "newtextchannel":
                p = Pattern.compile("[^-a-zA-Z0-9_]");
                res = !p.matcher(argsAsString()).find();
                break;
            case "newvoicechannel":
                p = Pattern.compile("[^-a-zA-Z0-9_ ]");
                res = !p.matcher(argsAsString()).find();
                break;
            case "role":
                res = !(roles.isEmpty() && users.isEmpty());
                break;
            case "unrole":
                res = !(roles.isEmpty() && users.isEmpty());
                break;
            case "createrole":
                res = !(args.isEmpty());
                break;
            case "deleterole":
                res = !(roles.isEmpty());
                break;
            case "ban":
                res = !(users.isEmpty());
                break;
            case "unban":
                res = !(users.isEmpty());
                break;
            case "authorize":
                res = !(roles.isEmpty() && args.isEmpty());
                break;
            case "unauthorize":
                res = !(roles.isEmpty() && args.isEmpty());
                break;
        }
        return res;
    }

    private void say(String sentence) {
        try {
        if (!("".equals(sentence))) {
            e.getChannel().sendMessage(sentence);
        }
        } catch (NullPointerException e){
            democracy.getGuild().getPublicChannel().sendMessage(sentence);
        }
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
        if (check()) {
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
            say(role.getAsMention() + " are now authorized to do the \"" + args.get(0) + "\" command");
        }
        if (config.getAuthorization().containsKey(args.get(0))) {
            config.getAuthorization().get(args.get(0)).addAll(rolesString);
            if (args.contains("@everyone")) {
                config.getAuthorization().remove(args.get(0));
                say("@everyone is now authorized to do the \"" + args.get(0) + "\" command");
            }
        } else {
            config.getAuthorization().put(args.get(0), rolesString);
        }
    }

    private void unauthorize() {
        if (config.getAuthorization().containsKey(args.get(0))) {
            for (Role role : roles) {
                if (config.getAuthorization().get(args.get(0)).contains(role.getId())) {
                    say(role.getAsMention() + " are no longer authorized to do the \"" + args.get(0) + "\" command");
                    config.getAuthorization().get(args.get(0)).remove(role.getId());
                }
            }
        } else {
            say("There is no authorization for this command");
        }
    }

    private void shconfig() {
        String configString = "Config : \n";
        for (String commandStr : config.getAuthorization().keySet()) {
            configString += "\n\nThe \"" + commandStr + "\" command can be done by :\n";
            for (String roleID : config.getAuthorization().get(commandStr)) {
                configString += " | " + democracy.getGuild().getRoleById(roleID).getAsMention() + " | ";
            }
        }
        say(configString);
    }

    private void newVoiceChannel() {
        String channelName = argsAsString();
        if (check()) {
            democracy.getGuild().createVoiceChannel(channelName);
            say("The voice channel " + channelName + " has been created");
        } else {
            say("Your name cannot contain non-aplhanumerical characacters.");
        }
    }

    public String getCommand() {
        return command;
    }

    public String getArgsString() {
        String string = "";
        for (String arg : args) {
            string += arg;
        }
        return string;
    }

    public String getUsersByMention() {
        String string = "";
        for (User user : users) {
            string += user.getAsMention();
        }
        return string;
    }

    public String getRolesString() {
        String string = "";
        for (Role role : roles) {
            string += "@" + role.getName();
        }
        return string;
    }

    public String getRolesasMention() {
        String string = "";
        for (Role role : roles) {
            string += role.getAsMention();
        }
        return string;
    }

    private void shroles() {
        String string = "Roles :\n";
        for (Role role : democracy.getGuild().getRoles()) {
            string += "\n" + role.getAsMention() + " : " + role.getId();
        }
        say(string);
    }

    private void vote() {
        if (args.size() == 2) {
            try {
              Votation votation = config.getVotations().get(Integer.parseInt(args.get(0)));
              switch(args.get(1).toLowerCase()){
                  case "for":
                      votation.voteFor(author);
                      break;
                  case "against":
                      votation.voteAgainst(author);
                      break;
                  default :
                      say("What do you mean you are \""+args.get(1)+"\" this votation ?");
              }
            } catch (NumberFormatException | IndexOutOfBoundsException ex) {
                say("There is no votation with this ID");
            }
        }
    }

    private void end() {
        try {
              Votation votation = config.getVotations().get(Integer.parseInt(args.get(0)));
              int against = votation.getVoteAgainst();
              int fors = votation.getVoteFor();
              say("There is "+fors+" votes for and "+ against + " votes against");            
              if(votation.getResult()){
                  say("The referednum is accepted !");
              } else {
                  say("The referendum is refused !");
              }
              config.getReferendums().remove(votation.getSubject().getAuthor());
              config.getVotations().remove(votation);
              votation.endVote();
            } catch (NumberFormatException | IndexOutOfBoundsException ex) {
                say("There is no votation with this ID");
            }
    }
}
