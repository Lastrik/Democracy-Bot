/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.lastrik.botTest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import net.dv8tion.jda.managers.GuildManager;

/**
 *
 * @author Jordan
 */
public class Config implements Serializable {

    private HashMap<String, ArrayList<String>> authorization;
    private HashMap<String, Referendum> referendums;
    private HashMap<String, Long> usersNoReferendums; //utilisateurs qui ne peuvent pas faire de referendums (Car déjà en cours) avec temps de leur dernier referendum
    private HashMap<Integer, Votation> votations;
    private ArrayList<SerializableVotation> serializedVotations;

    public Config() {
        this.authorization = new HashMap<>();
        this.referendums = new HashMap<>();
        this.usersNoReferendums = new HashMap<>();
        this.votations = new HashMap<>();
        this.serializedVotations = new ArrayList<>();
    }

    public HashMap<String, ArrayList<String>> getAuthorization() {
        return authorization;
    }

    public void setAuthorization(HashMap<String, ArrayList<String>> authorization) {
        this.authorization = authorization;
    }

    public HashMap<String, Referendum> getReferendums() {
        return referendums;
    }

    public HashMap<Integer, Votation> getVotations() {
        return votations;
    }

    public int addVotation(Votation votation) {
        int i = 0;
        while (votations.keySet().contains(i)) {
            i++;
        }
        votations.put(i, votation);
        return i;
    }

    public void serialize(GuildManager democracy) {
        for (String userID : referendums.keySet()) {
            democracy.getGuild().getUserById(userID).getPrivateChannel().sendMessage("This bot shut down while you had an ongoing referendum. So you lost it, sorry");
        }
        referendums = new HashMap<>();
        for (Integer id : votations.keySet()) {
            serializedVotations.add(new SerializableVotation(id, votations.get(id)));
        }
        votations = new HashMap<>();
    }

    public void unserialize(GuildManager democracy) {
        for (SerializableVotation serializedVotation : serializedVotations) {
            votations.put(serializedVotation.getId(), serializedVotation.unserialize(this, democracy));
        }
    }

    public HashMap<String, Long> getUsersNoReferendums() {
        return usersNoReferendums;
    }
}
