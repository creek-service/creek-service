
import org.creekservice.api.service.extension.CreekExtensionBuilder;

module creek.service.extension {
    requires transitive creek.platform.metadata;

    exports org.creekservice.api.service.extension;

    uses CreekExtensionBuilder;
}
