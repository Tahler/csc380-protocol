package edu.neumont.csc380.scalablesystem.ring;

import edu.neumont.csc380.scalablesystem.repo.LocalRepository;
import edu.neumont.csc380.scalablesystem.repo.RemoteRepository;
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
        RxHallaStor repositoryWithKey = this.getRepoWithKey(key);
        return repositoryWithKey.containsKey(key);
    }

    @Override
    public Completable put(String key, Object value) {
        RxHallaStor repositoryWithKey = this.getRepoWithKey(key);
        return repositoryWithKey.put(key, value)
                .onErrorResumeNext(err -> {
                    assert repositoryWithKey == this.localRepo;

                    Spawner.split(this.ringInfo, this.localRepo);
                    // Try again.
                    return this.localRepo.put(key, value);
                });
    }

    @Override
    public Single<Object> get(String key) {
        RxHallaStor repositoryWithKey = this.getRepoWithKey(key);
        return repositoryWithKey.get(key);
    }

    @Override
    public Completable update(String key, Object value) {
        RxHallaStor repositoryWithKey = this.getRepoWithKey(key);
        return repositoryWithKey.update(key, value);
    }

    @Override
    public Completable delete(String key) {
        RxHallaStor repositoryWithKey = this.getRepoWithKey(key);
        return repositoryWithKey.delete(key);
    }

    private RxHallaStor getRepoWithKey(String key) {
        RingNodeInfo nodeWithKey = this.ringInfo.getNodeWithKey(key);
        RxHallaStor repoWithKey = nodeWithKey.equals(this.localInfo)
                ? this.localRepo
                : new RemoteRepository(nodeWithKey);
        return repoWithKey;
    }
}