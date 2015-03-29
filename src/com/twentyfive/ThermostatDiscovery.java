package com.twentyfive;


import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ThermostatDiscovery {

    private final static String DISCOVER_MESSAGE = "TYPE: WM-DISCOVER\r\n" +
            "VERSION: 1.0\r\n\r\n" +
            "services:com.marvell.wm.system*\r\n\r\n";

    private final static String     ADDRESS     = "239.255.255.250";
    private final static int        PORT        = 1900;

    private MulticastSocket         Sock        = null;
    private InetAddress             InetAddr    = null;

    private Map<String, Boolean>    Addresses   = null;


    public ThermostatDiscovery() {
        Addresses = new HashMap<String, Boolean>();
    }


    //Shutdown and clean up
    public void Close()
    {
        CleanUpSocket();
    }

    //Did we find any addresses
    public boolean FoundAddress()
    {
        return !Addresses.isEmpty();
    }

    //Returns a list of all the addresses found
    public List<String> GetAddresses()
    {
        List<String> retVal = new ArrayList();
        retVal.addAll( Addresses.keySet() );
        return retVal;
    }

    //Sets up the socket and joins the multicast group
    private void ConfigureSocket() throws Exception
    {

        try
        {
            Sock = new MulticastSocket(PORT);

            Sock.setReuseAddress(true);
            Sock.joinGroup(InetAddr);
            Sock.setLoopbackMode(true);
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    //Leaves multicast group and closes the socket
    private void CleanUpSocket()
    {
        if( Sock != null )
        {
            try
            {
                Sock.leaveGroup(InetAddr);
                Sock.close();
            }
            catch(Exception e)
            {
            }

        }
    }

    //Looks for a string LOCATION: in the return message, if found get the url
    // and add it to the list of found addresses
    private void ProcessMessage(String message)
    {
        final String  LOCATION        = "LOCATION:";
        final int     LOCATION_LEN    = LOCATION.length();

        if( message.contains(LOCATION) )
        {
            String location = message.substring( message.indexOf(LOCATION) + LOCATION_LEN, message.length() );
            //Replace with Log
            //System.out.println("Location: " + location);
            Addresses.put(location.trim(), true );
        }
    }

    //Reads messages off the UDP socket until a timeout is reached.
    private void HandleMessages()
    {
        byte[]          buff    = new byte[4096];
        DatagramPacket  msg     = new DatagramPacket(buff, buff.length);

        try
        {

            while(true)
            {
                Sock.receive(msg);
                InetAddress remoteAddr = msg.getAddress();
                //REPLACE WITH LOG
                //System.out.println( "Got a msg from: " + remoteAddr.toString());
                String strMessage = new String(buff, 0, buff.length, "UTF-8");
                //REPLACE WITH LOG
                //System.out.println(strMessage);
                ProcessMessage(strMessage);
            }

        }
        catch(Exception e)
        {
        }

    }

    //Broadcast the discovery message to the multicast group & port.
    //The thermostats won't broad cast otherwise.
    private void SendDiscoveryMessage() throws Exception
    {
        byte[] discoverMsg = DISCOVER_MESSAGE.getBytes("UTF-8");
        DatagramPacket datagramPacket = new DatagramPacket(discoverMsg, discoverMsg.length, InetAddr, PORT);

        Sock.setSoTimeout(3000);

        Sock.send(datagramPacket);
    }

    //Setup the socket
    public void Init()
    {
        try {
            InetAddr = InetAddress.getByName(ADDRESS);

            ConfigureSocket();
        }
        catch(Exception e) {
            //Log failure
            System.out.println("Failed to setup discovery");
            CleanUpSocket();
        }

    }


    //Run a discovery round.
    public void Discover() {

        try
        {
            SendDiscoveryMessage();

            HandleMessages();
        }
        catch( Exception e )
        {
            System.out.println( "Caught Exception: " + e.toString() );
            e.printStackTrace();
        }

    }

}