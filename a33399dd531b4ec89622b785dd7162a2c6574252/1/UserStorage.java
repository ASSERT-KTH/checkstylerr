package vkaretko;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vkaretko.interfaces.Storage;
import vkaretko.models.User;

import java.util.List;

/**
 * Class UserStorage.
 * Description TODO.
 * Created by vitoss.
 *
 * @author Karetko Victor.
 * @version 1.00.
 * @since 06.05.17 12:53.
 */
@Component
public class UserStorage implements Storage {
    private final Storage storage;

    @Autowired
    public UserStorage(final Storage storage) {
        this.storage = storage;
    }

    public void add(User user) {
        this.storage.add(user);
    }

    public List<User> getAll() {
        return this.storage.getAll();
    }

    public void update(User user) {
        this.storage.update(user);
    }

    public User get(int id) {
        return this.storage.get(id);
    }

    @Override
    public void remove(User user) {
        this.storage.remove(user);
    }
}
