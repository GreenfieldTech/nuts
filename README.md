# Nuts Framework

Nuts is a framework that lets you handle NUTS messages in a hierarchical and convenient way.
It lets you subscribe to paths of messages, receive them and handle them.

## Usage

Under Nuts we use the concept of a "Controller" - a class whose fields and methods are used as
handlers for incoming messages. 

A "master controller" is created to define the root of the message path hierarchy - all configured paths
on that controller will be parsed relative to the root of the host.

### Simple Routing

To configure a path, create your controller class by extending the
nuts `Controller` class, define fields or methods to handle HTTP requests and annotate them with
"Subscribe" annotation and paths that those handlers should handle messages for.

#### A Sample Controller

```
package com.example.api;

import tech.greenfield.vertx.Nuts.*

Class MyController(){

    @Subscribe("sayHello")
    void hello(Message msg){
        msg.reply("hello world");
    }
}

```

### Sub Controllers

Complex routing topologies can be implemented by "mounting" sub-controllers under
the main controller - by setting fields to additional `Controller` implementations and annotating
them with the `@Subscriber` annotation with the path set to the method or field you want your sub-controller
to be accessible under.

### A Sample Main and Sub Controllers

```
package com.example.api;

import tech.greenfield.vertx.Nuts.*

Class MyController extends Controller {
    @Subscribe("greeting")
    OtherController x;
}
```

```
package com.example.api;

import tech.greenfield.vertx.Nuts.*

Class OtherController extends Controller {
    @Subscribe("helloGreeting")
    void foo(Message msg){
        msg.reply("hello to you too");
    }
}
```

This will get the message from "greeting.helloGreeting" and reply to it

### Initializing

After creating your set of `Controller` implementations, deploy them by setting up
a `Verticle` in the standard way, and set Nuts to execute the controllers.
The `executePath` method returns a Nuts object that contains the client and lets you get or reconfigure it.

#### Sample Vert.x HTTP Server

```
Nuts nuts = new Nuts().executePath(ctx.getController());
```

