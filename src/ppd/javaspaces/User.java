/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ppd.javaspaces;

import java.util.ArrayList;
import net.jini.core.entry.Entry;

/**
 *
 * @author guilhermecosta
 */
public class User implements Entry {
    public String username, latitude, longitude;

    
    public User(){
    }
    
    public User(String username, String latitude, String longitude){
        this.username = username;
        this.latitude = latitude;
        this.longitude = longitude;
    } 
}
