/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package protocol.communication;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import runtime.executor.E_CryptoNodeID;
import protocol.node.CryptoNode;

/**
 *
 * @author hamza
 */
public class ClusterChoice implements Serializable {

    public List< Set<E_CryptoNodeID>> nodeToClusterList;
    public int keyNum=-1;
    //       public int steps;

    public void add(int chosenCluster, E_CryptoNodeID nodeID) {
        nodeToClusterList.get(chosenCluster).add(nodeID);
    }
    
    public int myGroupID=-1;
    public E_CryptoNodeID myNodeID;
    
    public Set<E_CryptoNodeID> get(int cluster) {
        return nodeToClusterList.get(cluster);
    }
    
    public double [] dishonestFractions;
//
//        public int getSmallestCluster() {
//            int minSize = 0;
//            int min = 0;
//            int currentSize = 0;
//            for (int i = 0; i < CryptoNode.numClusters; i++) {
//
//                currentSize = nodeToClusterList.get(i).size();
//                if (currentSize < minSize) {
//                    min = i;
//                    minSize = currentSize;
//                }
//            }
//            return min;
//
//        }
//        
//    public void mergeClusterChoice(ClusterChoice recNodeToCluster ) {
//
//            steps--;
//            for (int i = 0; i < CryptoNode.numClusters; i++) {                                
//                nodeToClusterList.add(i,recNodeToCluster.get(i));               
//   
//            }
//        
//    }
//    public int getNbNodes(){
//        int count=0;    
//        for (int i = 0; i < CryptoNode.numClusters; i++) {                                
//                count+=nodeToClusterList.get(i).size();
//        }
//        return count;
//   
//    }
//        public void resetSteps() {        
//            steps=(int) (Math.log(CryptoNode.VOTERCOUNT)+Math.log(CryptoNode.VOTERCOUNT)/Math.log(2)+CryptoNode.stepsConstant);
//        }

    public ClusterChoice() {
        //        steps=(int) (Math.log(CryptoNode.VOTERCOUNT)+Math.log(CryptoNode.VOTERCOUNT)/Math.log(2)+CryptoNode.stepsConstant);
        nodeToClusterList = new LinkedList< Set< E_CryptoNodeID>>();

        for (int i = 0; i < CryptoNode.numClusters; i++) {
            Set<E_CryptoNodeID> singleList = new HashSet<E_CryptoNodeID>();
            nodeToClusterList.add(singleList);
        }
    }

    public ClusterChoice(List<E_CryptoNodeID> initNodeToClusterList,E_CryptoNodeID myNodeID) {

        nodeToClusterList = new LinkedList< Set< E_CryptoNodeID>>();

        Set<E_CryptoNodeID> singleList;
        E_CryptoNodeID tempId;
        int index=0;
        int size=initNodeToClusterList.size();
        for (int i = 0; i < CryptoNode.numClusters; i++) {
            singleList = new HashSet<E_CryptoNodeID>();
            
            for (int j = 0; j < CryptoNode.nodesPerCluster; j++) {
                index=i * CryptoNode.nodesPerCluster + j;
                if (index>=size)
                    break;
                
                tempId=initNodeToClusterList.get(index);               
                singleList.add(tempId);
            //    System.out.println(tempId.toString());
          //      System.out.println(myNodeID.toString());
                if (myNodeID.equals(tempId))
                {
                    myGroupID=i;
                    if (i==0)
                        keyNum=j;
                }
            }
            
            nodeToClusterList.add(singleList);
        }
        
        
        if (myGroupID==0 && keyNum==0)
        {
            dishonestFractions=new double [CryptoNode.numClusters];
            
            for (int i = 0; i < CryptoNode.numClusters; i++) {
                
                for (E_CryptoNodeID curID:nodeToClusterList.get(i))
                {
                    if (curID.isMalicious)
                        dishonestFractions[i]++;
                        
                }
                dishonestFractions[i]=dishonestFractions[i]/nodeToClusterList.get(i).size();
                System.out.println("Group "+i+": "+dishonestFractions[i]);
                
                                
            }
            
        
        }
    }
}
