import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.sikuli.script.App;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Key;
import org.sikuli.script.KeyModifier;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;
import org.sikuli.script.TextRecognizer;

/**
 * 
 * Remote Object Handles Sikuli Functionality
 * 
 */
class Remote{
	// Image Filename Strings
	String FIND_MATCH = "imgs/FIND_MATCH_REBORN.png";//"imgs/FIND_MATCH.png";
	String FINDING_MATCH = "imgs/FINDINGMATCH.png";
	String ACCEPT = "imgs/ACCEPT_REBORN.png";//"imgs/ACCEPT-1.png";
	String PLAY_TAB_UNSELECTED = "imgs/PLAY-1.png";
	String DOTA_LOGO_LOADING = "imgs/DOTA_LOGO_LOADING_REBORN.png";//"imgs/DOTA_LOGO_LOADING.png";
	String PARTY_CHATBAR = "imgs/PARTY_CHATBAR.png";
	String IN_PARTY = "imgs/INPARTY.png";
	String CANCEL_SEARCH = "imgs/CANCEL_SEARCH.png";
	final double TIMEOUT = 5;	// Timeout for waits
	private Thread tid;
	private App doters;
	private Screen s;
	private String roshan_time = "0:00";

	public Remote(){
		s = new Screen();
		doters = new App("DOTA 2");
	}
	public Screen getScrn(){
		return s;
	}

	public void start(){

		//		startDota2(s);
		//		if(!findMatch(s))
		//			System.out.println("Find Failed.");
	}

	public void dotaToFront(Screen s){
		doters.focus();
	}
	public boolean existClick(String str){
		try{
			if(s.exists(str,TIMEOUT)!=null){
				s.click(str);
				return true;
			}
		}catch(FindFailed ff){}
		return false;
	}
	
	public void lifeStealerFarmBotInit(){
		
		
		
		
		
	}
	public void sellItems(Screen s){
		long timeOut=5000;
		boolean selling = true;
		String sellValue = ".01";
		/*
		while(selling){
		try {
			existClick("imgs/SELL_AN_ITEM.png");
			if(existClick("imgs/TF2CRATE.png"))
				if(existClick("imgs/SELL.png"))
					if(existClick("imgs/UP4SALE.png"))
						existClick("imgs/OK.png");
			/*
			s.wait("imgs/SELL_AN_ITEM.png", TIMEOUT);
			s.click("imgs/SELL_AN_ITEM.png");
			s.wait("imgs/TF2CRATE.png", TIMEOUT*5);
			s.click("imgs/TF2CRATE.png");
			s.click("imgs/SELL.png");
			//s.type(".01");
			s.click("UP4SALE.png");
			s.click("OK.png");
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		}
		 */
	}
	public void msgParty(Screen s, String msg){
		doters.focus();

		/*		try {
			s.wait(s.exists(IN_PARTY),TIMEOUT*2.0);
		} catch (FindFailed e1) {
			e1.printStackTrace();
		}*/

		if(s.exists(IN_PARTY)!=null){
			try{
				Pattern party = new Pattern(PARTY_CHATBAR);
				party = party.targetOffset(-335, -15);
				Match m = s.exists(party);
				s.click(party);
				s.type(msg);
				s.type(Key.ENTER);
			}catch(FindFailed ff){
				ff.printStackTrace();
			}
		}
	}
	public void msgPartyPaste(Screen s, String msg){
		doters.focus();

		/*		try {
			s.wait(s.exists(IN_PARTY),TIMEOUT*2.0);
		} catch (FindFailed e1) {
			e1.printStackTrace();
		}*/
		 java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		 /*
		 robot.keyPress(KeyEvent.VK_CONTROL);
		 robot.keyPress(KeyEvent.VK_V);
		 robot.keyRelease(KeyEvent.VK_V);
		 robot.keyRelease(KeyEvent.VK_CONTROL);
		 */
		if(s.exists(IN_PARTY)!=null){
			try{
				Pattern party = new Pattern(PARTY_CHATBAR);
				party = party.targetOffset(-335, -15);
				Match m = s.exists(party);
				s.click(party);

				StringSelection stringSelection = new StringSelection( msg );
    			//clipboard.setContents(stringSelection, instance);
    			clipboard.setContents(stringSelection, ClipboardOwner.class.newInstance());
				//s.type(msg);
    			s.type(null, "v", java.awt.event.InputEvent.CTRL_DOWN_MASK);
    
				s.type(Key.ENTER);
			}catch(FindFailed ff){
				ff.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}
	public String getLobbyText(Screen s){
		Region rChats = Region.create(1070,310,579,307);
		Vector<String[]> vss;
		String[] mesg = new String[3];
		vss = new Vector<String[]>();
		vss.add(new String[3]);
		try {
			// Capture Image of chat region
			BufferedImage buffImg = s.capture(rChats).getImage();
			RescaleOp rescaleOp = new RescaleOp(1.5f, 15, null);
			// Change Contrast
			ColorConvertOp cco = 
					new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
			cco.filter(buffImg,buffImg);
			Kernel kernel = new Kernel(3, 3,
					new float[] {
					0.f, -1.f, 0.f, -1.f, 5.f, -1.f, 0.f, -1.f, 0.f});

			BufferedImageOp op = new ConvolveOp(kernel);
			buffImg = op.filter(buffImg, null);

			rescaleOp.filter(buffImg, buffImg);  // Source and destination are the same.
			ImageIO.write(buffImg, "png", new File("imgs/cap4.png"));
			TextRecognizer tr = TextRecognizer.getInstance();
			return tr.recognize(buffImg);
			//return rChats.text();
		} catch (IOException e) {
			e.printStackTrace();
		}


		return "";
	}

	public BufferedImage getLobbyChatScrn(Screen s){
		Region rChats = Region.create(1070,310,579,307);
		BufferedImage buffImg = s.capture(rChats).getImage();
		return buffImg;
	}

	/**
	 * @param s
	 * @param msg: message to send
	 */
	public void msgSteam(Screen s, String msg){
		//if in game 
		try {
			s.type(msg);
			s.type(Key.ENTER);
		} catch (FindFailed e) {
			e.printStackTrace();
		}
	}
	public void msgTeam(Screen s, String msg, boolean allchat){
		//if in game 
		try {
			if(allchat)
				s.type(null, Key.ENTER, java.awt.event.InputEvent.SHIFT_MASK);
			else
				s.type(Key.ENTER);
			s.type(msg);
			s.type(Key.ENTER);
		} catch (FindFailed e) {
			e.printStackTrace();
		}

	}
	public void msgTeamPaste(Screen s, String msg, boolean allchat){
		//if in game 
		try {
			if(allchat)
				s.type(null, Key.ENTER, java.awt.event.InputEvent.SHIFT_MASK);
			else
				s.type(Key.ENTER);
			s.type(msg);
			s.type(Key.ENTER);
		} catch (FindFailed e) {
			e.printStackTrace();
		}
	}
	// COlored text
	public void msgColor(Screen s, String msg, String color, boolean allchat){
		// Resolve Color
		char c = ' ';
		if(color=="white")
			c = '';
		else if(color=="pink1")
			c = '';
		else if(color=="gold/brown")
			c = '';
		else if(color=="pink2")
			c = '';
		else if(color=="red")
			c = '';
		else if(color=="orange2")
			c = '';
		else if(color=="gold")
			c = '';
		else if(color=="green")
			c = '';
		else if(color=="purple")
			c = '';
		else if(color=="gray")
			c = '';
		else if(color=="green2")
			c = '';
		else if(color=="blue1")
			c = '';
		else if(color=="purple/pink")
			c = '';
		
		try{
			if(allchat)
				s.type(null, Key.ENTER, java.awt.event.InputEvent.SHIFT_MASK);
			else
				s.type(null, Key.ENTER);
			s.type(String.valueOf(c));
			s.type(msg);
		}catch(FindFailed e){}
	}
	
	
	// AA
	public boolean searchFlag = false;
	public void autoAccept(Screen s){
		searchFlag = true;
		boolean loading = false;
		while(searchFlag){
			// Click Accept
			if(!loading){
				try {
					s.wait(ACCEPT,TIMEOUT);
					s.click(ACCEPT);
				} catch (FindFailed ff) {
					System.out.println("Accept Not Found.");
				}
			}
			try {
				s.wait(DOTA_LOGO_LOADING,TIMEOUT);
				System.out.println("Game Loading...");
				if(s.exists(DOTA_LOGO_LOADING)!=null){
					System.out.println("Loading Screen Found.");
					searchFlag=false;
					loading=true;
				}
			} catch (FindFailed e) {
				System.out.println("Loading Screen Not Found.");
			}
		}
	}
	public void stopSearch(){
		searchFlag = false;
	}
	public void lobbyChatMsg(Screen s, String msg){
		//Region rChats = Region.create(1072,310,579,307);

		//int i = 0;
		//while(i<200){
		try{
			s.wait(PARTY_CHATBAR, TIMEOUT);
		}catch(FindFailed e){e.printStackTrace();}
		if(s.exists(PARTY_CHATBAR)!=null){
			try {
				System.out.println(msg);
				Pattern party = new Pattern(PARTY_CHATBAR);
				party = party.targetOffset(-335, -15);
				s.click(party);
				//s.type(rChats.text());
				s.type(msg);
				s.type(Key.ENTER);
			} catch (FindFailed e) {
				e.printStackTrace();
			}
			//}
		}
	}
	public void clickFindMatch(Screen s){
		try{
			if(s.exists(FIND_MATCH)!=null){
				System.out.println("FIND MATCH FOUND");
				s.click(FIND_MATCH);
			}
		}catch(FindFailed ff){
			ff.printStackTrace();
		}
	}

	/**
	 * Work in progress... get the game clock time
	 * Possible Solution: Capture clock, increase contrast, then use .text() recog?
	 * @param s
	 */
	public void getGameTime(Screen s){
		//doters.focus();
		boolean gotTime = false;
		while(!gotTime){
		Region clock = Region.create(927,2,64,18);
		BufferedImage raw = s.capture(clock).getImage();
		//RescaleOp rescaleOp = new RescaleOp(1.0f, 0.5f, null);
		RescaleOp rescaleOp = new RescaleOp(0.3f, 0.2f, null);
		// Change Contrast
		ColorConvertOp cco = 
				new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
		cco.filter(raw,raw);
		rescaleOp.filter(raw, raw);
		Kernel kernel = new Kernel(3, 3,
				new float[] {
				0.f, -1.f, 0.f, -1.f, 5.f, -1.f, 0.f, -1.f, 0.f});
		BufferedImageOp op = new ConvolveOp(kernel);
		// 26:02 27:02
		//raw = op.filter(raw, null);
		//Region clock = s.create(930,2,57,18);
		TextRecognizer tr = TextRecognizer.getInstance();
		String extractedTime = tr.recognize(raw);
		String time="";
		int i = 0;
		System.out.println(extractedTime);
		for(i=0;i<extractedTime.length(); i++){

			if(Character.isDigit(extractedTime.charAt(i))){
				time+=extractedTime.charAt(i);
			}
			else if(extractedTime.charAt(i)==';' || extractedTime.charAt(i)==':'){
				time+=":";
			}
			else if(extractedTime.charAt(i)=='I'){
				time+="1";
			}
		}

		
		roshan_time = time;
		if(time.matches("([0-9]{1,2}:){1,2}[0-5][0-9]")){
			gotTime=true;
			msgTeam(s,"Time: " + time,false);
		}
		/*
		else
		{
			System.out.println("Time corrupted.");
			try {
				ImageIO.write(raw, "png", new File("imgs/capTimeFailed.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		*/
		System.out.println("Time: " + time);
		}
	}

	public void printLastRoshanTime(){
		if(!roshan_time.equals("0:00")){
			msgTeam(s, "Last Roshan was at: " + roshan_time, false);
		}
	}

	public void printColorChar(){
		msgTeam(s, "", false);
	}
	
	public void pauseGame(Screen s){
		doters.focus();
		try {
			s.type(Key.F9);
		} catch (FindFailed e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param s
	 * @param matchStr
	 * @return
	 */
	public boolean click(Screen s, String matchStr){
		try{
			s.click(matchStr);
		}catch(FindFailed ff){
			ff.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * @param s screen obj
	 * @param matchStr filepath to image to match
	 * @return true if clicked
	 */
	public boolean clickIf(Screen s, String matchStr){
		if(s.exists(matchStr)!=null){
			try{
				s.click(matchStr);
			}catch(FindFailed ff){
				ff.printStackTrace();
				return false;
			}
		}
		else
			return false;

		return true;
	}
	
	public boolean cancelSearch(Screen s){
		searchFlag = false;
		try{
			s.wait(CANCEL_SEARCH, TIMEOUT);
		}catch(FindFailed e){e.printStackTrace();}
		if(s.exists(CANCEL_SEARCH)!=null){
			try {

				s.click(CANCEL_SEARCH);
				searchFlag=false;
				return true;
			} catch (FindFailed e) {
				e.printStackTrace();
			}
			//}
		}
		return false;
	}

	public void findMatch2(Screen s){
		System.out.println("Finding Match Initialized");
		boolean searching = false;
		boolean loading = false;
		long tPauseTime = 1000;
		while(!loading){
			if(!searching){
			/*	if(s.exists(PLAY_TAB_UNSELECTED)!=null){
					try {
						s.click(PLAY_TAB_UNSELECTED);
					} catch (FindFailed e) {
						e.printStackTrace();
					}
				}
				*/
				if(s.exists(FIND_MATCH)!=null)
				{
					if(click(s, FIND_MATCH)){
						click(s, FIND_MATCH);
						System.out.println("Finding Match...");
						searching = true;
					}
				}
			}
			// Is searching wait for accept
			while(searching)
			{
				if(clickIf(s, ACCEPT))
				{
					searching = false;
					loading = true;
					System.out.println("Accepted!");
				}
			}
		}
	}
	/*
	 * find MM
	 */
	public boolean findMatch(Screen s){
		System.out.println("Finding Match Initialized...");
		//setState(Frame.ICONIFIED);
		long seconds=(1000*60);
		try{
			Thread.currentThread().wait(seconds*70);
		}catch(InterruptedException ie){}
		boolean searching = false;
		boolean loading = false;
		while(!loading){
			if(!searching){
				try{
					// If Dota Open then If play tab then If FIND A MATCH then if can click
				/*	s.wait(PLAY_TAB_UNSELECTED, TIMEOUT);
					if(s.exists(PLAY_TAB_UNSELECTED)!=null)	
						s.click(PLAY_TAB_UNSELECTED);
*/
					// Click Find Match
					s.wait(FIND_MATCH,TIMEOUT);
					s.click(FIND_MATCH);
					searching = true;
				}catch(FindFailed ff){
					ff.printStackTrace();
				}
			}
			while(searching){
				// Click Accept
				try {	
					s.wait(ACCEPT,TIMEOUT);
					s.click(ACCEPT);
				} catch (FindFailed ff) {
					ff.printStackTrace();
				}
				try {
					s.wait(DOTA_LOGO_LOADING,TIMEOUT*3.0);
					if(s.exists(DOTA_LOGO_LOADING)!=null){
						searching=false;
						loading=true;
					}
				} catch (FindFailed e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	/*
	 * Start Dota.exe If steam is signed on
	 */
	public void startDota2(Screen s){
		boolean error = false;
		tid = new Thread(new Runnable() {
			@Override
			public void run(){
				Screen s2 = new Screen();
				try {
					s2.type("r", KeyModifier.WIN);
					s2.type("steam://run/570");
					s2.type(Key.ENTER);
				} catch (FindFailed e) {
					e.printStackTrace();
				}
			}
		});
		tid.start();
	}
}