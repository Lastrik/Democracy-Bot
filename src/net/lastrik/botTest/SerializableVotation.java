/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.lastrik.botTest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.managers.GuildManager;

/**
 *
 * @author Jordan
 */
public class SerializableVotation implements Serializable{

    private int id;
    private ArrayList<String> haveVoted;
    private int voteFor;
    private int voteAgainst;

    //Relatif au referendum
    private ArrayList<String> commands;
    private String author;
    private Calendar cal;

    public SerializableVotation(int id, Votation votation) {
        this.cal = votation.getSubject().getCal();
        this.id = id;
        this.haveVoted = new ArrayList<>();
        for (User user : votation.getHaveVoted()) {
            this.haveVoted.add(user.getId());
        }
        this.voteFor = votation.getVoteFor();
        this.voteAgainst = votation.getVoteAgainst();
        this.commands = new ArrayList<>();
        for (Command command : votation.getSubject().getCommands()) {
            String args = "";
            for (Object arg : command.getArgs()) {
                args += " " + arg;
            }
            for (User user : command.getUsers()) {
                args += " @" + user.getId();
            }
            for (Role role : command.getRoles()) {
                args += " #" + role.getId();
            }
            this.commands.add(command.getCommand() + args);
        }
        this.author = votation.getSubject().getAuthor().getId();
    }

    public Votation unserialize(Config config, GuildManager democracy) {
        Referendum referendum = new Referendum(config, author, democracy, cal);
        for (String command : commands) {
            ArrayList<String> splittedCommand = new ArrayList<>(Arrays.asList(command.split(" ")));
            String commandNoArgs = splittedCommand.get(0);
            splittedCommand.remove(0);
            ArrayList<String> args = splittedCommand;
            referendum.refCommand(commandNoArgs, args);
        }
        ArrayList<User> haveVotedUsers = new ArrayList<>();
        for (String id : haveVoted) {
            haveVotedUsers.add(democracy.getGuild().getUserById(id));
        }
        return new Votation(haveVotedUsers, voteFor, voteAgainst, referendum);
    }

    public int getId() {
        return id;
    }

}
