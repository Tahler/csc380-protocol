package edu.neumont.csc380.scalablesystem.ring;

import com.hallaLib.HallaStor;
import edu.neumont.csc380.scalablesystem.logging.Env;
import edu.neumont.csc380.scalablesystem.ring.repo.KeyDoesNotExistException;
import edu.neumont.csc380.scalablesystem.ring.repo.RepositoryFullException;
import edu.neumont.csc380.scalablesystem.ring.repo.RxHallaStor;
import rx.Completable;
import rx.Single;

// TODO: it is now the vnode repos job to check if key exists first

/**
 * Local repository of a sub node.
 */
public class LocalRepository implements RxHallaStor {
    private final HallaStor hallaStor;
    // Adds/removes to keyset in vnode if necessary
    private final VnodeRepository vnodeRepo;

    public LocalRepository(VnodeRepository vnodeRepo) {
        this.hallaStor = HallaStor.getInstance();
        this.vnodeRepo = vnodeRepo;
    }

    public Single<Boolean> containsKey(String key) {
        return Single.just(this.hallaStor.containsKey(key));
    }

    public boolean containsKeySync(String key) {
        return this.hallaStor.containsKey(key);
    }

    @Override
    public Completable put(String key, Object value) {
        this.vnodeRepo.keySet.add(key);
        Env.LOGGER.info("Putting " + key + " : " + value + "...");
        return value == null
                ? Completable.complete()
                : Completable.create(subscriber -> {
            try {
                this.hallaStor.add(key, value);
                Env.LOGGER.debug("LOCAL_REPOSITORY - Put " + key + " : " + value + ".");
                subscriber.onCompleted();
            } catch (IllegalStateException e) {
                subscriber.onError(new RepositoryFullException());
            }
        });
    }

    @Override
    public Single<Object> get(String key) {
        return Single.create(subscriber -> {
            Object value = this.hallaStor.get(key);
            Env.LOGGER.debug("LOCAL_REPOSITORY - Retrieved object " + value);
            if (value == null) {
                subscriber.onError(new KeyDoesNotExistException());
            } else {
                subscriber.onSuccess(value);
            }
        });
    }

    @Override
    public Completable update(String key, Object value) {
        return Completable.create(subscriber -> {
            if (this.hallaStor.containsKey(key)) {
                this.hallaStor.update(key, value);
                Env.LOGGER.debug("LOCAL_REPOSITORY - Updated " + key + " to " + value);
            }
        });
    }

    @Override
    public Completable delete(String key) {
        return Completable.create(subscriber -> {
            Env.LOGGER.debug("LOCAL_REPOSITORY - Deleting " + key + "...");
            Env.LOGGER.debug("LOCAL_REPOSITORY - vnode contains? " + this.vnodeRepo.keySet.contains(key));
            this.vnodeRepo.keySet.remove(key);
            Env.LOGGER.debug("LOCAL_REPOSITORY - removed key from vnode key");
            if (this.hallaStor.containsKey(key)) {
                this.hallaStor.delete(key);
                Env.LOGGER.debug("LOCAL_REPOSITORY - Physically deleted " + key);
            } else {
                Env.LOGGER.debug("LOCAL_REPOSITORY - skipping... does not physically contain key");
            }
            subscriber.onCompleted();
        });
    }
}
