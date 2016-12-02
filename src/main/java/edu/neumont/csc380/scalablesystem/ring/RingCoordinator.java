package edu.neumont.csc380.scalablesystem.ring;

import edu.neumont.csc380.scalablesystem.logging.Env;
import edu.neumont.csc380.scalablesystem.ring.repo.RxHallaStor;
import rx.Completable;
import rx.Single;

public class RingCoordinator implements RxHallaStor {
    private final VnodeRepository vnodeRepo;
    private final RingInfo ringInfo;

    public RingCoordinator(VnodeRepository vnodeRepo, RingInfo ringInfo) {
        this.vnodeRepo = vnodeRepo;
        this.ringInfo = ringInfo;
    }

    @Override
    public Single<Boolean> containsKey(String key) {
        RxHallaStor repositoryWithKey = this.getRepoWithKey(key);
        return repositoryWithKey.containsKey(key);
    }

    @Override
    public Completable put(String key, Object value) {
        Env.LOGGER.debug("RingCoordinator: putting " + key + ":" + value);

        RxHallaStor repositoryWithKey = this.getRepoWithKey(key);
        return repositoryWithKey.put(key, value)
                .onErrorResumeNext(err -> {
                    Env.LOGGER.debug("vnode is full. Splitting...");

                    assert repositoryWithKey == this.vnodeRepo;

                    Completable splitCompletable = Spawner.splitVnode(this.ringInfo, this.vnodeRepo);
                    // Try again once splitVnode.
                    return splitCompletable.andThen(this.vnodeRepo.put(key, value));
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
        RingNodeInfo startingNodeWithKey = this.ringInfo.getNodeWithKey(key);
        Env.LOGGER.debug("RingCoordinator: " + key + "(" + key.hashCode() + ") belongs to vnode starting at " + startingNodeWithKey);
        RxHallaStor repoWithKey = startingNodeWithKey.equals(this.vnodeRepo.getFirstSubNode())
                ? this.vnodeRepo
                : new RemoteRepository(startingNodeWithKey);
        return repoWithKey;
    }
}
