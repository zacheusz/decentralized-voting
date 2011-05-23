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
public class POSITION_ASSIGN_MSG extends Message {

    private static final long serialVersionUID = 1L;
    ClusterChoice nodeToCluster;

    public POSITION_ASSIGN_MSG(E_CryptoNodeID src, E_CryptoNodeID dest,  ClusterChoice nodeToCluster) {
        super(Message.POSITION_ASSIGN_MSG, src, dest);
		this.nodeToCluster = nodeToCluster;
    }
    public ClusterChoice getNodeToCluster() {
		return nodeToCluster;
	}
    
    @Override
    public void doCopy(Message msg) {
            super.doCopy(msg);

            POSITION_ASSIGN_MSG m = (POSITION_ASSIGN_MSG) msg;
            nodeToCluster = m.nodeToCluster;
    }
}

