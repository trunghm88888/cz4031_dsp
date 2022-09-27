package cz4031;

import cz4031.index.BpTree;
import cz4031.storage.Address;
import cz4031.storage.Disk;
import cz4031.storage.Record;
import cz4031.util.Log;
import cz4031.util.Utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

public class Database implements Constants {
    private static final String TAG = "App";
    Scanner scanner = new Scanner(System.in);
    private Disk disk;
    private BpTree index;


    public void run(int blockSize) throws Exception {
        // load IMDB file
        List<Record> records = Utilities.loadRecord(DATA_FILE_PATH);

        disk = new Disk(Constants.DISK_SIZE, blockSize);
        index = new BpTree(blockSize);

        Log.defaut(TAG,"Running program with block size of "+blockSize);
        Log.defaut(TAG,"Initalising record insertion to stoage and building Index");
        Address recordAddr;
        for (Record r: records) {
            // inserting records into disk and create index!
            recordAddr = disk.appendRecord(r);
            index.insert(r.numVot, recordAddr);
        }
        Log.defaut(TAG,"Records inserted and Index created");
        disk.log();
//		index.logStructure(1); // printing root and first level?

        index.treeStats();

        pause("\nPress any key to start Experiment 3\n");
        doExperiment3();
        pause("\nPress any key to start Experiment 4\n");
        doExperiment4();
        pause("\nPress any key to start Experiment 5\n");
        doExperiment5();
    }

    public void doExperiment3(){
        Log.defaut(TAG,"Experiment 3 initalised, retreiving records with numVotes of 500");
        ArrayList<Address> e3RecordAddresses = index.getRecordsWithKey(500);
        ArrayList<Record> records = disk.getRecords(e3RecordAddresses);
        // records collected, do calculate average rating
        double avgRating = 0;
        for (Record record: records) {
            avgRating += record.avgRat;
        }
        avgRating /= records.size();
        Log.defaut("Average rating="+avgRating);
    }

    public void doExperiment4(){
        Log.defaut(TAG,"Experiment 4 initalised, getting records with numVotes between 30000-40000 ");
        ArrayList<Address> e4RecordAddresses = index.getRecordsInRange(30000,40000);
        ArrayList<Record> records = disk.getRecords(e4RecordAddresses);
        // records collected, do calculate average rating
        double avgRating = 0;
        for (Record record: records) {
            avgRating += record.avgRat;
        }
        avgRating /= records.size();
        Log.defaut("Average rating="+avgRating);
    }

    public void doExperiment5(){
        index.deleteKey(2000);
        // TODO: get back address and delete records from storage
    }


    // app menu
    private String getOptions(String[] options, boolean includeQuit){
        for (int i = 0; i < options.length; i++) {
            System.out.println(String.format("[%d] %s",i+1, options[i]));
        }
        if (includeQuit){
            System.out.println("[3] EXIT");
        }
        System.out.print("Enter Option: ");
        return scanner.nextLine();
    }
    private void pause(){
        pause(null);
    }
    
    private void pause(String message){
        if (message == null){
            message = "Press any key to continue";
        }
        System.out.print(message);
        scanner.nextLine();
    }

    public void displayMainMenu() throws Exception {
        String[] menu = {
                "200B",
                "500B",
        };
        String input;
        do {
            System.out.println("CZ4031 - Database Assignment 1 (Group "+GROUP_NUM+")");
            System.out.println("Select Experiment Block Size:");
            input = getOptions(menu, true);
            switch (input) {
                case "1":
                    run(BLOCK_SIZE_200);
                    pause();
                    break;
                case "2" :
                    run(BLOCK_SIZE_500);
                    pause();
                    break;
            }
        } while (!input.equals("3"));
    }

    public static void main(String[] args) {
        try {
            // Log.setLevel(Log.LEVEL_DEBUG);
            Log.setLevel(Log.LEVEL_Default);
            Database app = new Database();
            app.displayMainMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
