package no.met.kvutil.dbutil;

public interface ISqlBase {
    String name();
    String create(); // Used to create a prepared statment if it does not exist.
}
