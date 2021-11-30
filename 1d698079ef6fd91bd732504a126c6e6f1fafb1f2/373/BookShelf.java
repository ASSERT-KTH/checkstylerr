package com.hlj.moudle.design.D01_适应设计模式.D01_Iterator模式;


import java.util.ArrayList;
import java.util.List;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName BookShelf
 * @Date 2019/7/31  13:35.
 * @Description  1、表示书架的类 存放书架的长度和书架的集合；
 *               2、实现集合接口 Aggregate 并实现方法iterator
 *
 * List<Book> books; 书架上放书
 * size 已经放了几本书 初始为 0，
 *
 * 问，为什么不用books的size来判断的放了多少本书，而是用last，
 * 我们这个设计模式叫Iterator 一个个读取，上面的list集合我们只用来存放数据，而不作为其他用户
 */
public class BookShelf implements Aggregate {

    private List<Book> books;

    /**已经放了几本书，从0开始 */
    private int size = 0;

    public BookShelf(){
        books = new ArrayList<>() ;
    }


    /**
     * 放书，每次放入
     * @param book
     */
    public void addBook(Book book) {
        this.books.add(book);
        size++;
    }

    /**
     * 根据位置取书
     */
    public Book getBookAt(int index) {
        return books.get(index);
    }


    /**
     * 获取目前书架长度
     * @return
     */
    public int getLength() {
        return size;
    }

    /**
     * 获取一个数据的读取工具 迭代器
     */
    @Override
    public Iterator iterator() {
        return new BookShelfIterator(this);
    }
}
