/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package protocol.communication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
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
public class ClusterAssignment implements Serializable{

        List< Set<E_CryptoNodeID>> nodeToClusterList;
        public int steps;

        public void add(int chosenCluster, E_CryptoNodeID nodeID) {
            nodeToClusterList.get(chosenCluster).add(nodeID);
        }

        public Set<E_CryptoNodeID> get(int cluster) {
            return nodeToClusterList.get(cluster);
        }

        public ClusterAssignment(Map<E_CryptoNodeID, Integer>  IDAssignment) {
            E_CryptoNodeID id;            
            int cluster = 0;
            int count = 1;
            Set<E_CryptoNodeID> singleSet = null;
            nodeToClusterList = new LinkedList< Set< E_CryptoNodeID>>();
            for (Iterator i = sortByValue(IDAssignment).iterator(); i.hasNext();) {
                id = (E_CryptoNodeID) i.next();                
                cluster = (int) Math.floor(count / (CryptoNode.kvalue * Math.log(CryptoNode.VOTERCOUNT)));
                count++;
                if (nodeToClusterList.get(cluster) == null) {
                    singleSet = new HashSet<E_CryptoNodeID>();
                    singleSet.add(id);
                } else {
                    nodeToClusterList.get(cluster).add(id);
                }
            }
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
    }
