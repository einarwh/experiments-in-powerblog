:page/title Introducing μnit 
:blog-post/tags [:tech :programming :dotnet :csharp :reflection :testing]
:blog-post/author {:person/id :einarwh}
:blog-post/published #time/ldt "2012-06-15T08:14:00"
:page/body

# Introducing μnit

Posted: June 15, 2012

Last week, I teamed up with Bjørn Einar (control-engineer gone js-hipster) and Jonas (bona fide smalltalk hacker) to talk about [.NET gadgeteer](https://en.wikipedia.org/wiki/.NET_Gadgeteer) at the [NDC](http://www.ndcoslo.com/) 2012 conference in Oslo. .NET gadgeteer is a rapid prototyping platform for embedded devices running the [.NET micro framework](https://en.wikipedia.org/wiki/.NET_Micro_Framework) - a scaled down version of the .NET framework itself. You can read the abstract of our talk [here](https://einarwh.github.io/talks.html#.NET%20in%20the%20Physical%20and%20Meta-Physical%20World%20(NDC%20Oslo%202012)) if you like. The talk itself is available online as well. You can view it [here](https://vimeo.com/43808809).

The purpose of the talk was to push the envelope a bit, and try out things that embedded .NET micro devices aren't really suited for. We think it's important for developers to fool around a bit, without considering mundane things like business value. That allows for immersion and engagement in projects that are pure fun.

I started gently though, with a faux-test driven implementation of [Conway's Game of Life](http://en.wikipedia.org/wiki/Conway%27s_Game_of_Life). That is, I wrote the implementation of Life first, and then retro-fitted a couple of unit tests to make it seem like I'd followed the rules of the TDD police. That way I could conjure up the illusion of a true software craftsman, when in fact I'd just written a few tests after the implementation was done, regression tests if you will.

I feel like I had a reasonable excuse for cheating though: at the time, there were no unit testing frameworks available for the .NET micro framework. So you know how TDD opponents find it tedious to write the _test_ before the implementation? Well, in this case I would have to write the unit testing _framework_ before writing the test as well. So the barrier to entry was a wee bit higher.

Now in order to create the illusion of proper craftsmanship in retrospect, I did end up writing tests, and in order to do that, I did have to write my own testing framework. So procrastination didn't really help all that much. But there you go. Goes to show that the TDD police is on to something, I suppose.

Anyways, the testing framework I wrote is called [μnit](https://github.com/einarwh/mjunit), pronounced \[mju:nit\]. Which is a terribly clever name, I'm sure you'll agree. First off, the _μ_ looks very much like a _u_. So in terms of glyphs, it basically reads like _unit_. At the same time, the _μ_ is used as a prefix signifying "micro" in the metric system of measurement - which is perfect since it's written for the .NET *micro* framework. So yeah, it just reeks of clever, that name.

Implementation-wise it's pretty run-of-the-mill, though. You'll find that μnit works just about like any other xUnit framework out there. While the .NET micro framework is obviously scaled down compared to the full .NET framework, it is not a toy framework. Among the capabilities it shares with its bigger sibling is _reflection_, which is the key ingredient in all the xUnit frameworks I know of. Or at least I suppose it is, I haven't really looked at the source code of any of them. Guess I should. Bound to learn something.

Anyways, the way I _think_ these frameworks work is that you have some mechanics for identifying _test methods_ hanging off of _test classes_. For each test method, you create an instance of the test class, run the method, and evaluate the result. Since you don't want to state explicitly which test methods to run, you typically use reflection to identify and run all the test methods instead. At least that's how μnit works.

One feature that got axed in the .NET micro framework is _custom attributes_, and hence there can be no \[Test\] annotation for labelling test methods. So μnit uses naming conventions for identifying test methods instead, just like in jUnit 3 and earlier. But that's just cosmetics, it doesn't really change anything. In μnit we use the arbitrary yet common convention that test methods should start with the prefix "Test". In addition, they must be public, return void and have no parameters. Test classes must inherit from the **Fixture** base class, and must have a parameterless constructor. All catering for the tiny bit of reflection voodoo necessary to run the tests.

Here's the **Fixture** class that all test classes must inherit from:

```csharp
namespace Mjunit
{
    public abstract class Fixture
    {
        public virtual void Setup() {}

        public virtual void Teardown() {}
    }
}
```

As you can see, **Fixture** defines empty virtual methods for set-up and tear-down, named **SetUp** and **TearDown**, respectively. Test classes can override these to make something actually happen before and after a test method is run. Conventional stuff.

The task of identifying test methods to run is handler by the **TestFinder** class.

```csharp
namespace Mjunit
{
    public class TestFinder
    {
        public ArrayList FindTests(Assembly assembly)
        {
            var types = assembly.GetTypes();
            var fixtures = GetTestFixtures(types);
            var groups = GetTestGroups(fixtures);
            return groups;
        }

        private ArrayList GetTestFixtures(Type[] types)
        {
            var result = new ArrayList();
            for (int i = 0; i < types.Length; i++)
            {
                var t = types[i];
                if (t.IsSubclassOf(typeof(Fixture)))
                {
                    result.Add(t);
                }
            }
            return result;
        }

        private ArrayList GetTestGroups(ArrayList fixtures)
        {
            var result = new ArrayList();
            foreach (Type t in fixtures)
            {
                var g = new TestGroup(t);
                if (g.NumberOfTests > 0)
                {
                    result.Add(g);                    
                }
            }
            return result;
        }
    }
}
```

You might wonder why I'm using the feeble, untyped **ArrayList**, giving the code that unmistakeable old-school C# 1.1 tinge? The reason is simple: the .NET micro framework doesn't have generics. But we managed to get by in 2003, we'll manage now.

What the code does is pretty much what we outlined above: fetch all the types in the assembly, identify the ones that inherit from **Fixture**, and proceed to create a **TestGroup** for each test class we find. A **TestGroup** is just a thin veneer on top of the test class:

```csharp
namespace Mjunit
{
    class TestGroup : IEnumerable
    {
        private readonly Type _testClass;
        private readonly ArrayList _testMethods = new ArrayList();

        public TestGroup(Type testClass)
        {
            _testClass = testClass;
            var methods = _testClass.GetMethods();
            for (int i = 0; i < methods.Length; i++)
            {
                var m = methods[i];
                if (m.Name.Substring(0, 4) == "Test" && 
                    m.ReturnType == typeof(void))
                {
                    _testMethods.Add(m);
                }
            }
        }

        public Type TestClass
        {
            get { return _testClass; }
        }

        public int NumberOfTests
        {
            get { return _testMethods.Count; }
        }

        public IEnumerator GetEnumerator()
        {
            return _testMethods.GetEnumerator();
        }
    }
}
```

The **TestFinder** is used by the **TestRunner**, which does the bulk of the work in μnit, really. Here it is:

```csharp
namespace Mjunit
{
    public class TestRunner
    {
        private Thread _thread;
        private Assembly _assembly;
        private bool _done;

        public event TestRunEventHandler SingleTestComplete;
        public event TestRunEventHandler TestRunStart;
        public event TestRunEventHandler TestRunComplete;

        public TestRunner() {}

        public TestRunner(ITestClient client)
        {
            RegisterClient(client);
        }

        public TestRunner(ArrayList clients)
        {
            foreach (ITestClient c in clients)
            {
                RegisterClient(c);
            }
        }

        public bool Done
        {
            get { return _done; }
        }

        public void RegisterClient(ITestClient client)
        {
            TestRunStart += client.OnTestRunStart;
            SingleTestComplete += client.OnSingleTestComplete;
            TestRunComplete += client.OnTestRunComplete;
        }

        public void Run(Type type)
        {
            Run(Assembly.GetAssembly(type));
        }

        public void Run(Assembly assembly)
        {
            _assembly = assembly;
            _thread = new Thread(DoRun);
            _thread.Start();
        }

        public void Cancel()
        {
            _thread.Abort();
        }

        private void DoRun()
        {
            FireCompleteEvent(TestRunStart, null);
            var gr = new TestGroupResult(_assembly.FullName);
            try
            {
                var finder = new TestFinder();
                var groups = finder.FindTests(_assembly);
                foreach (TestGroup g in groups)
                {
                    gr.AddResult(Run(g));
                }
            }
            catch (Exception ex)
            {
                Debug.Print(ex.Message);
                Debug.Print(ex.StackTrace);
            }

            FireCompleteEvent(TestRunComplete, gr);
            _done = true;
        }

        private void FireCompleteEvent(TestRunEventHandler handler, 
            ITestResult result)
        {
            if (handler != null)
            {
                var args = new TestRunEventHandlerArgs 
                { Result = result };
                handler(this, args);
            }
        }

        private TestClassResult Run(TestGroup group)
        {
            var result = new TestClassResult(group.TestClass);
            foreach (MethodInfo m in group)
            {
                var r = RunTest(m);
                FireCompleteEvent(SingleTestComplete, r);
                result.AddResult(r);
            }
            return result;
        }

        private SingleTestResult RunTest(MethodInfo m)
        {
            try
            {
                DoRunTest(m);
                return TestPassed(m);
            }
            catch (AssertFailedException ex)
            {
                return TestFailed(m, ex);
            }
            catch (Exception ex)
            {
                return TestFailedWithException(m, ex);
            }
        }

        private void DoRunTest(MethodInfo method)
        {
            Fixture testObj = null;
            try
            {
                testObj = GetInstance(method.DeclaringType);
                testObj.Setup();
                method.Invoke(testObj, new object[0]);
            }
            finally
            {
                if (testObj != null)
                {
                    testObj.Teardown();
                }
            }
        }

        private Fixture GetInstance(Type testClass)
        {
            var ctor = testClass.GetConstructor(new Type[0]);
            return (Fixture)ctor.Invoke(new object[0]);
        }

        private SingleTestResult TestFailedWithException(
            MethodInfo m, Exception ex)
        {
            return new SingleTestResult(m, TestOutcome.Fail) 
            { Exception = ex };
        }

        private SingleTestResult TestFailed(
            MethodInfo m, AssertFailedException ex)
        {
            return new SingleTestResult(m, TestOutcome.Fail) 
            { AssertFailedException = ex };
        }

        private SingleTestResult TestPassed(MethodInfo m)
        {
            return new SingleTestResult(m, TestOutcome.Pass);
        }
    }
}
```

That's a fair amount of code, and quite a few new concepts that haven't been introduced yet. At a high level, it's not that complex though. It works as follows. The user of a test runner will typically be interested in notification during the test run. Hence **TestRunner** exposes three events that fire when the test run starts, when it completes, and when each test has been run respectively. To receive notifications, the user can either hook up to those events directly or register one or more so-called test clients. We'll look at some examples of test clients later on. To avoid blocking test clients and support cancellation of the test run, the tests run in their own thread.

As you can see from the **RunTest** method, each test results in a **SingleTestResult**, containing a **TestOutcome** of **Pass** or **Fail**. I don't know how terribly useful it is, but μnit currently distinguishes between failures due to failed assertions and failures due to other exceptions. It made sense at the time.

The **SingleTestResult** instances are aggregated into **TestClassResult** instances, which in turn are aggregated into a single **TestGroupResult** instance representing the entire test run. All of these classes implement **ITestResult**, which looks like this:

```csharp
namespace Mjunit
{
    public interface ITestResult
    {
        string Name { get; }

        TestOutcome Outcome { get; }

        int NumberOfTests { get; }

        int NumberOfTestsPassed { get; }

        int NumberOfTestsFailed { get; }
    }
}
```

Now for a **SingleTestResult**, the **NumberOfTests** will obviously be _1_, whereas for a **TestClassResult** it will match the number of **SingleTestResult** instances contained by the **TestClassResult**, and similarly for the **TestGroupResult**.

So that pretty much wraps it up for the core of μnit. Let's take a look at how it looks at the client side, for someone who might want to use μnit to write some tests. The most convenient thing to do is probably to register a test client; that is, some object that implements **ITestClient**. **ITestClient** looks like this:

```csharp
namespace Mjunit
{
    public interface ITestClient
    {
        void OnTestRunStart(object sender, 
            TestRunEventHandlerArgs args);

        void OnSingleTestComplete(object sender, 
            TestRunEventHandlerArgs args);

        void OnTestRunComplete(object sender, 
            TestRunEventHandlerArgs args);
    }
}
```

The registered test client will then receive callbacks as appropriate when the tests are running.

In order to be useful, test clients typically need to translate notifications into something that a human can see and act upon if necessary. In the .NET gadgeteer world, it means you need to interact with some hardware.

For the Game of Life implementation (which can be browsed [here](https://github.com/einarwh/life-mf) if you're interested) I implemented two test clients interacting with elements of the [FEZ Spider kit](https://www.sparkfun.com/products/retired/13306): a **DisplayTestClient** that shows test results on a small display, and a **LedTestClient** that simply uses a multicolored LED light to give feedback to the user. Here's the code for the latter:

```csharp
namespace Mjunit.Clients.GHI
{
    public class LedTestClient : ITestClient
    {
        private readonly MulticolorLed _led;

        private bool _isBlinking;
        private bool _hasFailed;

        public LedTestClient(MulticolorLed led)
        {
            _led = led;
            Init();
        }

        public void Init()
        {
            _led.TurnOff();            
            _isBlinking = false;
            _hasFailed = false;
        }

        public void OnTestRunStart(object sender, 
            TestRunEventHandlerArgs args)
        {
            Init();
        }

        public void OnTestRunComplete(object sender, 
            TestRunEventHandlerArgs args)
        {
            OnAnyTestComplete(sender, args);
        }

        private void OnAnyTestComplete(object sender, 
            TestRunEventHandlerArgs args)
        {
            if (!_hasFailed)
            {
                if (args.Result.Outcome == TestOutcome.Fail)
                {
                    _led.BlinkRepeatedly(Colors.Red);
                    _hasFailed = true;
                }
                else if (!_isBlinking)
                {
                    _led.BlinkRepeatedly(Colors.Green);
                    _isBlinking = true;
                }
            }
        }

        public void OnSingleTestComplete(object sender, 
            TestRunEventHandlerArgs args)
        {
            OnAnyTestComplete(sender, args);
        }
    }
}
```

As you can see, it starts the test run by turning the LED light off. Then, as individual test results come in, the LED light starts blinking. On the first passing test, it will start blinking green. It will continue to do so until a failing test result comes in, at which point it will switch to blinking red instead. Once it has started blinking red, it will stay red, regardless of subsequent results. So the **LedTestClient** doesn't actually tell you _which_ test failed, it just tells you if _some_ test failed. Useful for a sanity check, but not much else. That's where the **DisplayTestClient** comes in, since it actually shows the names of the tests as they pass or fail.

How does it look in practice? [Here's a video](https://vimeo.com/43139536) of μnit tests for Game of Life running on the FEZ Spider. When the tests all succeed, we proceed to run Game of Life. Whee!
