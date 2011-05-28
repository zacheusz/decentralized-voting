package launchers.executor;

/*
import OldVoting.PublicKey;
import OldVoting.SecretKey;
*/
import java.io.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import paillierp.PaillierThreshold;
import protocol.node.CryptoNode;

import runtime.NetworkSend;
import runtime.NodeIDReader;
import runtime.Stopper;
import runtime.TaskManager;
import runtime.executor.E_CryptoConnectionListener;
import runtime.executor.E_CryptoNodeID;

import runtime.executor.E_CryptoThreadPerTaskTaskManager;
import runtime.executor.E_NetworkSend;

import runtime.executor.E_Stopper;


public class CryptoGossipLauncher {

	public static void main(String[] args) throws Exception {

		HashMap<String, String> arguments = new HashMap<String, String>();
		for (int i = 0; i < args.length; i++) {
			arguments.put(args[i], args[i + 1]);
			i++;
		}
		CryptoSetup.configure(arguments);
		
		// From Launcher
		String bset = arguments.get("-bset");
		String name = arguments.get("-name");
		int port = Integer.parseInt(arguments.get("-port"));
                
                CryptoNode.secKeyFile = arguments.get("-secretKeyFile");
          //      String pubKeyFile = arguments.get("-publicKeyFile");

            //    int groupId = Integer.parseInt(arguments.get("-groupId"));
		
		E_CryptoNodeID id = new E_CryptoNodeID(name, port,false);

		TaskManager taskManager = new E_CryptoThreadPerTaskTaskManager();
		NetworkSend networkSend = new E_NetworkSend();

//		Set<E_CryptoNodeID> bootstrapSet = initBootstrapSet(bset, nodeIDReader);

		Stopper stopper = new E_Stopper(taskManager);

/*                PublicKey pub=(PublicKey) getObject(pubKeyFile);
                SecretKey sec=(SecretKey) getObject(secKeyFile);
                int shareOrder=Integer.parseInt(arguments.get("-shareOrder"));
*/
               // PaillierThreshold sec=(PaillierThreshold) getObject(CryptoNode.secKeyFile+"0");
                CryptoNode node = new CryptoNode(id,taskManager,networkSend,stopper);

		((E_CryptoThreadPerTaskTaskManager) taskManager).setCryptoNode(node);

		new Thread(new E_CryptoConnectionListener(port, taskManager, node)).start();
	}
 public static Object getObject(String filename)
        {
        FileInputStream fis = null;
       ObjectInputStream in = null;
       Object obj=null;
        try {
            fis = new FileInputStream(filename);
            in = new ObjectInputStream(fis);
            obj =  in.readObject();            
            in.close();
            fis.close();
            return obj;
        } catch (FileNotFoundException ex) {
            System.out.println("file not found, mycount: " +CryptoNode.mycount);
            Logger.getLogger(CryptoGossipLauncher.class.getName()).log(Level.SEVERE, null, ex);

       }
       catch(IOException ex)
       {
         ex.printStackTrace();
       }
       catch(ClassNotFoundException ex)
       {
         ex.printStackTrace();
       }
       return obj;
       }

	static void printUsage() {
		System.out
				.println("SimpleGossipLauncher -name inet_name -p port"
						+ "-bset boostrapset_file" + "-fileName output_filename");
	}

	public static Set<E_CryptoNodeID> initBootstrapSet(String bootstrapSetFile,
			NodeIDReader nodeIDReader) {
		try {
			Set<E_CryptoNodeID> bootstrapSet = new HashSet<E_CryptoNodeID>();
			FileReader fr = new FileReader(bootstrapSetFile);
			BufferedReader inBuff = new BufferedReader(fr);
			String peer = "";
			while ((peer = inBuff.readLine()) != null) {
				E_CryptoNodeID id = nodeIDReader.readBootstrapNodeID(peer);
				bootstrapSet.add(id);
			}
			inBuff.close();
			fr.close();
			return bootstrapSet;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}

     

}
