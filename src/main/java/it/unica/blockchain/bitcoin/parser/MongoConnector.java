package it.unica.blockchain.bitcoin.parser;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.client.*;
import it.unica.blockchain.bitcoin.parser.Model.ExchangeJava;
import org.bitcoinj.core.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;
import org.bson.Document;

import java.util.*;
import java.util.stream.Collectors;

import static it.unica.blockchain.bitcoin.parser.Settings.*;

/**
 * Created by Ponzi on 23/02/17.
 */
public class MongoConnector {
    private int secondsInOneDAy = 86400;
    private MongoClient mongoClient;
    private MongoCollection<Document> transactionCollection;
    private MongoCollection<Document> ratecollection;

    private boolean connected = false;

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public void connect() {
        if (!connected) {
            MongoCredential credential = MongoCredential.createCredential(MONGO_USER, MONGO_DB_NAME, MONGO_PWD);
            //mongoClient = new MongoClient(new ServerAddress(Settings.MONGO_SERVER_IP, Settings.MONGO_SERVER_PORT), Arrays.asList(credential));
            mongoClient = new MongoClient(Settings.MONGO_SERVER_IP, Settings.MONGO_SERVER_PORT);

            MongoDatabase db = mongoClient.getDatabase(MONGO_DB_NAME);
            transactionCollection = db.getCollection(Settings.MONGO_COLLECTION_NAME);
            connected = true;
        }
    }

    public void disconnect() {
        if (connected) {
            mongoClient.close();
            connected = false;
            System.out.println("Disconnect to Mongo");
        }
    }

    public void connect1() {
        if (!connected) {
            MongoCredential credential = MongoCredential.createCredential(MONGO_USER, MONGO_DB_NAME_2, MONGO_PWD);
            //mongoClient = new MongoClient(new ServerAddress(Settings.MONGO_SERVER_IP, Settings.MONGO_SERVER_PORT), Arrays.asList(credential));
            mongoClient = new MongoClient(Settings.MONGO_SERVER_IP, Settings.MONGO_SERVER_PORT);

            MongoDatabase db = mongoClient.getDatabase(MONGO_DB_NAME_2);
            ratecollection = db.getCollection(Settings.MONGO_COLLECTION_NAME_2);
            connected = true;
        }
    }

    public void disconnect1() {
        if (connected) {
            mongoClient.close();
            connected = false;
            System.out.println("Disconnect to Mongo");
        }
    }

    public double getRates(String hash) {
        double rate = 0;
        Document queryBilancioInIngresso = new Document();
        queryBilancioInIngresso.append("txHash", hash);
        try (MongoCursor<Document> cursor = ratecollection.find(queryBilancioInIngresso).iterator()) {
            while (cursor.hasNext()) {
                Document transaction = cursor.next();
                rate = transaction.getDouble("rate");
            }

        }
        return rate;
    }

    /*
    * This method add the transaction "T" to the mongo DB
     */
    public void addTransaction(Transaction t, Block block, int height) {
        if (!connected) {
            throw new RuntimeException("Mongo not connected");
        }

        Document txDoc = new Document("txid", t.getHashAsString());
        //txDoc.append("locktime", t.getLockTime());

        List<Document> vin = new ArrayList<>();

        try {

            for (TransactionInput tIn : t.getInputs()) {
                Document in = new Document();


                if (!tIn.isCoinBase()) {
                    String txid = tIn.getOutpoint().getHash().toString();
                    in.append("vout", tIn.getOutpoint().getIndex());

                    in.append("txid", txid);
                    //in.append("scriptSig", tIn.getScriptSig().toString());

                    //try to extract the address from the scriptSig - valid only for pay-to-pubkey-hash
                    try {
                        List<ScriptChunk> chuncks = tIn.getScriptSig().getChunks();

                        byte[] keyBytes = chuncks.get(1).data;
                        ECKey key = ECKey.fromPublicOnly(keyBytes);
                        in.append("address", key.toAddress(MainNetParams.get()).toString());

                    } catch (Exception e) {
                        in.append("address", "");
                    }

                } else {
                    in.append("coinbase", true);
                }
                in.append("sequence", tIn.getSequenceNumber());


                vin.add(in);
            }

            txDoc.append("vin", vin);

            List<Document> vout = new ArrayList<>();

            for (TransactionOutput tOut : t.getOutputs()) {

                Document out = new Document();
                out.append("value", tOut.getValue().getValue());
                out.append("n", tOut.getIndex());
                //out.append("scriptPubKey", tOut.getScriptPubKey().toString());
                //out.append("type", tOut.getScriptPubKey().getScriptType().toString());
                List<String> keys = new ArrayList<>();

                //try to parse the destination address from the scriptPubKey
                if (tOut.getScriptPubKey().isSentToMultiSig()) {
                    for (ECKey key : tOut.getScriptPubKey().getPubKeys()) {
                        keys.add(key.getPrivateKeyAsWiF(MainNetParams.get()));
                    }
                } else if (tOut.getScriptPubKey().getScriptType() == Script.ScriptType.P2PKH) {
                    keys.add(tOut.getAddressFromP2PKHScript(MainNetParams.get()).toString());
                } else if (tOut.getScriptPubKey().getScriptType() == Script.ScriptType.PUB_KEY) {
                    ECKey key = ECKey.fromPublicOnly(tOut.getScriptPubKey().getPubKey());
                    keys.add(key.toAddress(MainNetParams.get()).toString());
                }

                /*
                if (tOut.getScriptPubKey().isOpReturn()){
                    out.append("isOpReturn", true);
                    try{
                        out.append("opReturnData", tOut.getScriptPubKey().getChunks().get(0).data);
                    } catch (Exception e){}
                }*/

                out.append("addresses", keys);
                vout.add(out);

            }
            txDoc.append("vout", vout);


            txDoc.append("blockhash", block.getHashAsString());
            txDoc.append("blockheight", height);
            txDoc.append("time", block.getTimeSeconds());

            double rate = ExchangeJava.getRate(block.getTime());
            txDoc.append("rate", rate);

        } catch (Exception e) {
        }

        transactionCollection.insertOne(txDoc);

    }

    /*
     * This method find the last block stored in the database and returns its Locktime
     * @return The time (a long value) of the last block stored in the DB
     */
    protected long getLastBlockTime() {
        //Take the last transaction from the DB
        Document cursor = transactionCollection.find().sort(new BasicDBObject("time", -1)).first();
        if (cursor == null) {
            return 0l;
        }
        return ((long) cursor.get("time"));
    }


    /*
    * This method find all the transactions where the given address is a recipient
    *
     */
    protected FindIterable<Document> getReceiveTransactions(String address) {
        //System.out.println("Call functiongetReceiveTransactions of an address!");
        Document queryBilancioInIngresso = new Document();
        queryBilancioInIngresso.append("vout.addresses", address);
        queryBilancioInIngresso.append("vin.address", new Document("$ne", address));
        return transactionCollection.find(queryBilancioInIngresso);
    }

    /*
    * This method find all the transactions received by an address and return that transaction sorted by time
    *
    */
    protected FindIterable<Document> getReceiveTransactionsSortedByTime(String address) {
        //System.out.println("Call functiongetReceiveTransactions of an address!");
        Document queryBilancioInIngresso = new Document();
        queryBilancioInIngresso.append("vout.addresses", address);
        queryBilancioInIngresso.append("vin.address", new Document("$ne", address));

        Document sortCondition = new Document();
        sortCondition.append("time", 1);
        return transactionCollection.find(queryBilancioInIngresso).sort(sortCondition);//ordinati
    }

    /*
    * This method find all the sendind transactions of an address
     */
    protected AggregateIterable<Document> getSendTransactions(String address) {
        //System.out.println("Call function getSendTransactions of an address!");
        List<Document> query = new ArrayList<>();
        query.add(Document.parse("{$match : {\"vin.address\" : \"" + address + "\"}}"));
        query.add(Document.parse("{$unwind : \"$vin\"}"));
        query.add(Document.parse("{$match: {\"vin.address\" : \"" + address + "\"}}"));
        query.add(Document.parse("{$lookup:{from: \"transaction\",localField: \"vin.txid\",foreignField: \"txid\", as: \"input\" }}"));
        return transactionCollection.aggregate(query).allowDiskUse(true);
    }

    /*
    * This method find all the sendind transactions of an address and return that transaction sorted by time
    */
    protected AggregateIterable<Document> getSendTransactionsSortedByTime(String address) {
        //System.out.println("Call function getSendTransactions of an address!");
        List<Document> query = new ArrayList<>();
        query.add(Document.parse("{$match : {\"vin.address\" : \"" + address + "\"}}"));
        query.add(Document.parse("{$unwind : \"$vin\"}"));
        query.add(Document.parse("{$match: {\"vin.address\" : \"" + address + "\"}}"));
        query.add(Document.parse("{$lookup:{from: \"transaction\",localField: \"vin.txid\",foreignField: \"txid\", as: \"input\" }}"));
        query.add(Document.parse("{$sort: {\"time\":1}}"));
        return transactionCollection.aggregate(query).allowDiskUse(true);
    }

    protected FindIterable<Document> getAllSendTransactions(String address) {
        //System.out.println("Call function getSendTransactions of an address!");
        org.bson.Document query = new org.bson.Document("vin.address", address);
        return transactionCollection.find(query);
    }

    /*
    * This method find when the address init to receive/send and when the address stop to receive/send
     */
    protected ArrayList<Long> getTotalInitAndEndTimeOfAddress(String address) {
        long initTime = 0;
        long endTime = 0;
        long currentTime = 0;
        ArrayList<Long> initAndEnd = new ArrayList<>();

        ArrayList<Document> orr = new ArrayList<>();
        orr.add(new Document("vout.addresses", address));
        orr.add(new Document("vin.address", address));
        Document query = new Document();
        query.append("$or", orr);

        MongoCursor<Document> cursor = transactionCollection.find(query).iterator();
        initTime = (long) cursor.next().get("time");
        endTime = initTime;
        while (cursor.hasNext()) {
            currentTime = (long) cursor.next().get("time");
            if (currentTime > endTime) {
                endTime = currentTime;
            } else {
                if (currentTime < initTime) {
                    initTime = currentTime;
                }
            }
        }
        cursor.close();
        initAndEnd.add(initTime);
        initAndEnd.add(endTime);
        //System.out.println("InitTime-> " + initTime);
        //System.out.println("EndTime -> " + endTime);
        return initAndEnd;
    }

    /*
    * This method find when the address init to send and when it stop
     */
    protected ArrayList<Long> getSendingInitAndEndTimeOfAddress(String address) {
        long initTime = 0;
        long endTime = 0;
        long currentTime = 0;
        ArrayList<Long> initAndEnd = new ArrayList<>();

        Document query = new Document();
        query.append("vin.address", address);

        MongoCursor<Document> cursor = transactionCollection.find(query).iterator();
        initTime = (long) cursor.next().get("time");
        endTime = initTime;
        while (cursor.hasNext()) {
            currentTime = (long) cursor.next().get("time");
            if (currentTime > endTime) {
                endTime = currentTime;
            } else {
                if (currentTime < initTime) {
                    initTime = currentTime;
                }
            }
        }
        cursor.close();
        initAndEnd.add(initTime);
        initAndEnd.add(endTime);
        //System.out.println("InitTime-> " + initTime);
        //System.out.println("EndTime -> " + endTime);
        return initAndEnd;
    }

    /*
    * This method find when the address init to receive and when it stop
     */
    protected ArrayList<Long> getReceivingInitAndEndTimeOfAddress(String address) {
        long initTime = 0;
        long endTime = 0;
        long currentTime = 0;
        ArrayList<Long> initAndEnd = new ArrayList<>();

        MongoCursor<Document> cursor = getReceiveTransactions(address).iterator();
        initTime = (long) cursor.next().get("time");
        endTime = initTime;


        while (cursor.hasNext()) {
            currentTime = (long) cursor.next().get("time");
            if (currentTime > endTime) {
                endTime = currentTime;
            } else {
                if (currentTime < initTime) {
                    initTime = currentTime;
                }
            }
        }

        cursor.close();

        initAndEnd.add(initTime);
        initAndEnd.add(endTime);
        //System.out.println("InitTime-> " + initTime);
        //System.out.println("EndTime -> " + endTime);
        return initAndEnd;
    }

    /*
    * This method return a list of random address take in the blockchain
     */
    protected ArrayList<String> getRandomAddress() {
        System.out.println("Init GetRandomAddress");
        ArrayList<String> address = new ArrayList<>();
        long currentTime;
        int i = 0;
        Document query = new Document();
        query.append("time", new Document("$gt", 1388478628)); 
        FindIterable<Document> iterable = transactionCollection.find(query);
        try (MongoCursor<Document> cursor = iterable.iterator()) {
            while (cursor.hasNext()) {
                try {
                    Document transaction = cursor.next();

                    currentTime = (long) transaction.get("time");

                    for (Document dcVout : ((ArrayList<Document>) transaction.get("vout"))) {
                        for (String dcAddress : ((ArrayList<String>) dcVout.get("addresses"))) {
                            if (!address.contains(dcAddress)) {
                                address.add(dcAddress);
                                //long numberTransaction = getNumberTransactions(dcAddress);
                                System.out.println(i + "   " + dcAddress + "      Time:"+currentTime );
                                //System.out.println(i + "   " + dcAddress + "      Time:"+currentTime + "    NumT:"+numberTransaction);
                                i++;
                            }
                        }
                    }

                } catch (NoSuchElementException e) {
                    System.out.println("Problema ->" + e);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return address;
    }

    public HashMap<String, Long> getRandomAddressWithMoreTransactions() {
        HashMap<String, Long> addressesHM = new HashMap<String, Long>();
        ArrayList<String> address = new ArrayList();

        long currentTime;
        int i = 0;
        Document query = new Document();
        query.append("time", new Document("$gt", 1285506980));
        FindIterable<Document> iterable = transactionCollection.find(query);

        try (MongoCursor<Document> cursor = iterable.iterator()) {
            while (cursor.hasNext()) {
                try {
                    Document transaction = cursor.next();
                    for (Document dcVout : ((ArrayList<Document>) transaction.get("vout"))) {
                        for (String dcAddress : ((ArrayList<String>) dcVout.get("addresses"))) {
                            if (!address.contains(dcAddress)) {
                                address.add(dcAddress);


                                long numberTransaction = getNumberTransactions(dcAddress);
                                //System.out.println(transactionCollection.count(new Document("$or",orr)));

                                if (numberTransaction > 10) {
                                    addressesHM.put(dcAddress, numberTransaction);
                                    System.out.println(dcAddress + "  -> transazioni= " + numberTransaction);
                                }

                                //System.out.println(i + "   " + dcAddress);
                                i++;
                            }
                        }
                    }


                    if (i > 1000000) break;

                } catch (NoSuchElementException e) {
                    System.out.println("Problema ->" + e);
                }
            }

        } catch (Exception e) {
            System.out.println(e);
        }
        return addressesHM;
    }


    /*
    *
    *
     */
    public int getNumberTransactions(String address) {
        ArrayList<Document> orr = new ArrayList<>();
        orr.add(new Document("vout.addresses", address));
        orr.add(new Document("vin.address", address));
        return (int) transactionCollection.count(new Document("$or", orr));
    }

    /*
   *
   *
    */
    public int getNumberTransactionsCluster(ArrayList<String> clusterAddress) {
        ArrayList<Document> orr = new ArrayList<>();
        orr.add(new Document("vout.addresses", new Document("$in", clusterAddress)));
        orr.add(new Document("vin.address", new Document("$in", clusterAddress)));
        return (int) transactionCollection.count(new Document("$or", orr));
    }


    public int getNumberTransactions(ArrayList<Long> send, ArrayList<Long> income) {
        return (send.size() + income.size());
    }


    protected FindIterable<Document> getReceiveTransactionsCluster(ArrayList<String> addresses) {
        //System.out.println("Call functiongetReceiveTransactions of an address!");
        Document queryBilancioInIngresso = new Document();
        queryBilancioInIngresso.append("vout.addresses",
                new Document("$in", addresses));
        queryBilancioInIngresso.append("vin.address",
                new Document("$nin", addresses));
        return transactionCollection.find(queryBilancioInIngresso);
    }

    /*
  * This method find when the clusterAddresses init to receive/send and when the clusterAddresses stop to receive/send
   */
    protected ArrayList<Long> getTotalInitAndEndTimeOfCluster(ArrayList<String> clusterAddresses) {
        long initTime = 0;
        long endTime = 0;
        long currentTime = 0;
        ArrayList<Long> initAndEnd = new ArrayList<>();

        ArrayList<Document> orr = new ArrayList<>();
        orr.add(new Document("vout.addresses", new Document("$in", clusterAddresses)));
        orr.add(new Document("vin.address", new Document("$in", clusterAddresses)));
        Document query = new Document();
        query.append("$or", orr);

        MongoCursor<Document> cursor = transactionCollection.find(query).iterator();
        initTime = (long) cursor.next().get("time");
        endTime = initTime;
        while (cursor.hasNext()) {
            try {
                currentTime = (long) cursor.next().get("time");
                if (currentTime > endTime) {
                    endTime = currentTime;
                } else {
                    if (currentTime < initTime) {
                        initTime = currentTime;
                    }
                }
            } catch (NullPointerException e) {
                System.out.println("Errore! Transazione senza il \"time\"");
            }
        }
        cursor.close();
        initAndEnd.add(initTime);
        initAndEnd.add(endTime);
        //System.out.println("InitTime-> " + initTime);
        //System.out.println("EndTime -> " + endTime);
        return initAndEnd;
    }

    /*
* This method find all the sendind transactions of the cluster
 */
    protected AggregateIterable<Document> getSendTransactionsCluster(ArrayList<String> clusteraAdresses) {
        //System.out.println("Call function getSendTransactions of an address!");
        String asArray = "";
        for (int i = 0; i < clusteraAdresses.size(); i++) {
            asArray += "\"" + clusteraAdresses.get(i) + "\"";
            if (i < (clusteraAdresses.size() - 1)) asArray += ",";
        }
        List<Document> query = new ArrayList<>();
        query.add(Document.parse("{$match : {\"vin.address\" : {$in :[" + asArray + "]}}}"));
        query.add(Document.parse("{$unwind : \"$vin\"}"));
        query.add(Document.parse("{$match: {\"vin.address\" : {$in :[" + asArray + "]}}}"));
        query.add(Document.parse("{$lookup:{from: \"transaction\",localField: \"vin.txid\",foreignField: \"txid\", as: \"input\" }}"));
        return transactionCollection.aggregate(query).allowDiskUse(true);
    }

}
