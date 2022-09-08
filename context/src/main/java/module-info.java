
import org.creekservice.api.service.extension.CreekExtensionProvider;

module creek.service.context {
    requires transitive creek.base.type;
    requires transitive creek.service.api;
    requires creek.observability.logging;
    requires creek.platform.resource;
    requires com.github.spotbugs.annotations;

    exports org.creekservice.api.service.context;

    uses CreekExtensionProvider;
}
