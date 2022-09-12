package cz4031.storage;

import cz4031.util.ObjectSizeFetcher;

import java.lang.instrument.Instrumentation;

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
	
	//Get Method for tConst
	public String getTConst() {
		return tConst;
	}

	//Set Method for tConst
	public void setTConst(String tConst) {
		this.tConst = tConst;
	}

	//Get Method for average rating
	public float getAvgRat() {
		return avgRat;
	}
	
	//Set Method for average rating
	public void setAvgRat(float avgRat) {
		this.avgRat = avgRat;
	}

	//Get Method for number of votes
	public int getNumVot() {
		return numVot;
	}

	//Set Method for number of votes
	public void setNumVot(int numVot) {
		this.numVot = numVot;
	}
	
	//Method to return size of 3 arguments
	// tConst is 9 char, rating is float so 4B and votes is integer so 4B also
	public static int size() {
		return 17;
	}

	@Override
	public String toString( ) {
		return "{" + tConst + "; " + avgRat + "; " + numVot + "}";
	}
}
