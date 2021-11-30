package de.codecentric.worblehat.acceptancetests.step.page;

import de.codecentric.psd.worblehat.domain.Book;
import de.codecentric.psd.worblehat.domain.BookService;
import de.codecentric.worblehat.acceptancetests.adapter.SeleniumAdapter;
import de.codecentric.worblehat.acceptancetests.step.StoryContext;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@Component("DetailPage")
public class BookDetailsPage {

    private SeleniumAdapter seleniumAdapter;
    private StoryContext storyContext;
    private BookService bookService;

    @Autowired
    public BookDetailsPage(SeleniumAdapter seleniumAdapter, StoryContext storyContext, BookService bookService) {
        this.seleniumAdapter = seleniumAdapter;
        this.storyContext = storyContext;
        this.bookService = bookService;
    }

    @When("I navigate to the detail page of the book with the isbn $isbn")
    public void navigateToDetailPage(String isbn) {
        seleniumAdapter.clickOnPageElementByClassName("detailsLink-" + isbn);
        storyContext.put("LAST_BROWSED_BOOK_DETAILS", isbn);
    }

    @Then("I can see all book details for that book")
    public void allBookDetailsVisible() {
        String isbn = storyContext.get("LAST_BROWSED_BOOK_DETAILS");
        Set<Book> books = bookService.findBooksByIsbn(isbn);
        assertThat(books, hasSize(greaterThanOrEqualTo(1)));
        Book book = books.iterator().next();
        String description = book.getDescription();
        assertThat(seleniumAdapter.containsTextOnPage(book.getIsbn()), is(true));
        assertThat(seleniumAdapter.containsTextOnPage(book.getAuthor()), is(true));
        assertThat(seleniumAdapter.containsTextOnPage(book.getTitle()), is(true));
        assertThat(seleniumAdapter.containsTextOnPage(book.getEdition()), is(true));
        assertThat(seleniumAdapter.containsTextOnPage(String.valueOf(book.getYearOfPublication())), is(true));
        Optional.ofNullable(description).ifPresent(desc -> assertThat(seleniumAdapter.containsTextOnPage(description), is(true)));
    }
}
