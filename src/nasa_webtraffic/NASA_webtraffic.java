/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nasa_webtraffic;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author ss2688
 */
public class NASA_webtraffic {

    /**
     * @param args the command line arguments
     * 
     */
    Map<String, Integer> input1 = new HashMap<String, Integer>();
    Map<String, Integer> input2 = new HashMap<String, Integer>();
    //ArrayList<String[]> input3 = new ArrayList<String[]>();
    Map<String, Failedlogin> input4 = new HashMap<String, Failedlogin>();
    ArrayList<String> output4 = new ArrayList<String>();
    Map<String, Integer> input3 = new TreeMap<String, Integer>();
    LinkedList<Linkedlist1> ml = new LinkedList<Linkedlist1>();
    Map<String, Integer> output3 = new TreeMap<String, Integer>();
    
    public void readfile()
    {
        try
        {
            String filename = "log.txt";
            FileReader file_to_read = new FileReader(filename);
            BufferedReader bf = new BufferedReader(file_to_read);
            String aLine;            
            
            //Getting each line from input text file 
            while (( aLine = bf.readLine() ) != null ) 
            {
                //create input for feature1 - top 10 most active host/IP addresses
                String[] words=aLine.split("- -");
                String ipaddr = null;
                String req_part = null;
                int replycode = 0;
                
                //parse log file and get the IP address. Create an input in the
                //form of Hash Map; Key : Ip address Value : count of IP
                
                ipaddr = words[0];
                if (input1.isEmpty())
                {
                    input1.put(ipaddr,1);
                }
                else
                {
                    if (input1.get(ipaddr) != null)
                    {
                        input1.put(ipaddr, input1.get(ipaddr) + 1);
                    }
                    else
                    {
                        input1.put(ipaddr, 1);
                    }
                }
                            
                //create input for feature2 - 10 resources - most bandwidth
                req_part = words[1];
                
                //Parse log file to get the resourse name
                String req_split = req_part.substring(req_part.indexOf("GET")+3,
                        req_part.length());
                String[] reqwords=req_split.split(" ");
                
                int bytessent = 0;
                if (reqwords[reqwords.length-1].equals("-"))
                {
                    bytessent = 0;
                }
                else
                {
                    bytessent = Integer.parseInt(reqwords[reqwords.length-1]);
                }
                
                //Create an input in the form of Hash Map; Key : resource Value : Bytes
                
                if (reqwords[1].equals("/") == false)
                {
                    input2.put(reqwords[1],bytessent);            
                }
                
                //create input for feature4 - Failedlogin attempt pattern
                
                //parse log file to get timestamp
                String timest = req_part.substring(req_part.indexOf("[") + 1,
                        req_part.indexOf(']'));
                replycode = Integer.parseInt(reqwords[reqwords.length-2]);
                int counter = 0;
                
                SimpleDateFormat sdf = new SimpleDateFormat ("dd/MMM/yyyy:HH:mm:ss z");
                
                //create input in the form of Hash Map; Key : IP addr Value : Object of
                //Failed Login class which contains timestamp and counter
                if(input4.get(ipaddr) != null)
                {
                    Failedlogin oldfl = input4.get(ipaddr);
                    String olddate = oldfl.timestamp;
                    int ctr = oldfl.counter;    
                    if(ctr == 3)
                    {
                        Date d1 = sdf.parse(olddate);
                        Date d2 = sdf.parse(timest);
                        long diff = d2.getTime() - d1.getTime();

                        long diffMinutes = diff / (60 * 1000) % 60;
                        if(diffMinutes <= 5)
                        {
                            output4.add(aLine);
                            //System.out.println("Blocked IP : " + aLine);
                        }
                        else
                            input4.remove(ipaddr);
                    } //end ctr = 3
                    else
                    {
                        if(replycode == 401)
                        {
                            Date d1 = sdf.parse(olddate);
                            Date d2 = sdf.parse(timest);
                            long diff = d2.getTime() - d1.getTime();
                            long diffSeconds = diff / 1000 % 60;
                            int newctr = ctr + 1;
                        
                            if (diffSeconds <= 20)
                            {
                                if (newctr == 3)
                                {
                                    Failedlogin newfl = new Failedlogin(timest,newctr);
                                    input4.put(ipaddr,newfl);
                                }
                                else
                                {
                                    Failedlogin newfl = new Failedlogin(olddate,newctr);
                                    input4.put(ipaddr,newfl);
                                }
                                
                            }
                            else 
                                input4.remove(ipaddr);
                        }
                        else //not 401
                            input4.remove(ipaddr);
                    }
                }
                else
                {
                    Failedlogin newfl = new Failedlogin(timest,counter+1);
                    input4.put(ipaddr,newfl);
                }
                
                //feature 3
                
                if (input3.isEmpty())
                {
                    input3.put(timest,1);
                }
                else
                {
                    if (input3.get(timest) != null)
                    {
                        input3.put(timest, input3.get(timest) + 1);
                    }
                    else
                    {
                        input3.put(timest, 1);
                    }
                }
            } //end of log file
           /*
            Set set2 = input3.entrySet();
            Iterator iterator2 = set2.iterator();
        
            while(iterator2.hasNext()) 
            {
                Map.Entry mentry2 = (Map.Entry)iterator2.next();
                System.out.print("Key is: "+mentry2.getKey() + " & Value is: ");
                System.out.println(mentry2.getValue());
            }
            */
           
            perform_feature1();
            perform_feature2();
            perform_feature3();
            perform_feature4();
            
        }
        catch (Exception ex)
        {
            System.out.println("error reading " + ex.getMessage());
        }
    }
   
    public void perform_feature1()
    {
        try
        {
            //sort input1 hashmap by count of IP address and display top 10 to output
            String ofilename = "hosts.txt";
            File file = new File(ofilename);

            // if file doesn't exist,  create one
	    if (!file.exists())
            {
		file.createNewFile();
	    }
            
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
	    BufferedWriter bw = new BufferedWriter(fw);
        
            
            Map<String, Integer> sortedMap = sortByValue(input1);
            Set set2 = sortedMap.entrySet();
            Iterator iterator2 = set2.iterator();
            int count = 0;
            String fileop = null;
            while(iterator2.hasNext()) 
            {
                if(count <= 9)
                {
                    Map.Entry mentry2 = (Map.Entry)iterator2.next();
                    fileop =  mentry2.getKey() + "," + mentry2.getValue();
                    /*
                    System.out.print("Key is: "+mentry2.getKey() + " & Value is: ");
                    System.out.println(mentry2.getValue());
                    System.out.println("count : " + count);
                    */
                    count++;
                    bw.write(fileop);
                    bw.newLine();
                    bw.flush();
                    fileop = "";
                }
                else
                    break;
            }
            bw.close();
        }
        catch(Exception e)
        {
            System.out.println("Error writing hosts.txt: " + e.getMessage());
        }
    }
    
    public void perform_feature2()
    {
        try
        {
            //sort input2 hashmap by resource bytes and write top 10 to output
            String ofilename = "resources.txt";
            File file = new File(ofilename);

            // if file doesn't exist,  create one
	    if (!file.exists())
            {
		file.createNewFile();
	    }
            
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
	    BufferedWriter bw = new BufferedWriter(fw);
        
            Map<String, Integer> sortedMap = sortByValue(input2);
            Set set2 = sortedMap.entrySet();
            Iterator iterator2 = set2.iterator();
            int count = 0;
            String fileop = null;
            
            while(iterator2.hasNext()) 
            {
                if(count <= 9)
                {
                    Map.Entry mentry2 = (Map.Entry)iterator2.next();
                    fileop =  mentry2.getKey() + "";
                    count++;
                    bw.write(fileop);
                    bw.newLine();
                    bw.flush();
                    fileop = "";
                }
                else
                    break;
            }
            bw.close();
        }
        catch(Exception e)
        {
            System.out.println("Error writing resources.txt : " + e.getMessage());
        }
    }
    
    public void perform_feature4()
    {
        try
        {
            //write all the failed login attemps to output file
            String ofilename = "blocked.txt";
            File file = new File(ofilename);

            // if file doesn't exist,  create one
	    if (!file.exists())
            {
		file.createNewFile();
	    }
            
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
	    BufferedWriter bw = new BufferedWriter(fw);
            for(int i = 0; i< output4.size(); i++)
            {
                bw.write(output4.get(i));
                bw.newLine();
                bw.flush();
            }
            bw.close();
        }
        catch(Exception e)
        {
            System.out.println("Error writing blocked.txt : " + e.getMessage());
        }
    } 
    
    public void perform_feature3()
    {
        try
        {
            String ofilename = "hours.txt";
            File file = new File(ofilename);

            // if file doesn't exist,  create one
	    if (!file.exists())
            {
		file.createNewFile();
	    }
            
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
	    BufferedWriter bw = new BufferedWriter(fw);
            Set<String> keys = input3.keySet();
            SimpleDateFormat sdf = new SimpleDateFormat ("dd/MMM/yyyy:HH:mm:ss z");
            Date d1 = null;
            Date readdate = null;
            Date startdate = null;
            String newdate = null;
            int count_ip = 0;
            String startinterval = null;
            
            for (Iterator i = keys.iterator(); i.hasNext();) 
            {
                String key = (String) i.next();
                Integer value = (Integer) input3.get(key);
                Linkedlist1 ml1 = new Linkedlist1(key, value);
                readdate = sdf.parse(key);
                boolean inputadded = false;
                if(startdate == null) //if first record
                {
                    ml.add(ml1);
                    count_ip = count_ip + value;
                    startinterval = key;
                    startdate = readdate;
                }
                else
                {
                    while(!inputadded)
                    {
                        long diff = readdate.getTime() - startdate.getTime();
                        long diffSeconds = diff / 1000 % 60;
                        long diffMinutes = diff / (60 * 1000) % 60;
                        if(diffSeconds <= 10)
                        //if(diffMinutes <= 60)
                        {
                            ml.add(ml1);
                            inputadded = true;
                            count_ip = count_ip + value;
                        }
                        else
                        {
                            Linkedlist1 templist = ml.getFirst();
                            int removecount = templist.timecount;
                            ml.removeFirst();
                            templist = ml.getFirst();
                            
                            output3.put(startinterval, count_ip);
                            startinterval = templist.listtime;
                            count_ip = count_ip - removecount;
                            startdate = sdf.parse(startinterval);
                        }
                    }
                }
            }
            output3.put(startinterval, count_ip);
            
            Map<String, Integer> sortedMap = sortByValue(output3);
            Set set2 = sortedMap.entrySet();
            Iterator iterator2 = set2.iterator();
            int count = 0;
            String fileop = null;
            while(iterator2.hasNext()) 
            {
                if(count <= 9)
                {
                    Map.Entry mentry2 = (Map.Entry)iterator2.next();
                    fileop =  mentry2.getKey() + "," + mentry2.getValue();
                    count++;
                    bw.write(fileop);
                    bw.newLine();
                    bw.flush();
                    fileop = "";
                    count++;
                }
                else
                    break;
            }
            bw.close();
        
        }
        catch (Exception ex)
        {
            System.out.println("Error : " + ex.getMessage());
        }
    }
    
    public static Map<String, Integer> sortByValue(Map<String, Integer> unsortMap) 
    {

        // Sort Hashmap by value 
        List<Map.Entry<String, Integer>> list =
                new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>()
        {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) 
            {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });


        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list) 
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
    
    public static void main(String[] args) {
        // TODO code application logic here
        NASA_webtraffic nw = new NASA_webtraffic();
        nw.readfile();
    }
    
}
