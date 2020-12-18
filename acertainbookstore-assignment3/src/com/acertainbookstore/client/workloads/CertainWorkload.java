
package com.acertainbookstore.client.workloads;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.acertainbookstore.business.*;
import com.acertainbookstore.client.BookStoreHTTPProxy;
import com.acertainbookstore.client.StockManagerHTTPProxy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;

/**
 * 
 * CertainWorkload class runs the workloads by different workers concurrently.
 * It configures the environment for the workers using WorkloadConfiguration
 * objects and reports the metrics
 * 
 */
public class CertainWorkload {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		int numConcurrentWorkloadThreads = 10;
		String serverAddress = "http://localhost:8081";
		boolean localTest = true;
		List<WorkerRunResult> workerRunResults = new ArrayList<WorkerRunResult>();
		List<Future<WorkerRunResult>> runResults = new ArrayList<Future<WorkerRunResult>>();

		// Initialize the RPC interfaces if its not a localTest, the variable is
		// overriden if the property is set
		String localTestProperty = System
				.getProperty(BookStoreConstants.PROPERTY_KEY_LOCAL_TEST);
		localTest = (localTestProperty != null) ? Boolean
				.parseBoolean(localTestProperty) : localTest;

		BookStore bookStore = null;
		StockManager stockManager = null;
		if (localTest) {
			CertainBookStore store = new CertainBookStore();
			bookStore = store;
			stockManager = store;
		} else {
			stockManager = new StockManagerHTTPProxy(serverAddress + "/stock");
			bookStore = new BookStoreHTTPProxy(serverAddress);
		}

		// Generate data in the bookstore before running the workload
		initializeBookStoreData(bookStore, stockManager);

		ExecutorService exec = Executors
				.newFixedThreadPool(numConcurrentWorkloadThreads);

		for (int i = 0; i < numConcurrentWorkloadThreads; i++) {
			WorkloadConfiguration config = new WorkloadConfiguration(bookStore,
					stockManager);
			Worker workerTask = new Worker(config);
			// Keep the futures to wait for the result from the thread
			runResults.add(exec.submit(workerTask));
		}

		// Get the results from the threads using the futures returned
		for (Future<WorkerRunResult> futureRunResult : runResults) {
			WorkerRunResult runResult = futureRunResult.get(); // blocking call
			workerRunResults.add(runResult);
		}

		exec.shutdownNow(); // shutdown the executor

		// Finished initialization, stop the clients if not localTest
		if (!localTest) {
			((BookStoreHTTPProxy) bookStore).stop();
			((StockManagerHTTPProxy) stockManager).stop();
		}

		// Sending localTest as parameter just for text file naming
		reportMetric(workerRunResults, localTest);
	}

	/**
	 * Computes the metrics and prints them
	 * 
	 * @param workerRunResults
	 */
	public static void reportMetric(List<WorkerRunResult> workerRunResults, Boolean localTest) throws FileNotFoundException {
		// Calculate throughput and latency
		Boolean issueFound = false;
		double throughput = 0;
		double latency = 0;
		try {
			String locality = localTest ? "local" : "rpc";
			FileWriter resultOut = new FileWriter(locality+"_"+workerRunResults.size()+"_clients.txt");

			for (WorkerRunResult result : workerRunResults) {
				// Check less than 1% interactions are unsuccessful and customer interactions roughly 60% of all interactions
				if (result.getTotalRuns() * 0.99 > result.getSuccessfulInteractions() &&
						result.getSuccessfulInteractions() * 0.55 < result.getSuccessfulFrequentBookStoreInteractionRuns() &&
						result.getSuccessfulInteractions() * 0.65 > result.getSuccessfulFrequentBookStoreInteractionRuns()) {
					System.out.println("Issue in implementation");
					issueFound = true;
					break;
				}
				throughput += (double)result.getSuccessfulFrequentBookStoreInteractionRuns() / result.getElapsedTimeInNanoSecs();
				latency += result.getElapsedTimeInNanoSecs();
			}

			if (!issueFound) {
				latency /= workerRunResults.size();
				resultOut.write("Throughput: "+String.valueOf(throughput)+"\r\nLatency: "+String.valueOf(latency));
				System.out.println("Throughput: " + throughput);
				System.out.println("Latency: " + latency);
			}
			resultOut.close();
		} catch (IOException e) {
			System.out.println("An error occurred with writing to txt.");
			e.printStackTrace();
		}
	}

	/**
	 * Generate the data in bookstore before the workload interactions are run
	 * 
	 * Ignores the serverAddress if its a localTest
	 * 
	 */
	public static void initializeBookStoreData(BookStore bookStore,
			StockManager stockManager) throws BookStoreException {

		Set<StockBook> initBooks = new HashSet<>();
		initBooks.add(new ImmutableStockBook(123456,
				"Harry Potter and JUnit", "JK Unit",
				(float) 7, 10, 0,
				0, 0, false));
        initBooks.add(new ImmutableStockBook(654321,
				"The Adventures of JUnit", "Hope Unit",
				(float) 1, 300, 5,
				0, 0, false));
        initBooks.add(new ImmutableStockBook(491283,
				"A Guide On How to Use Face Mask", "Pringles",
				(float) 10, 100, 1,
				0, 0, true));
		initBooks.add(new ImmutableStockBook(123457,
				"The Silver Bottle", "Miss. Princess",
				(float) 7, 10, 3,
				8, 3, false));
		initBooks.add(new ImmutableStockBook(123458,
				"The Bet of Your Life", "Mr. Green",
				(float) 1, 79, 0,
				34, 3, false));
		initBooks.add(new ImmutableStockBook(123459,
				"A Very Ugly Swam", "HCA",
				(float) 10, 100, 4,
				29, 10, true));
		initBooks.add(new ImmutableStockBook(123410,
				"U-Boats", "Peter Madsen",
				(float) 7, 10, 0,
				2, 1, false));
		initBooks.add(new ImmutableStockBook(123411,
				"How to not fail", "Mr. Brown",
				(float) 1, 198, 0,
				60, 10, true));
		initBooks.add(new ImmutableStockBook(123412,
				"A Certain BookStore", "ACS",
				(float) 10, 50, 0,
				3, 7, true));
		initBooks.add(new ImmutableStockBook(123413,
				"The Color Yellow", "MC Einer",
				(float) 7, 56, 0,
				1, 10, true));
		initBooks.add(new ImmutableStockBook(123414,
				"Geometric Shapes and more", "Doc. Geo",
				(float) 1, 1, 17,
				12, 8, false));
		initBooks.add(new ImmutableStockBook(123415,
				"We Decide What's Red", "MacAfee",
				(float) 10, 10, 10,
				10, 10, false));

//		Random rand = new Random();
//
//		for (int i = 0; i < 20; i++) {
//			initBooks.add(new ImmutableStockBook(123456,
//					"Title"+i, "Author"+1,
//					(float) 7, rand.nextInt(50), 0,
//					0, 0, (Boolean) rand.nextBoolean()));
//		}

		stockManager.addBooks(initBooks);

	}
}
