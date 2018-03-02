package it.unica.blockchain.bitcoin.parser;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Ponzi on 31/07/2017.
 */
public class Cluster {

    Set<String> heuristic1(MongoConnector mongo, String address) {
        FindIterable<Document> iterable = mongo.getAllSendTransactions(address);
        int numDiffAddress = 0;
        Set<String> addressSamePerson = new TreeSet<>();

        try (MongoCursor<Document> cursor = iterable.iterator()) {
            while (cursor.hasNext()) {
                try {
                    Document transaction = cursor.next();
                    ArrayList<Document> vin = (ArrayList<Document>) transaction.get("vin");

                    for (Document vinEl : vin) {
                        String vinAddress = vinEl.getString("address");
                        if (!address.equals(vinAddress) && !vinAddress.equals("") && !vinAddress.equals(null)) {
                            addressSamePerson.add(vinAddress);
                        }

                    }

                } catch (NoSuchElementException e) {
                    System.out.println("ProblemaH1 ->" + e);
                } catch (NullPointerException e) {
                    System.out.println("ProblemaH1 ->" + e);
                }
            }
            for (String ad : addressSamePerson) {
                //System.out.println("Address: " + ad + "     NumT: " + mongo.getNumberTransactions(ad));
            }
            System.out.println("Numero Indirizzi Diversi: " + addressSamePerson.size());
        }
        return addressSamePerson;
    }
}
