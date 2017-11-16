# Nuts Framework

Nuts is a framework that lets you handle NUTS messages in a hierarchical and convenient way.
It lets you subscribe to paths of messages, receive them and handle them.

## Installation

In your `pom.xml` file, add the repository for Nuts (we are currently not hosted
in the public Maven repository) as an element under `<project>`:

```
<repositories>
  <repository>
    <id>cloudonix-dist</id>
    <url>http://cloudonix-dist.s3-website-us-west-1.amazonaws.com/maven2/releases</url>
  </repository>
</repositories>
```

Then add Nuts as a dependency:

```
<dependency>
	<groupId>tech.greenfield</groupId>
	<artifactId>nuts</artifactId>
	<version>[0,)</version>
</dependency>
```


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
them with the `@Subscribe` annotation with the path set to the method or field you want your sub-controller
to be accessible under.

### A Sample Main and Sub Controllers

```
Class MyController extends Controller {
    @Subscribe("greeting")
    OtherController x;
}
```

```
Class OtherController extends Controller {
    @Subscribe("helloGreeting")
    void foo(Message msg){
        msg.reply("hello to you too");
    }
}
```

This will get the message from `greeting.helloGreeting` and reply to it

### NATS methods

Nuts has a few methods that support NATS messaging protocol. Here are examples for some of them:
```
public class HelloApi extends Controller{

	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	@Inject
	public HelloApi() {
	}

	@Subscribe("say")
	public void createUserAccount(NutsMessage msg){
		msg.reply("Hello to you too");
		msg.publish("some.subject", "publish content");
		msg.subscribeAsync("subject.to.listen.to").thenAccept(message -> {
			//when a message will be recieved on this subject, this code will be executed
			log.info("Message recieved on subject.to.listen.to with data: " + message.getDataString());
		});
	}
	
}
```

### Initializing

After creating your set of `Controller` implementations, deploy them by setting up
a `Verticle` in the standard way, and set Nuts to execute the controllers.
The `setupController` method returns a Nuts object that contains the client and 
lets you configure more controllers or reconfigure the Nats client.

#### Sample Vert.x NATS Server

```
public class App extends AbstractVerticle {

	@Override
	public void start(Future<Void> fut) throws Exception {
        Nuts nuts = new Nuts().setupController(new MainController());
        fut.complete()
    }
    
}
```

