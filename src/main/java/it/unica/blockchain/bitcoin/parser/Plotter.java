package it.unica.blockchain.bitcoin.parser;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.util.*;

/**
 * Created by Ponzi on 16/03/2017.
 */

public class Plotter {

    private static final int secondsInOneDay = 86400 * 1;


    static protected void dailyIncome(MongoConnector mongo, String address) {
        FindIterable<Document> iterable = mongo.getReceiveTransactions(address);
        ArrayList<Long> initAndEndTime = mongo.getTotalInitAndEndTimeOfAddress(address);

        System.out.println("Enter On Plotter dailyIncome");
        long initTotalTime = initAndEndTime.get(0);
        long endTotalTime = initAndEndTime.get(1);
        CSVFile csv = new CSVFile();
        String path = "Receive//Money//" + address + ".csv";

        System.out.println("Giorni -> " + ((endTotalTime - initTotalTime) / secondsInOneDay));
        int days = (int) ((endTotalTime - initTotalTime) / secondsInOneDay);
        long currentTime;
        long receiveValue = 0;
        int i = 0;
        HashMap<Integer, Long> bilancioInIngressoPerGiorno = new HashMap<>();

        for (int c = 0; c <= days; c++) {
            bilancioInIngressoPerGiorno.put(c, (long) 0);
        }

        try (MongoCursor<Document> cursor = iterable.iterator()) {
            while (cursor.hasNext()) {
                try {
                    Document transaction = cursor.next();

                    currentTime = (long) transaction.get("time");
                    int currentDay = (int) (currentTime - initTotalTime) / secondsInOneDay;
                    long currentValue = 0;

                    for (Document dcVout : ((ArrayList<Document>) transaction.get("vout"))) {
                        for (String dcAddress : ((ArrayList<String>) dcVout.get("addresses"))) {
                            if (dcAddress.equals(address)) {
                                currentValue += (long) dcVout.get("value");
                                //System.out.println("GIorno ->"+currentDay );
                            }
                        }
                    }
                    receiveValue += currentValue;
                    bilancioInIngressoPerGiorno.merge(currentDay, currentValue, (newValue, oldValue) -> {
                        if (oldValue == null)
                            return newValue;
                        return oldValue + newValue;
                    });

                    i++;

                } catch (NoSuchElementException e) {
                    System.out.println("Problema ->" + e);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        System.out.println("Numero transazioni: " + i);
        Date timeI = new Date(initTotalTime * 1000);
        System.out.println("Init Time: " + initTotalTime);
        Date timeE = new Date(endTotalTime * 1000);
        System.out.println("End  Time: " + endTotalTime);
        System.out.println("INIT     : " + timeI);
        System.out.println("END      : " + timeE);
        System.out.println("Receive V: " + receiveValue);
        System.out.println("HAshMap->" + bilancioInIngressoPerGiorno);
        System.out.println("Giorni Totali    :" + ((endTotalTime - initTotalTime) / secondsInOneDay));

        csv.writePlotter(bilancioInIngressoPerGiorno, path);
    }

    static protected void dailySpending(MongoConnector mongo, String address) {
        AggregateIterable<Document> iterable = mongo.getSendTransactions(address);
        ArrayList<Long> initAndEndTime = mongo.getTotalInitAndEndTimeOfAddress(address);

        System.out.println("Enter On Plotter dailySpending");
        long initTotalTime = initAndEndTime.get(0);
        long endTotalTime = initAndEndTime.get(1);
        long endTimeSend = 0;
        long initTimeSend = 0;
        CSVFile csv = new CSVFile();
        String path = "Send//Money//" + address + ".csv";

        System.out.println("Giorni -> " + ((endTotalTime - initTotalTime) / secondsInOneDay));
        int days = (int) ((endTotalTime - initTotalTime) / secondsInOneDay);
        long currentTime = initTimeSend;
        long sendValue = 0;
        int i = 0;
        HashMap<Integer, Long> bilancioInUscitaPerGiorno = new HashMap<>();

        for (int c = 0; c <= days; c++) {
            bilancioInUscitaPerGiorno.put(c, 0l);
        }

        String prevTxid = "";

        try (MongoCursor<Document> cursor = iterable.iterator()) {
            while (cursor.hasNext()) {

                try {
                    Document transaction = cursor.next();

                    currentTime = (long) transaction.get("time");
                    if (i == 0) initTimeSend = currentTime;
                    int currentDay = (int) (currentTime - initTotalTime) / secondsInOneDay;
                    long currentValue = 0;


                    String currentTxid = (String) transaction.get("txid");
                    System.out.println(transaction.get("txid"));
                    ArrayList<Document> vout = (ArrayList<Document>) transaction.get("vout");
                    Document vin = (Document) transaction.get("vin");
                    System.out.println(vin.get("vout"));
                    int index = new Integer(vin.get("vout").toString());

                    Document input = ((ArrayList<Document>) transaction.get("input")).get(0);
                    ArrayList<Document> voutInput = (ArrayList<Document>) input.get("vout");
                    Document documentInput = voutInput.get(index);
                    long inputValue = (long) documentInput.get("value");
                    System.out.println("Inputvalue : " + inputValue);
                    long returnToAddress = 0;
                    if (!currentTxid.equals(prevTxid)) {
                        for (Document dc : vout) {
                            ArrayList<String> docAddresses = (ArrayList<String>) dc.get("addresses");

                            System.out.println(docAddresses);
                            String localAddress = "";
                            try {
                                localAddress = docAddresses.get(0);
                            } catch (IndexOutOfBoundsException e) {
                            }
                            System.out.println(localAddress);

                            if (localAddress.equals(address)) {
                                returnToAddress += (long) dc.get("value");
                                System.out.println("Stesso indirizzo :" + returnToAddress);
                            }
                        }
                    }
                    currentValue = (inputValue - returnToAddress);
                    bilancioInUscitaPerGiorno.merge(currentDay, currentValue, (newValue, oldValue) -> {
                        if (oldValue == null)
                            return newValue;
                        return oldValue + newValue;
                    });
                    sendValue += currentValue;

                    System.out.println("SendValue : " + sendValue);
                    prevTxid = currentTxid;

                    i++;
                    //System.out.println(transaction.get("txid"));
                    //System.out.println(currentTime);
                    endTimeSend = currentTime;

                } catch (NoSuchElementException e) {
                    System.out.println("Problema ->" + e);
                }
            }
        }

        System.out.println("Numero transazioni: " + i);
        Date timeI = new Date(initTotalTime * 1000);
        System.out.println("Init Time: " + initTotalTime);
        Date timeE = new Date(endTotalTime * 1000);
        System.out.println("End  Time: " + endTotalTime);
        System.out.println("INIT     : " + timeI);
        System.out.println("END      : " + timeE);
        System.out.println("Send  V: " + sendValue);
        System.out.println("HAshMap->" + bilancioInUscitaPerGiorno);
        System.out.println("Inizio Ricezione :" + initTimeSend);
        System.out.println("Fine   Ricezione :" + endTimeSend);
        System.out.println("Giorni Totali    :" + ((endTotalTime - initTotalTime) / secondsInOneDay));
        System.out.println("Giorni Ricezione :" + ((endTimeSend - initTimeSend) / secondsInOneDay));

        //csv.writePlotter(bilancioInUscitaPerGiorno, path);
    }

    static protected void dailyBalance(String address) {
        String path = "Balance//Money//" + address + ".csv";

        CSVFile csv = new CSVFile();
        HashMap<Integer, Long> dailyIncome = csv.getDailyIncome(address);
        HashMap<Integer, Long> dailySend = csv.getDailySend(address);

        HashMap<Integer, Long> dailyBalance = new HashMap<>();

        long currentBalance = 0;
        for (int i = 0; i < dailyIncome.size(); i++) {
            int day = i;
            long currentIncome = dailyIncome.get(day);
            long currentSend = dailySend.get(day);
            currentBalance = currentBalance + (currentIncome - currentSend);
            dailyBalance.put(day, currentBalance);
        }

            /*
            System.out.println("Income - "+dailyIncome);
            System.out.println("Send   - "+dailySend  );
            System.out.println("Balance - "+dailyBalance);
            */
        csv.writePlotter(dailyBalance, path);
    }

    static protected void dailyBalanceCluster(ArrayList<String> addresses, String address) {
        String path = "Cluster//Balance//Money//" + address + ".csv";

        CSVFile csv = new CSVFile();
        HashMap<Integer, Long> dailyIncome = csv.getDailyIncome(address);
        HashMap<Integer, Long> dailySend = csv.getDailySend(address);

        HashMap<Integer, Long> dailyBalance = new HashMap<>();

        long currentBalance = 0;
        for (int i = 0; i < dailyIncome.size(); i++) {
            int day = i;
            long currentIncome = dailyIncome.get(day);
            long currentSend = dailySend.get(day);
            currentBalance = currentBalance + (currentIncome - currentSend);
            dailyBalance.put(day, currentBalance);
        }

            /*
            System.out.println("Income - "+dailyIncome);
            System.out.println("Send   - "+dailySend  );
            System.out.println("Balance - "+dailyBalance);
            */
        csv.writePlotter(dailyBalance, path);
    }

    static protected ArrayList<Long> getTotalNumberDaysSendingActivities(MongoConnector mongo, String address) {
        ArrayList<Long> initAndEndTime = mongo.getSendingInitAndEndTimeOfAddress(address);
        long initTime = initAndEndTime.get(0);
        long endTime = initAndEndTime.get(1);
        long days = ((endTime - initTime) / secondsInOneDay);
        System.out.println("Init Time : " + initTime);
        System.out.println("End  Time : " + endTime);
        System.out.println("Total Day : " + days);
        initAndEndTime.add(days);
        return initAndEndTime;
    }

    static protected ArrayList<Long> getTotalNumberDaysReceivingActivities(MongoConnector mongo, String address) {
        ArrayList<Long> initAndEndTime = mongo.getReceivingInitAndEndTimeOfAddress(address);
        long initTime = initAndEndTime.get(0);
        long endTime = initAndEndTime.get(1);
        long days = ((endTime - initTime) / secondsInOneDay);
        System.out.println("Init Time : " + initTime);
        System.out.println("End  Time : " + endTime);
        System.out.println("Total Day : " + days);
        initAndEndTime.add(days);
        return initAndEndTime;
    }

    static protected ArrayList<Long> getTotalNumberDaysBalanceActivities(MongoConnector mongo, String address) {
        ArrayList<Long> initAndEndTime = mongo.getTotalInitAndEndTimeOfAddress(address);
        long initTime = initAndEndTime.get(0);
        long endTime = initAndEndTime.get(1);
        long days = ((endTime - initTime) / secondsInOneDay);
        System.out.println("Init Time : " + initTime);
        System.out.println("End  Time : " + endTime);
        System.out.println("Total Day : " + days);
        initAndEndTime.add(days);
        return initAndEndTime;
    }

    //Disegna il grafico di guadagno di un indirizzo (in numero di transazioni)
    static protected void dailyIncomeTransactionNumber(MongoConnector mongo, String address) {
        HashMap<Integer, Integer> bilancioInIngressoPerGiorno = new HashMap<>();
        String head = "day,transactions";
        String path = "Receive//NumberTransaction//" + address + ".csv";
        HashMap<Integer, Long> bilancioInIngressoPerGiornoLong = new HashMap<>();
        bilancioInIngressoPerGiorno = getDailyIncomeTransactionNumber(mongo, address);

        for (Map.Entry<Integer, Integer> ent : bilancioInIngressoPerGiorno.entrySet()) {
            bilancioInIngressoPerGiornoLong.put(ent.getKey(), (long) ent.getValue());
        }

        CSVFile csv = new CSVFile();
        csv.writePlotter(bilancioInIngressoPerGiornoLong, path, head);
    }

    //restituisce una hash map con giorno->numerotransazioni
    static protected HashMap<Integer, Integer> getDailyIncomeTransactionNumber(MongoConnector mongo, String address) {
        FindIterable<Document> iterable = mongo.getReceiveTransactions(address);
        ArrayList<Long> initAndEndTime = mongo.getTotalInitAndEndTimeOfAddress(address);

        System.out.println("Enter On Plotter dailyIncomeTransactionNumber");
        long initTotalTime = initAndEndTime.get(0);
        long endTotalTime = initAndEndTime.get(1);

        //System.out.println("Giorni -> " + ((endTotalTime - initTotalTime) / secondsInOneDay));
        int days = (int) ((endTotalTime - initTotalTime) / secondsInOneDay);
        long currentTime;
        long receiveValue = 0;
        int i = 0;
        int z = 0;
        HashMap<Integer, Integer> bilancioInIngressoPerGiorno = new HashMap<>();

        for (int c = 0; c <= days; c++) {
            bilancioInIngressoPerGiorno.put(c, 0);
        }

        try (MongoCursor<Document> cursor = iterable.iterator()) {
            while (cursor.hasNext()) {
                try {
                    Document transaction = cursor.next();

                    currentTime = (long) transaction.get("time");
                    int currentDay = (int) (currentTime - initTotalTime) / secondsInOneDay;
                    long currentValue = 0;

                    for (Document dcVout : ((ArrayList<Document>) transaction.get("vout"))) {
                        for (String dcAddress : ((ArrayList<String>) dcVout.get("addresses"))) {
                            if (dcAddress.equals(address)) {
                                currentValue += (long) dcVout.get("value");
                                //System.out.println("GIorno ->"+currentDay );
                            }
                        }
                    }
                    int toSum = 0;
                    if (currentValue > 0) {
                        toSum = 1;
                        z++;
                    }

                    bilancioInIngressoPerGiorno.merge(currentDay, toSum, (newValue, oldValue) -> {
                        if (oldValue == null)
                            return newValue;
                        return oldValue + newValue;
                    });

                    i++;

                } catch (NoSuchElementException e) {
                    System.out.println("Problema ->" + e);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        //System.out.println("Numero transazioni: " + i + "    Z:" + z);
        Date timeI = new Date(initTotalTime * 1000);
        //System.out.println("Init Time: " + initTotalTime);
        Date timeE = new Date(endTotalTime * 1000);
        //System.out.println("End  Time: " + endTotalTime);
        //System.out.println("INIT     : " + timeI);
        //System.out.println("END      : " + timeE);
        //System.out.println("Receive V: " + receiveValue);
        //System.out.println("HAshMap->" + bilancioInIngressoPerGiorno);
        //System.out.println("Giorni Totali    :" + ((endTotalTime - initTotalTime) / secondsInOneDay));
        return bilancioInIngressoPerGiorno;
    }

    //restituisce una hash map con giorno->numerotransazioni
    static protected HashMap<Integer, Integer> getDailySendTransactionNumber(MongoConnector mongo, String address) {
        AggregateIterable<Document> iterable = mongo.getSendTransactions(address);
        ArrayList<Long> initAndEndTime = mongo.getTotalInitAndEndTimeOfAddress(address);

        System.out.println("Enter On Plotter dailyIncomeTransactionNumber");
        long initTotalTime = initAndEndTime.get(0);
        long endTotalTime = initAndEndTime.get(1);

        System.out.println("Giorni -> " + ((endTotalTime - initTotalTime) / secondsInOneDay));
        int days = (int) ((endTotalTime - initTotalTime) / secondsInOneDay);
        long currentTime;
        long receiveValue = 0;
        int i = 0;
        int z = 0;
        HashMap<Integer, Integer> bilancioInIngressoPerGiorno = new HashMap<>();

        for (int c = 0; c <= days; c++) {
            bilancioInIngressoPerGiorno.put(c, 0);
        }

        try (MongoCursor<Document> cursor = iterable.iterator()) {
            while (cursor.hasNext()) {
                try {
                    Document transaction = cursor.next();

                    currentTime = (long) transaction.get("time");
                    int currentDay = (int) (currentTime - initTotalTime) / secondsInOneDay;
                    long currentValue = 0;

                    for (Document dcVout : ((ArrayList<Document>) transaction.get("vout"))) {
                        for (String dcAddress : ((ArrayList<String>) dcVout.get("addresses"))) {
                            if (dcAddress.equals(address)) {
                                currentValue += (long) dcVout.get("value");
                                //System.out.println("GIorno ->"+currentDay );
                            }
                        }
                    }
                    int toSum = 0;
                    if (currentValue > 0) {
                        toSum = 1;
                        z++;
                    }

                    bilancioInIngressoPerGiorno.merge(currentDay, toSum, (newValue, oldValue) -> {
                        if (oldValue == null)
                            return newValue;
                        return oldValue + newValue;
                    });

                    i++;

                } catch (NoSuchElementException e) {
                    System.out.println("Problema ->" + e);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        System.out.println("Numero transazioni: " + i + "    Z:" + z);
        Date timeI = new Date(initTotalTime * 1000);
        System.out.println("Init Time: " + initTotalTime);
        Date timeE = new Date(endTotalTime * 1000);
        System.out.println("End  Time: " + endTotalTime);
        System.out.println("INIT     : " + timeI);
        System.out.println("END      : " + timeE);
        System.out.println("Receive V: " + receiveValue);
        System.out.println("HAshMap->" + bilancioInIngressoPerGiorno);
        System.out.println("Giorni Totali    :" + ((endTotalTime - initTotalTime) / secondsInOneDay));
        return bilancioInIngressoPerGiorno;
    }

    //Disegna il grafico di spesa di un indirizzo (in numero di transazioni)
    static protected void dailySpendingTransactionNumber(MongoConnector mongo, String address) {
        AggregateIterable<Document> iterable = mongo.getSendTransactions(address);
        ArrayList<Long> initAndEndTime = mongo.getTotalInitAndEndTimeOfAddress(address);

        System.out.println("Enter On Plotter dailySpendingTransactionnumber");
        long initTotalTime = initAndEndTime.get(0);
        long endTotalTime = initAndEndTime.get(1);
        long endTimeSend = 0;
        long initTimeSend = 0;
        String head = "day,transactions";
        CSVFile csv = new CSVFile();
        String path = "Send//NumberTransaction//" + address + ".csv";

        System.out.println("Giorni -> " + ((endTotalTime - initTotalTime) / secondsInOneDay));
        int days = (int) ((endTotalTime - initTotalTime) / secondsInOneDay);
        long currentTime = initTimeSend;
        int i = 0;
        HashMap<Integer, Long> bilancioInUscitaPerGiorno = new HashMap<>();

        for (int c = 0; c <= days; c++) {
            bilancioInUscitaPerGiorno.put(c, 0l);
        }

        String prevTxid = "";

        try (MongoCursor<Document> cursor = iterable.iterator()) {
            while (cursor.hasNext()) {

                try {
                    Document transaction = cursor.next();
                    currentTime = (long) transaction.get("time");
                    int currentDay = (int) (currentTime - initTotalTime) / secondsInOneDay;
                    long currentValue = 0;

                    String currentTxid = (String) transaction.get("txid");

                    if (!currentTxid.equals(prevTxid)) {
                        currentValue = 1;
                        bilancioInUscitaPerGiorno.merge(currentDay, currentValue, (newValue, oldValue) -> {
                            if (oldValue == null)
                                return newValue;
                            return oldValue + newValue;
                        });
                        i++;
                    }
                    prevTxid = currentTxid;
                } catch (NoSuchElementException e) {
                    System.out.println("Problema ->" + e);
                }
            }
        }

        System.out.println("Numero transazioni: " + i);
        System.out.println("HAshMap->" + bilancioInUscitaPerGiorno);

        csv.writePlotter(bilancioInUscitaPerGiorno, path, head);
    }

    //Disegna il grafico con due serie: transazioni in ingresso e transazioni in uscita (Numero di transazioni)
    static protected void dailyBalanceTransactionInOutNumber(String address) {
        String path = "Balance//NumberTransaction//" + address + ".csv";
        String head = "day,transactions in,transactions out";
        CSVFile csv = new CSVFile();
        HashMap<Integer, Long> dailyIncome = csv.getDailyIncome("NumberTransaction//" + address);
        HashMap<Integer, Long> dailySend = csv.getDailySend("NumberTransaction//" + address);

        HashMap<Integer, ArrayList<Long>> dailyBalance = new HashMap<>();

        for (int i = 0; i < dailyIncome.size(); i++) {
            int day = i;
            ArrayList<Long> currentBalance = new ArrayList<>();
            currentBalance.add(0, dailyIncome.get(day));
            currentBalance.add(1, dailySend.get(day));
            dailyBalance.put(day, currentBalance);
        }
        csv.writePlotterWithSeries(dailyBalance, path, head);
    }

    //Disegna il grafico con due serie: transazioni in ingresso e transazioni in uscita (Valori in BTC)
    static protected void dailyBalanceTransactionInOutMoneySeries(String address) {
        String path = "Balance//MoneySeries//" + address + ".csv";
        String head = "day,value transactions in,value transactions out";
        CSVFile csv = new CSVFile();
        HashMap<Integer, Long> dailyIncome = csv.getDailyIncome("Money//" + address);
        HashMap<Integer, Long> dailySend = csv.getDailySend("Money//" + address);

        HashMap<Integer, ArrayList<Long>> dailyBalance = new HashMap<>();

        for (int i = 0; i < dailyIncome.size(); i++) {
            int day = i;
            ArrayList<Long> currentBalance = new ArrayList<>();
            currentBalance.add(0, dailyIncome.get(day));
            currentBalance.add(1, dailySend.get(day));
            dailyBalance.put(day, currentBalance);
        }
        csv.writePlotterWithSeries(dailyBalance, path, head);
    }

    //Questo metodo serve per prelevare i valori delle transazioni ricevute
    static protected ArrayList<Long> incomeValueTransactions(MongoConnector mongo, String address) {
        FindIterable<Document> iterable = mongo.getReceiveTransactions(address);
        ArrayList<Long> lista = new ArrayList();


        //System.out.println("Enter On Plotter incomeValueTransactions");
        long receiveValue = 0;
        int i = 0;

        try (MongoCursor<Document> cursor = iterable.iterator()) {
            while (cursor.hasNext()) {
                try {
                    Document transaction = cursor.next();

                    long currentValue = 0;

                    for (Document dcVout : ((ArrayList<Document>) transaction.get("vout"))) {
                        for (String dcAddress : ((ArrayList<String>) dcVout.get("addresses"))) {
                            if (dcAddress.equals(address)) {
                                currentValue = (long) dcVout.get("value");
                                lista.add(currentValue);
                                receiveValue += currentValue;
                                //System.out.println("GIorno ->"+currentDay );
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
        //System.out.println("TotaleValoreRicevuto-> " + receiveValue);

        return lista;
    }

    //Questo metodo serve per prelevare i valori delle transazioni inviate
    static protected ArrayList<Long> sendingValueTransactions(MongoConnector mongo, String address) {
        AggregateIterable<Document> iterable = mongo.getSendTransactions(address);
        ArrayList<Long> sendingValue = new ArrayList<>();


        long sendValue = 0;
        int i = 0;
        long sendValueTransaction = 0;
        String prevTxid = "";

        try (MongoCursor<Document> cursor = iterable.iterator()) {
            while (cursor.hasNext()) {

                try {
                    Document transaction = cursor.next();
                    long currentValue = 0;


                    String currentTxid = (String) transaction.get("txid");
                    //System.out.println(transaction.get("txid"));
                    ArrayList<Document> vout = (ArrayList<Document>) transaction.get("vout");
                    Document vin = (Document) transaction.get("vin");
                    //System.out.println(vin.get("vout"));
                    int index = new Integer(vin.get("vout").toString());

                    Document input = ((ArrayList<Document>) transaction.get("input")).get(0);
                    ArrayList<Document> voutInput = (ArrayList<Document>) input.get("vout");
                    Document documentInput = voutInput.get(index);
                    long inputValue = (long) documentInput.get("value");
                    //System.out.println("Inputvalue : " + inputValue);
                    long returnToAddress = 0;
                    if (!currentTxid.equals(prevTxid)) {
                        if (i > 0) {
                            //System.out.println("sendValueTransaction -> " + sendValueTransaction);
                            sendingValue.add(sendValueTransaction);
                        }
                        sendValueTransaction = 0;
                        for (Document dc : vout) {
                            ArrayList<String> docAddresses = (ArrayList<String>) dc.get("addresses");

                            //System.out.println(docAddresses);
                            String localAddress = "";
                            try {
                                localAddress = docAddresses.get(0);
                            } catch (IndexOutOfBoundsException e) {
                            }
                            //System.out.println(localAddress);

                            if (localAddress.equals(address)) {
                                returnToAddress += (long) dc.get("value");
                                //System.out.println("Stesso indirizzo :" + returnToAddress);
                            }
                        }

                    }


                    currentValue = (inputValue - returnToAddress);
                    sendValueTransaction += currentValue;
                    sendValue += currentValue;
                    //System.out.println("SendValue : " + sendValue);
                    prevTxid = currentTxid;

                    i++;


                } catch (NoSuchElementException e) {
                    System.out.println("Problema ->" + e);
                } catch (NullPointerException nb) {
                    System.out.println("Problema ->" + nb);
                }
            }
        }
        sendingValue.add(sendValueTransaction);
        //System.out.println("sendValueTransaction -> " + sendValueTransaction);
        return sendingValue;
    }

    //Questo metodo serve per prelevare i valori delle transazioni ricevute e restituisce una mappa da indirizzo -> valori
    static protected HashMap<String, ArrayList<Long>> incomeValueTransactionsAndAddress(MongoConnector mongo, String address) {
        FindIterable<Document> iterable = mongo.getReceiveTransactionsSortedByTime(address);
        HashMap<String, ArrayList<Long>> mapAddress_value = new HashMap<>();

        //System.out.println("Enter On Plotter incomeValueTransactions");
        long receiveValue = 0;
        int i = 0;

        try (MongoCursor<Document> cursor = iterable.iterator()) {
            while (cursor.hasNext()) {
                try {
                    Document transaction = cursor.next();

                    long currentValue = 0;

                    //System.out.println("Address -> " + transaction);
                    String inAddress = null;
                    try {
                        for (Document dcInp : ((ArrayList<Document>) transaction.get("vin"))) {
                            inAddress = dcInp.get("address", String.class);
                            //System.out.println("Address -> " + inAddress);
                            break; //Metto il break perchè per ora prendo solo il primo indirizzo
                        }
                    } catch (Exception e) {
                        System.out.println("Errore -> " + e);
                    }

                    for (Document dcVout : ((ArrayList<Document>) transaction.get("vout"))) {
                        for (String dcAddress : ((ArrayList<String>) dcVout.get("addresses"))) {
                            if (dcAddress.equals(address)) {
                                currentValue = (long) dcVout.get("value");
                                //lista.add(currentValue);
                                //System.out.println("Value -> " + currentValue);
                                receiveValue += currentValue;
                            }
                        }
                    }

                    if (!mapAddress_value.containsKey(inAddress)) {
                        mapAddress_value.put(inAddress, new ArrayList<>());
                    }
                    ArrayList<Long> list = mapAddress_value.get(inAddress);
                    list.add(currentValue);


                } catch (NoSuchElementException e) {
                    System.out.println("Problema ->" + e);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("TotaleValoreRicevuto-> " + receiveValue);

        return mapAddress_value;
    }

    //Questo metodo serve per prelevare i valori delle transazioni inviate
    static protected HashMap<String, ArrayList<Long>> sendingValueTransactionsAndAddress(MongoConnector mongo, String address) {
        AggregateIterable<Document> iterable = mongo.getSendTransactionsSortedByTime(address);
        HashMap<String, ArrayList<Long>> sendingValue = new HashMap<>();

        long sendValue = 0;
        int i = 0;
        long sendValueTransaction = 0;
        String prevTxid = "";
        String addressSended = "";

        try (MongoCursor<Document> cursor = iterable.iterator()) {
            while (cursor.hasNext()) {

                try {
                    Document transaction = cursor.next();
                    long currentValue = 0;


                    String currentTxid = (String) transaction.get("txid");
                    //System.out.println(transaction.get("txid"));
                    ArrayList<Document> vout = (ArrayList<Document>) transaction.get("vout");
                    Document vin = (Document) transaction.get("vin");
                    //System.out.println(vin.get("vout"));
                    int index = new Integer(vin.get("vout").toString());

                    Document input = ((ArrayList<Document>) transaction.get("input")).get(0);
                    ArrayList<Document> voutInput = (ArrayList<Document>) input.get("vout");
                    Document documentInput = voutInput.get(index);
                    long inputValue = (long) documentInput.get("value");
                    //System.out.println("Inputvalue : " + inputValue);
                    long returnToAddress = 0;
                    if (!currentTxid.equals(prevTxid)) {
                        if (i > 0) {
                            //System.out.println("addressValueTransaction -> " + addressSended);
                            //System.out.println("sendValueTransaction    -> " + sendValueTransaction);

                            if (!sendingValue.containsKey(addressSended)) {
                                sendingValue.put(addressSended, new ArrayList<>());
                            }
                            ArrayList<Long> list = sendingValue.get(addressSended);
                            list.add(sendValueTransaction);
                        }
                        sendValueTransaction = 0;
                        for (Document dc : vout) {
                            ArrayList<String> docAddresses = (ArrayList<String>) dc.get("addresses");

                            //System.out.println(docAddresses);
                            String localAddress = "";
                            try {
                                localAddress = docAddresses.get(0);
                            } catch (IndexOutOfBoundsException e) {
                            }
                            //System.out.println(localAddress);

                            if (localAddress.equals(address)) {
                                returnToAddress += (long) dc.get("value");
                                //System.out.println("Stesso indirizzo :" + returnToAddress);
                            } else {
                                addressSended = localAddress;
                            }
                        }

                    }


                    currentValue = (inputValue - returnToAddress);
                    sendValueTransaction += currentValue;
                    sendValue += currentValue;
                    //System.out.println("SendValue : " + sendValue);
                    prevTxid = currentTxid;

                    i++;
                } catch (NoSuchElementException e) {
                    System.out.println("Problema ->" + e);
                }
            }
        }

        //Ultimo giro
        if (!sendingValue.containsKey(addressSended)) {
            sendingValue.put(addressSended, new ArrayList<>());
        }
        ArrayList<Long> list = sendingValue.get(addressSended);
        list.add(sendValueTransaction);
        System.out.println("addressValueTransaction -> " + addressSended);
        System.out.println("sendValueTransaction    -> " + sendValueTransaction);
        return sendingValue;
    }

}
