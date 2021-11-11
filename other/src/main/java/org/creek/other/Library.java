package org.creek.other;

public class Library {
    public boolean someLibraryMethod() {
        return true;
    }

    public boolean someOtherLibraryMethod() {
        if (System.currentTimeMillis() % 10000 == 0) {
            return true;
        }
        if (System.getenv("WillNotFindMe") == null) {
            return true;
        }
        return true;
    }
}
