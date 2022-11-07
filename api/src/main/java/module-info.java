/** API implementation of Creek Service. */
module creek.service.api {
    requires transitive creek.service.extension;
    requires creek.base.annotation;
    requires creek.base.type;
    requires com.github.spotbugs.annotations;

    exports org.creekservice.internal.service.api to
            creek.service.context,
            creek.system.test.executor;
    exports org.creekservice.internal.service.api.component to
            creek.service.context;
    exports org.creekservice.internal.service.api.component.model to
            creek.service.context,
            creek.system.test.executor;
    exports org.creekservice.internal.service.api.extension to
            creek.service.context,
            creek.system.test.executor;
    exports org.creekservice.internal.service.api.options to
            creek.service.context,
            creek.system.test.executor;
}
