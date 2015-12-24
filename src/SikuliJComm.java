import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.io.*;
import java.lang.reflect.Field;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.logging.Handler;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import javax.security.auth.callback.Callback;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.text.DefaultCaret;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;
import org.sikuli.script.*;

public class SikuliJComm {
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		try
		{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		}
		catch (Exception e)
		{
			System.out.println("Unable to load Windows look and feel");
		}
		Remote r = new Remote();
		RemoteUI UI = new RemoteUI(r);
	}
}
/*
 * User Interface
 */
class RemoteUI extends JFrame {
	// UI Components
	private JButton bAutoAccept, bStartDota2, bFindMatch, bStopAutoAccept, bShowDota2, bCloseDota,
					bCancelSearch;
	private JButton bSendPartyMsg, bSendTeamMsg, bSendAllChatMsg, bConnectMobile, bExtraButton, bPause;
	private JButton bGetGameTime, bStartNetAnalyze, bStopNetAnalyze;
	private JButton bSpamChatLobby;
	private JButton bSpamHeroPick; // TODO THIS
	private JCheckBox cbRoshBindBox;
	private JTextField tMsg;
	private JLabel phoneConnStatus;
	private JPanel connPane, msgPane, actionPane, centerGPane;
	private JTextArea outWindow;
	
	private JTabbedPane tabsPane;
	private JPanel fncPane, mobilePane, netPane, settingsPane;
	private JTextArea consoleOut;
	private JScrollPane sP;
	// Global Handles and Class objects
	public Remote r;
	public RemoteUI rUI = this;
	private Thread imgStreamThr;
	ServHelper serverTid;
	Thread netTid, autoAccptTid;
	
	
	///////////Constants\\\\\\\\\\
	final int START_DOTA = 1;
	final int AUTO_ACCEPT = 2;
	final int FIND_GAME = 3;
	final int PAUSE_GAME = 4;
	final int MESSAGE_LOBBY = 5;
	final int MESSAGE_TEAM = 6;
	final int MESSAGE_ALLCHAT = 7;
	final int EXIT_DOTA = 8;
	final int SERVER_CONNECT = 0;
	final int SERVER_DISCONNECT = 10;
	
	public RemoteUI(Remote remote){
		r = remote;
		//buildFrame();
		buildUI();
		setVisible(true);
		r.start();
	}

	public void buildUI(){
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Dota2 Remote");
		setSize(600,450);
		setLayout(new BorderLayout());
		setResizable(false);
		setLocationRelativeTo(null);
		
		// FNC PANEL COMPONENTS
		fncPane = new JPanel();

		bAutoAccept = new JButton("Auto Accept");
		bStartDota2 = new JButton("Start Dota 2");
		bFindMatch = new JButton("Find Match");
		bStopAutoAccept = new JButton("Stop AutoAccept");
			bStopAutoAccept.setEnabled(false);
		bShowDota2 = new JButton("Show Dota 2");
		bCloseDota = new JButton("Close Dota 2");
		bPause = new JButton("Pause Game");
		bCancelSearch = new JButton("Cancel Match Search");
		
		bCancelSearch.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				showDota();
				new Thread(new Runnable(){
					public void run(){
						
						boolean result = r.cancelSearch(r.getScrn());
						if(result)
							consoleOut.append("Search Canceled.\n");
						else
							consoleOut.append("Cancel Not Found.\n");
							
					}
				}).start();
			}
		});
		
	
		// Close Dota 
				bCloseDota.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						App doters = new App("DOTA 2");
						doters.close();
					}
				});

		// Find Match and look for accept
		bFindMatch.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				showDota();
				new Thread(new Runnable() {
					@Override
					public void run(){
						//r.clickFindMatch(r.getScrn());
						r.findMatch2(r.getScrn());
					}
				}).start();
			}
		});

		// Start dota.exe
		bStartDota2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable(){
					public void run(){
						r.startDota2(r.getScrn());
					}
				}).start();
			}
		});

		// Auto Accept Mode
		bAutoAccept.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				showDota();
				autoAccptTid = new Thread(new Runnable() {
					@Override
					public void run(){
						bStopAutoAccept.setEnabled(true);
						r.autoAccept(r.getScrn());
					}
				});
				autoAccptTid.start();
				//System.out.println("Lobby Text Contents: " + r.getLobbyText(r.getScrn()));
			}
		});

		bStopAutoAccept.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				showDota();
				r.stopSearch();
				bStopAutoAccept.setEnabled(false);
			}
			
		});
		
		// Show dota2 application
		bShowDota2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				//App doters = new App("C:\\Program Files (x86)\\Steam\\steamapps\\common\\dota 2 beta\\dota.exe");
				App doters = new App("DOTA 2");
				doters.focus();
			}
		});
		
		// Pause game
		bPause.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				new Thread(new Runnable() {
					@Override
					public void run(){
						r.pauseGame(r.getScrn());
					}
				}).start();
			}
		});
		
		fncPane.add(bAutoAccept);
		fncPane.add(bStartDota2);
		fncPane.add(bStopAutoAccept);
		fncPane.add(bShowDota2);
		fncPane.add(bCloseDota);
		fncPane.add(bPause);
		fncPane.add(bFindMatch);
		fncPane.add(bCancelSearch);
		// ----MOBILE | NET
		mobilePane = new JPanel();
		bConnectMobile = new JButton("Connect Mobile");
		connPane = new JPanel();
		connPane.add(new JLabel("Mobile Connection Status: "));
		phoneConnStatus = new JLabel("Disconnected.");
		connPane.add(phoneConnStatus);
		connPane.add(bConnectMobile);
		connPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		
		
		/*
		 * Connect Event Listener
		 * Spawn thread to run server helper
		 */
		bConnectMobile.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(netTid==null){
					netTid = new Thread(new ServHelper(r,rUI));
					netTid.start();
				}
				/*
				new Thread(new Runnable() {
					@Override
					public void run(){
						//initServer();
						Thread thr = new Thread(new ServHelper(r,rUI));
						thr.start();
					}
				}).start();
			*/
			}
		});
		
		mobilePane.add(bConnectMobile);
		mobilePane.add(connPane);
		// ----
		netPane = new JPanel();
		bStartNetAnalyze = new JButton("Start Network Analyzer");
		bStopNetAnalyze = new JButton("Stop Network Analyzer");
		
		
		bStartNetAnalyze.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				netTid = new Thread(new Network());
				netTid.start();
			}
		});

		bStopNetAnalyze.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				
				
			}
		});
		
		netPane.add(bStartNetAnalyze);
		netPane.add(bStopNetAnalyze);
		
		// ----
		settingsPane = new JPanel();
		
		
		tabsPane = new JTabbedPane();
		tabsPane.add("D2 Functions", fncPane);
		tabsPane.add("Mobile", mobilePane);
		tabsPane.add("Networking", netPane);
		tabsPane.add("Settings", settingsPane);
		
		
		
		setConsoleOut(new JTextArea(10,20));
		getConsoleOut().setBackground(Color.black);
		getConsoleOut().setForeground(Color.GREEN);
		getConsoleOut().setMaximumSize(getSize());
		getConsoleOut().setWrapStyleWord(true);
		getConsoleOut().setEditable(false);
		getConsoleOut().applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		/*DefaultCaret caret = (DefaultCaret)getConsoleOut().getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		caret.setVisible(true);
		*/
		sP = new JScrollPane(getConsoleOut(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		new SmartScroller(sP);
		
		add(tabsPane, BorderLayout.CENTER);
		add(sP, BorderLayout.SOUTH);
		checkNetworking();
	}
	
	///
	// OLD: Initialize UI Components and events
	///
	public void buildFrame()
	{
		// UI Window Settings
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Dota2 Remote");
		setSize(800,600);
		setLayout(new BorderLayout());
		setResizable(false);

		// Create components
		bAutoAccept = new JButton("Auto Accept");
		bStartDota2 = new JButton("Start Dota 2");
		bFindMatch = new JButton("Find Match");
		bStopAutoAccept = new JButton("Stop AutoAccept");
			bStopAutoAccept.setEnabled(false);
		bShowDota2 = new JButton("Show Dota 2");
		bCloseDota = new JButton("Close Dota 2");
		bStartNetAnalyze = new JButton("Start Network Analyzer");
		bStopNetAnalyze = new JButton("Stop Newtwork Analyzer");
		// Spam button
		bSpamChatLobby = new JButton("SPam Chat");
		Markov mv = new Markov("alice30.txt");
		bSpamChatLobby.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				sayThings();
//				lobbyChatMsg()
				//printColorChar();
			}
		});
		connPane = new JPanel(new FlowLayout());
		msgPane = new JPanel(new FlowLayout());
		centerGPane = new JPanel(new GridLayout(2,1));
		actionPane = new JPanel(new FlowLayout());
		tMsg = new JTextField(15);
		bSendPartyMsg = new JButton("Send");
		bConnectMobile = new JButton("Connect Mobile");
		bExtraButton = new JButton("Button");
		bPause = new JButton("Pause Game");
		bSendTeamMsg = new JButton("Send Team Msg");
		bSendAllChatMsg = new JButton("Send All Chat Msg");
		bGetGameTime = new JButton("Get Game Time");
		cbRoshBindBox = new JCheckBox("Roshan Time Recall", false);

		outWindow = new JTextArea(10,20);
		outWindow.setWrapStyleWord(true);
		outWindow.setEditable(false);

		// Construct Component Panels \\
		//  Connection Status Panel
		connPane.add(new JLabel("Mobile Connection Status: "));
		phoneConnStatus = new JLabel("Disconnected.");
		connPane.add(phoneConnStatus);
		connPane.add(bConnectMobile);
		connPane.add(bStartNetAnalyze);
		connPane.add(bStopNetAnalyze);
		connPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

		// Messaging Panel
		msgPane.add(new JLabel("Message: "));
		msgPane.add(tMsg);
		msgPane.add(bSendPartyMsg);
		msgPane.add(bSendTeamMsg);
		msgPane.add(bSendAllChatMsg);
		msgPane.add(bSpamChatLobby);
		msgPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		// Action Panel
		actionPane.add(bAutoAccept);
		actionPane.add(bStopAutoAccept);
		actionPane.add(bStartDota2);
		actionPane.add(bFindMatch);
		actionPane.add(bShowDota2);
		actionPane.add(bPause);
		actionPane.add(bCloseDota);
		actionPane.add(bGetGameTime);
		actionPane.add(cbRoshBindBox);
		actionPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

		centerGPane.add(actionPane);
		centerGPane.add(msgPane);

		//********** EVENT LISTENERS************* \\
		// Check Box for Rosh Timer Recall Keybind
		cbRoshBindBox.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if(arg0.getStateChange()==ItemEvent.SELECTED)
				{
					initKeybind();
					System.out.println("Keybind On.");
				}else{
					if(GlobalScreen.isNativeHookRegistered())
						GlobalScreen.unregisterNativeHook();
				}
			}
		});
		// Get Clock Time
		bGetGameTime.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				new Thread(new Runnable(){
					@Override
					public void run() {
						showDota();
						r.getGameTime(r.getScrn());
					}
				}).start();
			}

		});


		// Close DOta Listener
		bCloseDota.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				App doters = new App("DOTA 2");
				doters.close();
			}
		});

		// Pause game
		bPause.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				new Thread(new Runnable() {
					@Override
					public void run(){
						r.pauseGame(r.getScrn());
					}
				}).start();
			}
		});

		// Send in game message to all
		bSendAllChatMsg.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				showDota();
				new Thread(new Runnable() {
					@Override
					public void run(){
						String msg = tMsg.getText();
						r.msgTeam(r.getScrn(), msg, true);
					}
				}).start();
			}
		});

		bSendTeamMsg.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				showDota();
				new Thread(new Runnable() {
					@Override
					public void run(){
						String msg = tMsg.getText();
						r.msgTeam(r.getScrn(), msg, false);
					}
				}).start();
			}
		});

		// Send active lobby a message
		bSendPartyMsg.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				showDota();
				new Thread(new Runnable() {
					@Override
					public void run(){
						String msg = tMsg.getText();
						r.lobbyChatMsg(r.getScrn(), msg);
					}
				}).start();
				/*String msg = tMsg.getText();
				r.msgParty(r.getScrn(), msg);*/
			}
		});

		// Find Match and look for accept
		bFindMatch.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run(){
						//r.clickFindMatch(r.getScrn());
						r.findMatch2(r.getScrn());
					}
				}).start();
			}
		});

		// Start dota.exe
		bStartDota2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable(){
					public void run(){
						r.startDota2(r.getScrn());
					}
				}).start();
			}
		});

		// Auto Accept Mode
		bAutoAccept.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				showDota();
				autoAccptTid = new Thread(new Runnable() {
					@Override
					public void run(){
						bStopAutoAccept.setEnabled(true);
						r.autoAccept(r.getScrn());
					}
				});
				autoAccptTid.start();
				//System.out.println("Lobby Text Contents: " + r.getLobbyText(r.getScrn()));
			}
		});

		bStopAutoAccept.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				r.stopSearch();
				bStopAutoAccept.setEnabled(false);
			}
			
		});
		
		// Show dota2 application
		bShowDota2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				//App doters = new App("C:\\Program Files (x86)\\Steam\\steamapps\\common\\dota 2 beta\\dota.exe");
				App doters = new App("DOTA 2");
				doters.focus();
			}
		});

		/*
		 * Connect Event Listener
		 * Spawn thread to run server helper
		 */
		bConnectMobile.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				Thread thr = new Thread(new ServHelper(r,rUI));
				thr.start();
				/*
				new Thread(new Runnable() {
					@Override
					public void run(){
						//initServer();
						Thread thr = new Thread(new ServHelper(r,rUI));
						thr.start();
					}
				}).start();
			*/
			}
		});
		

		// Extra button to run things
		bExtraButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run(){
						Thread thr = new Thread(new ServHelper());
						thr.start();
					}
				}).start();
			}
		});
		
		bStartNetAnalyze.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				netTid = new Thread(new Network());
				netTid.start();
			}
		});
		bStartNetAnalyze.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				// Stop the anlzyzur
			}
		});
		
		// Add Panes to ui
		add(connPane, BorderLayout.NORTH);
		//add(msgPane);
		add(centerGPane, BorderLayout.CENTER);
		//add(bExtraButton);
		add(outWindow, BorderLayout.SOUTH);

		// Display IPs to user for input in mobile client
		checkNetworking();

		//System.out.println("Listening to Dota Port...");
		
		//listenToDota();
		
		
		//Markov mv = new Markov("alice30.txt");
	}

	
	// Update UI with server Connection status
	public void setServerConnStatus(boolean connected){
		if(connected){
			getConsoleOut().append("Connection Established.");
		}
		else{
			getConsoleOut().append("Disconnected.");
		}
	}

	// Start Streaming
	public void startImgStreamThread(){
		if(imgStreamThr==null)
			imgStreamThr = new Thread(new ImageStreamer(r));

		imgStreamThr.start();
	}
	
	// Stop The Image Streaming Thread
	public void stopImgStreamThread(){
		if(imgStreamThr!=null){
			imgStreamThr.interrupt();
		}
	}
	
	/**
	 * Register Hook for global keybind
	 */
	char getRoshTimeChar = '*';
	char showRoshTimeChar = '-';
	char setTextColor = '+';
	int num_KeyBind = 0;
	char[] keyBinds;
	public void initKeybind(){
		try {
			GlobalScreen.registerNativeHook();
		}
		catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());
		}

		GlobalScreen.getInstance().addNativeKeyListener(new NativeKeyListener(){

			@Override
			public void nativeKeyPressed(NativeKeyEvent arg0) {

			}

			@Override
			public void nativeKeyReleased(NativeKeyEvent arg0) {


			}

			@Override
			public void nativeKeyTyped(NativeKeyEvent arg0) {
				/*
				if(arg0.getKeyChar()==getRoshTimeChar){
					new Thread(new Runnable(){
						public void run(){
							System.out.println("Getting Roshan Time.");
							r.getGameTime(r.getScrn());
						}
					}).start();
				}
				else if(arg0.getKeyChar()==showRoshTimeChar){
					new Thread(new Runnable(){
						public void run(){
							System.out.println("Displaying Roshan Time.");
							r.printLastRoshanTime();
						}
					}).start();
				}
				 */
			}    	 
		});

	}

	public void checkNetworking(){
		System.out.println("Identifying Network IP");
		String ip = "Connecting to network...";
		String ipOutput = "";
		try{
			Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
			for (NetworkInterface netint : Collections.list(nets)){
				if(!netint.isLoopback() && netint.isUp() && !netint.isVirtual()){
					//System.out.printf("Display name: %s\n", netint.getDisplayName());
					//System.out.printf("Name: %s\n", netint.getName());
					Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
					for (InetAddress inetAddress : Collections.list(inetAddresses)) {
						//System.out.printf("InetAddress: %s\n", inetAddress);
						ipOutput+=netint.getDisplayName().toString() + "\n";
						ipOutput+=inetAddress.toString().substring(1) + "\n";
					}
				}
			}
		}catch(SocketException se){
			se.printStackTrace();
		}
		if(ipOutput.equals("")){
			getConsoleOut().append("No Active Network Connections Found.\n");
		}
		else{
			getConsoleOut().append("Possible IP Addresses  \n ");
			getConsoleOut().append(ipOutput);
		}
	}
	
	
	
	// Packet Sniffin
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
		            if(udp.destination()==27005){
		            	String hDump = packet.toHexdump();
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
	
	
	
	
	
	// SayThings driven by the markov chain implementation and a given text file
	
	String s;
	Vector<String> vs;
	BufferedReader in;
	public void sayThings(){
		int i = 0;
		try{
			System.out.println("OPening markov text");
			in = new BufferedReader(new FileReader("output.txt"));
			String all = "";
			while((s = in.readLine())!=null)
			{
				all+=s;
			}
			
			s = all;
			vs = new Vector<String>(0);
			int t1 = s.indexOf('.');
			while(t1>0){
				vs.add(s.substring(0,t1));
				s=s.substring(t1+1);
				t1 = s.indexOf('.');
			}
			vs.add(s);
			new Thread(new Runnable() {
				@Override
				public void run(){
					for(int i = 0; i<vs.size(); i++){
						r.msgParty(r.getScrn(), vs.get(i));
					}
				} 
			}).start();

		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
	public void sayThingsClipboard(){
		int i = 0;
		try{
			System.out.println("OPening markov text");
			in = new BufferedReader(new FileReader("output.txt"));
			String all = "";
			while((s = in.readLine())!=null)
			{
				all+=s;
			}
			
			s = all;
			vs = new Vector<String>(0);
			int t1 = s.indexOf('.');
			while(t1>0){
				vs.add(s.substring(0,t1));
				s=s.substring(t1+1);
				t1 = s.indexOf('.');
			}
			vs.add(s);
			new Thread(new Runnable() {
				@Override
				public void run(){
					for(int i = 0; i<vs.size(); i++){
						r.msgPartyPaste(r.getScrn(), vs.get(i));
					}
				} 
			}).start();

		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}

	public void showDota(){
		App doters = new App("DOTA 2");
		doters.focus();
	}

	ServerSocket serverSock = null;

	public void initServer(){
		serverSock = null;
		int PORT = 8095;
		try {
			serverSock = new ServerSocket(PORT);
			serverSock.setSoTimeout(1600000);

			System.out.println(" Waiting a client ... ");
			Socket socket = serverSock.accept();
			int i = 0;
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			DataInputStream in = new DataInputStream(socket.getInputStream());
			while(true) {
				i = in.readInt();
				// Start Dota
				if(i==1){
					r.startDota2(r.getScrn());
					try {
						r.wait(55);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					r.dotaToFront(r.getScrn());
				}
				// Find Match
				else if(i==2){
					r.findMatch2(r.getScrn());
				}
			}
		} catch (IOException e) {
			System.err.println(" Error IO \n" + e);
		} finally {
			try {
				serverSock.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public JTextArea getConsoleOut() {
		return consoleOut;
	}

	public void setConsoleOut(JTextArea consoleOut) {
		this.consoleOut = consoleOut;
	}
}

/*
 * Class: ServHelper
 * Serves as an interface between program functionality 
 * 	   and network communication to mobile device.
 * Protocol for messages
 * 1st 2 bytes = action value/type
 */
class ServHelper implements Runnable{
	RemoteUI rUI;
	ServerSocket ss;
	Socket sock;
	DataOutputStream dOut;
	DataInputStream dIn;
	BufferedReader buffR;
	PrintStream pOut;
	Remote r;
	String msg="";
	boolean serverUp = true;
	
	// Constants for commands over network
	final int CONNECTION_ESTABLISHED = 0;
	final int START_DOTA = 1;
	final int AUTO_ACCEPT = 2;
	final int FIND_GAME = 3;
	final int PAUSE_GAME = 4;
	final int MESSAGE_LOBBY = 5;
	final int MESSAGE_TEAM = 6;
	final int MESSAGE_ALLCHAT = 7;
	final int EXIT_DOTA = 8;
	final int STREAM_PIC = 9;
	final int CLIENT_DISCONNECTED = 10;


	// Bunch of constructors to init remoteui handler
	public ServHelper(){
		r = new Remote();
	}
	public ServHelper(Remote r_){
		r = r_;
	}
	public ServHelper(Remote r_, RemoteUI h_remoteUI){
		r = r_;
		rUI = h_remoteUI;
	}
	public ServHelper(RemoteUI h_remoteUI){
		rUI = h_remoteUI;
	}

	public String getAddress(){
		System.out.println(sock.getLocalAddress().toString());
		return sock.getLocalAddress().toString();
	}

	@Override
	public void run() {
		while(serverUp){
		System.out.println("Server Thread Running...");
		try {
			ss = new ServerSocket(8095);
//			ss.setSoTimeout(160000);
			System.out.println("Server waiting for connection...");
			sock = ss.accept();
			System.out.println("Server found connection.");
			rUI.setServerConnStatus(true);
			String buffd = "";
			int action = 0;
			// IO objects
			dIn = new DataInputStream(sock.getInputStream());
			dOut = new DataOutputStream(sock.getOutputStream());
			buffR = new BufferedReader(new InputStreamReader(dIn));
			pOut = new PrintStream(sock.getOutputStream());
			pOut.println("00");

			//dOut.writeBytes("00"+"\n");
			while(buffd!=null){
				System.out.println("Listening...");
				rUI.getConsoleOut().append("Listening... On 8085" + "\n");
				buffd = buffR.readLine();
				if(buffd!=null){
					if(!buffd.equals("")){
						try{
							action = Integer.parseInt(buffd.substring(0,2));
						}catch(NumberFormatException nfe){
							nfe.printStackTrace();
							action = 0;
							System.out.println("Error");
						}
						System.out.println("Incoming Message: " + buffd);
						rUI.getConsoleOut().append("Incoming Mobile Action ID: " + action + " :" + buffd.substring(2) + "\n");
						switch(action){
						case START_DOTA:
							r.startDota2(r.getScrn());
							pOut.println("01");
							System.out.println("Out Written.");

							break;
						case AUTO_ACCEPT:
							new Thread(new Runnable(){
								@Override
								public void run(){
									r.autoAccept(r.getScrn());
								}
							}).start();
							break;
						case FIND_GAME:
							new Thread(new Runnable(){
								@Override
								public void run(){
									//r.findMatch(r.getScrn());
									r.findMatch2(r.getScrn());
								}
							}).start();
							break;
						case PAUSE_GAME:
							new Thread(new Runnable(){
								@Override
								public void run(){
									r.pauseGame(r.getScrn());
								}
							}).start();
							break;
						case MESSAGE_LOBBY:
							msg = buffd.substring(2,buffd.length());
							new Thread(new Runnable() {
								@Override
								public void run(){
									r.lobbyChatMsg(r.getScrn(),msg);
								}
							}).start();
							break;
						case MESSAGE_TEAM:
							msg = buffd.substring(2,buffd.length());
							new Thread(new Runnable() {
								@Override 
								public void run(){
									r.msgTeam(r.getScrn(), msg, false);
								}
							}).start();
							break;
						case MESSAGE_ALLCHAT:
							msg = buffd.substring(2, buffd.length());
							new Thread(new Runnable(){
								@Override
								public void run(){
									r.msgTeam(r.getScrn(), msg, true);
								}
							}).start();
							break;
						case EXIT_DOTA:
							App doters = new App("DOTA 2");
							doters.close();
							//dOut.writeBytes("08"+"\n");
							pOut.println("08");
							break;
						case STREAM_PIC:
							pOut.println("09");

							//dOut.writeBytes("09"+"\n");
							rUI.startImgStreamThread();
							break;
						case 0:
							System.out.println("Message From Client: " + msg);
							break;
						default:
							System.out.println("default switch reached. Action Value: " + Integer.toString(action));
							break;
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Listening I/O Error Throw.");
		}
		System.out.println("End of Run...Bye");
		}
	}
}

/*
 * Stream portion of screen to device
 */
class ImageStreamer implements Runnable{
	Socket s;
	Remote r;
	int PORT = 8094;
	public ImageStreamer(Remote _r, Socket _s){
		s=_s;
		r=_r;
	}
	public ImageStreamer(Remote _r){
		r=_r;
	}

	static public byte[] object2Bytes( BufferedImage o ){
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream( baos );
			ImageIO.write(o, "png", oos);
			//oos.writeObject( o );
			return baos.toByteArray();
		}catch(IOException e){e.printStackTrace();}
		return null;
	}
	static public byte[] object2Bytes( Object o ){
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream( baos );
			oos.writeObject( o );
			return baos.toByteArray();
		}catch(IOException e){e.printStackTrace();}
		return null;
	}
	DataOutputStream dOut;
	ServerSocket ss;
	boolean isActive = true;
	@Override
	public void run(){

		try{
			ss = new ServerSocket(PORT);
			ss.setSoTimeout(160000);
			System.out.println("Server waiting for Image Stream connection...");
			s = ss.accept();	// Blocks Here until accept\timeout
			System.out.println("Server on port 8094 Connected.");
			dOut = new DataOutputStream(s.getOutputStream());
			BufferedImage bimg = r.getLobbyChatScrn(r.getScrn());
			File cachedImg = new File("cache.png");
			//FileOutputStream fw = ;
			long size = 0;
			BufferedOutputStream bout = new BufferedOutputStream(s.getOutputStream());
			while(isActive){
				Image img;
				bimg = r.getLobbyChatScrn(r.getScrn());
				size = cachedImg.length();
				System.out.println(size);
				dOut.writeBytes(Long.toString(size)+"\n");
				System.out.println("Cache Write: " + ImageIO.write(bimg, "png", cachedImg));
				System.out.println("Network Write: " + ImageIO.write(bimg, "png", bout));
				//byte[] outStuff = object2Bytes(bimg);
				//dOut.write(outStuff);
				//bout.write(outStuff);
				//bout.flush();
				System.out.println("Writing Image To Stream...");
				Thread.sleep(90000);
				dOut.flush();
			}

		} catch(IOException e){
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally{
			try {
				dOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}


