import simpledb.server.SimpleDB;
import simpledb.file.*;
import simpledb.buffer.PageFormatter;
import simpledb.tx.Transaction;
import simpledb.tx.concurrency.LockAbortException;

// Under wait-die, the output will correspond to the following schedule:
//    W(B, 1); W(C, 2); ROLLBACK(C); R(A, 2); COMMIT(B); W(A, 1); COMMIT(A)
// If we uncomment line 120, the output will correspond to the schedule:
//    W(B, 1); W(C, 2); COMMIT(B); R(C, 1); COMMIT(C); R(A, 2); W(A, 1); COMMIT(A)

public class WaitDieTest {
	public static void main(String[] args) {
		SimpleDB.initFileLogAndBufferMgr("waitdie");
		PageFormatter fmtr = new PageFormatter(){
			public void format(Page p) {
				for (int i=0; i<Page.BLOCK_SIZE; i+=Page.INT_SIZE)
					p.setInt(i, 0);
			}};

			try {
				Transaction tx = new Transaction();
				Block blk1 = tx.append("testfile", fmtr);
				Block blk2 = tx.append("testfile", fmtr);
				tx.commit();
				Thread th1 = new Thread(new TestA(blk1, blk2));
				th1.start();
				Thread.sleep(200); // Ensure that tx A is oldest.
				Thread th2 = new Thread(new TestB(blk1));
				th2.start();
				Thread.sleep(200); // Ensure that tx B is second oldest.

				Thread th3 = new Thread(new TestC(blk1, blk2));
				th3.start();  // Tx C will be the youngest.

				th1.join();
				th2.join();
				th3.join();
			}
			catch (InterruptedException e) {}
	}
}

class TestA implements Runnable {
	private Block blk1, blk2;
	public TestA(Block blk1, Block blk2) {
		this.blk1 = blk1;
		this.blk2 = blk2;
	}

	public void run() {
		Transaction tx = new Transaction();
		try {
			tx.pin(blk1); tx.pin(blk2);
			Thread.sleep(1200);
			System.out.println("Tx A: read 2 start");
			// Tx A must wait for tx C to release its writelock on block 2.
			int val = tx.getInt(blk2, 0);
			System.out.println("Tx A: read 2 end");
			System.out.println("Tx A: write 1 start");
			// Tx A must wait for tx B to release its writelock on block 1.
			tx.setInt(blk1, 0, val+1);
			System.out.println("Tx A: write 1 end");
			tx.commit();
		}
		catch(InterruptedException e) {}
		catch(LockAbortException e) {
			System.out.println("Transaction A aborts");
			tx.rollback();
		}
	}
}

class TestB implements Runnable {
	private Block blk1;
	public TestB(Block blk1) {
		this.blk1 = blk1;
	}

	public void run() {
		Transaction tx = new Transaction();
		try {
			tx.pin(blk1);
			System.out.println("Tx B: write 1 start");
			// Tx B is first to get the writelock on block 1.
			tx.setInt(blk1, 0, 0);
			System.out.println("Tx B: write 1 end");
			Thread.sleep(1500);
			tx.commit();
		}
		catch(InterruptedException e) {}
		catch(LockAbortException e) {
			System.out.println("Transaction B aborts");
			tx.rollback();
		}
	}
}

class TestC implements Runnable {
	private Block blk1, blk2;
	public TestC(Block blk1, Block blk2) {
		this.blk1 = blk1;
		this.blk2 = blk2;
	}

	public void run() {
		Transaction tx = new Transaction();
		try {
			Thread.sleep(500);
			tx.pin(blk1);
			tx.pin(blk2);
			System.out.println("Tx C: write 2 start");
			// Tx C gets its writelock on block 2 first.
			tx.setInt(blk2, 0, 0);
			System.out.println("Tx C: write 2 end");
			System.out.println("Tx C: read 1 start");
			// Under wait-die, tx C gets aborted because it must wait
			// for tx B to release its writelock on block 1.
			// If we uncomment the following statement, tx C will not get aborted.
			// Thread.sleep(2000);
			int val = tx.getInt(blk1, 0);
			System.out.println("Tx C: read 1 end");
			tx.commit();
		}
		catch(InterruptedException e) {}
		catch(LockAbortException e) {
			System.out.println("Transaction C aborts");
			tx.rollback();
		}
	}
}
