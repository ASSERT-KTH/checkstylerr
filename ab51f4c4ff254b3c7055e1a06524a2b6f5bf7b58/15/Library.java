package de.codecentric.worblehat.acceptancetests.step.business;

import de.codecentric.psd.worblehat.domain.Book;
import de.codecentric.psd.worblehat.domain.BookParameter;
import de.codecentric.psd.worblehat.domain.BookService;
import de.codecentric.worblehat.acceptancetests.step.StoryContext;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;

@Component("Library")
public class Library {

    private BookService bookService;

    private StoryContext storyContext;

    @Autowired
    public Library(ApplicationContext applicationContext, StoryContext storyContext) {
        this.bookService = applicationContext.getBean(BookService.class);
        this.storyContext = storyContext;
    }

    // *******************
    // *** G I V E N *****
    // *******************

    @Given("an empty library")
    public void emptyLibrary() {
        bookService.deleteAllBooks();
    }

    @Given("a library, containing only one book with isbn $isbn")
    public void createLibraryWithSingleBookWithGivenIsbn(String isbn) {
        emptyLibrary();
        Book book = DemoBookFactory.createDemoBook().withISBN(isbn).build();
        Optional<Book> createdBook = bookService.createBook(new BookParameter(book.getTitle(), book.getAuthor(), book.getEdition(), book.getIsbn(), book.getYearOfPublication(), book.getDescription()));
        createdBook.ifPresent(b -> storyContext.putObject("LAST_INSERTED_BOOK", b));
    }

    // just an example of how a step looks that is different from another one, after the last parameter
    // see configuration in AllAcceptanceTestStories
    @Given("a library, containing only one book with isbn $isbn and title $title")
    public void createLibraryWithSingleBookWithGivenIsbnAndTitle(String isbn, String title) {
        emptyLibrary();
        Book book = DemoBookFactory.createDemoBook()
            .withISBN(isbn)
            .withTitle(title)
            .build();
        Optional<Book> createdBook = bookService.createBook(new BookParameter(book.getTitle(), book.getAuthor(), book.getEdition(), book.getIsbn(), book.getYearOfPublication(), book.getDescription()));
        createdBook.ifPresent(b -> storyContext.putObject("LAST_INSERTED_BOOK", b));
    }

    @Given("borrower $borrower has borrowed books $isbns")
    public void borrower1HasBorrowerdBooks(String borrower,
                                           String isbns) {
        List<String> isbnList = getListOfItems(isbns);
        for (String isbn : isbnList) {
            Book book = DemoBookFactory.createDemoBook().withISBN(isbn).build();
            bookService.createBook(new BookParameter(book.getTitle(), book.getAuthor(), book.getEdition(), book.getIsbn(), book.getYearOfPublication(), book.getDescription()))
                .orElseThrow(IllegalStateException::new);

            bookService.borrowBook(book.getIsbn(), borrower);
        }
    }

    private List<String> getListOfItems(String isbns) {
        return isbns.isEmpty() ? Collections.emptyList() : Arrays.asList(isbns.split(" "));
    }
    // *****************
    // *** W H E N *****
    // *****************

    // *****************
    // *** T H E N *****
    // *****************


    @Then("the library contains only the book with $isbn")
    public void shouldContainOnlyOneBook(String isbn) {
        waitForServerResponse();
        List<Book> books = bookService.findAllBooks();
        assertThat(books.size(), is(1));
        assertThat(books.get(0).getIsbn(), is(isbn));
    }

    @Then("the library contains $copies of the book with $isbn")
    public void shouldContainCopiesOfBook(Integer copies, String isbn) {
        waitForServerResponse();
        assertNumberOfCopies(isbn, copies);
    }

    @Then("the new book $can be added")
    public void shouldNotHaveCreatedANewCopy(String can) {
        Book lastInsertedBook = (Book) storyContext.getObject("LAST_INSERTED_BOOK");
        int numberOfCopies = "CAN".equals(can) ? 2 : 1;
        assertNumberOfCopies(lastInsertedBook.getIsbn(), numberOfCopies);
    }

    private void assertNumberOfCopies(String isbn, int copies) {
        Set<Book> books = bookService.findBooksByIsbn(isbn);
        assertThat(books.size(), is(copies));
        assertThat(books, everyItem(hasProperty("isbn", is(isbn))));
    }

    private void waitForServerResponse() {
        // normally you would have much better mechanisms for waiting for a
        // server response. We are choosing a simple solution for the sake of this
        // training
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // pass
        }
    }


}
