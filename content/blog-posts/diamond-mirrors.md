:page/title Diamond mirrors
:blog-post/tags [:tech :programming :kata :fsharp]
:blog-post/author {:person/id :einarwh}
:page/body

# Diamond mirrors

Posted: February 10, 2015

My friend Bjørn Einar did a nice write-up about the [Diamond code kata](http://claysnow.co.uk/recycling-tests-in-tdd/) in F# the other day. He did so in the context of TDD-style evolutionary design vs up-front thinking away from the keyboard. Apparently he has this crazy idea that it might be worthwhile to do a bit of conceptual problem-solving and thinking about properties of the domain before you start typing. Very out of vogue, I know.

Anyways, he ended up with an interesting implementation centered on exploiting something called the [taxicab norm](https://en.wikipedia.org/wiki/Taxicab_geometry). (I hadn’t heard of it either, which makes it all the more interesting.) I really like that approach: cast your problem as an instance of an existing, well-understood problem for which there exists a well-understood solution. It replaces ad-hoc code with a mathematical idea, and is rather a far step away from typical implementations that get heavy on string manipulations and where the solution to the problem in general is swamped with things related to outputting the diamond to the console.

I wondered if I could come up with an alternative approach, and hence I got to thinking a bit myself. Away from the keyboard, like a madman. The solution I came up is perhaps a bit more conventional, a bit less mathematical (I’m sorry to say), but still centered on a single idea. That idea is _mirroring_.

To illustrate the approach, consider a sample diamond built from five letters, A through E. It should look like the following:

```
....A....
...B.B...
..C...C..
.D.....D.
E.......E
.D.....D.
..C...C..
...B.B...
....A....
```

The mirroring is fairly obvious. One way to look at the diamond is to consider it as a pyramid mirrored along the E-row. But at the same time, it is also a pyramid mirrored along the A-column. So it goes both ways. This means that we could rather easily build our pyramid from just a quarter of the diamond, by mirroring it twice. We would start with just this:

```
A....
.B...
..C..
...D.
....E
```

We could then proceed by mirroring along the A-column to produce this:

```
....A....
...B.B...
..C...C..
.D.....D.
E.......E
```

And then we could complete the diamond by mirroring along the E-row, and it would look like the diamond we wanted.

So far so good. But we need the first quarter. How could we go about producing that?

Assume we start with a list ['A' .. 'E']. We would like to use that to produce this list:

```fsharp
[ 
  ['A'; '.'; '.'; '.'; '.']; 
  ['.'; 'B'; '.'; '.'; '.']; 
  ['.'; '.'; 'C'; '.'; '.']; 
  ['.'; '.'; '.'; 'D'; '.']; 
  ['.'; '.'; '.'; '.'; 'E']; 
]
```

But that’s rather easy. Each inner list is just the original list ['A' .. 'E'] with all letters except one replaced by ‘.’. That’s a job for **map**. Say I want to keep only the ‘B’:

```fsharp
List.map (fun x -> if x = 'B' then x else '.') ['A' .. 'E'] 
```

And so on and so forth for each letter in the original list. We can use a list comprehension to generate all of them for us. For convenience, we’ll create a function **genLists**:

```fsharp
let genLists lst =
  [ for e in lst do List.map (fun x -> if x = e then x else '.') lst ]
```

This gives us the first quarter. Now for the mirroring. That’s easy too:

```fsharp
let mirror lst = 
  match lst with 
    | [] -> []
    | h::t -> List.rev t @ lst
```

(We’ll never actually call **mirror** with an empty list, but I think it’s better form to include it anyway.)

So now we can **map** the **mirror** function over the quarter diamond to produce a half diamond:

```fsharp
[ 
  ['.'; '.'; '.'; '.'; 'A'; '.'; '.'; '.'; '.']; 
  ['.'; '.'; '.'; 'B'; '.'; 'B'; '.'; '.'; '.']; 
  ['.'; '.'; 'C'; '.'; '.'; '.'; 'C'; '.'; '.']; 
  ['.'; 'D'; '.'; '.'; '.'; '.'; '.'; 'D'; '.']; 
  ['E'; '.'; '.'; '.'; '.'; '.'; '.'; '.'; 'E']; 
]
```

Excellent. Now we’re almost ready to do the second mirroring. The only problem is that the **mirror** function uses the head element as the pivot for mirroring, so we would end up with an X instead of a diamond!

That’s trivial to fix though. We’ll just reverse the list first, and then do the mirroring. I’m not even going to write up the result for that – it is obviously the completed diamond. Instead, here’s the complete **diamond** function, built from the parts we’ve seen so far:

```fsharp
let diamond letters =
  letters |> genLists 
          |> List.map (fun a -> mirror a) 
          |> List.rev 
          |> mirror 
```

Could I speed up things by reversing my lists before the first mapping instead of after? No, because the (outer) list has the same number of elements before and after the first mirroring. Plus it’s easier to explain this way. And really, perf optimization for a code kata? Come on!

Now for rendering:

```fsharp
let toStr d =
  d |> List.map (fun a -> new string(Array.ofList(a))) 
    |> String.concat "\n"
```

And to run everything (for a full-sized diamond, because why not):

```fsharp
['A' .. 'Z'] |> diamond |> toStr |> printfn "%s"
```

And that’s all there is to it. The entire code looks like this:

```fsharp
open System

let genLists lst =
  [ for e in lst do yield List.map (fun x -> if x = e then x else '.') lst ]

let mirror lst =
  match lst with
   | [] -> []
   | h::t -> List.rev t @ lst

let diamond letters =
  letters |> genLists 
          |> List.map (fun a -> mirror a) 
          |> List.rev 
          |> mirror 

let toStr d =
  d |> List.map (fun a -> new string(Array.ofList(a))) 
    |> String.concat "\n"

['A' .. 'Z'] |> diamond |> toStr |> printfn "%s"
```
