/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.lastrik.botTest;

import java.util.ArrayList;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.impl.GuildImpl;
import net.dv8tion.jda.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.managers.GuildManager;

/**
 *
 * @author Jordan
 */
class MpCommand {
    private GuildManager democracy;
    private PrivateMessageReceivedEvent p;
    private String command;
    private ArrayList<String> args;
    private ArrayList<User> users;
    private User author;

    public MpCommand(GuildManager democracy, PrivateMessageReceivedEvent p, String command, ArrayList<String> args) {
        this.democracy = democracy;
        this.p = p;
        this.command = command;
        this.args = args;
        this.users = new ArrayList<>(p.getMessage().getMentionedUsers());
        this.author = p.getAuthor();
    }

    public void process() {
        switch (command) {
            case "ping":
                sayMP("pong");
                break;
            case "referendum":
                referendum();
                break;
            default:
                sayMP("You can only do these commands :");
                listCommands();
        }
    }

    private void sayMP(String sentence) {
        author.getPrivateChannel().sendMessage(sentence);
    }

    private void referendum() {
        Referendum referendum = new Referendum(p, democracy);
        referendum.process();
    }

    private void listCommands() {
        sayMP("La liste des commandes envoyables en mp");
    }
}
