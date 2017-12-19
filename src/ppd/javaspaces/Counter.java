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
public class Counter implements Entry {
    public String count;
    
    public Counter(){
        
    }
    
    public Counter(String count){
        this.count = count;
    }
}
