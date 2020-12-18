/**
 * 
 */
package com.acertainbookstore.client.workloads;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreException;

/**
 *
 * Worker represents the workload runner which runs the workloads with
 * parameters using WorkloadConfiguration and then reports the results
 *
 */
public class Worker implements Callable<WorkerRunResult> {
    private WorkloadConfiguration configuration = null;
    private int numSuccessfulFrequentBookStoreInteraction = 0;
    private int numTotalFrequentBookStoreInteraction = 0;

    public Worker(WorkloadConfiguration config) {
	configuration = config;
    }

    /**
     * Run the appropriate interaction while trying to maintain the configured
     * distributions
     *
     * Updates the counts of total runs and successful runs for customer
     * interaction
     *
     * @param chooseInteraction
     * @return
     */
    private boolean runInteraction(float chooseInteraction) {
	try {
	    float percentRareStockManagerInteraction = configuration.getPercentRareStockManagerInteraction();
	    float percentFrequentStockManagerInteraction = configuration.getPercentFrequentStockManagerInteraction();

	    if (chooseInteraction < percentRareStockManagerInteraction) {
		runRareStockManagerInteraction();
	    } else if (chooseInteraction < percentRareStockManagerInteraction
		    + percentFrequentStockManagerInteraction) {
		runFrequentStockManagerInteraction();
	    } else {
		numTotalFrequentBookStoreInteraction++;
		runFrequentBookStoreInteraction();
		numSuccessfulFrequentBookStoreInteraction++;
	    }
	} catch (BookStoreException ex) {
	    return false;
	}
	return true;
    }

    /**
     * Run the workloads trying to respect the distributions of the interactions
     * and return result in the end
     */
    public WorkerRunResult call() throws Exception {
	int count = 1;
	long startTimeInNanoSecs = 0;
	long endTimeInNanoSecs = 0;
	int successfulInteractions = 0;
	long timeForRunsInNanoSecs = 0;

	Random rand = new Random();
	float chooseInteraction;

	// Perform the warmup runs
	while (count++ <= configuration.getWarmUpRuns()) {
	    chooseInteraction = rand.nextFloat() * 100f;
	    runInteraction(chooseInteraction);
	}

	count = 1;
	numTotalFrequentBookStoreInteraction = 0;
	numSuccessfulFrequentBookStoreInteraction = 0;

	// Perform the actual runs
	startTimeInNanoSecs = System.nanoTime();
	while (count++ <= configuration.getNumActualRuns()) {
	    chooseInteraction = rand.nextFloat() * 100f;
	    if (runInteraction(chooseInteraction)) {
		successfulInteractions++;
	    }
	}
	endTimeInNanoSecs = System.nanoTime();
	timeForRunsInNanoSecs += (endTimeInNanoSecs - startTimeInNanoSecs);
	return new WorkerRunResult(successfulInteractions, timeForRunsInNanoSecs, configuration.getNumActualRuns(),
		numSuccessfulFrequentBookStoreInteraction, numTotalFrequentBookStoreInteraction);
    }

    /**
     * Runs the new stock acquisition interaction
     *
     * @throws BookStoreException
     */
    private void runRareStockManagerInteraction() throws BookStoreException {
    // TODO: Add code for New Stock Acquisition Interaction
		List<StockBook> listBooks = configuration.getStockManager().getBooks();
		BookSetGenerator bookSetGenerator = new BookSetGenerator();
		Set<StockBook> randBooks = bookSetGenerator.nextSetOfStockBooks(configuration.getNumBooksToAdd());

		Set<StockBook> nonExistingBooks = new HashSet<>();
		for (StockBook book : randBooks) {
			// From assignment text: "It then checks if the set of ISBNs is in the list of books fetched"
			if (!listBooks.stream().anyMatch(b -> b.getISBN() == book.getISBN())) { //
				nonExistingBooks.add(book);
			}
		}

		configuration.getStockManager().addBooks(nonExistingBooks);
    }

    /**
     * Runs the stock replenishment interaction
     *
     * @throws BookStoreException
     */
    private void runFrequentStockManagerInteraction() throws BookStoreException {
	// TODO: Add code for Stock Replenishment Interaction
	//TODO is this in the correct path
		List<StockBook> listBooks = configuration.getStockManager().getBooks();

		// Sort by quantities
		listBooks = listBooks.stream()
							.sorted(Comparator.comparingDouble(StockBook::getNumCopies))
							.collect(Collectors.toList())
							.subList(0, configuration.getNumBooksWithLeastCopies());

		// Convert to Set<BookCopy>
		Set<BookCopy> bookCopies = new HashSet<>();
		for (StockBook book: listBooks) {
			bookCopies.add(new BookCopy(book.getISBN(), configuration.getNumAddCopies()));
		}

		configuration.getStockManager().addCopies(bookCopies);
    }

    /**
     * Runs the customer interaction
     *
     * @throws BookStoreException
     */
    private void runFrequentBookStoreInteraction() throws BookStoreException {
	// TODO: Add code for Customer Interaction
	// TODO figure out how to add set-isbns
		int num = 10;
		BookStore bookStore = null;
		BookSetGenerator bookSetGenerator = new BookSetGenerator();
		Set<Integer> isbn = new HashSet<>(); // TODO this is only a temp sol until i fig out the sol
		Set<Integer> isbns = bookSetGenerator.sampleFromSetOfISBNs(isbn, num);

		assert bookStore != null;
		Set<Book> books = bookStore.getEditorPicks(isbns.stream().collect(Collectors.toList()).subList(0, 2).size()).stream().collect(Collectors.toSet());
    //TODO add buyBooks(Set<BookCopy>);
    }

}

