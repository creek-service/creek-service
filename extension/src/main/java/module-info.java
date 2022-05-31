
import org.creekservice.api.service.extension.CreekExtensionProvider;

module creek.service.extension {
    requires transitive creek.platform.metadata;

    exports org.creekservice.api.service.extension;
    exports org.creekservice.api.service.extension.model;
    exports org.creekservice.api.service.extension.option;

    uses CreekExtensionProvider;
}
