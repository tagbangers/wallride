package org.wallride.core.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * データをページングするクラスです。
 * 
 * @author ogawa
 */
public class Paginator<E> implements Serializable {
	
	private static final long serialVersionUID = -9211053486050832101L;

	public static final int DEFAULT_PER_PAGE = 50;

	public static final int DEFAULT_DELTA = 10;

	/** ページごとに表示するアイテムの数 */
	private int perPage = DEFAULT_PER_PAGE;

	/** 現在のページの前後に表示するページ番号の数 */
	private int delta = DEFAULT_DELTA;

	private int current = 1;

	private int[] numbers;

	private List<E> elements;
	
	public Paginator(List<E> elements) {
		this(elements, DEFAULT_PER_PAGE);
	}

	public Paginator(List<E> elements, int perPage) {
		this(elements, perPage, DEFAULT_DELTA);
	}

	public Paginator(List<E> elements, int perPage, int delta) {
		this.elements = elements;
		this.perPage = perPage;
		this.delta = delta;

		int n = elements.size() / perPage;
		if ((elements.size() % perPage) != 0) {
			n++;
		}
		numbers = new int[n];
		for (int i = 0; i < n; i++) {
			numbers[i] = i + 1;
		}
	}

	private Paginator() {
		this(new ArrayList<E>(0));
	}

	/**
	 * 現在のページ番号を返します。
	 * @return 現在のページ番号
	 */
	public int getNumber() {
		return current;
	}

	/**
	 * 現在のページ番号をセットします。
	 * @param num 現在のページ番号
	 * @throws IllegalArgumentException 不正なページ番号が指定された場合
	 */
	public void setNumber(int num) throws IllegalArgumentException {
		if (num < 1 || num > numbers.length) {
			throw new IllegalArgumentException("The specified page number is illegal. [" + num + "]");
		}
		this.current = num;
	}

	/**
	 * 前のページ番号を返します。
	 * @return 前のページ番号。ない場合は -1 を返します。
	 */
	public int getPreviousNumber() {
		if (!hasPrevious()) {
			return -1;
		}
		return current - 1;
	}

	/**
	 * 次のページ番号を返します。
	 * @return 次のページ番号。ない場合は -1 を返します。
	 */
	public int getNextNumber() {
		if (!hasNext()) {
			return -1;
		}
		return current + 1;
	}

	/**
	 * 全てのページ番号のリストを返します。
	 * @return ページ番号のリスト
	 */
	public int[] getAllNumbers() {
		return Arrays.copyOf(numbers, numbers.length);
	}

	/**
	 * 現在のページ番号のリストを返します。
	 * @return 現在のページ番号のリスト
	 */
	public int[] getNumbers() {
		int start = current - delta;
		if (start < 1) {
			start = 1;
		}

		int end = current + delta;
		if (end > numbers.length) {
			end = numbers.length;
		}

		int num = (end - start) + 1;
		int[] pages = new int[num];
		for (int i = 0; i < num; i++) {
			pages[i] = start + i;
		}

		return pages;
	}

	/**
	 * 全ての要素のリストを返します。
	 * @return 全ての要素のリスト
	 */
	public List<E> getAllElements() {
		return elements;
	}

	/**
	 * 現在の要素のリストを返します。
	 * @return 現在の要素のリスト
	 */
	public List<E> getElements() {
		int start = (current - 1) * perPage;
		if (start > elements.size()) {
			start = elements.size();
		}

		int end = current * perPage;
		if (!hasNext()) {
			end = elements.size();
		}

		return elements.subList(start, end);
	}

	/**
	 * このページャに要素があるかどうかを返します。
	 * @return 要素がひとつでもある場合は true。
	 */
	public boolean hasElement() {
		return (elements != null && !elements.isEmpty());
	}

	/**
	 * 前のページがあるかどうかを返します。
	 * @return 前のページがある場合は true。
	 */
	public boolean hasPrevious() {
		return (current > 1);
	}

	/**
	 * 次のページがあるかどうかを返します。
	 * @return 次のページがある場合は true。
	 */
	public boolean hasNext() {
		return (current < numbers.length);
	}

	public int getTotal() {
		return elements.size();
	}

	public int getStart() {
		int start = ((current - 1) * perPage) + 1;
		return (getTotal() > 0) ? start : 0;
	}

	public int getEnd() {
		int end = current * perPage;
		if (!hasNext()) {
			end = elements.size();
		}
		return end;
	}

	public int pages() {
		return numbers.length;
	}

	public static <E> Paginator<E> getEmptyPaginator() {
		return new Paginator<E>();
	}
}