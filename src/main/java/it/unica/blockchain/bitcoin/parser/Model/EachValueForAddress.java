package it.unica.blockchain.bitcoin.parser.Model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ponzi on 24/07/2017.
 */
public class EachValueForAddress {
    private ArrayList<Long> arrayListBTCIncome;
    private ArrayList<Long> arrayListBTCSend;
    private ArrayList<Double> arrayListUSDIncome;
    private ArrayList<Double> arrayListUSDSend;
    private HashMap<Integer, Integer> incomeFreq;
    private HashMap<Integer, Integer> sendFreq;

    private HashMap<Integer, Long> dailyBalance;

    private double totalUSDSend;
    private double totalUSDIncome;
    private int paying;
    private int paid;
    private int numberAddressesPaidAfterTheyPaid;
    private Responsiveness responsiveness;


    public ArrayList<Long> getArrayListValueIncome() {
        return arrayListBTCIncome;
    }

    public void setArrayListValueIncome(ArrayList<Long> arrayListValueIncome) {
        this.arrayListBTCIncome = arrayListValueIncome;
    }

    public ArrayList<Long> getArrayListValueSend() {
        return arrayListBTCSend;
    }

    public void setArrayListValueSend(ArrayList<Long> arrayListValueSend) {
        this.arrayListBTCSend = arrayListValueSend;
    }

    public HashMap<Integer, Integer> getIncomeFreq() {
        return incomeFreq;
    }

    public void setIncomeFreq(HashMap<Integer, Integer> incomeFreq) {
        this.incomeFreq = incomeFreq;
    }

    public HashMap<Integer, Long> getDailyBalance() {
        return dailyBalance;
    }

    public void setDailyBalance(HashMap<Integer, Long> dailyBalance) {
        this.dailyBalance = dailyBalance;
    }

    public ArrayList<Double> getArrayListUSDIncome() {
        return arrayListUSDIncome;
    }

    public void setArrayListUSDIncome(ArrayList<Double> arrayListUSDIncome) {
        this.arrayListUSDIncome = arrayListUSDIncome;
    }

    public ArrayList<Double> getArrayListUSDSend() {
        return arrayListUSDSend;
    }

    public void setArrayListUSDSend(ArrayList<Double> arrayListUSDSend) {
        this.arrayListUSDSend = arrayListUSDSend;
    }

    public int getPaying() {
        return paying;
    }

    public void setPaying(int paying) {
        this.paying = paying;
    }

    public int getPaid() {
        return paid;
    }

    public void setPaid(int paid) {
        this.paid = paid;
    }

    public double getTotalUSDSend() {
        return totalUSDSend;
    }

    public void setTotalUSDSend(double totalUSDSend) {
        this.totalUSDSend = totalUSDSend;
    }

    public double getTotalUSDIncome() {
        return totalUSDIncome;
    }

    public void setTotalUSDIncome(double totalUSDIncome) {
        this.totalUSDIncome = totalUSDIncome;
    }

    public int getNumberAddressesPaidAfterTheyPaid() {
        return numberAddressesPaidAfterTheyPaid;
    }

    public void setNumberAddressesPaidAfterTheyPaid(int numberAddressesPaidAfterTheyPaid) {
        this.numberAddressesPaidAfterTheyPaid = numberAddressesPaidAfterTheyPaid;
    }

    public HashMap<Integer, Integer> getSendFreq() {
        return sendFreq;
    }

    public void setSendFreq(HashMap<Integer, Integer> sendFreq) {
        this.sendFreq = sendFreq;
    }


    public Responsiveness getResponsiveness() {
        return responsiveness;
    }

    public void setResponsiveness(Responsiveness responsiveness) {
        this.responsiveness = responsiveness;
    }
}
