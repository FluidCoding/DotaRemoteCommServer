import java.util.Vector;

class Prefix {
	public Vector pref; // NPREF adjacent words from input
	static final int MULTIPLIER = 31; // for hashCode()
	// Prefix constructor: duplicate existing prefix
	Prefix(Prefix p)
	{
		pref = (Vector) p.pref.clone();
	}
	
	// Prefix constructor: n copies of str
	Prefix(int n, String str)
	{
		pref = new Vector();
		for (int i = 0; i < n; i++)
			pref.addElement(str);
	}
	
	// Prefix hashCode: generate hash from all prefix words
	public int hashCode()
	{
		int h = 0;
		for (int i = 0; i < pref.size(); i++)
			h = MULTIPLIER * h + pref.elementAt(i).hashCode();
		return h;
	}

	// Prefix equals: compare two prefixes for equal words
	public boolean equals(Object o)
	{
		Prefix p = (Prefix) o;
		for (int i = 0; i < pref.size(); i++)
			if (!pref.elementAt(i).equals(p.pref.elementAt(i)))
				return false;
		return true;
	}
}