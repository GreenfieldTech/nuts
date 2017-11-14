package tech.greenfield.vertx.nuts.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Endpoints {
	Endpoint[] value();
}
