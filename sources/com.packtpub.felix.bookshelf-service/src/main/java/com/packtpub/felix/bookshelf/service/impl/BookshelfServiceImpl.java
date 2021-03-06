package com.packtpub.felix.bookshelf.service.impl;

import com.packtpub.felix.bookshelf.inventory.api.*;
import com.packtpub.felix.bookshelf.log.api.BookShelfLogHelper;
import com.packtpub.felix.bookshelf.service.api.BookInventoryNotRegisteredRuntimeException;
import com.packtpub.felix.bookshelf.service.api.BookshelfService;
import com.packtpub.felix.bookshelf.service.api.InvalidCredentialsException;
import com.packtpub.felix.bookshelf.service.api.SessionNotValidRuntimeException;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.ServiceReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

@Component(name = "BookshelfServiceImpl")
@Provides
@Instantiate(name ="BookshelfServiceImplInstance")
public class BookshelfServiceImpl implements BookshelfService {

    private String sessionId;

    @Requires
    private BookInventory inventory;

    @Requires
    private BookShelfLogHelper logger;

    private BookShelfLogHelper getLogger(){
        return logger;
    }

    public String login(String username, char[] password) throws InvalidCredentialsException {
        if ("admin".equals(username) &&
                Arrays.equals(password, "admin".toCharArray())) {
            this.sessionId = Long.toString(System.currentTimeMillis());

            getLogger().debug("username {0} logged in", username);
            return this.sessionId;
        }
        throw new InvalidCredentialsException(username);
    }

    public void logout(String sessionId) throws SessionNotValidRuntimeException {
        checkSession(sessionId);
        this.sessionId = null;
    }


    public boolean sessionIsValid(String sessionId) {
        return this.sessionId != null
                && this.sessionId.equals(sessionId);
    }

    protected void checkSession(String sessionId) throws SessionNotValidRuntimeException {
        if (!sessionIsValid(sessionId)) {
            throw new SessionNotValidRuntimeException(sessionId);
        }
    }

    public Set<String> getGroups(String session) throws SessionNotValidRuntimeException, BookInventoryNotRegisteredRuntimeException {
        checkSession(session);
        BookInventory inventory = lookupBookInventory();
        return inventory.getCategories();
    }

    public void addBook(String session, String isbn, String title, String author, String category, int rating) throws BookAlreadyExistsException, InvalidBookException, SessionNotValidRuntimeException, BookInventoryNotRegisteredRuntimeException {
        getLogger().debug("add book ISBN {0}", isbn);
        checkSession(session);
        BookInventory inventory = lookupBookInventory();

        MutableBook book = inventory.createBook(isbn);
        book.setTitle(title);
        book.setAuthor(author);
        book.setCategory(category);
        book.setRating(rating);
        inventory.storeBook(book);
    }


    public void modifyBookCategory(String session, String isbn, String category) throws BookNotFoundException, InvalidBookException, SessionNotValidRuntimeException, BookInventoryNotRegisteredRuntimeException {
        getLogger().debug("modify book category ISBN {0}", isbn);
        checkSession(session);
        BookInventory inventory = lookupBookInventory();
        MutableBook book = inventory.loadBookForEdit(isbn);
        book.setCategory(category);
    }

    public void modifyBookRating(String session, String isbn, int rating) throws BookNotFoundException, InvalidBookException, SessionNotValidRuntimeException, BookInventoryNotRegisteredRuntimeException {
        getLogger().debug("remove book rating ISBN {0}", isbn);
        checkSession(session);
        BookInventory inventory = lookupBookInventory();
        MutableBook book = inventory.loadBookForEdit(isbn);
        book.setRating(rating);
    }

    public void removeBook(String session, String isbn) throws BookNotFoundException, SessionNotValidRuntimeException, BookInventoryNotRegisteredRuntimeException {
        getLogger().debug("remove book ISBN {0}", isbn);
        checkSession(session);
        BookInventory inventory = lookupBookInventory();
        inventory.removeBook(isbn);
    }

    public Book getBook(String session, String isbn) throws BookNotFoundException, SessionNotValidRuntimeException, BookInventoryNotRegisteredRuntimeException {
        getLogger().debug("get book ISBN {0}", isbn);
        checkSession(session);
        BookInventory inventory = lookupBookInventory();
        return inventory.loadBook(isbn);
    }

    public MutableBook getBookForEdit(String session, String isbn) throws BookNotFoundException, SessionNotValidRuntimeException, BookInventoryNotRegisteredRuntimeException {
        getLogger().debug("get book for edit ISBN {0}", isbn);
        checkSession(session);
        BookInventory inventory = lookupBookInventory();
        return inventory.loadBookForEdit(isbn);
    }

    public Set<String> searchBooksByCategory(String session, String categoryLike) throws SessionNotValidRuntimeException, BookInventoryNotRegisteredRuntimeException {
        getLogger().debug("get book catetory {0}", categoryLike);
        checkSession(session);
        BookInventory inventory = lookupBookInventory();
        HashMap<BookInventory.SearchCriteria, String> criteria = new HashMap<BookInventory.SearchCriteria, String>();
        criteria.put(BookInventory.SearchCriteria.CATEGORY_LIKE, categoryLike);
        return inventory.searchBooks(criteria);
    }

    public Set<String> searchBooksByAuthor(String session, String authorLike) throws SessionNotValidRuntimeException, BookInventoryNotRegisteredRuntimeException {
        getLogger().debug("get book author {0}", authorLike);
        checkSession(session);
        BookInventory inventory = lookupBookInventory();
        HashMap<BookInventory.SearchCriteria, String> criteria = new HashMap<BookInventory.SearchCriteria, String>();
        criteria.put(BookInventory.SearchCriteria.AUTHOR_LIKE, authorLike);
        return inventory.searchBooks(criteria);
    }

    public Set<String> searchBooksByTitle(String session, String titleLike) throws BookInventoryNotRegisteredRuntimeException, SessionNotValidRuntimeException {
        getLogger().debug("get book title {0}", titleLike);
        checkSession(session);
        BookInventory inventory = lookupBookInventory();
        HashMap<BookInventory.SearchCriteria, String> criteria = new HashMap<BookInventory.SearchCriteria, String>();
        criteria.put(BookInventory.SearchCriteria.TITLE_LIKE, titleLike);
        return inventory.searchBooks(criteria);
    }

    //5 - 1
    public Set<String> searchBooksByRating(String session, int ratingLower, int ratingUpper) throws SessionNotValidRuntimeException, BookInventoryNotRegisteredRuntimeException {
        checkSession(session);
        BookInventory inventory = lookupBookInventory();
        HashMap<BookInventory.SearchCriteria, String> criteria = new HashMap<BookInventory.SearchCriteria, String>();
        criteria.put(BookInventory.SearchCriteria.RATE_LT, Integer.toString(ratingLower));
        criteria.put(BookInventory.SearchCriteria.RATE_GT, Integer.toString(ratingUpper));
        return inventory.searchBooks(criteria);
    }

    private BookInventory lookupBookInventory() throws BookInventoryNotRegisteredRuntimeException {
        return this.inventory;
    }

    @PostRegistration
    public void registered(ServiceReference ref) {
        getLogger().debug("{0} registered", this.getClass().getSimpleName());
    }

    @PostUnregistration
    public void unregistered(ServiceReference ref) {
        getLogger().debug("{0} unregistered", this.getClass().getSimpleName());
    }

}
