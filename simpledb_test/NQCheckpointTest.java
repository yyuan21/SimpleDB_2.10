import simpledb.server.SimpleDB;
import simpledb.file.*;
import simpledb.buffer.PageFormatter;
import simpledb.tx.Transaction;
import java.util.*;

public class NQCheckpointTest {
	private static Collection<Transaction> uncommittedTxs = new ArrayList<Transaction>();
	
	public static void main(String[] args) {
		SimpleDB.initFileLogAndBufferMgr("nqcheckpoint");
		PageFormatter fmtr = new PageFormatter(){
			public void format(Page p) {
				for (int i=0; i<Page.BLOCK_SIZE; i+=Page.INT_SIZE)
					p.setInt(i, 0);
			}};

		Transaction[]  txs = new Transaction[18];
		Transaction master = new Transaction();
		for (int i=2; i<18; i++) {
			Block blk = master.append("testfile", fmtr);
			txs[i] = new Transaction();
			uncommittedTxs.add(txs[i]);
			txs[i].pin(blk);
			int x = txs[i].getInt(blk, 0);
			txs[i].setInt(blk, 0, 1000+i);
			System.out.println("transaction " + i + " setint old=" + x + " new=" + 1000+i);
			txs[i].unpin(blk);
			if (i%3==2)
				pareDown();
		}
		
		master.commit();
		System.out.println("\n---------------------\n");
		Transaction tx = new Transaction();
		System.out.println("Initiating Recovery");
		System.out.println("Here are the visited log records");
		tx.recover();
	}

	// commit half of the uncommitted txs
	private static void pareDown() {
		Iterator<Transaction> iter = uncommittedTxs.iterator();
		int count = 0;
		while (iter.hasNext()) { // loop back to the beginning
			Transaction tx = iter.next();
			if (count % 2 == 0) {
				tx.commit();
				iter.remove();
			}				
			count++;
		}
	}
}
