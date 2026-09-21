:page/title Fluent logs
:blog-post/tags [:tech ]
:blog-post/author {:person/id :einarwh}

<!-- :blog-post/published #time/ldt "2026-09-22T21:30:00" -->

:blog-post/description

I've made a silly little fluent interface for making assertions about logging in my unit tests.

:page/body

# Fluent logs

<p class="blog-post-date">September 22, 2026</p>

I've made a silly little fluent interface for making assertions about logging in my unit tests, specifically my xUnit tests. 

On reading that, certain thoughts might pop into your head, such as "but is it a good or a bad idea to validate logging in unit tests?" That's fine, I don't blame you. But such thoughts don't interest me, so I won't be addressing them. I will just concern myself with how the interface works. 

So how does it work?

## Basic checks

A simple thing we can check is whether or not the log is empty. We do it like this:

```csharp
Check.That(log).IsEmpty();
```

If the log is empty, all is well. Otherwise, the test will fail. For instance, we could write a self-defeating test like this:

```csharp
[Fact]
void IsEmptyFailsIfLogIsntEmpty() {
    var log = new InMemoryLogger<object>();
    log.LogInformation("Hello");
    log.LogWarning("Oh no!");
    Check.That(log).IsEmpty();
}
```

This predictably fails, with the message "Expected the log to be empty, but found 2 log entries.". That's because the log wasn't empty, it had two entries!

You'll notice that I'm using an `InMemoryLogger`. It is an implementation of the `ILogger` interface that keeps log entries in memory, just as the name indicates. The `That` method requires an instance of `InMemoryLogger`, because I need some way to figure out what was and wasn't logged.

To assert that something _has_ been written to the log, we can write:

```csharp
Check.That(log).IsNotEmpty();
```

We can make that fail as well, by providing an empty log.

```csharp
[Fact]
void IsNotEmptyFailsIfLogIsEmpty() {
    var log = new InMemoryLogger<object>();
    Check.That(log).IsNotEmpty();
}
```

This time the message will be "Expected the log to be non-empty, but there was nothing there."

We can also check the number of entries, if we want to:

```csharp
Check.That(log).HasLogEntries(2);
```

If there's a mismatch, we will get a message like "Expected 2 log entries, but found 3."

Assertions can be chained together, that's the whole thing with fluent interfaces. So we might have something like:

```csharp
Check.That(log).IsNotEmpty().HasLogEntries(2);
```

Chained assertions will short-circuit, in the sense that the test will stop at the first failing assertion. 

There's nothing stopping us from chaining together absurdities like this:

```csharp
Check.That(log).IsEmpty().IsNotEmpty();
```

That test is never going to pass regardless of what's in the log, but that's a pure practical matter.

## But what was logged?

Usually, we will want to be be more specific about what has been logged than a mere headcount. We do that with the `Logged` method.

```csharp
Check.That(log).Logged("Oh me!", "Oh my!", "Oh no!");
```

The `Logged` assertion does a line-by-line comparison between expectations and actually logged lines. The test will fail if any log message is wrong, missing, or extraneous. I try to make the error messages as explicit as possible, to make it easy to spot the problem.

As an example, assume that the assertion above passed. That means that prior to the assertion, exactly the lines "Oh me!", "Oh my!", and "Oh no!" were written to the log, in that sequence. Nothing more, and nothing less.

What would happen if we made some changes to the log messages?

For instance, say we keep the assertion in the test unchanged, but in the code, we change the second log message from "Oh my!" to "Oh heart!". Now the test fails:

```text
> Mismatch in log entry #1: Expected 'Oh my!', but was 'Oh heart!'.
```

Counting starts at 0, so log entry #1 refers to the second message. And indeed there is a mismatch in the second message, that's the message we changed.

What happens if we change multiple log messages? What if the message we actually log are "Oh dear!", "Oh heart!", and "Oh fie!"? The test fails, listing all the mismatching messages.

```text
> Mismatch in log entry #0: Expected 'Oh me!', but was 'Oh dear!'.
> Mismatch in log entry #1: Expected 'Oh my!', but was 'Oh heart!'.
> Mismatch in log entry #2: Expected 'Oh no!', but was 'Oh fie!'.
```

That covers mismatched messages, but what about messages that are missing altogether? Let's assume we're back to writing the original log messages, but then we decide to drop the second message, the "Oh my!". This leads to two complaints in the test:

```text
> Mismatch in log entry #1: Expected 'Oh my!', but was 'Oh no!'.
> Expected log entry #2: 'Oh no!', but found nothing.
```

We did nothing to the 'Oh me!', so the first comparison checks out fine. But when we get to log entry #1, there is a problem. The test expects the next message to be "Oh my!", but we don't log that any more. Instead, the second (and last) message actually logged is "Oh no!". But even though we're out of actual log messages, the test says that there should be a third log message saying "Oh no!".

Finally, we come to messages that weren't supposed to be there. So for instance, a fourth log message is added to the code, an "Oh shoot!" at the end. The test fails with:

```text
> Unexpected log entry #3: 'Oh shoot!'.
```

## Log levels

One thing that is completely missing from the example above is log levels. It may be that one message is a warning, another an informational message, a third an error, and this is something we might care about. To be explicit about log levels, we can use an overload that accepts `LogEntry` instances rather than strings, like so:

```csharp
Check.That(log)
    .Logged(
        LogEntry.Information("Hello"),
        LogEntry.Warning("Hmm..."),
        LogEntry.Error("Oh no!"));
```

A `LogEntry` is what the `InMemoryLogger` keeps in memory. It holds on to the log message and the log level, and can also have an associated exception and one or more logging scopes, which we'll return to shortly. 

For now, we have one more thing that a test might complain about. Say we log a single "Oh no!" at warning level, but our assertion species that it should have been logged at error level instead:

```csharp
Check.That(log).Logged(LogEntry.Error("Oh no!"));
```

The failure message is basically what I just wrote:

```text
> Expected log level of log entry #0 'Oh no!' to be 'Error' but was 'Warning'.
```

## Filtering

It may be that we don't care about _all_ the log messages, just some of them. Perhaps we're only interested in messages at certain log levels. If so, there are a couple of options for filtering. 

To check messages at a certain log level or above, we use the `Level` method. It allows us to specify the lower bound for the messages to include. 

For instance, the code may contain volatile trace logging that is irrelevant for the test. We don't want to have to change test all the time to accommodate that. So instead we'd write something like this:

```csharp
Check.That(log)
    .Level(LogLevel.Warning)
    .Logged(
        LogEntry.Warning("Oh me!"),
        LogEntry.Error("Oh no!"),
        LogEntry.Critical("Take cover!"));
```

The effect is similar to configuring the log level to warning. Only messages with log level warning, error, and critical will be included. 

If we want to keep _just_ the errors, we can use the `Just` method. 

```csharp
Check.That(log)
    .Just(LogLevel.Error)
    .Logged("Oh no!");
```

There is also a general filtering option through the `Where` method, which works as you expect it to. 

```csharp
Check.That(log)
    .Where(msg => msg.Contains("Oh"))
    .Logged("Oh me!", "Oh my!", "Oh no!");
```

There are two overloads, one which filters based on the full log entry, one on just the string message.

## Exceptions

Sometimes, we want to an log exception alongside the message. Or perhaps it's the other way around, we want to log the exception, and then we have to log a message as well. 

How should the interface verify that the right exception was logged? It can't be too strict about it, because then it would be impossibly tedious to write the tests. It's a bad idea to require users to provide the exact same exception instance, and I definitely don't want to force anyone to construct false stack traces. Instead, I'm happy if the exception type and message are the same. 

For instance, say we want to make sure that our code catches and logs a `DisasterException`, and that we decide that our conventional "Oh no!" is the appropriate message to accompany such an event. Here's how we would express that in a test:

```csharp
Check.That(log)
    .Logged(
        LogEntry.Error("Oh no!")
            .WithException(new DisasterException("Boom")));
```

If we're lucky, the test passes and everything is fine. If not, we could get either one of these fairly lengthy error messages:

```text
> Exception check failed for log entry #0: 'Oh no!'. Expected exception of type 'DisasterException' with message 'Boom' but found none.
> Exception check failed for log entry #0: 'Oh no!'. Expected exception of type 'DisasterException' with message 'Boom' but was exception of type 'NotImplementedException' with message 'Boom'.
> Exception check failed for log entry #0: 'Oh no!'. Expected exception of type 'DisasterException' with message 'Boom' but was exception of type 'DisasterException' with message 'BOOOOOM'.
```

This probably covers most cases, but sometimes we get into awkward situations. For instance, what if there is no public constructor for the exception we're expecting? In that case, we can do this instead:

```csharp
Check.That(log)
    .Logged(
        LogEntry.Error("Oh no!")
            .WithException<AnnoyingException>("Badaboom"));
```

Or maybe we would like to provide a custom check of some sort, perhaps to inspect some important property of some particular exception. We can do that too:

```csharp
Check.That(log)
    .Logged(
        LogEntry.Error("Oh no!")
            .WithException<FancyException>(it => it.Code == 17));
```

In this case the error messages will be a bit more generic, since the content of the check is opaque to the test. These are typical errors:

```text
> Exception check failed for log entry #0: 'Oh no!'. Expected exception to check but found none.
> Exception check failed for log entry #0: 'Oh no!'. Expected exception of type 'FancyException' with message 'Boom' didn't satisfy the provided check.
> Exception check failed for log entry #0: 'Oh no!'. Expected exception of type 'NotImplementedException' with message 'Boom' didn't satisfy the type requirement 'FancyException' of the provided check.
```

There are certainly other ways to do this, but this is how I've done it for now.

## Scopes

In .NET logging, scopes can be used to associate metadata with all log statements issued by a logger. For instance, in a processing pipeline, we might do something like this:

```csharp
using var scope = logger.BeginScope(
    new Dictionary<string, object> { 
        ["JobId"] = job.Id,
        ["CustomerId"] = job.CustomerId });

logger.LogInformation("Starting processing.");
...
logger.LogInformation("Finished processing.");
```

This makes sure that the IDs for the job and the customer accompany all the log statements until the scope falls out of scope so to speak. The scope metadata is handled by whatever logging provider we're using. If we're logging to Application Insights, the metadata will pop up in customDimensions, for instance. This is very neat and practical.

The `BeginScope` method is incredibly flexible, which is both a blessing and a curse. In the example above we used a `Dictionary`, but we can provide the same metadata in many, many ways. 

One alternative is a sequence of key-value pairs, like so:

```csharp
using var scope = logger.BeginScope(new [] {
    new KeyValuePair<string, string>("JobId", job.Id),
    new KeyValuePair<string, string>("CustomerId", job.CustomerId)
});
```

Or we could use an anonymous object: 

```csharp
using var scope = logger.BeginScope(
    new {
        JobId = job.Id,
        CustomerId = job.CustomerId
    }); 
```

These are all equivalent, in that they lead to the same metadata being logged. They're all ways of creating a scope out of a property bag of sorts. 

In fact, `BeginScope` will treat any composite object we pass it as a property bag. Which means we could do weird stuff like this: 

```csharp
using var scope = logger.BeginScope(Stopwatch.StartNew());
```

If we do that, the logging provider will scavenge the `Stopwatch` object for any and all properties it has (like `Elapsed`, `ElapsedMilliseconds`, `ElapsedTicks` and `IsRunning`) and use them as metadata. I mention this not because it's a good idea, but because it's possible and so must be handled somehow. 

The exact semantics of something like this can be hard to predict. Does `BeginScope` create a snapshot of the properties, or are the property values sampled anew from the scope object on every log statement? As far as I can tell, that's up to the concrete `ILogger` implementation to decide. 

There's a different use case for scopes as well, which may be thought of as tagging as opposed to passing a bag of properties. In that case, we simply pass in a string. 

```csharp
using var scope = logger.BeginScope("Processing");
```

What happens to this string? Again, it depends on the logging provider. In the case of Application Insights, it ends up as the value of the `Scope` property in customDimensions. The same goes for any primitive value we might pass in, such as an integer or a boolean.

That pretty much covers the various uses of `BeginScope` as defined on `ILogger`. However, there is also an extension method called `BeginScope`, which combines the tagging and property bag approaches in a way. We can pass it a message template and parameters, much like we would for a typical log statement:

```csharp
using var scope = logger.BeginScope(
    "Processing job {JobId} for customer {CustomerId}", 
    job.Id,
    job.CustomerId);
```

In that case, the template is formatted into a message that ends up as the `Scope` property, and the placeholder names in the template become properties as well. 

So that's roughly how scopes work in .NET logging. How can the fluent interface make sure that we got the right scopes for the right log entries? 

There are two main choices that must be made: 

1. Since scopes can be nested (with the innermost scope taking precedence), is it the effective scope that should be considered, or each layered scope separately?
2. How should scopes be compared? Since a scope can be any object, we have to consider annoying things like cyclic references. 

In making my choices, I leaned towards minimizing the work I had to do, while providing reasonably useful error messages. 

Calculating effective scope is more work than not doing it, so that was an easy choice in my mind. I treat each layered scope separately. That leaves just the matter of comparing individual scopes. 

So how _should_ scopes be compared, given my aim to minimize the work I have to do? 

The tagging scenario is easy. Strings and primitive values can just be compared directly. 

It's the property bags that can lead to headaches, in particular nested property bags. Shallow property bags are easy, we just compare keys and values. But nested property bags are a pain, because they can be arbitrarily deep in principle. We must tackle those without working too hard. 

Here's an example with a minimal nested property bag:

```csharp
using var scope = logger.BeginScope(
    new {
        JobId = 17,
        Details = new {
            CustomerId = "..."
        }
    }); 
```

If we pass a scope like that to the Application Insights logging provider, what happens? We get two properties added to customDimensions, `JobId` and `Details`. The value for the `JobId` is the integer 17. But what about `Details`? It becomes serialized as JSON:

```json
{ "CustomerId": "..." }
```

I decided to do something similar. Instead of descending into the dark depths of nested property bags, I treat all property bags as shallow. Nested property bags are converted into JSON which is used for comparison. 

What about the hybrid tagging/property-bag scenario? Well, that automatically becomes a shallow property bag, so it's not really much of a problem. 

So with all those things sorted, we can finally look at some examples. They might seem a bit anti-climatic after all that talk and hand-wringing. With scopes, there are things to think through to handle potentially weird usage gracefully. But normal usage is straightforward. 

```csharp
Check.That(log)
    .Logged(
        LogEntry.Information("Hello")
            .WithScopes(
                new Dictionary<string, object> {
                    { "JobId", 12 },
                    { "CustomerId", "..." }
                }));
```

Here we're expecting a single scope with two properties. In the code using the `log`, there should be a corresponding `BeginScope` call with an equivalent bag of properties. What happens if there isn't? There are some obvious things that can go wrong. 

We could have a scope with the correct properies, but one or more of the values is wrong. For instance, if the actual value for the "JobId" property were 11 instead of 12, we would get the following error: 

```text
> Expected property 'JobId' to have value '12' in scope #0 for log entry #0 'Hey there!" but found '11' instead.
```

(You can see that each scope is referred to by index, starting at 0, just like the log entries.)

What if the value of the property is of the wrong type, say, string instead of integer?

```text
> Expected property 'JobId' to have value '12' of type 'Int32' in scope #0 for log entry #0 'Hey there!" but found 'abc' of type 'String' instead.
```

If the value mismatch occurs inside a nested property bag, the error message will include JSON representations of both the expected and the actual property value. Say, for instance, that we assert that the "CustomerId" should be "boop", like so: 

```csharp
Check.That(log)
    .Logged(
        LogEntry.Information("Hello")
            .WithScopes(
                new {
                    JobId = 12,
                    Details = new {
                        CustomerId = "boop"
                    }
                }));
```

Now if it happens to be "beep" instead, we get this error:

```text
> Expected property 'Details' to have value '{"CustomerId":"boop"}' in scope #0 for log entry #0 'Hey there!" but found '{"CustomerId":"beep"}' instead.
```

What else could go wrong? One or more properties could be missing from the scope, or it could have additional properties.

For instance, if the actual scope lacks the "CustomerId" property, the test will say as much: 

```text
> Expected property 'CustomerId' with value '...' in scope #0 for log entry #0 'Hey there!" but found nothing. 
```

Similarly if the scope contains an additional "RequestedBy" property with some value: 

```text
> Unexpected property 'RequestedBy' with value 'some value' in scope #0 for log entry #0 'Hey there!". 
```

It could be that there is no scope at all, or that there is more than one, which leads to messages like the following:

```text
> Expected scope #0 of type Dictionary for log entry #0 'Hey there! but found nothing.
> Unexpected scope #1 of type Dictionary for log entry #0 'Hey there!.
```

Mixing up different kinds of scopes spells trouble too: 

```text
> Expected scope #0 of type Dictionary for log entry #0 'Hey there! but found scope of type String instead.
```

And of course we could put the right property on the wrong scope, but that should manifest itself as some combination of the errors we've already seen.

## Individual messages

Sometimes, we only care about individual log messages or entries. We can use the `Contains` method for that. There are four overloads; the example below uses all of them. 

```csharp
Check.That(log)
    .Contains("Hey there!")
    .Contains(LogEntry.Error("Ooops!")
        .WithException<DisasterException>("Boom"))
    .Contains(msg => msg.StartsWith("Oh"))
    .Contains(entry => 
        entry.Message.Contains("no") && 
        entry.Level > LogLevel.Information);
```

There is no implied order of log statements here. You could log the "Hey there!" last, no problem. Each `Contains` checks all the log messages. 

Here's a list of possible test failures, one per line above:

```text
> Found no log entry with message 'Hey there!'.
> Expected log level of log entry 'Ooops!!' to be 'Error', but was 'Warning'.
> Found no log entry with log message satisfying the check.
> Found no log entry satisfying the check.
```

The `LogEntry` overload can fail for various reasons (message, log level, exception, scope), whereas the rest are more or less fixed. The failure messages for the overloads that take boolean predicates are particularly generic and useless, since the check is opaque. 

There is also a method called `ContainsMatching` which matches using a regular expression. 

```csharp
Check.That(log)
    .ContainsMatching("Oh [a-z]+!");
```

If there are no log entries matching the pattern, the error message spells it out:

```text
> Found no log entry with message matching the pattern 'Oh [a-z]+!'.
```

## Line-by-line assertions

The interface supports one more way of asserting log statements, which combines elements of `Logged` and `Contains`. The idea is to start at the beginning of the log, and then make line-by-line assertions as appropriate for each entry. It works by starting a subflow with its own tiny interface. 

The subflow is started by calling `Begin` and ended by calling `Stop` or `Done`. In between, there are various methods (`Equals`, `Matches`, `StartsWith`, `Satisfies`) to make assertions for log entries in sequence. An example should make it clear how it works. 

Here's a subflow which makes six assertions against a sequence of log statements. 

```csharp
Check.That(log)
    .Begin()
        .Equals("It begins...")
        .Matches("Oh [a-z]+!")
        .StartsWith("Fiddle")
        .Equals("Oh no!")
        .StartsWith("Time")
        .Satisfies(msg => msg.Contains("z"))
    .Done();
```

To satisfy this test, we must have made exactly six log statements, each of which satisfies its own assertion. The following sequence of log messages will do: "It begins...", "Oh my!", "Fiddlesticks!", "Oh no!", "Timeout", "Bizarre".

The assertion flow halts at the first broken assertion. For instance, if we change the second log message to "Ohhh my!", we will get this error:

```text
> Log entry #1 'Ohhh my!" doesn't match pattern 'Oh [a-z]+!'.
```

The difference between `Stop` and `Done` is that `Stop` merely breaks out of the assertion flow, whereas `Done` requires us to be done. That is, there should be no more log entries to verify. If there were a seventh log message, the test would fail. 

Once the subflow has been ended, we are back at the regular interface, and can make any of the assertions we have discussed so far. 

## Final words

That concludes the tour of the interface. If you found it interesting and would like to play around with it, you can find the code [here](https://codeberg.org/einarwh/fluent-logs/raw/branch/main/FluentLogs/FluentLogs.cs). It's a single file that you can download or copy. I can't be bothered to create a Nuget package out of it. I offer absolutely no guarantees and no support. Things may change. There is no versioning. It may disappear without notice. But if you find it useful, that's cool. 