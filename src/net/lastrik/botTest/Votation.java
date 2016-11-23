package net.lastrik.botTest;

import java.util.ArrayList;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.managers.ChannelManager;
import net.dv8tion.jda.managers.GuildManager;
import java.util.*;

/**
 *
 * @author Lastrik
 */
public class Votation {

    public final static int PERCENTAGE_FOR_YES = 50;
    public final static int VOTES_MIN = 2;

    private ArrayList<User> haveVoted;
    private int voteFor;
    private int voteAgainst;
    private ChannelManager votations;
    private String subject;

    public Votation(String subject, GuildManager democracy) {
        voteFor = 0;
        voteAgainst = 0;
        democracy.getGuild().getPublicChannel().sendMessage("A new votation has begun. Please go and vote in the votation channel. If you vote in this channel, your votes will not be accounted.");
        democracy.getGuild().getPublicChannel().sendMessage("The subject of the vote is : " + subject);
        haveVoted = new ArrayList<>();
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

    public String getSubject() {
        return subject;
    }
}