package it.unica.blockchain.bitcoin.parser;


import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import it.unica.blockchain.bitcoin.parser.Model.EachValueForAddress;
import it.unica.blockchain.bitcoin.parser.Model.IncomeValues;
import it.unica.blockchain.bitcoin.parser.Model.Responsiveness;
import it.unica.blockchain.bitcoin.parser.Model.SendValues;
import org.bson.Document;
import weka.core.Instance;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Sergio Serusi on 23/05/2017.
 */

public class CalculateFeatures {
    int MINTRANSACTIONNUMBER = 100;
    int MINFREQUENCYNUMBER = 50;


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

    boolean isExponential(HashMap<Integer, Long> dailyBalance) {
        boolean isExp = false;
        int i = 0;
        int variableMax = 0;
        int max = 0;
        int day = 0;

        long value = 0;
        long baseValue = 0;

        int totalDay = getNumberActivityDay(dailyBalance);

        for (HashMap.Entry<Integer, Long> entry : dailyBalance.entrySet()) {
            day = entry.getKey();
            value = entry.getValue();
            //System.out.println("Day \"" + day + "\"   value: " + value);

            if (i == 0) {
                baseValue = value;
                variableMax++;
            } else {
                if (value == 0) {
                    variableMax = 0;
                } else {
                    float mult = (float) value / baseValue;
                    if (mult > 1.1) {
                        variableMax++;
                        baseValue = value;
                    } else {
                        if (variableMax > max) {
                            max = variableMax;
                        }
                        variableMax = 0;
                        baseValue = value;

                    }
                }
            }
            i++;
            if (i == totalDay) break;
        }

        System.out.println("Giorni crescita esponenziale: " + max + "   Day Live = " + totalDay);
        if (totalDay > 10 && totalDay < 32) {
            if (max >= 3) return true;
        } else {
            if (totalDay >= 32 && totalDay < 170) {
                if (max >= 4) return true;
            } else {
                if (totalDay >= 170 && totalDay < 200) {
                    if (max >= 5) return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    boolean isIncreasing(HashMap<Integer, Long> dailyBalance) {
        int i = 0;
        int day = 0;
        int variableMax = 0;
        int max = 0;

        long valsom = 0;
        long value = 0;
        long momentvalue = 0;
        boolean flag = true;
        HashMap<Integer, Long> dailyBalanceModified = new HashMap<>();

        int dd = getNumberActivityDay(dailyBalance);
        if (dd <= 50) {
            for (HashMap.Entry<Integer, Long> entry : dailyBalance.entrySet()) {
                day = entry.getKey();
                value = entry.getValue();
                //System.out.println("Day \"" + i + "\"   value: " + value);
                dailyBalanceModified.put(i, value);

                i++;
                if (day > dd) break;
            }
        } else {
            if (dd > 50 && dd < 200) {
                for (HashMap.Entry<Integer, Long> entry : dailyBalance.entrySet()) {
                    day = entry.getKey();
                    value = entry.getValue();
                    valsom += value;
                    if (day % 2 == 0) {
                        //System.out.println("Day \"" + i + "          Original Day:" + day + "\"   value: " + valsom);
                        dailyBalanceModified.put(i, valsom);
                        valsom = 0;
                        i++;
                    }
                    if (day > dd) break;
                }
            } else {
                for (HashMap.Entry<Integer, Long> entry : dailyBalance.entrySet()) {
                    day = entry.getKey();
                    value = entry.getValue();
                    valsom += value;
                    if (day % 3 == 0) {
                        //System.out.println("Day \"" + i + "          Original Day:" + day + "\"   value: " + valsom);
                        dailyBalanceModified.put(i, valsom);
                        valsom = 0;
                        i++;
                    }
                    if (day > dd) break;
                }
            }
        }

        i = 0;
        for (HashMap.Entry<Integer, Long> entry : dailyBalanceModified.entrySet()) {
            day = entry.getKey();
            value = entry.getValue();
            //System.out.println("Day \"" + day + "\"   value: " + value);
            if(i==0){
                momentvalue = value;
            }else{
                if(momentvalue<value){
                    variableMax++;
                }else{
                    if(value!=0) {
                        momentvalue = value;
                        if (variableMax > max) {
                            max = variableMax;
                            System.out.println("ID " + i);
                        }
                    }else{
                        if (variableMax > max) {
                            max = variableMax;
                            System.out.println("ID " + i);
                        }
                    }
                    variableMax = 0;
                }

            }
            i++;
        }
        System.out.println("Giorni di crescita: "+max);
        return true;
    }

    int getNumberActivityDay(HashMap<Integer, Long> dailyBalance) {
        long value;
        int days = 0;
        for (HashMap.Entry<Integer, Long> entry : dailyBalance.entrySet()) {
            value = entry.getValue();
            if (value != 0) days++;
        }
        return days;
    }

    int getNumberActivityDay(String address, String path) {
        CSVFile cv = new CSVFile();
        HashMap<Integer, Long> dailyBalance = cv.getDailyBalance(address, path);
        int maxDayZeroConsecutives = 10;
        int maxDaySameValue = 25;
        int daysToConsiderShortLive = 110;
        //System.out.println("DailyBalance address \""+address);
        boolean flag = false;
        int day = 0;
        int dayp = -1;
        long value = 0;
        long precvalue = 0;
        int numberZeroConsecutive = 0;
        long sameValue = 0;
        int i = 0;
        for (HashMap.Entry<Integer, Long> entry : dailyBalance.entrySet()) {
            value = entry.getValue();
            if (value == 0) {

                if (numberZeroConsecutive == 0) {
                    flag = true;
                } else {
                    flag = false;
                }

                if ((day - dayp) == 1 || flag) {
                    dayp = day;
                    numberZeroConsecutive++;
                } else {
                    numberZeroConsecutive = 0;
                }
            }

            if (value == precvalue) {
                sameValue++;
            } else {
                sameValue = 0;
            }

            if (numberZeroConsecutive > maxDayZeroConsecutives) {
                day -= maxDayZeroConsecutives;
                //System.out.println("Indirizzo con 10 0 consecutivi -> "+address);
                break;
            } else {
                if (sameValue > maxDaySameValue) {
                    day -= maxDaySameValue;
                    break;
                }
            }

            precvalue = value;
            day++;
            i++;
        }

        System.out.println("Life Day -> " + day);
        if (day < daysToConsiderShortLive) {
            flag = true;
        } else {
            flag = false;
        }
        return day;
    }

    /*
    * see http://shlegeris.com/2016/12/29/gini
     */
    float calculateGini(ArrayList<Long> values) {
        Collections.sort(values);
        float sum_of_absolute_differences = 0;
        float subsum = 0;

        int i;
        float x;
        for (i = 0; i < values.size(); i++) {
            x = values.get(i);
            sum_of_absolute_differences += i * x - subsum;
            subsum += x;
        }
        float result = (float) sum_of_absolute_differences / subsum / (values.size());
        result *= 100;

        //System.out.println("GINI: " + (result * 100));
        if (result > 100) return 100;
        return result;
    }

    public float calculateGiniAlternative(ArrayList<Long> incomes) {
        Collections.sort(incomes);
        float height = 0;
        float area = 0;
        float fair_area = 0;

        for (long value : incomes) {
            height += value;
            area += height - (value / 2);
        }
        fair_area = height * incomes.size() / 2;
        return ((fair_area - area) / fair_area);
    }

    public float calculateGiniProva(HashMap<Long, Integer> incomes, int tot) {
        float gini = 1;
        int x;

        //System.out.println("TOT: " + tot);

        for (Map.Entry<Long, Integer> vv : incomes.entrySet()) {
            x = vv.getValue();
            //System.out.println("x:"+x);
            //System.out.println("v:"+Math.pow((double)((float)x/tot),2));
            //System.out.println("z:"+(float)(x/tot));
            gini = gini - (float) Math.pow((double) ((float) x / tot), 2);
            //System.out.println("GINI: "+gini);
        }

        //System.out.println("GINI: " + (gini * 100));
        return (gini * 100);
    }

    float calculateGiniForValueSend(ArrayList<Long> arrayListValueSend) {
        return calculateGini(arrayListValueSend);
    }

    float calculateGiniForValueIncome(ArrayList<Long> arrayListValueIncome) {
        return calculateGini(arrayListValueIncome);
    }

    float calculateGiniForClassIncome(ArrayList<Long> arrayListValueIncome) {
        HashMap<Long, Integer> ricorrenze = new HashMap<>();
        int i = 0;

        for (long value : arrayListValueIncome) {
            //System.out.println("Valore -> " + value);
            ricorrenze.merge(value, 1, (newValue, oldValue) -> {
                if (oldValue == null)
                    return newValue;
                return oldValue + newValue;
            });
            i++;
        }
        HashMap<Long, Integer> orderedRicorrenzeIncome = (HashMap<Long, Integer>) sortByValue(ricorrenze);

        int sumFirt = 0;
        int j = 0;
        for (Map.Entry<Long, Integer> vv : orderedRicorrenzeIncome.entrySet()) {
            if (j == 3) break;
            sumFirt += vv.getValue(); //si gode!
            j++;
        }

        //System.out.println("\n " + orderedRicorrenzeIncome + "");
        //System.out.println("\ntotaleTransazioni-> " + i + "   :" + orderedRicorrenzeIncome.size() + "   SumFirst3: " + sumFirt);
        //System.out.println("Ricorrenze first3 " + ((float) sumFirt / i * 100) + "%");

        //System.out.println("Call to GINI for class Income:");
        return calculateGiniProva(orderedRicorrenzeIncome, i);
    }

    float calculateGiniForClassSend(ArrayList<Long> arrayListValueSend) {
        HashMap<Long, Integer> ricorrenze = new HashMap<>();
        int i = 0;

        for (long value : arrayListValueSend) {
            //System.out.println("Valore -> " + value);
            ricorrenze.merge(value, 1, (newValue, oldValue) -> {
                if (oldValue == null)
                    return newValue;
                return oldValue + newValue;
            });
            i++;
        }
        HashMap<Long, Integer> orderedRicorrenzeSend = (HashMap<Long, Integer>) sortByValue(ricorrenze);

        int sumFirt = 0;
        int j = 0;
        for (Map.Entry<Long, Integer> vv : orderedRicorrenzeSend.entrySet()) {
            if (j == 3) break;
            sumFirt += vv.getValue(); //si gode!
            j++;
        }

        //System.out.println("\n " + orderedRicorrenzeSend + "");
        //System.out.println("\ntotaleTransazioni-> " + i + "   :" + orderedRicorrenzeSend.size() + "   SumFirst3: " + sumFirt);
        //System.out.println("Ricorrenze first3 " + ((float) sumFirt / i * 100) + "%");

        //System.out.println("Call to GINI for class Send:");
        return calculateGiniProva(orderedRicorrenzeSend, i);
    }

    boolean hasMoreTransactionNumber(MongoConnector mongo, String add) {
        return (mongo.getNumberTransactions(add)) > MINTRANSACTIONNUMBER;
    }

    boolean hasMoreTransactionNumber(ArrayList<Long> send, ArrayList<Long> income) {
        return (send.size() + income.size()) > MINTRANSACTIONNUMBER;
    }

    int transactionNumber(ArrayList<Long> send, ArrayList<Long> income) {
        return (send.size() + income.size());
    }

    boolean hasFrequentlyUsage(HashMap<Integer, Integer> dailyIncomeNumber) {
        int max = 0;
        for (Map.Entry entry : dailyIncomeNumber.entrySet()) {
            int value = (Integer) entry.getValue();
            if (value > max) {
                max = value;
            }
        }
        System.out.println("Max: " + max);
        return max > MINFREQUENCYNUMBER;
    }

    int maxTrans(HashMap<Integer, Integer> dailyIncomeNumber) {
        int max = 0;
        for (Map.Entry entry : dailyIncomeNumber.entrySet()) {
            int value = (Integer) entry.getValue();
            if (value > max) {
                max = value;
            }
        }
        //System.out.println("Max: " + max);
        return max;
    }


    float getMedia(ArrayList<Long> arrayList) {
        float media = 0;
        for (Long value : arrayList) {
            media += value;
        }
        return (media / arrayList.size());
    }

    double getDevStandard(ArrayList<Long> arrayList, float media) {
        double devStandard = 0;
        for (Long a : arrayList) {
            devStandard += Math.pow((double) (a - media), 2);
        }
        return Math.sqrt((devStandard / arrayList.size()));
    }

    float getVarianza(ArrayList<Long> arrayList, float media) {
        float devStandard = 0;
        for (Long a : arrayList) {
            devStandard += Math.pow((double) (a - media), 2);
        }
        return (devStandard / arrayList.size());
    }

    long getTotalAmount(ArrayList<Long> arrayList) {
        long sum = 0;
        for (long l : arrayList) {
            sum += l;
        }
        return sum;
    }

    //This method calculate the daily balance of the address "address" and return a map day->dailyBalance
    protected EachValueForAddress getDailyBalance(MongoConnector mongo, String address) {
        EachValueForAddress eachValueForAddress = new EachValueForAddress();

        IncomeValues income = dailyIncome(mongo, address);

        HashMap<Integer, Long> dailyIncomeBTC = income.getDailyIncomeBTC();
        SendValues spend = dailySpending(mongo, address);
        HashMap<Integer, Long> dailySend = spend.getDailySentBTC();//Bilancio in uscita per giorno
        HashMap<Integer, Long> dailyBalance = new HashMap<>();

        ArrayList<Long> arrayListBTCIncome = income.getArrayListBTCIncome();
        ArrayList<Long> arrayListBTCSend = spend.getArrayListBTCSent();

        long currentBalance = 0;
        for (int i = 0; i < dailyIncomeBTC.size(); i++) { //Sono uniformati
            int day = i;
            long currentIncome = dailyIncomeBTC.get(day);
            long currentSend = dailySend.get(day);
            currentBalance = currentBalance + (currentIncome - currentSend);
            dailyBalance.put(day, currentBalance);
        }

        eachValueForAddress.setArrayListValueIncome(arrayListBTCIncome);
        eachValueForAddress.setArrayListUSDIncome(income.getArrayListUSDIncome());
        eachValueForAddress.setArrayListValueSend(arrayListBTCSend);
        eachValueForAddress.setArrayListUSDSend(spend.getArrayListUSDSent());
        eachValueForAddress.setDailyBalance(dailyBalance);
        eachValueForAddress.setIncomeFreq(income.getIncomeFreq());
        eachValueForAddress.setSendFreq(spend.getSendFreq());
        eachValueForAddress.setPaid(spend.getPaid());
        eachValueForAddress.setPaying(income.getPaying());
        eachValueForAddress.setTotalUSDIncome(income.getTotalUSDIncome());
        eachValueForAddress.setTotalUSDSend(spend.getTotalUSDSend());

        eachValueForAddress.setResponsiveness(getResponsiveness(income.getTimestampTransaction(), spend.getTimestampTransaction()));


        int numberAddressesPaidAfterTheyPaid = numberOfAddressesThatReceivedAmongTheOnesThatHaveSend(income, spend);
        eachValueForAddress.setNumberAddressesPaidAfterTheyPaid(numberAddressesPaidAfterTheyPaid);
        //System.out.println("NumberAddressesPaidAfterTheyPaid:" + numberAddressesPaidAfterTheyPaid);

        return eachValueForAddress;
    }

    protected EachValueForAddress getDailyBalanceCluster(MongoConnector mongo, ArrayList<String> clusterAddresses) {

        EachValueForAddress eachValueForAddress = new EachValueForAddress();
        ArrayList<Long> initAndEndTime = mongo.getTotalInitAndEndTimeOfCluster(clusterAddresses);

        System.out.println("Start getDailyBalanceClusterFunction!");
        IncomeValues income = dailyIncomeCluster(mongo, clusterAddresses, initAndEndTime);
        SendValues spend = dailySpendingCluster(mongo, clusterAddresses, initAndEndTime);
        HashMap<Integer, Long> dailyIncomeBTC = income.getDailyIncomeBTC();

        HashMap<Integer, Long> dailySend = spend.getDailySentBTC();//Bilancio in uscita per giorno
        HashMap<Integer, Long> dailyBalance = new HashMap<>();

        ArrayList<Long> arrayListBTCIncome = income.getArrayListBTCIncome();
        ArrayList<Long> arrayListBTCSend = spend.getArrayListBTCSent();

        long currentBalance = 0;
        for (int i = 0; i < dailyIncomeBTC.size(); i++) { //Sono uniformati
            int day = i;
            long currentIncome = dailyIncomeBTC.get(day);
            long currentSend = dailySend.get(day);
            currentBalance = currentBalance + (currentIncome - currentSend);
            dailyBalance.put(day, currentBalance);
        }

        eachValueForAddress.setArrayListValueIncome(arrayListBTCIncome);
        eachValueForAddress.setArrayListUSDIncome(income.getArrayListUSDIncome());
        eachValueForAddress.setArrayListValueSend(arrayListBTCSend);
        eachValueForAddress.setArrayListUSDSend(spend.getArrayListUSDSent());
        eachValueForAddress.setDailyBalance(dailyBalance);
        eachValueForAddress.setIncomeFreq(income.getIncomeFreq());
        eachValueForAddress.setSendFreq(spend.getSendFreq());
        eachValueForAddress.setPaid(spend.getPaid());
        eachValueForAddress.setPaying(income.getPaying());
        eachValueForAddress.setTotalUSDIncome(income.getTotalUSDIncome());
        eachValueForAddress.setTotalUSDSend(spend.getTotalUSDSend());

        eachValueForAddress.setResponsiveness(getResponsiveness(income.getTimestampTransaction(), spend.getTimestampTransaction()));


        int numberAddressesPaidAfterTheyPaid = numberOfAddressesThatReceivedAmongTheOnesThatHaveSend(income, spend);
        eachValueForAddress.setNumberAddressesPaidAfterTheyPaid(numberAddressesPaidAfterTheyPaid);
        //System.out.println("NumberAddressesPaidAfterTheyPaid:" + numberAddressesPaidAfterTheyPaid);

        return eachValueForAddress;
    }

    IncomeValues dailyIncome(MongoConnector mongo, String address) {
        IncomeValues incomeValues = new IncomeValues();
        FindIterable<Document> iterable = mongo.getReceiveTransactions(address);
        ArrayList<Long> initAndEndTime = mongo.getTotalInitAndEndTimeOfAddress(address);
        HashMap<Set<String>, Long> incomeToAddress = new HashMap<>();
        Set<Long> timestampTransaction = new TreeSet<>();


        double totalIncomeUSD = 0;
        long totalebtc = 0;
        int paying = 0;


        //System.out.println("Enter On Plotter dailyIncome");
        long initTotalTime = initAndEndTime.get(0);
        long endTotalTime = initAndEndTime.get(1);

        //System.out.println("Giorni -> " + ((endTotalTime - initTotalTime) / Settings.SECONDS_IN_A_DAY));
        int days = (int) ((endTotalTime - initTotalTime) / Settings.SECONDS_IN_A_DAY);
        long currentTime;
        long receiveValue = 0;
        Set<String> addresses = new HashSet<>();

        int i = 0;
        HashMap<Integer, Long> bilancioInIngressoPerGiorno = new HashMap<>();
        HashMap<Integer, Integer> numberDayIncomeTransaction = new HashMap<>();
        ArrayList<Long> arrayListIncomeBTC = new ArrayList<>();
        ArrayList<Double> arrayListIncomeUSD = new ArrayList<>();



        for (int c = 0; c <= days; c++) {
            bilancioInIngressoPerGiorno.put(c, (long) 0);
            numberDayIncomeTransaction.put(c, 0);
        }

        try (MongoCursor<Document> cursor = iterable.iterator()) {
            while (cursor.hasNext()) {
                try {
                    Document transaction = cursor.next();
                    String txid = (String) transaction.get("txid");
                    //System.out.println(transaction);
                    currentTime = (long) transaction.get("time");
                    timestampTransaction.add(currentTime);
                    int currentDay = (int) (currentTime - initTotalTime) / Settings.SECONDS_IN_A_DAY;
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
                    arrayListIncomeBTC.add(currentValue);
                    double exch = transaction.getDouble("rate");
                    //System.out.println(exch);
                    double usdV = (exch * ((float) currentValue / 100000000));
                    totalIncomeUSD += usdV;
                    arrayListIncomeUSD.add(usdV);

                    bilancioInIngressoPerGiorno.merge(currentDay, currentValue, (newValue, oldValue) -> {
                        if (oldValue == null)
                            return newValue;
                        return oldValue + newValue;
                    });

                    int toSum = 0;
                    if (currentValue > 0) {
                        toSum = 1;
                    }

                    numberDayIncomeTransaction.merge(currentDay, toSum, (newValue, oldValue) -> {
                        if (oldValue == null)
                            return newValue;
                        return oldValue + newValue;
                    });

                    i++;


                    //ArrayList<String> addressInTransaction = new ArrayList<>();
                    Set<String> addressInTransaction = new HashSet<>();

                    for (Document dcVin : ((ArrayList<Document>) transaction.get("vin"))) {
                        String addr = (String) dcVin.get("address");
                        //System.out.println("Addr: "+addr);
                        if (!addr.equals(address) && !addr.equals("")) {
                            addressInTransaction.add(addr);
                        }
                    }
                    //System.out.println("Addrrr: "+addressInTransaction);


                    boolean contains = false;
                    int notNull = 0;
                    for (String st : addressInTransaction) {
                        if (addresses.contains(st)) {
                            contains = true;
                            break;
                        }
                        if (!st.equals("")) {
                            notNull++;
                        }
                    }

                    if (notNull > 0) {
                        incomeToAddress.merge(addressInTransaction, currentValue, (newValue, oldValue) -> {
                            if (oldValue == null)
                                return newValue;
                            return oldValue + newValue;
                        });
                    }

                    if (contains) {
                        addresses.addAll(addressInTransaction);
                    } else {
                        addresses.addAll(addressInTransaction);
                        paying++;
                    }

                } catch (NoSuchElementException e) {
                    System.out.println("Problema ->" + e);
                }
            }
        } catch (Exception e) {
            System.out.println("Problema -> " + e);
            //e.printStackTrace();
        }

        //System.out.println("IncomeToAddress: "+incomeToAddress);

        incomeValues.setIncomeFreq(numberDayIncomeTransaction);
        incomeValues.setArrayListBTCIncome(arrayListIncomeBTC);
        incomeValues.setArrayListUSDIncome(arrayListIncomeUSD);
        incomeValues.setDailyIncomeBTC(bilancioInIngressoPerGiorno);
        incomeValues.setPaying(paying);
        incomeValues.setTotalUSDIncome(totalIncomeUSD);
        incomeValues.setIncomeToAddress(incomeToAddress);
        incomeValues.setTimestampTransaction(timestampTransaction);

        //System.out.println(address + "     RicevutoUSD: " + totalIncomeUSD);
        //System.out.println(address + "     RicevutoBTC: " + receiveValue);

        return incomeValues;
    }

    IncomeValues dailyIncomeCluster(MongoConnector mongo, ArrayList<String> clusterAddresses, ArrayList<Long> initAndEndTime) {
        IncomeValues incomeValues = new IncomeValues();
        FindIterable<Document> iterable = mongo.getReceiveTransactionsCluster(clusterAddresses);
        HashMap<Set<String>, Long> incomeToAddress = new HashMap<>();
        Set<Long> timestampTransaction = new TreeSet<>();


        double totalIncomeUSD = 0;
        long totalebtc = 0;
        int paying = 0;


        //System.out.println("Enter On Plotter dailyIncome");
        long initTotalTime = initAndEndTime.get(0);
        long endTotalTime = initAndEndTime.get(1);

        //System.out.println("Giorni -> " + ((endTotalTime - initTotalTime) / Settings.SECONDS_IN_A_DAY));
        int days = (int) ((endTotalTime - initTotalTime) / Settings.SECONDS_IN_A_DAY);
        long currentTime;
        long receiveValue = 0;
        Set<String> addresses = new HashSet<>();

        int i = 0;
        HashMap<Integer, Long> bilancioInIngressoPerGiorno = new HashMap<>();
        HashMap<Integer, Integer> numberDayIncomeTransaction = new HashMap<>();
        ArrayList<Long> arrayListIncomeBTC = new ArrayList<>();
        ArrayList<Double> arrayListIncomeUSD = new ArrayList<>();


        for (int c = 0; c <= days; c++) {
            bilancioInIngressoPerGiorno.put(c, (long) 0);
            numberDayIncomeTransaction.put(c, 0);
        }

        try (MongoCursor<Document> cursor = iterable.iterator()) {
            while (cursor.hasNext()) {
                try {
                    Document transaction = cursor.next();
                    String txid = (String) transaction.get("txid");
                    //System.out.println(transaction);
                    currentTime = (long) transaction.get("time");
                    timestampTransaction.add(currentTime);
                    int currentDay = (int) (currentTime - initTotalTime) / Settings.SECONDS_IN_A_DAY;
                    long currentValue = 0;

                    for (Document dcVout : ((ArrayList<Document>) transaction.get("vout"))) {
                        for (String dcAddress : ((ArrayList<String>) dcVout.get("addresses"))) {
                            if (clusterAddresses.contains(dcAddress)) {
                                currentValue += (long) dcVout.get("value");
                                //System.out.println("GIorno ->"+currentDay );
                            }
                        }
                    }
                    receiveValue += currentValue;
                    arrayListIncomeBTC.add(currentValue);
                    double exch = transaction.getDouble("rate");
                    //System.out.println(exch);
                    double usdV = (exch * ((float) currentValue / 100000000));
                    totalIncomeUSD += usdV;
                    arrayListIncomeUSD.add(usdV);

                    bilancioInIngressoPerGiorno.merge(currentDay, currentValue, (newValue, oldValue) -> {
                        if (oldValue == null)
                            return newValue;
                        return oldValue + newValue;
                    });

                    int toSum = 0;
                    if (currentValue > 0) {
                        toSum = 1;
                    }

                    numberDayIncomeTransaction.merge(currentDay, toSum, (newValue, oldValue) -> {
                        if (oldValue == null)
                            return newValue;
                        return oldValue + newValue;
                    });

                    i++;


                    //ArrayList<String> addressInTransaction = new ArrayList<>();
                    Set<String> addressInTransaction = new HashSet<>();

                    for (Document dcVin : ((ArrayList<Document>) transaction.get("vin"))) {
                        String addr = (String) dcVin.get("address");
                        //System.out.println("Addr: "+addr);
                        if (addr != null) {
                            if (!clusterAddresses.contains(addr) && !addr.equals("")) {
                                addressInTransaction.add(addr);
                            }
                        }
                    }
                    //System.out.println("Addrrr: "+addressInTransaction);


                    boolean contains = false;
                    int notNull = 0;
                    for (String st : addressInTransaction) {
                        if (addresses.contains(st)) {
                            contains = true;
                            break;
                        }
                        if (!st.equals("")) {
                            notNull++;
                        }
                    }

                    if (notNull > 0) {
                        incomeToAddress.merge(addressInTransaction, currentValue, (newValue, oldValue) -> {
                            if (oldValue == null)
                                return newValue;
                            return oldValue + newValue;
                        });
                    }

                    if (contains) {
                        addresses.addAll(addressInTransaction);
                    } else {
                        addresses.addAll(addressInTransaction);
                        paying++;
                    }

                } catch (NoSuchElementException e) {
                    System.out.println("Problema ->" + e);
                } catch (IndexOutOfBoundsException e) {
                }
            }
        } catch (Exception e) {
            System.out.println("Problema -> " + e);
            e.printStackTrace();
        }

        //System.out.println("IncomeToAddress: "+incomeToAddress);

        incomeValues.setIncomeFreq(numberDayIncomeTransaction);
        incomeValues.setArrayListBTCIncome(arrayListIncomeBTC);
        incomeValues.setArrayListUSDIncome(arrayListIncomeUSD);
        incomeValues.setDailyIncomeBTC(bilancioInIngressoPerGiorno);
        incomeValues.setPaying(paying);
        incomeValues.setTotalUSDIncome(totalIncomeUSD);
        incomeValues.setIncomeToAddress(incomeToAddress);
        incomeValues.setTimestampTransaction(timestampTransaction);

        //System.out.println(address + "     RicevutoUSD: " + totalIncomeUSD);
        //System.out.println(address + "     RicevutoBTC: " + receiveValue);

        return incomeValues;
    }


    SendValues dailySpending(MongoConnector mongo, String address) {
        AggregateIterable<Document> iterable = mongo.getSendTransactions(address);
        ArrayList<Long> initAndEndTime = mongo.getTotalInitAndEndTimeOfAddress(address);
        double totalSendUSD = 0;
        SendValues sendValues = new SendValues();
        HashMap<String, Long> sendToAddress = new HashMap<>();

        //System.out.println("Enter On Plotter dailySpending");
        long initTotalTime = initAndEndTime.get(0);
        long endTotalTime = initAndEndTime.get(1);
        long endTimeSend = 0;
        long initTimeSend = 0;
        long sendValue = 0;
        //System.out.println("Giorni -> " + ((endTotalTime - initTotalTime) / Settings.SECONDS_IN_A_DAY));
        int days = (int) ((endTotalTime - initTotalTime) / Settings.SECONDS_IN_A_DAY);
        long currentTime = initTimeSend;
        int i = 0;
        HashMap<Integer, Long> btcInUscitaPerGiorno = new HashMap<>();
        HashMap<Integer, Integer> numberSendTransactionForDay = new HashMap<>();
        ArrayList<Long> arrayListBTCSent = new ArrayList<>();
        ArrayList<Double> arrayListUSDSent = new ArrayList<>();

        Set<String> addresses = new HashSet<>();
        Set<Long> timestampTransaction = new TreeSet<>();

        int paid = 0;


        for (int c = 0; c <= days; c++) {
            btcInUscitaPerGiorno.put(c, 0l);
            numberSendTransactionForDay.put(c, 0);
        }

        String prevTxid = "";
        long sendValueTransaction = 0;
        long totalSum = 0;
        double rate = 0;
        int currentDay = 0;
        try (MongoCursor<Document> cursor = iterable.iterator()) {
            while (cursor.hasNext()) {

                try {
                    Document transaction = cursor.next();
                    //System.out.println("Transazione: "+transaction);

                    currentTime = (long) transaction.get("time");
                    if (i == 0) initTimeSend = currentTime;
                    currentDay = (int) (currentTime - initTotalTime) / Settings.SECONDS_IN_A_DAY;
                    long currentValue = 0;

                    //Aggiunta Timestamp su
                    timestampTransaction.add(currentTime);


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
                            arrayListBTCSent.add(sendValueTransaction);
                            totalSum += sendValueTransaction;
                            rate = transaction.getDouble("rate");
                            double usdV = (rate * ((float) sendValueTransaction / 100000000));
                            totalSendUSD += usdV;
                            arrayListUSDSent.add(usdV);

                            int toSum = 0;
                            if (sendValueTransaction > 0) {
                                toSum = 1;
                            }
                            numberSendTransactionForDay.merge(currentDay, toSum, (newValue, oldValue) -> {
                                if (oldValue == null)
                                    return newValue;
                                return oldValue + newValue;
                            });

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
                            //System.out.println(localAddress+"    Value:"+dc.get("value"));//giusto


                            if (localAddress.equals(address)) {
                                returnToAddress += (long) dc.get("value");
                                //System.out.println("Stesso indirizzo :" + returnToAddress);
                            } else {
                                sendToAddress.merge(localAddress, (long) dc.get("value"), (newValue, oldValue) -> {
                                    if (oldValue == null)
                                        return newValue;
                                    return oldValue + newValue;
                                });
                            }
                        }
                    }

                    currentValue = (inputValue - returnToAddress);
                    sendValueTransaction += currentValue;

                    /*
                    double rate = transaction.getDouble("rate");
                    double usdV = (rate * ((float) currentValue / 100000000));
                    totalSendUSD += usdV;
                    arrayListUSDSent.add(usdV);
                    */

                    btcInUscitaPerGiorno.merge(currentDay, currentValue, (newValue, oldValue) -> {
                        if (oldValue == null)
                            return newValue;
                        return oldValue + newValue;
                    });

                    sendValue += currentValue;

                    //System.out.println("SendValue : " + sendValue);
                    prevTxid = currentTxid;

                    i++;
                    //System.out.println(transaction.get("txid"));
                    //System.out.println(currentTime);
                    endTimeSend = currentTime;

                    for (Document dc : vout) {
                        for (String addr : (ArrayList<String>) dc.get("addresses")) {
                            if (!addr.equals(address) && !addresses.contains(addr)) {
                                addresses.add(addr);
                                paid++;
                            }
                        }
                    }

                } catch (NoSuchElementException e) {
                    System.out.println("Problema ->" + e);
                } catch (NullPointerException e) {
                    System.out.println("Problema ->" + e);
                }
            }
        }
        //Mi serve per inserire il valore dell'ultima transazione
        arrayListBTCSent.add(sendValueTransaction);
        totalSum += sendValueTransaction;
        double usdV = (rate * ((float) sendValueTransaction / 100000000));
        totalSendUSD += usdV;
        arrayListUSDSent.add(usdV);

        numberSendTransactionForDay.merge(currentDay, 1, (newValue, oldValue) -> {
            if (oldValue == null)
                return newValue;
            return oldValue + newValue;
        });
        /*
        //indirizzipagatidalloschema -> valoreBTC
        System.out.println("SentToAddress: "+sendToAddress);
        long tt=0;
        for(long c: sendToAddress.values()){
            tt+=c;
        }
        System.out.println("TotaleConsideratoAnkGliAltriIndirizzi:"+tt);
        */

        /*
        //Valore Inviato BTC e USD
        System.out.println(address + "     InviatoUSD: " + totalSendUSD);
        System.out.println(address + "     InviatoBTC: " + totalSum);
        */

        sendValues.setArrayListBTCSent(arrayListBTCSent);
        sendValues.setSendFreq(numberSendTransactionForDay);
        sendValues.setDailySentBTC(btcInUscitaPerGiorno);
        sendValues.setArrayListUSDSent(arrayListUSDSent);
        sendValues.setPaid(paid);
        sendValues.setTotalUSDSend(totalSendUSD);
        sendValues.setSendToAddress(sendToAddress);
        sendValues.setTimestampTransaction(timestampTransaction);
        return sendValues;
    }

    SendValues dailySpendingCluster(MongoConnector mongo, ArrayList<String> clusterAddress, ArrayList<Long> initAndEndTime) {
        AggregateIterable<Document> iterable = mongo.getSendTransactionsCluster(clusterAddress);
        double totalSendUSD = 0;
        SendValues sendValues = new SendValues();
        HashMap<String, Long> sendToAddress = new HashMap<>();

        //System.out.println("Enter On Plotter dailySpending");
        long initTotalTime = initAndEndTime.get(0);
        long endTotalTime = initAndEndTime.get(1);
        long endTimeSend = 0;
        long initTimeSend = 0;
        long sendValue = 0;
        //System.out.println("Giorni -> " + ((endTotalTime - initTotalTime) / Settings.SECONDS_IN_A_DAY));
        int days = (int) ((endTotalTime - initTotalTime) / Settings.SECONDS_IN_A_DAY);
        long currentTime = initTimeSend;
        int i = 0;
        HashMap<Integer, Long> btcInUscitaPerGiorno = new HashMap<>();
        HashMap<Integer, Integer> numberSendTransactionForDay = new HashMap<>();
        ArrayList<Long> arrayListBTCSent = new ArrayList<>();
        ArrayList<Double> arrayListUSDSent = new ArrayList<>();

        Set<String> addresses = new HashSet<>();
        Set<Long> timestampTransaction = new TreeSet<>();

        int paid = 0;


        for (int c = 0; c <= days; c++) {
            btcInUscitaPerGiorno.put(c, 0l);
            numberSendTransactionForDay.put(c, 0);
        }

        String prevTxid = "";
        long sendValueTransaction = 0;
        long totalSum = 0;
        double rate = 0;
        int currentDay = 0;
        try (MongoCursor<Document> cursor = iterable.iterator()) {
            while (cursor.hasNext()) {

                try {
                    Document transaction = cursor.next();
                    //System.out.println("Transazione: "+transaction);

                    currentTime = (long) transaction.get("time");
                    if (i == 0) initTimeSend = currentTime;
                    currentDay = (int) (currentTime - initTotalTime) / Settings.SECONDS_IN_A_DAY;
                    long currentValue = 0;

                    //Aggiunta Timestamp su
                    timestampTransaction.add(currentTime);


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
                            arrayListBTCSent.add(sendValueTransaction);
                            totalSum += sendValueTransaction;
                            rate = transaction.getDouble("rate");
                            double usdV = (rate * ((float) sendValueTransaction / 100000000));
                            totalSendUSD += usdV;
                            arrayListUSDSent.add(usdV);

                            int toSum = 0;
                            if (sendValueTransaction > 0) {
                                toSum = 1;
                            }
                            numberSendTransactionForDay.merge(currentDay, toSum, (newValue, oldValue) -> {
                                if (oldValue == null)
                                    return newValue;
                                return oldValue + newValue;
                            });

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
                            //System.out.println(localAddress+"    Value:"+dc.get("value"));//giusto


                            if (clusterAddress.contains(localAddress)) {
                                returnToAddress += (long) dc.get("value");
                                //System.out.println("Stesso indirizzo :" + returnToAddress);
                            } else {
                                sendToAddress.merge(localAddress, (long) dc.get("value"), (newValue, oldValue) -> {
                                    if (oldValue == null)
                                        return newValue;
                                    return oldValue + newValue;
                                });
                            }
                        }
                    }

                    currentValue = (inputValue - returnToAddress);
                    sendValueTransaction += currentValue;

                    /*
                    double rate = transaction.getDouble("rate");
                    double usdV = (rate * ((float) currentValue / 100000000));
                    totalSendUSD += usdV;
                    arrayListUSDSent.add(usdV);
                    */

                    btcInUscitaPerGiorno.merge(currentDay, currentValue, (newValue, oldValue) -> {
                        if (oldValue == null)
                            return newValue;
                        return oldValue + newValue;
                    });

                    sendValue += currentValue;

                    //System.out.println("SendValue : " + sendValue);
                    prevTxid = currentTxid;

                    i++;
                    //System.out.println(transaction.get("txid"));
                    //System.out.println(currentTime);
                    endTimeSend = currentTime;

                    for (Document dc : vout) {
                        for (String addr : (ArrayList<String>) dc.get("addresses")) {
                            if (!clusterAddress.contains(addr) && !addresses.contains(addr)) {
                                addresses.add(addr);
                                paid++;
                            }
                        }
                    }

                } catch (NoSuchElementException e) {
                    System.out.println("Problema ->" + e);
                } catch (NullPointerException e) {
                    System.out.println("Problema ->" + e);
                } catch (Exception e) {
                    System.out.println("Problema ->" + e);
                }
            }
        }
        //Mi serve per inserire il valore dell'ultima transazione
        arrayListBTCSent.add(sendValueTransaction);
        totalSum += sendValueTransaction;
        double usdV = (rate * ((float) sendValueTransaction / 100000000));
        totalSendUSD += usdV;
        arrayListUSDSent.add(usdV);

        numberSendTransactionForDay.merge(currentDay, 1, (newValue, oldValue) -> {
            if (oldValue == null)
                return newValue;
            return oldValue + newValue;
        });

        sendValues.setArrayListBTCSent(arrayListBTCSent);
        sendValues.setSendFreq(numberSendTransactionForDay);
        sendValues.setDailySentBTC(btcInUscitaPerGiorno);
        sendValues.setArrayListUSDSent(arrayListUSDSent);
        sendValues.setPaid(paid);
        sendValues.setTotalUSDSend(totalSendUSD);
        sendValues.setSendToAddress(sendToAddress);
        sendValues.setTimestampTransaction(timestampTransaction);
        return sendValues;
    }

    /*
    Pair<HashMap<Integer, Long>, ArrayList<Long>> dailySpendingCluster(MongoConnector mongo, ArrayList<String> addressesCluster) {
        AggregateIterable<Document> iterable = mongo.getSendTransactionsCluster(addressesCluster);
        ArrayList<Long> initAndEndTime = mongo.getTotalInitAndEndTimeOfCluster(addressesCluster);
        double totalSendUSD = 0;
        long totalebtc = 0;

        //System.out.println("Enter On Plotter dailySpending");
        long initTotalTime = initAndEndTime.get(0);
        long endTotalTime = initAndEndTime.get(1);
        long endTimeSend = 0;
        long initTimeSend = 0;
        long sendValue = 0;
        //System.out.println("Giorni -> " + ((endTotalTime - initTotalTime) / Settings.SECONDS_IN_A_DAY));
        int days = (int) ((endTotalTime - initTotalTime) / Settings.SECONDS_IN_A_DAY);
        long currentTime = initTimeSend;
        int i = 0;
        HashMap<Integer, Long> bilancioInUscitaPerGiorno = new HashMap<>();
        ArrayList<Long> arrayListSending = new ArrayList<>();

        for (int c = 0; c <= days; c++) {
            bilancioInUscitaPerGiorno.put(c, 0l);
        }

        String prevTxid = "";
        long sendValueTransaction = 0;
        long totalSum = 0;
        try (MongoCursor<Document> cursor = iterable.iterator()) {
            while (cursor.hasNext()) {

                try {
                    Document transaction = cursor.next();
                    String txid = (String) transaction.get("txid");

                    currentTime = (long) transaction.get("time");
                    if (i == 0) initTimeSend = currentTime;
                    int currentDay = (int) (currentTime - initTotalTime) / Settings.SECONDS_IN_A_DAY;
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
                            arrayListSending.add(sendValueTransaction);
                            totalSum += sendValueTransaction;
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

                            if (addressesCluster.contains(localAddress)) {
                                returnToAddress += (long) dc.get("value");
                                //System.out.println("Stesso indirizzo :" + returnToAddress);
                            }
                        }
                    }
                    currentValue = (inputValue - returnToAddress);
                    double rate = transaction.getDouble("rate");
                    totalSendUSD += (rate * ((float) currentValue / 100000000));
                    sendValueTransaction += currentValue;
                    bilancioInUscitaPerGiorno.merge(currentDay, currentValue, (newValue, oldValue) -> {
                        if (oldValue == null)
                            return newValue;
                        return oldValue + newValue;
                    });


                    sendValue += currentValue;

                    //System.out.println("SendValue : " + sendValue);
                    prevTxid = currentTxid;

                    i++;
                    //System.out.println(transaction.get("txid"));
                    //System.out.println(currentTime);
                    endTimeSend = currentTime;

                } catch (NoSuchElementException e) {
                    System.out.println("Problema ->" + e);
                } catch (NullPointerException e) {
                    System.out.println("Problema ->" + e);
                }
            }
        }
        totalSum += sendValueTransaction;
        //System.out.println("TotalSpending: "+totalSum);
        arrayListSending.add(sendValueTransaction);
        System.out.println("     SendUSD: " + totalSendUSD);
        System.out.println("     SendBTC: " + sendValue);
        return new Pair<>(bilancioInUscitaPerGiorno, arrayListSending);
        //return bilancioInUscitaPerGiorno;
    }
    */

    public int numberOfAddressesThatReceivedAmongTheOnesThatHaveSend(IncomeValues incomeValues, SendValues sendValues) { //From the Ponzi
        HashMap<Set<String>, Long> income = incomeValues.getIncomeToAddress();
        HashMap<String, Long> send = sendValues.getSendToAddress();
        int numberAddressThatReceive = 0;
        //System.out.println("Send:"+send);
        //System.out.println("Income:"+income);

        for (String sd : send.keySet()) {
            for (Set<String> sIn : income.keySet()) {
                if (sIn.contains(sd)) {
                    numberAddressThatReceive++;
                    break;
                }
            }
        }
        return numberAddressThatReceive;
    }

    public long getMaxDiffDay(HashMap<Integer, Long> dailyBalance) { //From the Ponzi
        long maxDifference = 0;
        long maxBTC = 0;
        long minBTC = Long.MAX_VALUE;
        long prevBTCValue = 0;
        for (int day = 0; day < dailyBalance.size(); day++) {
            long currentBTCValue = dailyBalance.get(day);
            if (currentBTCValue < minBTC) minBTC = currentBTCValue;
            if (currentBTCValue > maxBTC) maxBTC = currentBTCValue;

            if (day > 0) {
                if ((prevBTCValue - currentBTCValue) > maxDifference) maxDifference = (prevBTCValue - currentBTCValue);
            }
            prevBTCValue = currentBTCValue;
        }
        System.out.println("MaxScarto: " + maxDifference);

        return maxDifference;
    }

    public float getPercentMaxDiffDay(HashMap<Integer, Long> dailyBalance) { //From the Ponzi
        long maxDifference = 0;
        long maxBTC = 0;
        long minBTC = Long.MAX_VALUE;
        long prevBTCValue = 0;
        for (int day = 0; day < dailyBalance.size(); day++) {
            long currentBTCValue = dailyBalance.get(day);
            if (currentBTCValue < minBTC) minBTC = currentBTCValue;
            if (currentBTCValue > maxBTC) maxBTC = currentBTCValue;

            if (day > 0) {
                if ((prevBTCValue - currentBTCValue) > maxDifference) maxDifference = (prevBTCValue - currentBTCValue);
            }
            prevBTCValue = currentBTCValue;
        }
        //System.out.println("MaxPercentrScarto: "+ (float)maxDifference/maxBTC);
        return (float) maxDifference / maxBTC * 100;
    }

    /**
     * This method return the minimun, maximun and average time that
     *
     * @param timestampIn
     * @param timestampOut
     * @return Resoinsiveness
     */
    public Responsiveness getResponsiveness(Set<Long> timestampIn, Set<Long> timestampOut) {
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        long avg = 0;
        int n = 0;
        Responsiveness responsiveness = new Responsiveness();

        ArrayList<Long> input = new ArrayList<>();
        input.addAll(timestampIn);
        ArrayList<Long> output = new ArrayList<>();
        output.addAll(timestampOut);


        long currentInputValue = Long.MAX_VALUE;
        for (int i = 0; i < output.size(); i++) {
            long currentOutputValue = output.get(i);


            long precInputValue = Long.MAX_VALUE;
            boolean flag = false;
            ArrayList<Long> toRemoveFromInput = new ArrayList<>();

            for (int j = 0; j < input.size(); j++) {
                currentInputValue = input.get(j);
                //System.out.println("currentInputValue : " + currentInputValue);

                //toRemoveFromInput.add(currentInputValue);
                if (currentInputValue - currentOutputValue > 0) {
                    if (precInputValue != Long.MAX_VALUE) {
                        if (currentOutputValue - precInputValue < min) min = currentOutputValue - precInputValue;
                        if (currentOutputValue - precInputValue > max) max = currentOutputValue - precInputValue;
                        avg += currentOutputValue - precInputValue;
                        n++;
                        flag = true;
                    }
                    break;
                }

                precInputValue = currentInputValue;
                toRemoveFromInput.add(currentInputValue);
                if (j == input.size() - 1 && flag == false) {
                    flag = true;
                }
            }
            if (flag) input.removeAll(toRemoveFromInput);
            if (input.size() == 0) {
                if (min == Long.MAX_VALUE) {
                    min = max = avg = currentOutputValue - currentInputValue;
                } else {
                    if (currentOutputValue - currentInputValue < min) min = currentOutputValue - currentInputValue;
                    if (currentOutputValue - currentInputValue > max) max = currentOutputValue - currentInputValue;
                    avg += currentOutputValue - currentInputValue;
                    n++;
                }
                break;
            }
        }
        if (n > 0) {
            avg = avg / n;
        }

        responsiveness.setMin(min);
        responsiveness.setMax(max);
        responsiveness.setAvg(avg);
        System.out.println("\nMin: " + min + "  Max:" + max + "  Avg: " + avg);
        return responsiveness;
    }

    /*
    //This method calculate the daily balance of the address "address" and return a map day->dailyBalance
    protected EachValueForAddress getDailyBalanceCluster(MongoConnector mongo, ArrayList<String> clusterAddresses) {
        EachValueForAddress eachValueForAddress = new EachValueForAddress();

        IncomeValues income = dailyIncomeCluster(mongo, clusterAddresses);
        HashMap<Integer, Long> dailyIncome = income.getDailyIncomeBTC();
        Pair<HashMap<Integer, Long>, ArrayList<Long>> spend = dailySpendingCluster(mongo, clusterAddresses);
        HashMap<Integer, Long> dailySend = spend.getFirst();//Bilancio in uscita per giorno
        HashMap<Integer, Long> dailyBalance = new HashMap<>();

        ArrayList<Long> arrayListValueIncome = income.getArrayListBTCIncome();
        ArrayList<Long> arrayListValueSend = spend.getSecond();

        long currentBalance = 0;
        for (int i = 0; i < dailyIncome.size(); i++) {
            int day = i;
            long currentIncome = dailyIncome.get(day);
            long currentSend = dailySend.get(day);
            currentBalance = currentBalance + (currentIncome - currentSend);
            dailyBalance.put(day, currentBalance);
        }
        eachValueForAddress.setArrayListValueIncome(arrayListValueIncome);
        eachValueForAddress.setArrayListValueSend(arrayListValueSend);
        eachValueForAddress.setDailyBalance(dailyBalance);
        eachValueForAddress.setIncomeFreq(income.getIncomeFreq());
        return eachValueForAddress;
    }
    */

    /*
    Triple<HashMap<Integer, Long>, HashMap<Integer, Integer>, ArrayList<Long>> dailyIncomeCluster(MongoConnector mongo, ArrayList<String> addressesCluster) {

        FindIterable<Document> iterable = mongo.getReceiveTransactionsCluster(addressesCluster);
        ArrayList<Long> initAndEndTime = mongo.getTotalInitAndEndTimeOfCluster(addressesCluster);
        //System.out.println("Enter On Plotter dailyIncome");
        long initTotalTime = initAndEndTime.get(0);
        long endTotalTime = initAndEndTime.get(1);
        double totalIncomeUSD = 0;
        long totalebtc = 0;

        int days = (int) ((endTotalTime - initTotalTime) / Settings.SECONDS_IN_A_DAY);
        long currentTime;
        long receiveValue = 0;
        int i = 0;
        HashMap<Integer, Long> bilancioInIngressoPerGiorno = new HashMap<>();
        HashMap<Integer, Integer> bilancioInIngressoPerGiornoNumero = new HashMap<>();
        ArrayList<Long> arrayListIncomeValue = new ArrayList<>();


        for (int c = 0; c <= days; c++) {
            bilancioInIngressoPerGiorno.put(c, (long) 0);
            bilancioInIngressoPerGiornoNumero.put(c, 0);
        }

        try (MongoCursor<Document> cursor = iterable.iterator()) {
            while (cursor.hasNext()) {
                try {
                    Document transaction = cursor.next();
                    currentTime = (long) transaction.get("time");
                    String txid = (String) transaction.get("txid");

                    int currentDay = (int) (currentTime - initTotalTime) / Settings.SECONDS_IN_A_DAY;
                    long currentValue = 0;

                    for (Document dcVout : ((ArrayList<Document>) transaction.get("vout"))) {
                        for (String dcAddress : ((ArrayList<String>) dcVout.get("addresses"))) {
                            if (addressesCluster.contains(dcAddress)) {
                                currentValue += (long) dcVout.get("value");
                            }
                        }
                    }
                    double exch = transaction.getDouble("rate");
                    //System.out.println(exch);
                    totalIncomeUSD += (exch * ((float) currentValue / 100000000));
                    receiveValue += currentValue;
                    arrayListIncomeValue.add(currentValue);
                    bilancioInIngressoPerGiorno.merge(currentDay, currentValue, (newValue, oldValue) -> {
                        if (oldValue == null)
                            return newValue;
                        return oldValue + newValue;
                    });

                    int toSum = 0;
                    if (currentValue > 0) {
                        toSum = 1;
                    }

                    bilancioInIngressoPerGiornoNumero.merge(currentDay, toSum, (newValue, oldValue) -> {
                        if (oldValue == null)
                            return newValue;
                        return oldValue + newValue;
                    });

                    i++;

                } catch (NoSuchElementException e) {
                    System.out.println("Problema ->" + e);
                } catch (NullPointerException e) {
                    System.out.println("Problema ->" + e);
                }

                //if(i>2000)break;
            }

        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("     RicevutoUSD: " + totalIncomeUSD);
        System.out.println("     RicevutoBTC: " + receiveValue);
        //restituisce una coppia, giorno->bilancioInIngresso, giorno->numeroTransazioniInIngresso
        return new Triple<>(bilancioInIngressoPerGiorno, bilancioInIngressoPerGiornoNumero, arrayListIncomeValue);
        //return bilancioInIngressoPerGiorno;
    }
    */


    long getTotal(ArrayList<Long> arrayList) {
        long total = 0;
        for (long l : arrayList) {
            total += l;
        }
        return total;
    }


    static Instances getInstance(Float giniIncomeValue, Float giniSendValue, Float giniIncomeClass, Float giniSendClass, Integer totNumTransaction,
                                 Integer inNumTransaction, Integer outNumTransaction, Integer paying,Integer paid, Integer numberAddressesPaidAfterTheyPaid, Integer lifeAddresses, Integer activityDay, Boolean frequentUse,
                                 Boolean moreTransac, Float mediaIncomeV, Float mediaSendV, Double devStandardIncomeV, Double devStandardSendV,
                                 Float varianzaSendV, Float varianzaIncomeV, Long totalSendBTC, Long totalIncomeBTC, Double totalSendUSD, Double totalIncomeUSD,
                                 Integer maxNumTransRec, Long maxDiffDay, Responsiveness responsivenesses, Integer rangoCluster
                                ){
        Instances unlabeled = null;
        try {
            unlabeled = new Instances(
                    new BufferedReader(
                            new FileReader("C:\\Users\\serus\\Desktop\\DatasetClusterNewDataA.arff")));
        } catch (IOException e) {
            System.out.println("errore:"+e);
            e.printStackTrace();
        }
        // set class attribute

        Instance inst = unlabeled.firstInstance();
        unlabeled.clear();

        /*
        System.out.println("Gini:"+BigDecimal.valueOf(giniIncomeClass).setScale(2,BigDecimal.ROUND_HALF_UP).floatValue());
        giniIncomeClass = BigDecimal.valueOf(giniIncomeClass).setScale(2,BigDecimal.ROUND_HALF_UP).floatValue();
        System.out.println("Gini:"+giniIncomeClass);
        */

        inst.setValue(0, giniIncomeClass);
        inst.setValue(1, BigDecimal.valueOf(giniSendClass).setScale(2,BigDecimal.ROUND_HALF_UP).floatValue());
        inst.setValue(2, BigDecimal.valueOf(giniIncomeValue).setScale(2,BigDecimal.ROUND_HALF_UP).floatValue());
        inst.setValue(3, BigDecimal.valueOf(giniSendValue).setScale(2,BigDecimal.ROUND_HALF_UP).floatValue());
        inst.setValue(4, totNumTransaction);
        inst.setValue(5, inNumTransaction);
        inst.setValue(6, outNumTransaction);
        inst.setValue(7, paying);
        inst.setValue(8, paid);
        inst.setValue(9, numberAddressesPaidAfterTheyPaid);
        inst.setValue(10, lifeAddresses);
        inst.setValue(11, activityDay);

        String frU;
        if(frequentUse){frU="VERO";}else {frU="FALSO";}
        inst.setValue(12,frU);
        String mt;
        if(moreTransac){mt="VERO";}else {mt="FALSO";}
        inst.setValue(13, mt);

        float percent = (float) ((inNumTransaction) * 100) / (inNumTransaction + outNumTransaction);
        inst.setValue(14, BigDecimal.valueOf(percent).setScale(2,BigDecimal.ROUND_HALF_UP).floatValue());

        inst.setValue(15,mediaIncomeV);
        inst.setValue(16,mediaSendV);
        inst.setValue(17,BigDecimal.valueOf(devStandardIncomeV).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
        inst.setValue(18,BigDecimal.valueOf(devStandardSendV).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
        inst.setValue(19,varianzaSendV);
        inst.setValue(20,varianzaIncomeV);
        inst.setValue(21,totalSendBTC);
        inst.setValue(22,totalIncomeBTC);
        inst.setValue(23,BigDecimal.valueOf(totalSendUSD).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
        inst.setValue(24,BigDecimal.valueOf(totalIncomeUSD).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
        inst.setValue(25,maxNumTransRec);
        inst.setValue(26,maxDiffDay);
        inst.setValue(27,responsivenesses.getMin());
        inst.setValue(28,responsivenesses.getAvg());
        inst.setValue(29,responsivenesses.getMax());
        inst.setValue(30,rangoCluster);


        System.out.println("Instance: "+inst);
        unlabeled.add(inst);
        unlabeled.setClassIndex(unlabeled.numAttributes() - 1);
        return unlabeled;
    }

}