
import org.creek.api.service.extension.CreekExtensionBuilder;

module creek.service.extension {
    requires transitive creek.platform.metadata;

    exports org.creek.api.service.extension;

    uses CreekExtensionBuilder;
}
