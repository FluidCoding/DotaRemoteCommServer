import java.io.*;
import java.util.*;


class Chain {
	static final int NPREF = 2; // size of prefix
	static final String NONWORD = "\n";
	// "word" that can't appear
	public Hashtable statetab = new Hashtable();
	// key = Prefix, value = suffix Vector
	Prefix prefix = new Prefix(NPREF, NONWORD);
	// initial prefix
	Random rand = new Random();
	// Chain build: build State table from input stream
	void build(InputStream in) throws IOException
	{
		StreamTokenizer st = new StreamTokenizer(in);
		st.resetSyntax(); // remove default rules
		st.wordChars(0, Character.MAX_VALUE); // turn on all chars
		st.whitespaceChars(0, ' '); // except up to blank
		while (st.nextToken() != st.TT_EOF)
			add(st.sval);
		add(NONWORD);
	}
	void build(BufferedReader in) throws IOException
	{   
		String s;
		StringTokenizer st;
		while((s = in.readLine())!=null)
		{
			st = new StringTokenizer(s, " ");
			while (st.hasMoreTokens())
			{
				add(st.nextToken());
			}
		}
		add(NONWORD);
	}
	
	// Chain add: add word to suffix list, update prefix
	void add(String word)
	{
		Vector suf = (Vector) statetab.get(prefix);
		if (suf == null) {
			suf = new Vector();
			statetab.put(new Prefix(prefix), suf);
		}
		suf.addElement(word);
		prefix.pref.removeElementAt(0);
		prefix.pref.addElement(word);
	}

	// Chain generate: generate output words
	void generate(int nwords) throws IOException
	{
		PrintWriter fOut = new PrintWriter(new FileWriter("output.txt"));
		prefix = new Prefix(NPREF, NONWORD);
		for (int i = 0; i < nwords; i++) {
			Vector s = (Vector) statetab.get(prefix);
			if (s == null) {
				System.err.println("Markov: internal error: no state");
				System.exit(1);
			}
			int r = Math.abs(rand.nextInt()) % s.size();
			String suf = (String) s.elementAt(r);
			if (suf.equals(NONWORD))
				break;
			fOut.write(suf+" ");
			prefix.pref.removeElementAt(0);
			prefix.pref.addElement(suf);
			//OUTPUT CONSOLE
			//System.out.print(suf + " ");
		}
		System.out.println("");
		fOut.close();
	}
}