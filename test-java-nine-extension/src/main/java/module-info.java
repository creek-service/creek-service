
import org.creek.api.service.extension.CreekExtensionBuilder;
import org.creek.test.java.nine.service.extension.JavaNineExtensionBuilder;

module creek.service.test.java.nine.extension {
    requires creek.service.extension;

    provides CreekExtensionBuilder with
            JavaNineExtensionBuilder;
}
