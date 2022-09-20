package cz4031.storage;


//class for recording tconst (unique identifier), average rating and number of votes
public class Record {
	public String tConst;
	public float avgRat;
	public int numVot;
	
	//Constructor
	public Record(String tConst, float avgRat, int numVot) {
		this.tConst = tConst;
		this.avgRat = avgRat;
		this.numVot = numVot;
	}
	
	// Method to return size of 3 arguments
	// tConst is fixed-size 9 char, rating is float so 4B and votes is integer so 4B also
	public static int size() {
		return 17;
	}

	public String toString( ) {
		return "{tconst: " + tConst + "; avgRat: " + avgRat + "; numVot: " + numVot + "}";
	}
}
