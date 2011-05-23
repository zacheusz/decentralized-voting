/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package protocol.communication;

import java.util.Map;
import runtime.executor.E_CryptoNodeID;

/**
 *
 * @author hamza
 */
public class FINAL_CLUSTER_ASSIGN_MSG extends Message {

    private static final long serialVersionUID = 1L;
    ClusterAssignment  clusterAssignment;

    public FINAL_CLUSTER_ASSIGN_MSG(E_CryptoNodeID src, E_CryptoNodeID dest, ClusterAssignment clusterAssignment) {
        super(Message.FINAL_CLUSTER_ASSIGN_MSG, src, dest);
		this.clusterAssignment = clusterAssignment;
    }
    public ClusterAssignment getClusterAssignment() {
		return clusterAssignment;
	}
    
    @Override
    public void doCopy(Message msg) {
            super.doCopy(msg);

            FINAL_CLUSTER_ASSIGN_MSG m = (FINAL_CLUSTER_ASSIGN_MSG) msg;
            clusterAssignment = m.clusterAssignment;
    }
}

