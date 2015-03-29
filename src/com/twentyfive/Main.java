package com.twentyfive;

import com.sun.deploy.util.StringUtils;
import java.util.List;


//Simple test driver for the Wireless thermostat discovery
//NOTE: If you are running on Windows and the SSDP service is running this will fail.
public class Main
{

    public static void main(String[] args)
    {
        ThermostatDiscovery discover = new ThermostatDiscovery();

        try
        {
            discover.Init();
            discover.Discover();
        }
        catch(Exception e)
        {
            System.out.println("Caught exception: " + e.toString());
        }

        if( discover.FoundAddress() )
        {
            List<String>    thermostats = discover.GetAddresses();
            String          things      = StringUtils.join(thermostats, ", ");
            System.out.println(things);
        }

        discover.Close();

    }

}
