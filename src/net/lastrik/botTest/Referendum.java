/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.lastrik.botTest;

import java.io.Serializable;
import java.util.ArrayList;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.managers.GuildManager;

/**
 *
 * @author Jordan
 */
public class Referendum implements Serializable{

    private ArrayList<Command> commands;
    private User author;
    private PrivateMessageReceivedEvent p;
    private GuildManager democracy;
    private ArrayList<String> commandsText;
    private Config config;

    public Referendum(Config config, PrivateMessageReceivedEvent p, GuildManager democracy) {
        this.p = p;
        this.commands = new ArrayList<>();
        this.author = p.getAuthor();
        this.democracy = democracy;
        this.config = config;
    }

    public void process() {
        sayMP("New referendum created, type the commands you want to do on \"" + democracy.getGuild().getName() + "\"");
        listCommands();
        listRefSpecialCommands();
        sayMP("To refer to users, use @userID and to refer to roles, use #roleID");
    }

    private void refCommand(String command, ArrayList<String> args, ArrayList<String> usersID, ArrayList<String> rolesID) {
        Command commandC = new Command(democracy, config, command, args, usersID, rolesID);
        if(commandC.check()){
            commands.add(commandC);
        }else {
            refSpeCommand(command, args);
        }
    }

    private void sayMP(String sentence) {
        author.getPrivateChannel().sendMessage(sentence);
    }

    private void sayGuild(String sentence) {
        democracy.getGuild().getPublicChannel().sendMessage(sentence);
    }

    private void listCommands() {
        sayMP("La liste des commandes envoyables en referendum");
    }

    private void listRefSpecialCommands() {
        sayMP("La liste des commandes permettant de modifier le referendum (envoyer, annuler etc.)");
    }

    public void refCommand(String command, ArrayList<String> args) {
        ArrayList<String> usersID = new ArrayList<>();
        ArrayList<String> rolesID = new ArrayList<>();
        ArrayList<String> newArgs = new ArrayList<>(args);
        for (String arg : args) {
            if (arg.startsWith("@")) {
                usersID.add(arg.replaceFirst("@", ""));
                newArgs.remove(arg);
            }
            if (arg.startsWith("#")) {
                rolesID.add(arg.replaceFirst("#", ""));
                newArgs.remove(arg);
            }
        }
        refCommand(command, newArgs, usersID, rolesID);
    }

    private void refSpeCommand(String command, ArrayList<String> args) {
        switch(command){
            case "check":
                check();
                break;
            case "initiate" :
                initiate();
                break;
        }
    }

    private void check() {
        String string = "Your referendum's commands list : \n";
        for (Command command : commands) {
            string += "\n" + command.getCommand() + " " + command.getArgsString() + command.getRolesString() + " " + command.getUsersByMention();
        }
        sayMP(string);
    }

    private void initiate() {
                Votation votation = new Votation(this, democracy);
        String string = "New referendum created on ID "+config.addVotation(votation)+" :\n";
        for (Command command : commands) {
            string += "\n" + command.getCommand() + " " + command.getArgsString() + command.getRolesasMention() + " " + command.getUsersByMention();
        }
        sayGuild(string);
    }

    public User getAuthor() {
        return author;
    }

    public ArrayList<Command> getCommands() {
        return commands;
    }
    
}
