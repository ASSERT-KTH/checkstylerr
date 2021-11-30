package com.hlj.moudle.design.D01_适应设计模式.D01_Iterator模式;


/**
 * 遍历书架的迭代器 类
 * 迭代器需要的参数
 * 1、要迭代的对象 bookShelf书架
 * 2、迭代到哪里了index
 */
public class BookShelfIterator implements Iterator {

    /** 要迭代的对象 bookShelf书架  */
    private BookShelf bookShelf;

    /** 迭代到哪里了index */
    private int index;

    /**
     * 初始化迭代器，从 0 开始准备迭代
     * @param bookShelf
     */
    public BookShelfIterator(BookShelf bookShelf) {
        this.bookShelf = bookShelf;
        this.index = 0;
    }

    /**
     * 判断书架是否还有书
     * @return
     */
    @Override
    public boolean hasNext() {
        if (index < bookShelf.getLength()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 依次遍历 书架上的书
     * @return
     */
    @Override
    public Object next() {
        Book book = bookShelf.getBookAt(index);
        index++;
        return book;
    }
}
