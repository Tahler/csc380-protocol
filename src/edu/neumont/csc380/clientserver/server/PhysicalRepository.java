package edu.neumont.csc380.clientserver.server;

import com.hallaLib.HallaStor;
import edu.neumont.csc380.clientserver.models.repo.*;

public class PhysicalRepository implements Repository<String, Object> {
    private final HallaStor hallaStor;

    public PhysicalRepository() {
        this.hallaStor = HallaStor.getInstance();
    }

    @Override
    public boolean containsKey(String key) {
        return this.hallaStor.containsKey(key);
    }

    @Override
    public void put(String key, Object value) {
        if (this.containsKey(key)) {
            throw new KeyAlreadyExistsException();
        } else {
            try {
                this.hallaStor.add(key, value);
            } catch (Exception e) {
                throw new RepositoryFullException();
            }
        }
    }

    @Override
    public Object get(String key) {
        Object value;
        if (this.containsKey(key)) {
            value = this.hallaStor.get(key);
        } else {
            throw new KeyDoesNotExistException();
        }
        return value;
    }

    @Override
    public void update(String key, Object value) {
        if (this.containsKey(key)) {
            this.hallaStor.update(key, value);
        } else {
            throw new KeyDoesNotExistException();
        }
    }

    @Override
    public void delete(String key) {
        if (this.containsKey(key)) {
            this.hallaStor.delete(key);
        } else {
            throw new KeyDoesNotExistException();
        }
    }
}
