package edu.neumont.csc380.scalablesystem;

public class Config {
    public static final String HOST = "localhost";
    public static final int START_PORT = 3000;
    public static final int VNODE_SIZE = 5;
    // 3 = HallaStor.MIN_CONTENTS
    public static final int VNODE_CAPACITY = VNODE_SIZE * 3;
    public static final int  REPLICATION_FACTOR = 2;
}
