/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nasa_webtraffic;

/**
 *
 * @author ss2688
 */
public class Failedlogin 
{
    //String ipaddress;  
    String timestamp;  
    int counter;  
    Failedlogin(String timestamp,int counter)
    {  
        this.timestamp=timestamp;  
        this.counter=counter;  
    }  
}
