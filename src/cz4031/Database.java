package cz4031;

import cz4031.storage.Record;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Database {

    public static void main(String[] args) throws IOException {
        final String DATA_PATH = "./resource/data.tsv";
        BufferedReader br = new BufferedReader(new FileReader(DATA_PATH));
        br.readLine(); // read the column names line
        String line;
        while ((line = br.readLine()) != null) {
            String[] record = line.split("\t");
            Record r = new Record(record[0], Float.parseFloat(record[1]), Integer.parseInt(record[2]));
        }

    }
}
