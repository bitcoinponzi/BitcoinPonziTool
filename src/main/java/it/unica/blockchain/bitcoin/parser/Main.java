package it.unica.blockchain.bitcoin.parser;


import com.mongodb.ServerAddress;
import it.unica.blockchain.bitcoin.parser.Model.EachValueForAddress;
import it.unica.blockchain.bitcoin.parser.Model.Responsiveness;
import org.bitcoinj.core.Transaction;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.core.*;
import weka.classifiers.trees.RandomForest;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.utils.BlockFileLoader;
import org.bitcoinj.utils.BriefLogFormatter;
import weka.core.converters.ConverterUtils;

import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;


public class Main {
    static int TEST = 6400;
    static boolean AorC = true; //False = without cluster, TRUE with cluster
    private static MongoConnector mongo;

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        CSVFile csv = new CSVFile();
        mongo = new MongoConnector();
        mongo.connect();
        CostSensitiveClassifier rf = null;
        try {
            rf = (CostSensitiveClassifier) SerializationHelper.read(new FileInputStream(Settings.RANDOMFORESTMODEL_PATH));
        } catch (Exception e) {
            System.out.println("Error in loading model!");
            e.printStackTrace();
        }

        //ArrayList<String> addtosave = mongo.getRandomAddress();
        //csv.writeAddressForTheModel(addtosave);
        ArrayList<String> newTest = new ArrayList<>();
        newTest = csv.readAddressFromFile();//Take the new Ponzi addresses used to test with another dataset
        Collections.shuffle(newTest);

        //System.out.println(rf.toString());

        int numberOfPonziAddress = 0;


        ArrayList<Long> cc = new ArrayList<>();
        ArrayList<String> addresses = new ArrayList<>();
        addresses = csv.getAddresses(); //Take the Ponzi addresses from file, for the original dataset
        numberOfPonziAddress = addresses.size();

        //addresses = mongo.getRandomAddress();
        boolean isShort = false;
        boolean isIncreasing = false;


        ArrayList<Float> giniIncomeValue = new ArrayList<>();
        ArrayList<Float> giniSendValue = new ArrayList<>();
        ArrayList<Float> giniIncomeClass = new ArrayList<>();
        ArrayList<Float> giniSendClass = new ArrayList<>();
        ArrayList<String> addressToStore = new ArrayList<>();
        ArrayList<Integer> totNumTransaction = new ArrayList<>();
        ArrayList<Integer> inNumTransaction = new ArrayList<>();
        ArrayList<Integer> outNumTransaction = new ArrayList<>();
        ArrayList<Integer> paying = new ArrayList<>();
        ArrayList<Integer> paid = new ArrayList<>();
        ArrayList<Integer> maxNumTrans = new ArrayList<>();

        ArrayList<Integer> lifeAddresses = new ArrayList<>();
        ArrayList<Integer> activityDay = new ArrayList<>();
        ArrayList<Integer> rangoCluster = new ArrayList<>();
        ArrayList<Boolean> frequentUse = new ArrayList<>();
        ArrayList<Boolean> moreTransac = new ArrayList<>();
        ArrayList<String> type = new ArrayList<>();
        ArrayList<Float> mediaIncomeV = new ArrayList<>();
        ArrayList<Float> mediaSendV = new ArrayList<>();
        ArrayList<Double> devStandardIncomeV = new ArrayList<>();
        ArrayList<Double> devStandardSendV = new ArrayList<>();
        ArrayList<Float> varianzaSendV = new ArrayList<>();
        ArrayList<Float> varianzaIncomeV = new ArrayList<>();
        ArrayList<Long> totalSendBTC = new ArrayList<>();
        ArrayList<Long> totalIncomeBTC = new ArrayList<>();
        ArrayList<Double> totalSendUSD = new ArrayList<>();
        ArrayList<Double> totalIncomeUSD = new ArrayList<>();
        ArrayList<Long> maxDiffDay = new ArrayList<>();
        ArrayList<Float> maxPercentDiffDay = new ArrayList<>();
        ArrayList<Integer> numberAddressesPaidAfterTheyPaid = new ArrayList<>();
        ArrayList<Responsiveness> responsivenesses = new ArrayList<>();
        ArrayList<String> addressesForCSV = new ArrayList<>();



        HashMap<String, Long> hashMap = csv.readAddressWithNumberTransactionFromFile();//take the address not Ponzi

        long numberAddressStore = 0;
        //HashMap<String,Long> hm =mongo.getRandomAddressWithMoreTransactions();
        //csv.writeAddress(hm);


        int i = 0;
        ArrayList<String> notPonziAddress = new ArrayList<>();
        notPonziAddress.addAll(hashMap.keySet());
        Collections.shuffle(notPonziAddress);

        addresses.clear();
        //addresses.addAll(notPonziAddress);


        int z = 0;
        long totallReceive = 0;
        long totallSend = 0;
        long max = 0;


        Set<String> addressRecognizedAsPonzi = new HashSet<>();

        for (String add : addresses) {
            System.out.println("Address : " + add + "                    i:" + i);

            CalculateFeatures cf = new CalculateFeatures();


            //Gini Values
            float giniIncomeClassToAdd;
            float giniSendClassToAdd;
            float giniIncomeValueToAdd;
            float giniSendValueToAdd;
            int clusterDim = 1;


            EachValueForAddress eachValueForAddress = new EachValueForAddress();
            ArrayList<String> clusterAddresses;
                try {
                    if (AorC) {
                        Set<String> clusterAddress =new TreeSet<>();;
                        Cluster cl = new Cluster();
                        clusterAddress = cl.heuristic1(mongo, add);
                        clusterAddress.add(add);
                        System.out.println("Size:" + clusterAddress.size());
                        clusterDim = clusterAddress.size();

                        clusterAddresses = new ArrayList<>(clusterAddress);

;
                        eachValueForAddress = cf.getDailyBalanceCluster(mongo, clusterAddresses);
                        //csv.writePlotter(eachValueForAddress.getDailyBalance(), "Cluster//Balance//" + add + ".csv");

                        //System.out.println("Address:" + clusterAddresses);
                    } else {
                        eachValueForAddress = cf.getDailyBalance(mongo, add);
                    }


                    HashMap<Integer, Long> dailyBalance = eachValueForAddress.getDailyBalance();
                    ArrayList<Long> arrayListValueIncome = eachValueForAddress.getArrayListValueIncome();
                    ArrayList<Long> arrayListValueSend = eachValueForAddress.getArrayListValueSend();

                    HashMap<Integer, Integer> incomeFreq = eachValueForAddress.getIncomeFreq();
                    HashMap<Integer, Integer> sendFreq = eachValueForAddress.getSendFreq();

                    mediaIncomeV.add(cf.getMedia(eachValueForAddress.getArrayListValueIncome()));
                    mediaSendV.add(cf.getMedia(eachValueForAddress.getArrayListValueSend()));
                    devStandardIncomeV.add(cf.getDevStandard(eachValueForAddress.getArrayListValueIncome(), mediaIncomeV.get(z)));
                    devStandardSendV.add(cf.getDevStandard(eachValueForAddress.getArrayListValueSend(), mediaSendV.get(z)));
                    varianzaSendV.add(cf.getVarianza(eachValueForAddress.getArrayListValueSend(), mediaSendV.get(z)));
                    varianzaIncomeV.add(cf.getVarianza(eachValueForAddress.getArrayListValueSend(), mediaIncomeV.get(z)));
                    totalSendBTC.add(cf.getTotalAmount(eachValueForAddress.getArrayListValueSend()));
                    totalIncomeBTC.add(cf.getTotalAmount(eachValueForAddress.getArrayListValueIncome()));
                    totalIncomeUSD.add(eachValueForAddress.getTotalUSDIncome());
                    totalSendUSD.add(eachValueForAddress.getTotalUSDSend());
                    rangoCluster.add(clusterDim);

                    int numT = cf.transactionNumber(arrayListValueSend, arrayListValueIncome);
                    
                    int numberDayActivity = cf.getNumberActivityDay(dailyBalance);
                    //System.out.println("Activity day: " + numberDayActivity);

                    giniIncomeClassToAdd = cf.calculateGiniForClassIncome(arrayListValueIncome);

                    giniSendClassToAdd = cf.calculateGiniForClassSend(arrayListValueSend);

                    giniIncomeValueToAdd = cf.calculateGiniForValueIncome(arrayListValueIncome);

                    giniSendValueToAdd = cf.calculateGiniForValueSend(arrayListValueSend);
                    //System.out.println("Send Value: " + giniSendValueToAdd);

                    if (i < numberOfPonziAddress) { //The Ponzi schemes are the first "numberOfPonziAddresses"
                        type.add("P");
                    } else {
                        type.add("N");
                    }

                    addressesForCSV.add(add);
                    giniIncomeClass.add(giniIncomeClassToAdd);
                    giniSendClass.add(giniSendClassToAdd);
                    giniSendValue.add(giniSendValueToAdd);
                    giniIncomeValue.add(giniIncomeValueToAdd);
                    addressToStore.add(add);
                    totNumTransaction.add(numT);
                    inNumTransaction.add(arrayListValueIncome.size());
                    outNumTransaction.add(arrayListValueSend.size());
                    paid.add(eachValueForAddress.getPaid());
                    paying.add(eachValueForAddress.getPaying());
                    numberAddressesPaidAfterTheyPaid.add(eachValueForAddress.getNumberAddressesPaidAfterTheyPaid());
                    lifeAddresses.add(incomeFreq.size());
                    activityDay.add(numberDayActivity);
                    frequentUse.add(cf.hasFrequentlyUsage(incomeFreq));
                    moreTransac.add(cf.hasMoreTransactionNumber(arrayListValueSend, arrayListValueIncome));
                    maxNumTrans.add(cf.maxTrans(incomeFreq));
                    maxDiffDay.add(cf.getMaxDiffDay(dailyBalance));
                    maxPercentDiffDay.add(cf.getPercentMaxDiffDay(dailyBalance));
                    responsivenesses.add(eachValueForAddress.getResponsiveness());
                    //System.out.println("PerCentMax: " + maxPercentDiffDay.get(z));
                    float percent = ((inNumTransaction.get(z)) * 100) / (inNumTransaction.get(z) + outNumTransaction.get(z));

                    System.out.println("InputPerCent:" + percent);
                    //totallReceive += cf.getTotal(arrayListValueIncome);
                    totallReceive += arrayListValueIncome.size();
                    if (cf.getTotal(arrayListValueIncome) > max) max = cf.getTotal(arrayListValueIncome);
                    System.out.println("Total Receive: " + cf.getTotal(arrayListValueIncome));
                    totallSend += cf.getTotal(arrayListValueSend);
                    System.out.println("Total Send: " + cf.getTotal(arrayListValueSend));
                    System.out.println("NumTransaCREceive: " + arrayListValueIncome.size());

                    z++;

                    /* //With this code, you can use the classifier obtained
                    Instances unlabeled = CalculateFeatures.getInstance(giniIncomeValueToAdd,giniSendValueToAdd,giniIncomeClassToAdd,giniSendClassToAdd,
                            numT,inNumTransaction.get(i),outNumTransaction.get(i),paying.get(i), paid.get(i), numberAddressesPaidAfterTheyPaid.get(i), lifeAddresses.get(i),numberDayActivity,
                            frequentUse.get(i),moreTransac.get(i),mediaIncomeV.get(i),mediaSendV.get(i),devStandardIncomeV.get(i),devStandardSendV.get(i),
                            varianzaSendV.get(i),varianzaIncomeV.get(i),totalSendBTC.get(i),totalIncomeBTC.get(i),totalSendUSD.get(i),totalIncomeUSD.get(i),
                            maxNumTrans.get(i),maxDiffDay.get(i),responsivenesses.get(i),rangoCluster.get(i));
					

                    // create copy
                    Instances labeled = new Instances(unlabeled);

                    double clsLabel = rf.classifyInstance(unlabeled.instance(0));
                    labeled.instance(0).setClassValue(clsLabel);
                    if(clsLabel == 0.0 ) {
                        System.out.println("Is Ponzi");
                        addressRecognizedAsPonzi.add(add);
                    }else{
                        System.out.println("Is Other");
                    }
                    */
                    System.out.println("------------------------------------------------");
                    System.out.println("");

                } catch (NullPointerException e) {
                    System.out.println("Errore:" + e);
                    e.printStackTrace();
                } catch (Exception e) {
                    System.out.println("Errore:" + e);
                    e.printStackTrace();
                }
    
            i++;
        }
        //csv.writeAddressRecognizedAsPonzi(addressRecognizedAsPonzi);

        System.out.println("Number Address: " + i);


        csv.writeStatistics("DatasetWith" + i + ".csv",addressesForCSV, giniIncomeValue, giniSendValue, giniIncomeClass, giniSendClass, totNumTransaction,
                inNumTransaction, outNumTransaction, paid, paying, numberAddressesPaidAfterTheyPaid, lifeAddresses, activityDay, frequentUse, moreTransac, type, mediaIncomeV,
                mediaSendV, devStandardIncomeV, devStandardSendV, varianzaSendV, varianzaIncomeV, totalSendBTC, totalIncomeBTC, totalSendUSD,
                totalIncomeUSD, maxNumTrans, maxDiffDay, responsivenesses, rangoCluster);


        mongo.disconnect();
        System.out.println("Elapsed time: " + (System.currentTimeMillis() - startTime) / 1000 + " secondi");
        System.out.println("Elapsed time: " + (float) (System.currentTimeMillis() - startTime) / 1000 / 60 + " minuti");
        System.out.println("Elapsed time: " + (float) (System.currentTimeMillis() - startTime) / 1000 / 60 / 60 + " ore");

    }


    /*
    * This method insert the new transaction that are in the blockchain in the mongo DB
    */
    private static void updateDB(MongoConnector mongo){
        //Initalize bitcoinJ

        BriefLogFormatter.init();
        NetworkParameters networkParameters = new MainNetParams();
        Context.getOrCreate(MainNetParams.get());

        //read the blockchain files from the disk
        List<File> blockChainFiles = new LinkedList<File>();
        for (int i = 977; i == 977; i++) {
            File file = new File(Settings.BLOCKCHAIN_PATH + String.format(Locale.US, "blk%05d.dat", i));
            if (!file.exists()) {
                System.out.println("file not exist");
                break;
            }
            blockChainFiles.add(file);
        }
        System.out.println("Size: " + blockChainFiles.size());
        //blockChainFiles.remove(blockChainFiles.size()-1);

        long lastBlockTime = mongo.getLastBlockTime();//Ultima Transazione presente nel DB

        BlockFileLoader bfl = new BlockFileLoader(networkParameters, blockChainFiles);

        // Iterate over the blocks in the blockchain.
        int height = 1;
        int numberTransaction = 0;        
        Block block1 = null;
        try {
            for (Block block : bfl) {
                System.out.println("Current block: " + height + "     HashBlock: " + block.getHashAsString() + "      Time: " + block.getTime());

                if (height % 1000 == 0) {
                    System.out.println("Current block: " + height + "     HashBlock: " + block.getHashAsString() + "      NumberTransaction: " + numberTransaction);
                }
                block1 = block;
            
            if(block.getTimeSeconds() > lastBlockTime) {
                height++;
                //System.out.println("Add Transactions to mongo!");
                for (Transaction t : block.getTransactions()) {
                    numberTransaction++;
                    mongo.addTransaction(t, block, height);
                }
                //}
                //if(numberTransaction>3000000)break;
            }else{
                height++;
            }
            
                height++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("BlockHash: " + block1.getHashAsString() + "    PrevBlockHash: " + block1.getPrevBlockHash());
        }
    }


    /*
    * Method used to order a Map<K, V>
    */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        return map.entrySet()
                .stream()
                //.sorted(Map.Entry.comparingByValue())
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }


}
