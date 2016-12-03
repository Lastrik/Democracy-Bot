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
    
    public Command() {
        
    }
    
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
            case "changedaystovote":
                changeDaysToVote();
                break;
            case "help":
                help();
                break;
            case "changetoken":
                changeToken();
                break;
            case "killvotes":
                killvotes();
                break;            
            case "confbase":
                confBase();
                break;
            case "info":
                info();
                break;
            default:
                say("You can only do these commands :\n\n" + listCommands());
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
        } catch (NullPointerException ex) {
            democracy.getGuild().getPublicChannel().sendMessage(sentence);
        }
    }
    
    private void ban() {
        for (User user : users) {
            say("User " + user.getAsMention() + " has been banned.");
            if (e != null) {
                user.getPrivateChannel().sendMessage("Hello, you have been banned from " + democracy.getGuild().getName() + ". You can send a Private message to " + e.getAuthor().getAsMention() + " who banned you in order to know why you were banned and what you can do to get unbanned. Have a nice day :)");
            } else {
                user.getPrivateChannel().sendMessage("Hello, you have been banned from " + democracy.getGuild().getName() + ". That ban was voted by the members.");
            }
            democracy.ban(user, 1);
        }
        
    }
    
    private void unban() {
        for (String userID : args) {
            say("The user " + userID + " has been unbanned");
            democracy.unBan(userID);
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
    
    public void changeToken() {
        say("The token is now " + argsAsString());
        config.setTokenCommand(argsAsString());
    }
    
    private String argsAsString() {
        String res = "";
        for (String arg : args) {
            res += " " + arg;
        }
        return res.replaceFirst(" ", "");
    }
    
    public String listCommands() {
        String list = "```";
        list += "say : Repeat after me\n";
        list += "newtextchannel : create a textchannel\n";
        list += "newvoicechannel : create a new voice channel\n";
        list += "role : Give a role to someone\n";
        list += "unrole : remove a role from someone\n";
        list += "createrole create a role\n";
        list += "deleterole : delete a role\n";
        list += "ban : ban someone\n";
        list += "unban : unban someone (by ID)\n";
        list += "authorize : authorize a role to do a command\n";
        list += "unauthorize : unauthorize a role to do a command\n";
        list += "shconfig : show the config\n";
        list += "shrole : Show the roles and their ID's\n";
        list += "vote : vote for or agaist a referendum\n";
        list += "changedaysvote : Change the time before a referendum ends\n";
        list += "changetoken : Change the token\n";
        list += "info : Get infos about a referendum\n";
        list += "help : Show this\n";
        list += "```";
        return list;
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
        String configString = "Authorizations :";
        for (String commandStr : config.getAuthorization().keySet()) {
            configString += "\n\nThe \"" + commandStr + "\" command can be done by :\n";
            for (String roleID : config.getAuthorization().get(commandStr)) {
                configString += " | " + democracy.getGuild().getRoleById(roleID).getAsMention() + " | ";
            }
        }
        
        configString += "\n\nVotations in progress : \n\n";
        
        for (int votationID : config.getVotations().keySet()) {
            configString += "ID : " + votationID;
            for (Command command : config.getVotations().get(votationID).getSubject().getCommands()) {
                configString += "\n\t" + command.getCommand() + " " + command.getArgsString() + " " + command.getRolesString() + " " + command.getUsersByMention();
            }
        }
        configString += "\n\nNumber of days before the end of vote : " + config.getDaysToVote() + "\n";
        configString += "\n\nToken : " + config.getTokenCommand();
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
    
    private void changeDaysToVote() {
        int days = new Integer(args.get(0));
        config.setDaysToVote(days);
    }
    
    private void vote() {
        if (args.size() == 2) {
            try {
                if (config.getVotations().containsKey(Integer.parseInt(args.get(0)))) {
                    Votation votation = config.getVotations().get(Integer.parseInt(args.get(0)));
                    switch (args.get(1).toLowerCase()) {
                        case "for":
                            votation.voteFor(author);
                            say("Your vote is now counted\n There is " + votation.getVoteFor() + " for and " + votation.getVoteAgainst() + " against.");
                            break;
                        case "against":
                            votation.voteAgainst(author);
                            say("Your vote is now counted\n There is " + votation.getVoteFor() + " for and " + votation.getVoteAgainst() + " against.");
                            
                            break;
                        default:
                            say("What do you mean you are \"" + args.get(1) + "\" this votation ?");
                    }
                } else {
                    say("This ID doesn't exist");
                }
            } catch (NumberFormatException ex) {
                say("The ID must be a number");
            }
        } else {
            say("The command must contain the ID of the Referendum and your vote (\"for\" or \"against\")");
        }
    }
    
    public ArrayList<User> getUsers() {
        return users;
    }
    
    public ArrayList<Role> getRoles() {
        return roles;
    }
    
    private void help() {
        say("This is a democratic Guild.\nYou can make a referendum by sending me the command \"referendum\" and then doing some commands of the command list.\nThe referendum will be created once you did the command \"initiate\"\nYou can vote for a referendum by doing the command \"vote [ID] [for / against]\"\n If the referendum, the commands will be performed.");
        say(listCommands());
    }
    
    private void info() {
        if (args.size() == 1) {
            if (config.getVotations().containsKey(Integer.parseInt(args.get(0)))) {
                String str = "The vote with the ID " + args.get(0) + " contains these commands : \n";
                for (Command command : config.getVotations().get(Integer.parseInt(args.get(0))).getSubject().getCommands()) {
                    str += "\n" + command.getCommand() + " " + command.getArgsString() + command.getRolesasMention() + " " + command.getUsersByMention();
                }
                str += "\n\n This votation end on " + config.getVotations().get(Integer.parseInt(args.get(0))).getSubject().getEnd();
                say(str);
            }
        }
    }
    
    public ArrayList<String> getArgs() {
        return args;
    }
    
    private void killvotes() {
        config.killVote();
    }
    
    private void confBase() {
          ArrayList<String> list = new ArrayList<>();            
            list.add( "newtextchannel") ;               
            list.add( "newvoicechannel") ;              
            list.add( "role") ;                
            list.add( "unrole") ;              
            list.add( "createrole") ;             
            list.add( "deleterole") ;             
            list.add( "ban") ;               
            list.add( "unban") ;               
            list.add( "authorize") ;               
            list.add( "unauthorize") ;              
            list.add( "changedaystovote") ;                            
            list.add( "changetoken") ;                
            list.add( "killvotes") ;             
            list.add( "confbase") ;
            ArrayList<String> rolesID = new ArrayList<>();
            for (Role role : roles) {
            rolesID.add(role.getId());
        }
            for (String string : list) {
                config.getAuthorization().put(string, rolesID);
            }
    }
    
}
