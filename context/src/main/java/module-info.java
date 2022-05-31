
import org.creekservice.api.service.extension.CreekExtensionProvider;

module creek.service.context {
    requires transitive creek.base.type;
    requires transitive creek.service.extension;
    requires creek.observability.logging;
    requires com.github.spotbugs.annotations;

    exports org.creekservice.api.service.context;

    uses CreekExtensionProvider;
}
