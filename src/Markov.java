import java.io.*;

class Markov {
static final int MAXGEN = 10000; // maximum words generated
	public Markov(String fileName){
		Chain chain = new Chain();
		int nwords = MAXGEN;
		// File for output
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(fileName));
			chain.build(in);
			chain.generate(nwords);
			in.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}