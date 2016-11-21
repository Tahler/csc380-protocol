package edu.neumont.csc380.scalablesystem.ring;

import edu.neumont.csc380.scalablesystem.repo.LocalRepository;
import edu.neumont.csc380.scalablesystem.repo.Repository;

public class RingRepository implements Repository<String, Object> {
    private final LocalRepository localRepo;
    private final RingInfo ringInfo;

    public RingRepository(LocalRepository localRepo, RingInfo ringInfo) {
        this.localRepo = localRepo;
        this.ringInfo = ringInfo;
    }

    @Override
    public boolean containsKey(String key) {
        if (this.localRepo.containsKey(key)) {
            return true;
        } else {
            RingNodeInfo remoteNodeInfo = this.ringInfo.getNodeContainingKey(key);
            // TODO: make a connection to the other
        }
        return false;
    }

    @Override
    public void put(String key, Object value) {
        if (this.localRepo.containsKey(key)) {

        }
    }

    @Override
    public Object get(String key) {
        return null;
    }

    @Override
    public void update(String key, Object value) {

    }

    @Override
    public void delete(String key) {

    }
}
