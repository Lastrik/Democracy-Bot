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
public class Votation implements Serializable {

    public final static int PERCENTAGE_FOR_YES = 50;
    public final static int VOTES_MIN = 2;

    private ArrayList<User> haveVoted;
    private int voteFor;
    private int voteAgainst;
    private Referendum subject;

    public Votation(Referendum subject) {
        voteFor = 0;
        voteAgainst = 0;
        haveVoted = new ArrayList<>();
        this.subject = subject;
    }

    public Votation(ArrayList<User> haveVoted, int voteFor, int voteAgainst, Referendum subject) {
        this.haveVoted = haveVoted;
        this.voteFor = voteFor;
        this.voteAgainst = voteAgainst;
        this.subject = subject;
    }

    public String voteFor(User user) {
        String result = "You already voted for this referendum\n";
        if (!haveVoted.contains(user)) {
            voteFor++;
            haveVoted.add(user);
            result = "Your vote is now counted\n";
        }
        return result;
    }

    public String voteAgainst(User user) {
        String result = "You already voted for this referendum\n";
        if (!haveVoted.contains(user)) {
            voteAgainst++;
            haveVoted.add(user);
            result = "Your vote is now counted\n";
        }
        return result;
    }

    public boolean getResult() {
        boolean result = false;
        double percentage = (((double) voteFor) / (voteFor + voteAgainst)) * 100;
        if (percentage >= PERCENTAGE_FOR_YES) {
            result = true;
        }
        return result;
    }

    public void endVote() {
        if (getResult()) {
            for (Command command : subject.getCommands()) {
                command.process();
            }
        }
    }

    public int getVoteFor() {
        return voteFor;
    }

    public int getVoteAgainst() {
        return voteAgainst;
    }

    public Referendum getSubject() {
        return subject;
    }

    public ArrayList<User> getHaveVoted() {
        return haveVoted;
    }

}
