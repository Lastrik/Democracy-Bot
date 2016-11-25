/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.lastrik.botTest;

import java.util.ArrayList;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.managers.GuildManager;

/**
 *
 * @author Jordan
 */
public class Referendum {

    private ArrayList<Command> commands;
    private User author;
    private PrivateMessageReceivedEvent p;
    private GuildManager democracy;

    public Referendum(PrivateMessageReceivedEvent p, GuildManager democracy) {
        this.p = p;
        this.commands = new ArrayList<>();
        this.author = p.getAuthor();
        this.democracy = democracy;
    }

    public void process() {
        sayMP("New referendum created, type the commands you want to do on " + democracy.getGuild().getName());
        listCommands();
        listRefSpecialCommands();
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

}
