/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package launchers.executor;

import java.io.PrintStream;
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

class runner extends Thread {

    public static PrintStream out = null;
    public static Object LOCK = new Object();
    ;

    int numVoters;
    int kvalue;
    int RUNTHREADS;
    int runs;
    static int numofkvalues;
//    static Integer[] finalcountsArray = new Integer[numofkvalues];
//       static Integer[] counterArray = new Integer[numofkvalues];
    static int[] finalcountsArray;
    static int[] counterArray;

    runner(int numVoters, int kvalue, int runs, int runthreads, int numofkvalues) {
        this.numVoters = numVoters;
        this.kvalue = kvalue;
        this.runs = runs;
        this.RUNTHREADS = runthreads;
        this.numofkvalues = numofkvalues;
        finalcountsArray = new int[numofkvalues];
        counterArray = new int[numofkvalues];

        for (int i = 0; i < numofkvalues; i++) {
            counterArray[i] = 0;
            finalcountsArray[i] = 0;
        }
    }
    static int coun = 0;

    public void dump(String message) {
        if (out != null) {
            synchronized (out) {
                out.println(message);
                out.flush();
            }
        }
        synchronized (System.out) {
            System.out.println(message);
        }



    }

    public void run() {

        //values to change
        double epsilon = 0.16667;
        int MAXVOTERCOUNT = numVoters;
        int MINVOTERCOUNT = numVoters;
        int voterstep = 3 * 60;
        int KMIN = kvalue;
        int KMAX = kvalue;
        double kstep = 2;
        int runTimes = runs;
        int finalcounts=0;

        int kval = 1;
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

        int tempfraction = 0;
        double[] dishonestFractions;
        List<Double> finalFractions = new LinkedList<Double>();
        double fractionDishonestGroups = 0;
        int ktemp = 0;
        for (int voter = MINVOTERCOUNT; voter <= MAXVOTERCOUNT; voter += voterstep) {

            for (kval = KMIN; kval <= KMAX; kval += kstep) {
                for (int u = 0; u < runTimes / RUNTHREADS; u++) {

                    ktemp = 0;
                    fractionDishonestGroups = 0;
                    IDAssignment = new HashMap<wrapper, Integer>();
                    nodeToClusterList = new LinkedList< Set< wrapper>>();
                    for (int i = 1; i <= MAXVOTERCOUNT; i++) {

                        randomID = String.valueOf(generator.nextInt((int) Math.pow(MAXVOTERCOUNT, u)));

                        isMal = (i < threshOrder);

                        wr = new wrapper(isMal, randomID);
                        try {
                            IDAssignment.put(wr, MajK.getHash(wr.id, MAXVOTERCOUNT));
                        } catch (UnsupportedEncodingException ex) {
                            Logger.getLogger(MajK.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (NoSuchAlgorithmException ex) {
                            Logger.getLogger(MajK.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    sortedIDs = MajK.sortByValue(IDAssignment);

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
                    //  tempfractionArray[u] += Math.ceil(fractionDishonestGroups);
                    tempfraction += Math.ceil(fractionDishonestGroups);

                    //    System.out.println(kvalue+" "+fractionDishonestGroups);

                    System.out.println(++coun);
                }

//                for (double s : tempfractionArray) {
//                    System.out.print(s + " ");
//
//                }
                //    System.out.println("");
//                stdfractionArray[ktemp] = standard_deviation(tempfractionArray);
//                meanfractionArray[ktemp] = mean(tempfractionArray);

                ktemp++;
            }


        }
//            synchronized(finalcountsArray[(kvalue-10)/2])
//                    {
//                        synchronized(counterArray[(kvalue-10)/2])
//                    {
//                //        System.out.println(""+counterArray[(kvalue-10)/2].intValue());
//                        finalcountsArray[(kvalue-10)/2]=Integer.valueOf(finalcountsArray[(kvalue-10)/2].intValue()+tempfraction);
//                        counterArray[(kvalue-10)/2]=Integer.valueOf(counterArray[(kvalue-10)/2].intValue()+1);
//                        if (counterArray[(kvalue-10)/2].intValue()==RUNTHREADS){
//                            dump(MINVOTERCOUNT + " " + kvalue + " " + finalcountsArray[(kvalue-10)/2]);
//                        }
//                    }
//                    }


//
//        finalcountsArray[(kvalue - 10) / 2] += tempfraction;
//        counterArray[(kvalue - 10) / 2]++;
//        //   System.out.println(""+counterArray[(kvalue-10)/2]);
//        if (counterArray[(kvalue - 10) / 2] >= RUNTHREADS) {
//            dump(MINVOTERCOUNT + " " + kvalue + " " + finalcountsArray[(kvalue - 10) / 2]);
//        }

        
        
        
            dump(MINVOTERCOUNT + " " + kvalue + " " + finalcounts);
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


/**
 *
 * @author hamza
 */
public class MajK {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        HashMap<String, String> arguments = new HashMap<String, String>();
        for (int i = 0; i < args.length; i++) {
			arguments.put(args[i], args[i + 1]);
			i++;
		}
        int inputK = Integer.parseInt(arguments.get("-inputK"));
        int runs = Integer.parseInt(arguments.get("-runs"));

//int runs=10000;
        int runthreads = 1;
        int kmin = inputK;
        int kmax = inputK;
        for (int kval = kmin; kval <= kmax; kval += 2) {
            for (int u = 0; u < runthreads; u++) {
                Thread thread = new runner(1000, kval, runs, runthreads, 1 + (kmax - kmin) / 2);
                thread.start();
            }
        }
    }

    //    HashMap<String, String> arguments = new HashMap<String, String>();
//        int inputK = Integer.parseInt(arguments.get("-inputK"));
//                int nvoters = Integer.parseInt(arguments.get("-nvoters"));
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
