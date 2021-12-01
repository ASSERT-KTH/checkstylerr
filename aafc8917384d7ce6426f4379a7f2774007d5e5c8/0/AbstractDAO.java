package vkaretko.dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vkaretko.interfaces.Action;
import vkaretko.interfaces.ActionGet;
import vkaretko.service.HibernateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract item dao interface.
 *
 * @author Karetko Victor.
 * @version 1.00.
 * @since 24.04.2017.
 */
public abstract class AbstractDAO<T> {
    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDAO.class);

    /**
     * Template method w/o return.
     * @param action action to do.
     */
    protected void persist(Action action) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getFactory().openSession()) {
            transaction = session.beginTransaction();
            action.execute(session);
            transaction.commit();
        } catch (HibernateException he) {
            LOG.error(he.getMessage(), he);
            if (transaction != null) {
                transaction.rollback();
            }
        }
    }

    /**
     * Template method with list return.
     * @param action action to do.
     */
    @SuppressWarnings("unchecked")
    protected List<T> persistGetAll(ActionGet action) {
        Transaction transaction = null;
        List<T> list = new ArrayList<>();
        try (Session session = HibernateUtil.getFactory().openSession()) {
            transaction = session.beginTransaction();
            list = action.executeGet(session);
            transaction.commit();
        } catch (HibernateException he) {
            LOG.error(he.getMessage(), he);
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return list;
    }

    /**
     * Delete entry from db.
     * @param t entry to delete.
     */
    public void delete(T t) {
        persist(session -> session.delete(t));
    };

    /**
     * Update entry in db.
     * @param t entry to update.
     */
    public void update(T t) {
        persist(session -> session.update(t));
    }

    /**
     * Update entry in db.
     * @param t entry to update.
     */
    public void save(T t) {
        persist(session -> session.save(t));
    };

    /**
     * Get entry from db by id.
     * @param id if of entry to get.
     */
    public abstract T get(int id);

    /**
     * Get list of entries from database.
     * @return list of entries.
     */
    public abstract List<T> getAll();
}
