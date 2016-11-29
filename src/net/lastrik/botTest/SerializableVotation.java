/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.lastrik.botTest;

import java.util.ArrayList;
import java.util.Arrays;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.managers.GuildManager;

/**
 *
 * @author Jordan
 */
public class SerializableVotation {

    private int id;
    private ArrayList<String> haveVoted;
    private int voteFor;
    private int voteAgainst;

    //Relatif au referendum
    private ArrayList<String> commands;
    private String author;

    public SerializableVotation(int id, Votation votation) {
        this.id = id;
        this.haveVoted = new ArrayList<>();
        for (User user : votation.getHaveVoted()) {
            this.haveVoted.add(user.getId());
        }
        this.voteFor = votation.getVoteFor();
        this.voteAgainst = votation.getVoteAgainst();
        this.commands = new ArrayList<>();
        for (Command command : votation.getSubject().getCommands()) {
            this.commands.add(command.getCommand() + " " + command.getArgsString());
        }
        this.author = votation.getSubject().getAuthor().getId();
    }

    public Votation unserialize(Config config, GuildManager democracy) {
        Referendum referendum = new Referendum(config, author);
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
