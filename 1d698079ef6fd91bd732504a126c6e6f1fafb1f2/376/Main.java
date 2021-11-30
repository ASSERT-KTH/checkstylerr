package com.hlj.moudle.design.D01_适应设计模式.D01_Iterator模式;


/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName Main
 * @Date 2019/7/31  15:11.
 * @Description
 *
 */
public class Main {


    public static void main(String[] args) {

        BookShelf bookShelf = new BookShelf();
        bookShelf.addBook(new Book("Around the World in 80 Days"));
        bookShelf.addBook(new Book("Bible"));
        bookShelf.addBook(new Book("Cinderella"));
        bookShelf.addBook(new Book("Daddy-Long-Legs"));

        Iterator it = bookShelf.iterator();

        while (it.hasNext()) {
            Book book = (Book)it.next();
            System.out.println(book.getName());
        }
    }
}
