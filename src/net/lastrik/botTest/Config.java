/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.lastrik.botTest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Jordan
 */
public class Config implements Serializable{
    
   private HashMap<String, ArrayList<String>> authorization ;
   private HashMap<String, Referendum> referendums;
   private ArrayList<Votation> votations;

    public Config() {
        this.authorization = new HashMap<>();
        this.referendums = new HashMap<>();
        this.votations = new ArrayList<>();
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

    public ArrayList<Votation> getVotations() {
        return votations;
    }

}
