package cz4031;

import cz4031.index.BpTree;
import cz4031.storage.Address;
import cz4031.storage.Disk;
import cz4031.storage.Record;
import cz4031.util.Constants;
import cz4031.util.Log;
import cz4031.util.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Database implements Constants {
    Scanner scanner = new Scanner(System.in);
    private Disk disk;
    private BpTree index;


    public void runDatabase(int blockSize) throws Exception {
        // load IMDB file
        System.out.println("\nExperiment 1 & 2: Store Data & Build B+ Tree on 'numVotes' ");
        List<Record> records = Utilities.loadRecord(DATA_FILE_PATH);

        disk = new Disk(Constants.DISK_SIZE, blockSize);
        index = new BpTree(blockSize);
        
        System.out.println("Implementing database with block size: " + blockSize);
        System.out.println("Initalising record insertion and building index...");
        Address recordAddr;
        for (Record r: records) {
            // inserting records into disk and create index!
            recordAddr = disk.appendRecord(r);
            index.insert(r.numVot, recordAddr);
        }
        System.out.println("COMPLETED: Records inserted and index created");
        disk.diskStats();
//		index.logStructure(1); // printing root and first level?

        index.treeStats();

        pause("\nPress any key to start Experiment 3\n");
        experiment3();
        pause("\nPress any key to start Experiment 4\n");
        experiment4();
        pause("\nPress any key to start Experiment 5\n");
        experiment5();
    }

    public void experiment3(){
        System.out.println("Experiment 3: Retreiving records with numVotes = 500");
        //ArrayList<Address> e3RecordAddresses = index.getRecordsWithKey(1000);
        ArrayList<Record> records = disk.getRecords(index.getRecordsWithKey(500));
        // records collected, do calculate average rating
        double avgRating = 0;
        for (Record record: records) {
            avgRating += record.avgRat;
        }
        avgRating /= records.size();
        System.out.println("Average Rating: "+avgRating);
        //Log.defaultPrint("Average rating="+avgRating);
    }

    public void experiment4(){
        System.out.println("Experiment 4: Retreiving records with numVotes from: 30000-40000");
        //ArrayList<Address> e4RecordAddresses = index.getRecordsInRange(30000,40000);
        ArrayList<Record> records = disk.getRecords(index.getRecordsInRange(30000,40000));
        // records collected, do calculate average rating
        double avgRating = 0;
        for (Record record: records) {
            avgRating += record.avgRat;
        }
        avgRating /= records.size();
        Log.defaultPrint("Average rating="+avgRating);
    }

    public void experiment5(){
        System.out.println("Experiment 5: Delete records with numVotes = 1000 ");
        index.deleteKey(1000);
        // TODO: get back address and delete records from storage
    }


    // app menu
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
    
    private String getOptions(String[] options){
        for (int i = 0; i < options.length; i++) {
            System.out.println(String.format("[%d] %s",i+1, options[i]));
        }
        System.out.println("[3] EXIT\n");
    
        System.out.print("Enter Option: ");
        return scanner.nextLine();
    }

    public void displayMainMenu() throws Exception {
        String[] menu = {
                "200B",
                "500B",
        };
        String input;
        do {
            System.out.println("=======================================");
            System.out.println("\nCZ4031 Database Project 1 [Group 1] ");
            System.out.println("Select Experiment Block Size:");
            input = getOptions(menu);
            switch (input) {
                case "1":
                    runDatabase(BLOCK_SIZE_200);
                    pause();
                    break;
                case "2" :
                    runDatabase(BLOCK_SIZE_500);
                    pause();
                    break;
            }
        } while (!input.equals("3"));
    }

    public static void main(String[] args) {
        try {
            // Log.setLevel(Log.LEVEL_DEBUG);
            //Log.setState(Log.STATE_Default);
            Database app = new Database();
            app.displayMainMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
