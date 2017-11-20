package com.example.orangeparadise.clientorderannotates;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by 97159 on 11/4/2017.
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface ClientAnnotation {

    int id() default 0;
    usage use();

    enum usage {TEST, DEMO, SOURCE} ;
}
