package com.atguigu.bookstore.service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.atguigu.bookstore.dao.AccountDAO;
import com.atguigu.bookstore.dao.BookDAO;
import com.atguigu.bookstore.dao.TradeDAO;
import com.atguigu.bookstore.dao.TradeItemDAO;
import com.atguigu.bookstore.dao.UserDAO;
import com.atguigu.bookstore.dao.impl.AccountDAOIml;
import com.atguigu.bookstore.dao.impl.BookDAOImpl;
import com.atguigu.bookstore.dao.impl.TradeDAOImpl;
import com.atguigu.bookstore.dao.impl.TradeItemDAOImpl;
import com.atguigu.bookstore.dao.impl.UserDAOImpl;
import com.atguigu.bookstore.db.JDBCUtils;
import com.atguigu.bookstore.domain.Book;
import com.atguigu.bookstore.domain.ShoppingCart;
import com.atguigu.bookstore.domain.ShoppingCartItem;
import com.atguigu.bookstore.domain.Trade;
import com.atguigu.bookstore.domain.TradeItem;
import com.atguigu.bookstore.web.CriteriaBook;
import com.atguigu.bookstore.web.Page;

public class BookService {
	
	private BookDAO bookDAO = new BookDAOImpl();
	
	public Page<Book> getPage(CriteriaBook criteriaBook){
		return bookDAO.getPage(criteriaBook);
	}

	public Book getBook(int id) {
		return bookDAO.getBook(id);
	}

	public boolean addToCart(int id, ShoppingCart sc) {
		Book book = bookDAO.getBook(id);
		
		if(book != null){
			sc.addBook(book);
			return true;
		}
		
		return false;
	}

	public void removeItemFromShoppingCart(ShoppingCart sc, int id) {
		sc.removeItem(id);
	}

	public void clearShoppingCart(ShoppingCart sc) {
		sc.clear();
	}

	public void updateItemQuantity(ShoppingCart sc, int id, int quantity) {
		sc.updateItemQuantity(id, quantity);
	}
	
	private AccountDAO accountDAO = new AccountDAOIml();
	private TradeDAO tradeDAO = new TradeDAOImpl();
	private UserDAO userDAO = new UserDAOImpl();
	private TradeItemDAO tradeItemDAO = new TradeItemDAOImpl();

	//ҵ�񷽷�. 
	public void cash(ShoppingCart shoppingCart, String username,
			String accountId) {
		
		//1. ���� mybooks ���ݱ���ؼ�¼�� salesamount �� storenumber
		bookDAO.batchUpdateStoreNumberAndSalesAmount(shoppingCart.getItems());
		
		int i = 10 / 0;
		
		//2. ���� account ���ݱ�� balance
		accountDAO.updateBalance(Integer.parseInt(accountId), shoppingCart.getTotalMoney());
		
		//3. �� trade ���ݱ����һ����¼
		Trade trade = new Trade();
		trade.setTradeTime(new Date(new java.util.Date().getTime()));
		trade.setUserId(userDAO.getUser(username).getUserId());
		tradeDAO.insert(trade);
		
		//4. �� tradeitem ���ݱ���� n ����¼
		Collection<TradeItem> items = new ArrayList<>();
		for(ShoppingCartItem sci: shoppingCart.getItems()){
			TradeItem tradeItem = new TradeItem();
			
			tradeItem.setBookId(sci.getBook().getId());
			tradeItem.setQuantity(sci.getQuantity());
			tradeItem.setTradeId(trade.getTradeId());
			
			items.add(tradeItem);
		}
		tradeItemDAO.batchSave(items);
		
		//5. ��չ��ﳵ
		shoppingCart.clear();
	}
	
}
