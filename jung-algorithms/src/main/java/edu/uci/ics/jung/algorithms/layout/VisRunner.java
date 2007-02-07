package edu.uci.ics.jung.algorithms.layout;

import edu.uci.ics.jung.algorithms.IterativeContext;

public class VisRunner implements Relaxer, Runnable {
	
	protected boolean running;
	protected IterativeContext process;
	protected boolean stop;
	protected boolean manualSuspend;
	protected Thread thread;
	
	/**
	 * how long the relaxer thread pauses between iteration loops.
	 */
	protected long sleepTime = 100L;

	
	public VisRunner(IterativeContext process) {
		this.process = process;
	}

	/**
	 * @return the relaxerThreadSleepTime
	 */
	public long getSleepTime() {
		return sleepTime;
	}

	/**
	 * @param relaxerThreadSleepTime the relaxerThreadSleepTime to set
	 */
	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}
	
	public void prerelax() {
		manualSuspend = true;
//		long time = System.currentTimeMillis();
		long timeNow = System.currentTimeMillis();
		while (System.currentTimeMillis() - timeNow < 500 && !process.done()) {
			process.step();
		}
//		System.err.println("time was "+(System.currentTimeMillis()-time));
		manualSuspend = false;
	}
	

	public void pause() {
		manualSuspend = true;
		
	}

	public void relax() {
		// in case its running
		stop();
		stop = false;
		thread = new Thread(this);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}
	
	public Object pauseObject = new String("PAUSE OBJECT");

	public void resume() {
		manualSuspend = false;
		if(running == false) {
			prerelax();
			relax();
		} else {
			synchronized(pauseObject) {
				pauseObject.notifyAll();
			}
		}
	}


	public synchronized void stop() {
		if(thread != null) {
		manualSuspend = false;
		stop = true;
		// interrupt the relaxer, in case it is paused or sleeping
		// this should ensure that visRunnerIsRunning gets set to false
		try { thread.interrupt(); }
        catch(Exception ex) {
            // the applet security manager may have prevented this.
            // just sleep for a second to let the thread stop on its own
//            System.err.println("got "+ex);
            try { Thread.sleep(1000); }
            catch(InterruptedException ie) {} // ignore
        }
		synchronized (pauseObject) {
			pauseObject.notifyAll();
		}
		}
	}

	public void run() {
	    running = true;
	    try {
	        while (!process.done() && !stop) {
	            synchronized (pauseObject) {
	                while (manualSuspend && !stop) {
	                    try {
	                        pauseObject.wait();
	                    } catch (InterruptedException e) {
//	                        System.err.println("vis runner wait interrupted");
	                    }
	                }
	            }
	            process.step();
	            
	            if (stop)
	                return;
	            
	            try {
	                Thread.sleep(sleepTime);
	            } catch (InterruptedException ie) {
//	                System.err.println("vis runner sleep interrupted");
	            }
	        }

	    } finally {
	        running = false;
	    }
	}
}