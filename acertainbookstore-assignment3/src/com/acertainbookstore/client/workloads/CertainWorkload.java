
package com.acertainbookstore.client.workloads;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
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

		reportMetric(workerRunResults);
	}

	/**
	 * Computes the metrics and prints them
	 * 
	 * @param workerRunResults
	 */
	public static void reportMetric(List<WorkerRunResult> workerRunResults) throws FileNotFoundException {
		// TODO: You should aggregate metrics and output them for plotting here

		// Calculate throughput and latency
		Boolean issueFound = false;
		float throughput = 0;
		float latency = 0;
		PrintWriter throOut = new PrintWriter("../../../../../throFile.txt");
		PrintWriter lanOut = new PrintWriter("../../../../../lanFile.txt");

		for (WorkerRunResult result: workerRunResults) {
			// Check less than 1% interactions are unsuccessful and customer interactions roughly 60% of all interactions
			if (result.getTotalRuns() * 0.99 < result.getSuccessfulInteractions() &&
				result.getSuccessfulInteractions() * 0.55 < result.getSuccessfulFrequentBookStoreInteractionRuns() &&
				result.getSuccessfulInteractions() * 0.65 > result.getSuccessfulFrequentBookStoreInteractionRuns()) {
					issueFound = true;
			}
			throughput += result.getSuccessfulFrequentBookStoreInteractionRuns() / result.getElapsedTimeInNanoSecs(); // TODO: I assume this is customer interactions?
			latency += result.getElapsedTimeInNanoSecs();
			throOut.println(throughput);
			lanOut.println(latency);
			throOut.close();
			lanOut.close();
		}

		if (issueFound) {
			// TODO: What to do here?
		}
		else {
			latency /= workerRunResults.size();
			lanOut.println(latency);
			lanOut.close();
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

		//TODO tjek if this is the way forward
		Set<StockBook> booksToAdd_0 = new HashSet<StockBook>();
		Set<StockBook> booksToAdd_1 = new HashSet<StockBook>();
		Set<StockBook> booksToAdd_2 = new HashSet<StockBook>();

		booksToAdd_0 = Collections.singleton(new ImmutableStockBook(123456,
				"Harry Potter and JUnit", "JK Unit",
				(float) 7, 10, 0,
				0, 0, false));
		booksToAdd_1 = Collections.singleton(new ImmutableStockBook(654321,
				"The Adventures of JUnit", "Hope Unit",
				(float) 1, 300, 0,
				0, 0, false));
		booksToAdd_2 = Collections.singleton(new ImmutableStockBook(491283,
				"A Guide On How to Use Face Mask", "Pringles",
				(float) 10, 100, 0,
				0, 0, true));

		stockManager.addCopies((Set<BookCopy>)booksToAdd_0.stream());
		stockManager.addCopies((Set<BookCopy>)booksToAdd_1.stream());
		stockManager.addCopies((Set<BookCopy>)booksToAdd_2.stream());

		// TODO: You should initialize data for your bookstore here

	}
}
