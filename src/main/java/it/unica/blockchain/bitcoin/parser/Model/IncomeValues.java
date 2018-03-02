package it.unica.blockchain.bitcoin.parser.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Ponzi on 16/10/2017.
 */
public class IncomeValues {
    private ArrayList<Long> arrayListBTCIncome;
    private ArrayList<Double> arrayListUSDIncome;
    private HashMap<Integer, Integer> incomeFreq;
    private HashMap<Integer, Long> dailyIncomeBTC;
    private HashMap<Set<String>, Long> incomeToAddress = new HashMap<>();
    private Set<Long> timestampTransaction = new TreeSet<>();
    private double totalUSDIncome;
    private int paying;


    public ArrayList<Long> getArrayListBTCIncome() {
        return arrayListBTCIncome;
    }

    public void setArrayListBTCIncome(ArrayList<Long> arrayListValueIncome) {
        this.arrayListBTCIncome = arrayListValueIncome;
    }

    public HashMap<Integer, Integer> getIncomeFreq() {
        return incomeFreq;
    }

    public void setIncomeFreq(HashMap<Integer, Integer> incomeFreq) {
        this.incomeFreq = incomeFreq;
    }


    public ArrayList<Double> getArrayListUSDIncome() {
        return arrayListUSDIncome;
    }

    public void setArrayListUSDIncome(ArrayList<Double> arrayListUSDIncome) {
        this.arrayListUSDIncome = arrayListUSDIncome;
    }


    public int getPaying() {
        return paying;
    }

    public void setPaying(int paying) {
        this.paying = paying;
    }

    public HashMap<Integer, Long> getDailyIncomeBTC() {
        return dailyIncomeBTC;
    }

    public void setDailyIncomeBTC(HashMap<Integer, Long> dailyIncomeBTC) {
        this.dailyIncomeBTC = dailyIncomeBTC;
    }


    public double getTotalUSDIncome() {
        return totalUSDIncome;
    }

    public void setTotalUSDIncome(double totalUSDIncome) {
        this.totalUSDIncome = totalUSDIncome;
    }


    public HashMap<Set<String>, Long> getIncomeToAddress() {
        return incomeToAddress;
    }

    public void setIncomeToAddress(HashMap<Set<String>, Long> incomeToAddress) {
        this.incomeToAddress = incomeToAddress;
    }

    public Set<Long> getTimestampTransaction() {
        return timestampTransaction;
    }

    public void setTimestampTransaction(Set<Long> timestampTransaction) {
        this.timestampTransaction = timestampTransaction;
    }
}
