package org.creek.other;

public class Library {
    public boolean someLibraryMethod() {
        return true;
    }

    public boolean someOtherLibraryMethod() {
        if (System.currentTimeMillis() % 10000 == 0) {
            return true;
        }
        if (System.currentTimeMillis() % 50 == 0) {
            return true;
        }
        return true;
    }
}
