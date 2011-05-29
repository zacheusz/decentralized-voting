/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package launchers.executor;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

/**
 *
 * @author hamza
 */
public class MajK {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException {
       
        
        //values to change
        double epsilon = 0.16667;       
        int MAXVOTERCOUNT = 2000;               
        int MINVOTERCOUNT = 2000;
        int voterstep=3*60;
        double KMIN=10;
        double KMAX =30 ;
        double  kstep=2 ;
        int runTimes =40000000;

        double kval = 0.5;
        double threshOrder = (0.5 - epsilon) * MAXVOTERCOUNT;
        boolean isMal;
        Random generator = new Random();
        String randomID;
        wrapper wr;
        Map<wrapper, Integer> IDAssignment = new HashMap<wrapper, Integer>();
        List<wrapper> sortedIDs;

        List< Set<wrapper>> nodeToClusterList = new LinkedList< Set< wrapper>>();

        Set<wrapper> singleList = null;
        int index = 0;
//        double[] stdfractionArray = new double[KMAX];
//        double[] meanfractionArray = new double[KMAX];
        double[] tempfractionArray = new double[runTimes];
        double[] dishonestFractions;
        List<Double> finalFractions = new LinkedList<Double>();
        double fractionDishonestGroups = 0;
        int ktemp = 0;
        for (int voter = MINVOTERCOUNT; voter <= MAXVOTERCOUNT; voter += voterstep) {

            for (kval = KMIN; kval <= KMAX; kval +=kstep) {
                for (int u = 0; u < runTimes; u++) {
                    ktemp = 0;
                    fractionDishonestGroups = 0;
                    IDAssignment = new HashMap<wrapper, Integer>();
                    nodeToClusterList = new LinkedList< Set< wrapper>>();
                    for (int i = 1; i <= MAXVOTERCOUNT; i++) {

                        randomID = String.valueOf(generator.nextInt((int) Math.pow(MAXVOTERCOUNT, u)));

                        isMal = (i < threshOrder);

                        wr = new wrapper(isMal, randomID);

                        IDAssignment.put(wr, getHash(wr.id, MAXVOTERCOUNT));
                    }

                    sortedIDs = sortByValue(IDAssignment);

                    int numClusters = (int) (Math.ceil(MAXVOTERCOUNT / (kval * Math.log(MAXVOTERCOUNT))));
                    int nodesPerCluster = (int) (Math.ceil(MAXVOTERCOUNT * 1.0 / numClusters));
                    while (nodesPerCluster * numClusters >= MAXVOTERCOUNT) {
                        numClusters--;
                    }
                    numClusters++;

                    int size = sortedIDs.size();
                    wrapper tempId;
                    for (int i = 0; i < numClusters; i++) {
                        singleList = new HashSet<wrapper>();

                        for (int j = 0; j < nodesPerCluster; j++) {
                            index = i * nodesPerCluster + j;
                            if (index >= size) {
                                break;
                            }

                            tempId = sortedIDs.get(index);
                            singleList.add(tempId);

                        }

                        nodeToClusterList.add(singleList);

                    }


                    dishonestFractions = new double[numClusters];

                    for (int i = 0; i < numClusters; i++) {
                        dishonestFractions[i] = 0;
                    }

                    for (int i = 0; i < numClusters; i++) {

                        for (wrapper curID : nodeToClusterList.get(i)) {
                            if (curID.isMalicious) {
                                dishonestFractions[i]++;
                            }

                        }
                        dishonestFractions[i] = dishonestFractions[i] / nodeToClusterList.get(i).size();
                        if (dishonestFractions[i] >= 0.5) {
                            fractionDishonestGroups++;
                        }


                        // System.out.println("Group " + i + ": " + dishonestFractions[i]);


                    }

                    fractionDishonestGroups = fractionDishonestGroups / numClusters;
                    tempfractionArray[u] = Math.ceil(fractionDishonestGroups);
                    //    System.out.println(kvalue+" "+fractionDishonestGroups);


                }
                System.out.print(voter+" "+kval + " ");
                for (double s : tempfractionArray) {
                    System.out.print(s + " ");

                }
                System.out.println("");
//                stdfractionArray[ktemp] = standard_deviation(tempfractionArray);
//                meanfractionArray[ktemp] = mean(tempfractionArray);

                ktemp++;
            }
        }
//             kval = 2;
//            for (double s : finalFractions) {
//                //         System.out.println(VOTERCOUNT+" "+kval + " " + s + " ");
//                kval += 2;
//            }
//
//            kval = 2;
//            //    System.out.println("std:");
//            for (double s : stdfractionArray) {
//                //    System.out.print(kval + ":" + s + " ");
//                kval += 2;
//            }
//            //      System.out.println("");
//            //      System.out.println("mean:");
//            kval = 2;
//            for (double s : meanfractionArray) {
//                //    System.out.print(kval + ":" + s + " ");
//                kval += 2;
//
//            }
        }

    

    public static List sortByValue(final Map m) {
        List keys = new ArrayList();
        keys.addAll(m.keySet());
        Collections.sort(keys, new Comparator() {

            public int compare(Object o1, Object o2) {
                Object v1 = m.get(o1);
                Object v2 = m.get(o2);
                if (v1 == null) {
                    return (v2 == null) ? 0 : 1;
                } else if (v1 instanceof Comparable) {
                    return ((Comparable) v1).compareTo(v2);
                } else {
                    return 0;
                }
            }
        });
        return keys;
    }

    static int getHash(String input, int VOTERCOUNT) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        int hash;
        byte[] thedigest = null;



        byte[] bytesOfMessage = input.getBytes("UTF-8");
        MessageDigest md = MessageDigest.getInstance("MD5");
        thedigest = md.digest(bytesOfMessage);


        hash = (new String(thedigest)).hashCode();

        hash = (hash < 0) ? -hash : hash;
        return hash % VOTERCOUNT;
    }

//    /**
//     * @param population an array, the population
//     * @return the variance
//     */
//    public static double mean(double[] population) {
//        double sum = 0;
//        int count = 0;
//        for (double x : population) {
//            sum += x;
//            count++;
//        }
//        return sum / count;
//    }
//
//    public static double variance(double[] population) {
//        long n = 0;
//        double mean = 0;
//        double s = 0.0;
//
//        for (double x : population) {
//            n++;
//            double delta = x - mean;
//            mean += delta / n;
//            s += delta * (x - mean);
//        }
//        // if you want to calculate std deviation
//        // of a sample change this to (s/(n-1))
//        return (s / n);
//    }
//
//    /**
//     * @param population an array, the population
//     * @return the standard deviation
//     */
//    public static double standard_deviation(double[] population) {
//        return Math.sqrt(variance(population));
//    }
}
