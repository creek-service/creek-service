
import org.creekservice.api.service.extension.CreekExtensionBuilder;

module creek.service.context {
    requires transitive creek.base.type;
    requires transitive creek.service.extension;
    requires creek.observability.logging;

    exports org.creekservice.api.service.context;

    uses CreekExtensionBuilder;
}
