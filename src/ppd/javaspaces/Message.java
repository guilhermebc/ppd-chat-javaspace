/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ppd.javaspaces;

import net.jini.core.entry.Entry;

/**
 *
 * @author guilhermecosta
 */

public class Message implements Entry {
    
    public String from, content, receiver;
    
    public Message(){
    
    }
    
    public Message(String from, String content, String to){
        this.from = from;
        this.content = content;
        this.receiver = to;
    }
}
