import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;

// Combat Log
/*
 * .x-WDz^+FYQb^ 0	Y	QYQ"	^(Y$Qb:H*C0KfQX"AXTJ
	/"bad
	7
	6
"TLK_DOTA_CUSTOM
	p"4	eAE
	CPA
<"npc_dota_hero_announcer
"	A
=C

	?

	

	b"]
h"%b\X*L>%?jjfTj8f8j8f8jjTJ8	X
COMBAT SUMMARY
--- Faceless Void ---
"  Total Damage Done: 5797
    to Windranger: 193
%! 	!Legion Commander: 834
 	'Invok3013SileAx(: 1057
A$ $Razor: 700:(- ability d	,-
$ =otherdo%T%LStuns: 11.10 seconds2'$lows: 24.0'@--- Riki)s$# >D	)s<15802
 :t23!/ &""Fu3973jv2327nv83274
g-v3915w ,($>xwith riki_blink_strike(3): 16212X N141812-)72~F)36.5^Lich63An3120
524r63v7n564 *&"5lich_frosZ	@P`0^& 4RKvI-&(,.5 20@P`N	t^%&?w`#(jh(@?
 * 
 */

public class Network implements Runnable{
	int dota_tv_port = 27020;	
	String[] dota_labels = {"dota_select_starting_position", "dota_select_hero"
			,"DOTA_Chat_Team", "DOTA_Chat_All", "TLK_DOTA_CUSTOM", "npc_dota_hero", "pre_game", 
			"TLK_DOTA_PURCHASE", "paused", "unpaused", "tower_attacked", "Their_Dire_Tower_Fallen", 
			"TLK_DOTA_RESPAWN", "tower_killed", "hero_kill", "COMBAT SUMMARY"
	};
	
	int dota_game_port =  27029;
	int dg2  = 49159;
	int tvp2 = 28035;
	int dota_game_port2 = 27052;
	@Override
	public void run() {
		listenToDota();
		
	}
	// Packet Sniffin
	//
//	"tv_port" = "27020"                                                              - Host SourceTV port
//"clientport" = "-1" ( def. "27005" )                                             - Host game client port
	/*
	 * Connecting to 208.78.165.130:27029...
		25160.755:  Sending connect to 208.78.165.130:27029
		NotifyClientSignon: 2
		Connected to 208.78.165.130:27029

	 * 
	 */
	public void listenToDota(){
        List<PcapIf> alldevs = new ArrayList<PcapIf>(); // Will be filled with NICs  
        StringBuilder errbuf = new StringBuilder(); // For any error msgs  

        int r = Pcap.findAllDevs(alldevs, errbuf);
		if (r == Pcap.NOT_OK || alldevs.isEmpty()) {
			System.err.printf("Can't read list of devices, error is %s", errbuf
			    .toString());
			return;
		}

		System.out.println("Network devices found:");

		int i = 0;
		for (PcapIf device : alldevs) {
			String description =
			    (device.getDescription() != null) ? device.getDescription()
			        : "No description available";
			System.out.printf("#%d: %s [%s]\n", i++, device.getName(), description);
		}
		// Hardcoded Device index of microsoft adapter
		PcapIf device = alldevs.get(6); // We know we have atleast 1 device
		System.out
		    .printf("\nChoosing '%s' on your behalf:\n",
		        (device.getDescription() != null) ? device.getDescription()
		            : device.getName());

		/***************************************************************************
		 * Second we open up the selected device
		 **************************************************************************/
		int snaplen = 64 * 1024;           // Capture all packets, no trucation
		int flags = Pcap.MODE_PROMISCUOUS; // capture all packets
		int timeout = 10 * 1000;           // 10 seconds in millis
		Pcap pcap =
		    Pcap.openLive(device.getName(), snaplen, flags, timeout, errbuf);

		if (pcap == null) {
			System.err.printf("Error while opening device for capture: "
			    + errbuf.toString());
			return;
		}
		
		/***************************************************************************
		 * Third we create a packet handler which will receive packets from the
		 * libpcap loop.
		 **********************************************************************/
		PcapPacketHandler<String> jpacketHandler = new PcapPacketHandler<String>() {
	    Tcp tcp = new Tcp();  
	    Udp udp = new Udp();
			public void nextPacket(PcapPacket packet, String user) {
				if(packet.hasHeader(tcp)){
		            //System.out.printf("TCP: Source %d -> Destination %d\n", tcp.source(), tcp.destination());  
		            if(tcp.destination()==27005){
		            	System.out.println("FOUND TCP DOTA PACKET! from " + tcp.toHexdump() + tcp.source());
		            	System.out.println(packet.toString());
		            }
				}
				else if(packet.hasHeader(udp)){
		            //System.out.printf("UDP: Source %d -> Destination %d\n", udp.source(), udp.destination());
		            
		            if(udp.destination()==51747){
		            	//System.out.printf("UDP: Source %d -> Destination %d\n", udp.source(), udp.destination());
		            	String hDump = packet.toHexdump();
	            		//System.out.println(hDump);
	            		
		            	if(hDump.contains("hero_k")){
		            		System.out.println(hDump);
		            		System.out.println("Hero Killed: ");
		            	}
		            	else if(hDump.contains("pause")){
		            		System.out.println("Paused.");
		            	}
		            	// All Chat message
		            	else if(hDump.contains("Chat_All")){
		            		System.out.println(hDump);
		            		//int i = hDump.indexOf("Chat_All");
		            		System.out.println("Chat Message: ");
		            		//int nameLength = hDump.charAt(i+10);
		            		//System.out.println("Name Length: " + nameLength);
		            		///String name = hDump.substring(i+10,i+10+nameLength);
		            		//System.out.println("Name: " + name);
		            	}
		            	else if(hDump.contains("Chat")){
		            		System.out.println("ChAT: \n" + hDump);
		            	}
		            	else if(hDump.contains("dota")|| hDump.contains("DOTA")){
		            		//System.out.println(packet.toString());
		            		System.out.println(hDump);
		            	}
		            	else if(hDump.contains("COMBAT SUMMARY")){
		            		System.out.println(packet.toString());
		            		System.out.println(hDump);
		          //  		File n = new File("cs.txt");
		          
		            	}
		            	else{
		            		//System.out.println(hDump);
		            	}
		            	/*
		            	else if(hDump.contains("DOTA_Chat_Team")){
		            		System.out.println(hDump);
		            		//int i = hDump.indexOf("Chat_Team");
		            		System.out.println("Team Chat Message: ");
		            		//int nameLength = hDump.charAt(i+10);
		            		//System.out.println("Name Length: " + nameLength);
		            		//String name = hDump.substring(i+10,i+10+nameLength);
		            		//System.out.println("Name: " + name);
		            	}
		            	/*
		            	if(hDump.contains("Chat_All")){
//		            		System.out.println(packet.toString());
		            		int i = hDump.indexOf("Chat_All");
		            		int nameLength = hDump.charAt(i+10);
		            		System.out.println("Name Length: " + nameLength);
		            		String name = hDump.substring(i+10,i+10+nameLength);
		            		System.out.println("Name: " + name);
		            		int mesgLength = hDump.charAt(i+10+nameLength+2);
		            		String message = hDump.substring(i+14+nameLength+3,i+10+nameLength+2+mesgLength);
		            		System.out.println("Message: " + message);
		            	}
		            	else if(hDump.contains("ward")){
		            		System.out.println(packet.toString());
		            		
		            	}
		            	else if(hDump.contains("rosh")){
		            		System.out.println(packet.toString());
		            	}
		            	else if(hDump.contains("dota")){
		            		System.out.println(packet.toString());
		            	}
		            	//System.out.println("FOUND UDP DOTA PACKET! from " + udp.toHexdump() + udp.source());
		                System.out.println(packet.toString());
		                */
		            }
				}
				/*
				System.out.printf("Received packet at %s caplen=%-4d len=%-4d %s\n",
				    new Date(packet.getCaptureHeader().timestampInMillis()), 
				    packet.getCaptureHeader().caplen(),  // Length actually captured
				    packet.getCaptureHeader().wirelen(), // Original length 
				    user                                 // User supplied object
				    );
				System.out.println(packet.getCaptureHeader().peer(arg0, arg1)toString());
				*/
			}
		};
		
		/***************************************************************************
		 * Fourth we enter the loop and tell it to capture 10 packets. The loop
		 * method does a mapping of pcap.datalink() DLT value to JProtocol ID, which
		 * is needed by JScanner. The scanner scans the packet buffer and decodes
		 * the headers. The mapping is done automatically, although a variation on
		 * the loop method exists that allows the programmer to sepecify exactly
		 * which protocol ID to use as the data link type for this pcap interface.
		 **************************************************************************/
		pcap.loop(Pcap.LOOP_INFINITE, jpacketHandler, "jNetPcap rocks!");

		/***************************************************************************
		 * Last thing to do is close the pcap handle
		 **************************************************************************/
		pcap.close();
	}
	
	
	
}
