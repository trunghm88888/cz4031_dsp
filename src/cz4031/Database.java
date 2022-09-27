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
        // read records from data file
        List<Record> records = Utilities.loadRecord(DATA_FILE_PATH);

        disk = new Disk(Constants.DISK_SIZE, blockSize);
        index = new BpTree(blockSize);

        Log.defaut(TAG,"Running program with block size of "+blockSize);
        Log.defaut(TAG,"Prepare to insert records into storage and create index");
        Address recordAddr;
        for (Record r: records) {
            // inserting records into disk and create index!
            recordAddr = disk.appendRecord(r);
            index.insert( r.numVot, recordAddr);
        }
        Log.defaut(TAG,"Record inserted into storage and index created");
        disk.log();
//		index.logStructure(1); // printing root and first level?

        index.treeStats();

        // TODO do experiences
        pause("Press any key to start Experiment 3");
        doExperiment3();
        pause("Press any key to start Experiment 4");
        doExperiment4();
        pause("Press any key to start Experiment 5");
        doExperiment5();
    }

    public void doExperiment3(){
        Log.defaut(TAG,"Experiment 3 started, getting records with numVotes of 500");
        ArrayList<Address> e3RecordAddresses = index.getRecordsWithKey(1000, true);
        ArrayList<Record> records = disk.getRecords(e3RecordAddresses, true);
        // records collected, do calculate average rating
        double avgRating = 0;
        for (Record record: records) {
            avgRating += record.avgRat;
        }
        avgRating /= records.size();
        Log.defaut("Average rating="+avgRating);
    }

    public void doExperiment4(){
        Log.defaut(TAG,"Experiment 4 started, getting records with numVotes between 30k-40k ");
        ArrayList<Address> e4RecordAddresses = index.getRecordsWithKeyInRange(30000,40000, true);
        ArrayList<Record> records = disk.getRecords(e4RecordAddresses, true);
        // records collected, do calculate average rating
        double avgRating = 0;
        for (Record record: records) {
            avgRating += record.avgRat;
        }
        avgRating /= records.size();
        Log.defaut("Average rating="+avgRating);
    }

    public void doExperiment5(){
        index.deleteKey(1000);
        // TODO: get back address and delete records from storage
    }


    // app menu
    private String getOptions(String[] options, boolean includeQuit){
        for (int i = 0; i < options.length; i++) {
            System.out.println(String.format("[%d] %s",i+1, options[i]));
        }
        if (includeQuit){
            System.out.println("[q] quit");
        }
        System.out.print("Enter the option: ");
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
                "Experiment with block size 200B",
                "Experiment with block size 500B",
        };
        String input;
        do {
            System.out.println("CZ4031 - Database Assignment 1 (Group "+GROUP_NUM+")");
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
//                case "3":
//                    displayLogSetting();
//                    break;
            }
        } while (!input.equals("q"));
    }

//    public void displayLogSetting(){
//        String[] menu;
//        String input;
//        do {
//            menu = new String[]{
//                    String.format("Adjust log level (current: %s)", Log.getLogLevelString()),
//                    String.format("include timestamp (current %b)", Log.isTimestampEnabled()),
//                    String.format("change timestamp format (current: %s)", Log.getTimestampFormat())
//            };
//            System.out.println("Log Setting");
//            input = getOptions(menu, true);
//            switch (input){
//                case "1":
//                    adjustLogLevel();
//                    break;
//                case "2":
//                    adjustLogTimestamp();
//                    break;
//                case "3":
//                    adjustLogTimestampFormat();
//                    break;
//            }
//        } while (!input.equals("q"));
//
//    }
//
//    private void adjustLogLevel(){
//        String[] menu = {
//                "None", "Error", "Warn", "Info", "Debug", "Verbose"
//        };
//        String input = getOptions(menu, false);
//        switch (input){
//            case "1":
//                Log.setLevel(Log.LEVEL_NONE);
//                break;
//            case "2":
//                Log.setLevel(Log.LEVEL_ERROR);
//                break;
//            case "3":
//                Log.setLevel(Log.LEVEL_WARN);
//                break;
//            case "4":
//                Log.setLevel(Log.LEVEL_INFO);
//                break;
//            case "5":
//                Log.setLevel(Log.LEVEL_DEBUG);
//                break;
//            case "6":
//                Log.setLevel(Log.LEVEL_VERBOSE);
//                break;
//        }
//    }
//    private void adjustLogTimestamp(){
//        String[] menu = {"Enable", "Disable"};
//        String input = getOptions(menu, false);
//        switch (input){
//            case "1":
//                Log.setTimestampEnabled(true);
//                break;
//            case "2":
//                Log.setTimestampEnabled(false);
//                break;
//        }
//    }
//    private void adjustLogTimestampFormat(){
//        String[] menu = {"Detail", "Simple"};
//        String input = getOptions(menu, false);
//        switch (input){
//            case "1":
//                Log.setTimestampFormat(Log.FORMAT_DETAIL);
//                break;
//            case "2":
//                Log.setTimestampFormat(Log.FORMAT_SIMPLE);
//                break;
//        }
//    }


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
