package it.unica.blockchain.bitcoin.parser;

import it.unica.blockchain.bitcoin.parser.Model.Responsiveness;
import it.unica.blockchain.bitcoin.parser.Model.Triple;

import java.io.*;
import java.util.*;

/**
 * Created by Ponzi on 20/03/2017.
 */
public class CSVFile {
    //CSV
    private static final String CSVBasePath = "CSV//";
    private static final String CSVAddressesPath = "CSV//Addresses.csv";
    private String COMMA_DEL;
    private String NEW_LINE;
    private String FILE_HEAD;
    private String SPACE;
    private FileWriter fileWriter;
    private BufferedReader fileReader;


    CSVFile() {
        fileWriter = null;
        fileReader = null;
        COMMA_DEL = ",";
        SPACE = " ";
        NEW_LINE = "\n";
        FILE_HEAD = "day,value";
    }

    /*
    * The method writePlotter receive an hash map containing some day by day graphic and write it into a csv file
    * @param HashMap<Day,Value> containing some day by day graphic to write
    * @param String the path where create the csv file graphic
    * @return void
    */
    void writePlotter(HashMap<Integer, Long> dayByDay, String path, String head) {
        try {
            fileWriter = new FileWriter(CSVBasePath + path);

            //Write the CSV file header
            fileWriter.append(head);

            //Add a new line separator after the header
            fileWriter.append(NEW_LINE);
            for (int i = 0; i < dayByDay.size(); i++) {
                Long valueOfTheDay = dayByDay.get(i);
                fileWriter.append(new Integer(i).toString());
                fileWriter.append(COMMA_DEL);
                //System.out.println(" GIorno " + i + "  valore" + valueOfTheDay);
                fileWriter.append(valueOfTheDay.toString());   //fileWriter.append(COMMA_DEL);
                fileWriter.append(NEW_LINE);
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }
    }

    /*
    * The method writePlotter receive an hash map containing some day by day graphic and write it into a csv file
    * @param HashMap<Day,Value> containing some day by day graphic to write
    * @param String the path where create the csv file graphic
    * @return void
    */
    void writePlotterWithSeries(HashMap<Integer, ArrayList<Long>> dayByDay, String path, String head) {
        try {
            fileWriter = new FileWriter(CSVBasePath + path);

            //Write the CSV file header
            fileWriter.append(head);

            //Add a new line separator after the header
            fileWriter.append(NEW_LINE);
            for (int i = 0; i < dayByDay.size(); i++) {
                ArrayList<Long> valueOfTheDay = dayByDay.get(i);

                fileWriter.append(new Integer(i).toString());
                fileWriter.append(COMMA_DEL);
                //System.out.println(" GIorno " + i + "  valore" + valueOfTheDay);
                for (Long cl : valueOfTheDay) {
                    fileWriter.append(cl.toString());   //fileWriter.append(COMMA_DEL);
                    fileWriter.append(COMMA_DEL);
                }
                fileWriter.append(NEW_LINE);
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }
    }

    /*
 * The method writePlotter receive an hash map containing some day by day graphic and write it into a csv file
 * @param HashMap<Day,Value> containing some day by day graphic to write
 * @param String the path where create the csv file graphic
 * @return void
 */
    void writePlotter(HashMap<Integer, Long> dayByDay, String path) {
        try {
            fileWriter = new FileWriter(CSVBasePath + path);

            //Write the CSV file header
            fileWriter.append(FILE_HEAD);

            //Add a new line separator after the header
            fileWriter.append(NEW_LINE);
            for (int i = 0; i < dayByDay.size(); i++) {
                Long valueOfTheDay = dayByDay.get(i);
                fileWriter.append(new Integer(i).toString());
                fileWriter.append(COMMA_DEL);
                //System.out.println(" GIorno " + i + "  valore" + valueOfTheDay);
                fileWriter.append(valueOfTheDay.toString());   //fileWriter.append(COMMA_DEL);
                fileWriter.append(NEW_LINE);
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }
    }

    /*
    * The method getAddresses read the addresses from a file and get an ArrayList containing that addresses
    * @return ArrayList<String> of addresses
     */
    ArrayList<String> getAddresses() {
        ArrayList<String> addresses = new ArrayList<>();
        try {
            String line;
            fileReader = new BufferedReader(new FileReader(CSVAddressesPath)); //The Ponzi addresses
            String prev = "link";
            fileReader.readLine();
            while ((line = fileReader.readLine()) != null) {
                // use comma as separator
                String[] cols = line.split(COMMA_DEL);
                addresses.add(cols[0]);
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileReader!");
            e.printStackTrace();
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileReader!");
                e.printStackTrace();
            }
        }
        return addresses;

    }

    /*
    * The method getDailyIncome receive an address and get the an HashMap<Integer, Long> containing the coin receive by the address day by day
    * @param String The address
    * @return HashMap<Integer,Long> of the coin receive by the address day by day
     */
    HashMap<Integer, Long> getDailyIncome(String address) {
        HashMap<Integer, Long> dailyIncome = new HashMap<>();

        try {
            String line;
            fileReader = new BufferedReader(new FileReader(CSVBasePath + "Receive\\Money\\" + address + ".csv"));
            fileReader.readLine();//la prima riga è l'intestazione
            while ((line = fileReader.readLine()) != null) {
                // use comma as separator
                String[] cols = line.split(COMMA_DEL);
                int day = Integer.parseInt(cols[0]);
                long value = Long.parseLong(cols[1]);

                dailyIncome.put(day, value);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error in CsvFileReader!");
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileReader!");
                e.printStackTrace();
            }
        }

        return dailyIncome;
    }

    /*
    * The method getDailySend receive an address and get the an HashMap<Integer, Long> containing the coin send by the address day by day
    * @param String The address
    * @return HashMap<Integer,Long> of the coin send by the address day by day
    */
    HashMap<Integer, Long> getDailySend(String address) {
        HashMap<Integer, Long> dailySend = new HashMap<>();

        try {
            String line;
            fileReader = new BufferedReader(new FileReader(CSVBasePath + "Send\\Money\\" + address + ".csv"));
            fileReader.readLine();
            while ((line = fileReader.readLine()) != null) {
                // use comma as separator
                String[] cols = line.split(COMMA_DEL);
                int day = Integer.parseInt(cols[0]);
                long value = Long.parseLong(cols[1]);
                dailySend.put(day, value);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error in CsvFileReader!");
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileReader!");
                e.printStackTrace();
            }
        }

        return dailySend;
    }

    /*
    * The method getDailyBalance Balance an address and get the an HashMap<Integer, Long> containing the coin send by the address day by day
    * @param String The address
    * @return HashMap<Integer,Long> of the coin send by the address day by day
    */
    HashMap<Integer, Long> getDailyBalance(String address) {
        HashMap<Integer, Long> dailySend = new HashMap<>();

        try {
            String line;
            fileReader = new BufferedReader(new FileReader(CSVBasePath + "Balance\\Money\\" + address + ".csv"));
            fileReader.readLine();
            while ((line = fileReader.readLine()) != null) {
                // use comma as separator
                String[] cols = line.split(COMMA_DEL);
                int day = Integer.parseInt(cols[0]);
                long value = Long.parseLong(cols[1]);
                dailySend.put(day, value);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error in CsvFileReader!");
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileReader!");
                e.printStackTrace();
            }
        }
        return dailySend;
    }

    /*
    * The method getDailyBalance Balance an address and get the an HashMap<Integer, Long> containing the coin send by the address day by day
    * @param String The address
    * @return HashMap<Integer,Long> of the coin send by the address day by day
    */
    HashMap<Integer, Long> getDailyBalance(String address, String path) {
        HashMap<Integer, Long> dailySend = new HashMap<>();

        try {
            String line;
            fileReader = new BufferedReader(new FileReader(CSVBasePath + path + address + ".csv"));
            fileReader.readLine();
            while ((line = fileReader.readLine()) != null) {
                // use comma as separator
                String[] cols = line.split(COMMA_DEL);
                int day = Integer.parseInt(cols[0]);
                long value = Long.parseLong(cols[1]);
                dailySend.put(day, value);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error in CsvFileReader!");
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileReader!");
                e.printStackTrace();
            }
        }
        return dailySend;
    }


    void writeStatistics(String nameFile, ArrayList<String> address, ArrayList<Long> giniIncomeValue, ArrayList<Long> giniSendValue, ArrayList<Long> giniIncomeClass, ArrayList<Long> giniSendClass) {
        try {
            fileWriter = new FileWriter(CSVBasePath + "Statistics//" + nameFile);

            //Write the CSV file header
            fileWriter.append("address,GiniIncomeValue,GiniSendValue,GiniIncomeClass,GiniSendClass");

            //Add a new line separator after the header
            fileWriter.append(NEW_LINE);
            for (int i = 0; i < address.size(); i++) {
                fileWriter.append(address.get(i));
                fileWriter.append(COMMA_DEL);
                fileWriter.append(Long.toString(giniIncomeValue.get(i)));
                fileWriter.append(COMMA_DEL);
                fileWriter.append(Long.toString(giniSendValue.get(i)));
                fileWriter.append(COMMA_DEL);
                fileWriter.append(Long.toString(giniIncomeClass.get(i)));
                fileWriter.append(COMMA_DEL);
                fileWriter.append(Long.toString(giniSendClass.get(i)));
                fileWriter.append(COMMA_DEL);
                fileWriter.append(NEW_LINE);
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }
    }

    void writeStatistics(String nameFile, ArrayList<String> address, ArrayList<Long> giniIncomeValue, ArrayList<Long> giniSendValue, ArrayList<Long> giniIncomeClass, ArrayList<Long> giniSendClass, ArrayList<Long> numberTransaction) {
        try {
            fileWriter = new FileWriter(CSVBasePath + "Statistics//" + nameFile);

            //Write the CSV file header
            fileWriter.append("address,GiniIncomeValue,GiniSendValue,GiniIncomeClass,GiniSendClass,NumberTransaction");

            //Add a new line separator after the header
            fileWriter.append(NEW_LINE);
            for (int i = 0; i < address.size(); i++) {
                fileWriter.append(address.get(i));
                fileWriter.append(COMMA_DEL);
                fileWriter.append(Long.toString(giniIncomeValue.get(i)));
                fileWriter.append(COMMA_DEL);
                fileWriter.append(Long.toString(giniSendValue.get(i)));
                fileWriter.append(COMMA_DEL);
                fileWriter.append(Long.toString(giniIncomeClass.get(i)));
                fileWriter.append(COMMA_DEL);
                fileWriter.append(Long.toString(giniSendClass.get(i)));
                fileWriter.append(COMMA_DEL);
                fileWriter.append(Long.toString(numberTransaction.get(i)));
                fileWriter.append(COMMA_DEL);
                fileWriter.append(NEW_LINE);
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }
    }


    void writeAddress(HashMap<String, Long> hashMap) {
        try {
            fileWriter = new FileWriter(CSVBasePath + "AddressesWithNumberTransaction.csv");

            //Write the CSV file header
            fileWriter.append("address,numberTransaction");

            //Add a new line separator after the header
            fileWriter.append(NEW_LINE);
            for (Map.Entry entry : hashMap.entrySet()) {
                fileWriter.append(entry.getKey().toString());
                fileWriter.append(COMMA_DEL);
                fileWriter.append(entry.getValue().toString());
                fileWriter.append(NEW_LINE);
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }
    }

    public HashMap<String, Long> readAddressWithNumberTransactionFromFile() {
        HashMap<String, Long> addressWithNumberTransactions = new HashMap<>();

        try {
            String line;
            fileReader = new BufferedReader(new FileReader(CSVBasePath + "AddressesWithNumberTransaction.csv"));
            fileReader.readLine();
            while ((line = fileReader.readLine()) != null) {
                // use comma as separator
                String[] cols = line.split(COMMA_DEL);
                String address = cols[0];
                long value = Long.parseLong(cols[1]);
                addressWithNumberTransactions.put(address, value);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error in CsvFileReader!");
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileReader!");
                e.printStackTrace();
            }
        }

        return addressWithNumberTransactions;
    }

    public void writeAddressPonziOrNot(ArrayList<String> address, ArrayList<Integer> numbertTransaction, ArrayList<Boolean> isPonzi) {
        try {
            fileWriter = new FileWriter(CSVBasePath + "AddressPonziOrNotTag.csv");

            //Write the CSV file header
            fileWriter.append("address,numberTransaction,isPonzi");

            //Add a new line separator after the header
            fileWriter.append(NEW_LINE);
            for (int i = 0; i < address.size(); i++) {
                fileWriter.append(address.get(i));
                fileWriter.append(COMMA_DEL);
                fileWriter.append(numbertTransaction.get(i).toString());
                fileWriter.append(COMMA_DEL);
                fileWriter.append(isPonzi.get(i).toString());
                fileWriter.append(COMMA_DEL);
                fileWriter.append(NEW_LINE);
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }
    }

    void writeStatistics(String nameFile, ArrayList<String> addressesForCSV, ArrayList<Float> giniIncomeValue, ArrayList<Float> giniSendValue, ArrayList<Float> giniIncomeClass, ArrayList<Float> giniSendClass, ArrayList<Integer> totNumTransaction,
                         ArrayList<Integer> inNumTransaction, ArrayList<Integer> outNumTransaction, ArrayList<Integer> paid, ArrayList<Integer> paying, ArrayList<Integer> numberAddressesPaidAfterTheyPaid, ArrayList<Integer> lifeAddresses, ArrayList<Integer> activityDay, ArrayList<Boolean> frequentUse,
                         ArrayList<Boolean> moreTransac, ArrayList<String> type, ArrayList<Float> mediaIncomeV, ArrayList<Float> mediaSendV, ArrayList<Double> devStandardIncomeV, ArrayList<Double> devStandardSendV,
                         ArrayList<Float> varianzaSendV, ArrayList<Float> varianzaIncomeV, ArrayList<Long> totalSendBTC, ArrayList<Long> totalIncomeBTC, ArrayList<Double> totalSendUSD, ArrayList<Double> totalIncomeUSD,
                         ArrayList<Integer> maxNumTransRec, ArrayList<Long> maxDiffDay, ArrayList<Responsiveness> responsivenesses, ArrayList<Integer> rangoCluster) {
        try {
            fileWriter = new FileWriter(CSVBasePath + "Statistics//" + nameFile);

            //Write the CSV file header
            fileWriter.append("Address,GiniIncomeClass,GiniSendClass,GiniIncomeValue,GiniSendValue,totNumTransaction," +
                    "inNumTransaction,outNumTransaction,paying,paid,numberAddressesPaidAfterTheyPaid,lifeAddress," +
                    "activityDay,frequentUse,moreTransaction,inPerCent,mediaIncomeV,mediaSendV,devStandardIncomeV," +
                    "devStandardSendV,varianzaSendV,varianzaIncomeV,totalSendBTC,totalIncomeBTC,totalSendUSD," +
                    "totalIncomeUSD,maxNumTransRec,maxDiffDay,minResponsiveness,avgResponsiveness," +
                    "maxResponsiveness,rangoCluster,type");

            //Add a new line separator after the header
            fileWriter.append(NEW_LINE);
            for (int i = 0; i < giniIncomeClass.size(); i++) {
                fileWriter.append(addressesForCSV.get(i)); //Address
                fileWriter.append(SPACE);
                fileWriter.append(Float.toString(giniIncomeClass.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Float.toString(giniSendClass.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Float.toString(giniIncomeValue.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Float.toString(giniSendValue.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Integer.toString(totNumTransaction.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Integer.toString(inNumTransaction.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Integer.toString(outNumTransaction.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Integer.toString(paying.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Integer.toString(paid.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Integer.toString(numberAddressesPaidAfterTheyPaid.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Integer.toString(lifeAddresses.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Integer.toString(activityDay.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Boolean.toString(frequentUse.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Boolean.toString(moreTransac.get(i)));
                fileWriter.append(SPACE);
                float percent = (float) ((inNumTransaction.get(i)) * 100) / (inNumTransaction.get(i) + outNumTransaction.get(i));
                fileWriter.append(Float.toString(percent));
                fileWriter.append(SPACE);
                fileWriter.append(Float.toString(mediaIncomeV.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Float.toString(mediaSendV.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Double.toString(devStandardIncomeV.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Double.toString(devStandardSendV.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Float.toString(varianzaSendV.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Float.toString(varianzaIncomeV.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Long.toString(totalSendBTC.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Long.toString(totalIncomeBTC.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Double.toString(totalSendUSD.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Double.toString(totalIncomeUSD.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Integer.toString(maxNumTransRec.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Long.toString(maxDiffDay.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(Long.toString(responsivenesses.get(i).getMin())); //Responsiveness
                fileWriter.append(SPACE);
                fileWriter.append(Long.toString(responsivenesses.get(i).getAvg())); //Responsiveness
                fileWriter.append(SPACE);
                fileWriter.append(Long.toString(responsivenesses.get(i).getMax())); //Responsiveness
                fileWriter.append(SPACE);
                fileWriter.append(Integer.toString(rangoCluster.get(i)));
                fileWriter.append(SPACE);
                fileWriter.append(type.get(i)); //Tipo
                fileWriter.append(SPACE);
                fileWriter.append(NEW_LINE);
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }
    }

    ArrayList<Triple<String, ArrayList<String>, String>> getDonationAddresses() {
        ArrayList<Triple<String, ArrayList<String>, String>> donationAddresses = new ArrayList<>();
        ArrayList<String> links = new ArrayList<>();
        try {
            String line;
            fileReader = new BufferedReader(new FileReader("MongoBlockchain//CSV//Address//AddressDonation.csv"));
            String prev = "link";
            fileReader.readLine();
            while ((line = fileReader.readLine()) != null) {
                // use comma as separator
                String[] cols = line.split(COMMA_DEL);
                String name = cols[0];
                ArrayList<String> addresses = new ArrayList<>();
                String type = cols[2];
                String[] add = cols[1].split("-");
                for (String ad : add) {
                    addresses.add(ad);
                }
                //System.out.println("Type: "+type+"   Name: "+name+"    Address: "+addresses+"    Size: "+add.length);
                donationAddresses.add(new Triple(name, addresses, type));
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileReader!");
            e.printStackTrace();
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileReader!");
                e.printStackTrace();
            }
        }

        return donationAddresses;
    }

    ArrayList<String> getRansomwareAddresses() {
        ArrayList<String> addresses = new ArrayList<>();
        try {
            String line;
            fileReader = new BufferedReader(new FileReader("MongoBlockchain//CSV//Address//ransomware.csv"));
            fileReader.readLine();
            while ((line = fileReader.readLine()) != null) {
                // use comma as separator
                String[] cols = line.split(COMMA_DEL);
                String name = cols[0];
                addresses.add(name);
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileReader!");
            e.printStackTrace();
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileReader!");
                e.printStackTrace();
            }
        }

        return addresses;
    }


    void writeAddressForTheModel(ArrayList<String> arrayList) {
        try {
            fileWriter = new FileWriter(CSVBasePath + "AddressesForNewModel.csv");

            //Write the CSV file header
            fileWriter.append("address");

            //Add a new line separator after the header
            fileWriter.append(NEW_LINE);
            for (String add:arrayList) {
                fileWriter.append(add);
                 fileWriter.append(NEW_LINE);
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }
    }

    public ArrayList<String> readAddressFromFile() {
        ArrayList<String> addresses = new ArrayList<>();

        try {
            String line;
            fileReader = new BufferedReader(new FileReader(CSVBasePath + "AddressesForNewModel.csv"));
            fileReader.readLine();
            while ((line = fileReader.readLine()) != null) {
                // use comma as separator
                addresses.add(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error in CsvFileReader!");
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileReader!");
                e.printStackTrace();
            }
        }

        return addresses;
    }

    void writeAddressRecognizedAsPonzi(Set<String> arrayList) {
        try {
            fileWriter = new FileWriter(CSVBasePath + "AddressesRecognizedAsPonzi.csv");

            //Write the CSV file header
            fileWriter.append("address");

            //Add a new line separator after the header
            fileWriter.append(NEW_LINE);
            for (String add:arrayList) {
                fileWriter.append(add);
                fileWriter.append(NEW_LINE);
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }
    }
}
