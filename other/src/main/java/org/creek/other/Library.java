package org.creek.other;

public class Library {
    public boolean someLibraryMethod() {
        return true;
    }

    public boolean someOtherLibraryMethod() {

        final String willNotFindMe = System.getenv("WillNotFindMe");
        final String willNotFindMeEither = System.getenv("WillNotFindMeEither");
        System.out.println("Just adding some more lines");
        if (willNotFindMe == null) {
            System.out.println("Well this is unexpected");
        }
        if (willNotFindMe != null) {
            return true;
        }
        if (willNotFindMeEither == null) {
            System.out.println("Well this is unexpected");
        }
        if (willNotFindMeEither == null) {
            return true;
        }
        return true;
    }
}
