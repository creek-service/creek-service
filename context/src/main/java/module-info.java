
import org.creek.api.service.extension.CreekExtensionBuilder;

module creek.service.context {
    requires transitive creek.base.type;
    requires transitive creek.service.extension;
    requires creek.observability.logging;

    exports org.creek.api.service.context;

    uses CreekExtensionBuilder;
}
