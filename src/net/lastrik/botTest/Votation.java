package net.lastrik.botTest;

import java.io.Serializable;
import java.util.ArrayList;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.managers.ChannelManager;
import net.dv8tion.jda.managers.GuildManager;
import java.util.*;

/**
 *
 * @author Lastrik
 */
public class Votation implements Serializable{

    public final static int PERCENTAGE_FOR_YES = 50;
    public final static int VOTES_MIN = 2;

    private ArrayList<User> haveVoted;
    private int voteFor;
    private int voteAgainst;
    private ChannelManager votations;
    private Referendum subject;

    public Votation(Referendum subject, GuildManager democracy) {
        voteFor = 0;
        voteAgainst = 0;
        haveVoted = new ArrayList<>();
        this.subject = subject;
    }

    public boolean voteFor(User user) {
        boolean result = false;
        if (!haveVoted.contains(user)) {
            voteFor++;
            haveVoted.add(user);
        }
        return result;
    }

    public boolean voteAgainst(User user) {
        boolean result = false;
        if (!haveVoted.contains(user)) {
            voteAgainst++;
            haveVoted.add(user);
        }
        return result;
    }

    public boolean getResult() {
        boolean result = false;
        int totalVotes = voteFor + voteAgainst;
        double percentage = (((double) voteFor) / (voteFor + voteAgainst)) * 100;
        if (percentage >= PERCENTAGE_FOR_YES) {
            result = true;
        }
        return result;
    }
    
    public void endVote() {
        if(getResult())
            for (Command command : subject.getCommands()) {
                command.process();
            }
    }

    public int getVoteFor() {
        return voteFor;
    }

    public int getVoteAgainst() {
        return voteAgainst;
    }

    public ChannelManager getChan() {
        return votations;
    }

    public Referendum getSubject() {
        return subject;
    }

}