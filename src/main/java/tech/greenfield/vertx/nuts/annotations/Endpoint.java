package tech.greenfield.vertx.nuts.annotations;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Repeatable(Endpoints.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Endpoint {

	String value();

}
