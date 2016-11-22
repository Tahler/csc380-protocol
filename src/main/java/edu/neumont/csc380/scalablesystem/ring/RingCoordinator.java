package edu.neumont.csc380.scalablesystem.ring;

import edu.neumont.csc380.scalablesystem.repo.LocalRepository;
import edu.neumont.csc380.scalablesystem.repo.RxHallaStor;
import rx.Completable;
import rx.Single;

public class RingCoordinator implements RxHallaStor {
    private final RingNodeInfo localInfo;
    private final LocalRepository localRepo;
    private final RingInfo ringInfo;

    public RingCoordinator(RingNodeInfo localInfo, LocalRepository localRepo, RingInfo ringInfo) {
        this.localInfo = localInfo;
        this.localRepo = localRepo;
        this.ringInfo = ringInfo;
    }

    @Override
    public Single<Boolean> containsKey(String key) {
        RxHallaStor repositoryWithKey = this.ringInfo.getRepositoryWithKey(key);
        return repositoryWithKey.containsKey(key);
    }

    @Override
    public Completable put(String key, Object value) {
        RxHallaStor repositoryWithKey = this.ringInfo.getRepositoryWithKey(key);
        return repositoryWithKey.put(key, value);
    }

    @Override
    public Single<Object> get(String key) {
        RxHallaStor repositoryWithKey = this.ringInfo.getRepositoryWithKey(key);
        return repositoryWithKey.get(key);
    }

    @Override
    public Completable update(String key, Object value) {
        RxHallaStor repositoryWithKey = this.ringInfo.getRepositoryWithKey(key);
        return repositoryWithKey.update(key, value);
    }

    @Override
    public Completable delete(String key) {
        RxHallaStor repositoryWithKey = this.ringInfo.getRepositoryWithKey(key);
        return repositoryWithKey.delete(key);
    }

    // TODO: split on full errors
}
