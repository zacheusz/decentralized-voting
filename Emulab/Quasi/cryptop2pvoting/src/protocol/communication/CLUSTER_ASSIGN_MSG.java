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
public class CLUSTER_ASSIGN_MSG extends Message {

    private static final long serialVersionUID = 1L;
    Map<E_CryptoNodeID, Integer> IDAssignment;

    public CLUSTER_ASSIGN_MSG(E_CryptoNodeID src, E_CryptoNodeID dest, Map<E_CryptoNodeID, Integer> IDAssignment) {
        super(Message.CLUSTER_ASSIGN_MSG, src, dest);
		this.IDAssignment = IDAssignment;
    }
    public Map<E_CryptoNodeID, Integer> getIDAssignment() {
		return IDAssignment;
	}
    
    @Override
    public void doCopy(Message msg) {
            super.doCopy(msg);

            CLUSTER_ASSIGN_MSG m = (CLUSTER_ASSIGN_MSG) msg;
            IDAssignment = m.IDAssignment;
    }
}

