package edu.neumont.csc380.clientserver.server;

import com.hallaLib.HallaStor;
import edu.neumont.csc380.clientserver.models.repo.*;

import java.util.HashSet;
import java.util.Set;

public class PhysicalRepository implements Repository<String, Object> {
    private final HallaStor hallaStor;
    private final boolean shouldLock;
    // null if shouldLock = false
    private final Set<String> lockedKeys;

    public PhysicalRepository() {
        this(true);
    }

    public PhysicalRepository(boolean shouldLock) {
        this.hallaStor = HallaStor.getInstance();
        this.shouldLock = shouldLock;
        this.lockedKeys = this.shouldLock
                ? new HashSet<>()
                : null;
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
            if (this.shouldLock && this.isLocked(key)) {
                throw new KeyIsLockedException();
            } else {
                value = this.hallaStor.get(key);
            }
        } else {
            throw new KeyDoesNotExistException();
        }
        return value;
    }

    @Override
    public Object lock(String key) {
        Object value;
        if (this.containsKey(key)) {
            if (this.shouldLock && this.isLocked(key)) {
                throw new KeyIsLockedException();
            } else {
                this.lockKey(key);
                value = this.hallaStor.get(key);
            }
        } else {
            throw new KeyDoesNotExistException();
        }
        return value;
    }

    @Override
    public void update(String key, Object value) {
        if (this.containsKey(key)) {
            if (!this.shouldLock || this.isLocked(key)) {
                this.hallaStor.update(key, value);
                this.unlockKey(key);
            } else {
                throw new KeyNotLockedException();
            }
        } else {
            throw new KeyDoesNotExistException();
        }
    }

    @Override
    public void delete(String key) {
        if (this.containsKey(key)) {
            if (!this.shouldLock || this.isLocked(key)) {
                this.hallaStor.delete(key);
            } else {
                throw new KeyNotLockedException();
            }
        } else {
            throw new KeyDoesNotExistException();
        }
    }

    private boolean isLocked(String key) {
        return this.lockedKeys.contains(key);
    }

    private void lockKey(String key) {
        this.lockedKeys.add(key);
    }

    private void unlockKey(String key) {
        this.lockedKeys.remove(key);
    }
}
