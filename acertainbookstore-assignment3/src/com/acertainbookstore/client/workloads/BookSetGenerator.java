package com.acertainbookstore.client.workloads;

import java.util.*;
import java.util.stream.Collectors;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.BookStoreBook;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;

import java.util.Random; // added by beatZ

/**
 * Helper class to generate stockbooks and isbns modelled similar to Random
 * class
 */
public class BookSetGenerator {

	public BookSetGenerator() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Returns num randomly selected isbns from the input set
	 * 
	 * @param num
	 * @return
	 */
	public Set<Integer> sampleFromSetOfISBNs(Set<Integer> isbns, int num) {
		if (num == 0 || isbns.size() == 0 || num > isbns.size()) {
			return null; // TODO: is this the correct way to handle this?
		}

		Random rand = new Random();
		int randIndex;
		Set<Integer> returnISBNs = new HashSet<>();

		while (num > 0) {
			randIndex = rand.nextInt(isbns.size());
			int index = 0;

			for (Integer isbn : isbns) {
				if (index == randIndex) {
					returnISBNs.add(isbn);
					isbns.remove(isbn);
					num--;
					break;
				}
				index++;
			}
		}
		return returnISBNs;
	}

	/**
	 * Return num stock books. For now return an ImmutableStockBook
	 * 
	 * @param num
	 * @return
	 */
	public Set<StockBook> nextSetOfStockBooks(int num) {
		if (num == 0) {
			return null; // TODO: is this the correct way to handle this?
		}
		Random rand = new Random();
		Set<Integer> genBook = new HashSet<>();
		for (int i = 0; i < num; i++){
			genBook.add(rand.nextInt(i));
		}
		// TODO :: this is just returning a set of size num of random int
		return (Set<StockBook>) genBook.stream();
	}
}


