
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * This is a Book entity. It is used by Hibernate for persistence. Many books can be written by one {@link Author}
 *
 */
@Entity
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    private String title;
    
    private double price;
    
    @ManyToOne
    private Author author;


    /**
     * 
     * @param title
     *          title of the book
     * @param price
     *          price of the book
     * @param author
     *          author of the book
     */
    public Book(String title, double price, Author author) {
      super();
      this.title = title;
      this.price = price;
      this.author = author;
    }

    protected Book() {
      super();
    }

    public long getId() {
      return id;
    }

    public void setId(long id) {
      this.id = id;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public double getPrice() {
      return price;
    }

    public void setPrice(double price) {
      this.price = price;
    }

    public Author getAuthor() {
      return author;
    }

    public void setAuthor(Author author) {
      this.author = author;
    }

    @Override
    public String toString() {
      return "Book [title=" + title + ", price=" + price + ", author=" + author + "]";
    }

}
