:page/title Dry data
:blog-post/tags [:tech :programming :csharp :database :dotnet :orm :patterns :sql]
:blog-post/author {:person/id :einarwh}
:blog-post/published #time/ldt "2011-06-20T19:17:00"
:page/body

# Dry data

Posted: June 20, 2011 

I’m a big proponent of the NoORM movement. Haven’t heard of it? That’s because it doesn’t exist. But it sort of does, under a different name. So-called “micro-ORMs” like Simple.Data, dapper and massive all belong to this category. That’s three really smart guys (unwittingly) supporting the same cause as I. Not bad. I’d say that’s the genesis of a movement right there.

Unsurprisingly, NoORM means “not only ORM”. The implication is that there are scenarios where full-blown object-relational mapper frameworks like nHibernate and Entity Framework are overkill. Such frameworks really go beyond merely addressing the infamous object/relational impedence mismatch (which is arguably irreconcilable), to support an almost seamless experience of persistent objects stored in a relational database. To do so, they pull out the heavy guns, like the Unit of Work pattern from Martin Fowler’s Patterns of Enterprise Application Architecture (One of those seminal tomes with an “on bookshelf”-to-“actually read it” ratio that’s just a little too high.)

And that’s great! I always say, let someone else “maintain a list of objects affected by a business transaction and coordinate the writing out of changes and the resolution of concurrency problems”. Preferably someone smarter than me. It’s hard to get right, and it in the right circumstances, being able to leverage a mature framework to do that heavy lifting for you is a huge boon.

Make sure you have some heavy lifting to do, though. All the power, all the functionality, all the goodness supported by state-of-the-art ORMs, comes at a cost. There’s a cost in configuration, in conceptual overhead, in overall complexity of your app. Potentially there’s a cost in performance as well. What if you don’t care about flexible ways of configuring the mapping of a complex hierarchy of rich domain objects onto a highly normalized table structure? What if you don’t need to support concurrent, persistent manipulation of the state of those objects? What if all you need is to grab some data and go to town? In that case, you might be better served with something simpler, like raw ADO.NET calls or some thin, unambitious veneer on top of that.

Now there’s one big problem with using raw ADO.NET calls: repetition (lack of DRYness). You basically have to go through the same song-and-dance every time, with just minor variations. With that comes boredom and bugs. So how do we avoid that? How do fight repetition and duplication? By abstraction, of course. We single out the stuff that varies and abstract away the rest. Needless to say, the less that varies, the simpler and more powerful the abstraction. If you can commit to some serious constraints with respect to varity, your abstraction becomes that much more succinct and that much more powerful. Of course, in order to go abstract, we first need to go concrete. So let’s do that.

Here’s a scenario: there’s a database. A big, honkin’ legacy database. It’s got a gazillion stored procedures, reams and reams of business logic written in T-SQL by some tech debt nomad who has since moved on to greener pastures. A dozen business critical applications rely on it. It’s not something you’ll want to touch with a ten-foot pole. The good thing is you don’t have to. For the scope of the project you’re doing, all you need to do is grab some data and go. Invoke a handful of those archaic stored procedures to get the data you need, and you’re done. Home free. Have a cup of tea.

Now what sort of constraints can we embrace and exploit in this scenario?

1. Everything will be stored procedures.
2. It’s SQL Server, and that’s not going to change.

As it turns out, the second point is not really significant, since we’ll need database-agnostic code if we’re going to write tests. The first one is interesting though. We’ll also assume that the stored procedures will accept input parameters only. That’s going to simplify our code a great deal.

Let’s start by introducing a naive client doing straight invocation of a few stored procedures in plain ol’ ADO.NET:

```csharp
public class Client1
{
    private readonly string _connStr;

    private readonly DbProviderFactory _dpf;

    public Client1(string connStr) : this(connStr, 
      DbProviderFactories.GetFactory("System.Data.SqlClient"))
    {}

    public Client1(string connStr, DbProviderFactory dpf)
    {
        _connStr = connStr;
        _dpf = dpf;
    }

    private DbParameter CreateParameter(string name, object val) 
    {
        var p = _dpf.CreateParameter();
        p.ParameterName = name;
        p.Value = val;
        return p;
    }

    public IEnumerable<User> GetCompanyUsers(string company)
    {
        var result = new List<User>();
        using (var conn = _dpf.CreateConnection())
        using (var cmd = _dpf.CreateCommand())
        {
            conn.ConnectionString = _connStr;
            conn.Open();
            cmd.Connection = conn;
            cmd.CommandType = CommandType.StoredProcedure;
            cmd.CommandText = "spGetCompanyUsers";
            var p = CreateParameter("@companyName", company);
            cmd.Parameters.Add(p);
            var reader = cmd.ExecuteReader();
            while (reader.Read())
            {
                var u = new User
                {
                    Id = (string) reader["id"],
                    UserName = (string) reader["user"],
                    Name = (string) reader["name"],
                    Email = (string) reader["emailAddress"],
                    Phone = (string) reader["cellPhone"],
                    ZipCode = (string) reader["zip"]
                };
                result.Add(u);
            }
        }
        return result;
    }

    public string GetUserEmail(string userId)
    {
        using (var conn = _dpf.CreateConnection())
        using (var cmd = _dpf.CreateCommand())
        {
            conn.ConnectionString = _connStr;
            conn.Open();
            cmd.Connection = conn;
            cmd.CommandType = CommandType.StoredProcedure;
            cmd.CommandText = "spGetEmailForUser";
            var p = CreateParameter("@userId", userId);
            cmd.Parameters.Add(p);
            return (string) cmd.ExecuteScalar();
        }
    }

    public void StoreUser(User u)
    {
        using (var conn = _dpf.CreateConnection())
        using (var cmd = _dpf.CreateCommand())
        {
            conn.ConnectionString = _connStr;
            conn.Open();
            cmd.Connection = conn;
            cmd.CommandType = CommandType.StoredProcedure;
            cmd.CommandText = "spInsertOrUpdateUser";
            var ps = new [] {
                CreateParameter("@userId", u.Id),
                CreateParameter("@user", u.UserName),
                CreateParameter("@name", u.Name),
                CreateParameter("@emailAddress", u.Email),
                CreateParameter("@cellPhone", u.Phone),
                CreateParameter("@zip", u.ZipCode)
            };
            cmd.Parameters.AddRange(ps);
            cmd.CommandType = CommandType.StoredProcedure;
            cmd.ExecuteNonQuery();
        }
    }
}
```

So you can see, there’s a great deal of duplication going on there. And obviously, as you add new queries and commands, the amount of duplication increases linearly. It’s the embryo of a maintenance nightmare right there. But we’ll fight back with that trusty ol’ weapon of ours: abstraction! To arrive at a suitable one, let’s play a game of compare and contrast.

What varies?

* The list of input parameters.
* In the case of queries: the data row we’re mapping from and the .NET type we’re mapping to.
* The names of stored procedures.
* The execute method (ExecuteReader, ExecuteScalar, ExecuteNonQuery). We’re gonna ignore DataSets since I don’t like them. (I’ll be using my own anemic POCOs, thank you very much!).

What stays the same?

* The connection string.
* The need to create and open a connection.
* The need to create and configure a command object.
* The need to execute the command against the database.
* The need to map the result of the command to some suitable representation (unless we’re doing ExecuteNonQuery).

There are a couple of design patterns that spring to mind, like Strategy or Template method, that might help us clean things up. We’ll be leaving GoF on the shelf next to PoEAA, though, and use lambdas and generic methods instead.

I take “don’t repeat yourself” quite literally. So we’re aiming for a single method where we’ll be doing all our communication with the database. We’re going to channel all our queries and commands through that same method, passing in just the stuff that varies.

To work towards that goal, let’s refactor into some generic methods:

```csharp
public class Client2
{
    private readonly string _connStr;

    private readonly DbProviderFactory _dpf;

    public Client2(string connStr) : this(connStr, 
      DbProviderFactories.GetFactory("System.Data.SqlClient"))
    {}

    public Client2(string connStr, DbProviderFactory dpf)
    {
        _connStr = connStr;
        _dpf = dpf;
    }

    private DbParameter CreateParameter(string name, object val)
    {
        var p = _dpf.CreateParameter();
        p.ParameterName = name;
        p.Value = val;
        return p;
    }

    public IEnumerable<User> GetCompanyUsers(string company)
    {
        return ExecuteReader("spGetCompanyUsers",
            new[] {CreateParameter("@companyName", company)},
            r => new User
            {
                Id = (string) r["id"],
                UserName = (string) r["user"],
                Name = (string) r["name"],
                Email = (string) r["emailAddress"],
                Phone = (string) r["cellPhone"],
                ZipCode = (string) r["zip"]
            });
    }

    public IEnumerable<T> ExecuteReader<T>(string spName, 
      DbParameter[] sqlParams, Func<IDataRecord, T> map)
    {
        var result = new List<T>(); 
        using (var conn = _dpf.CreateConnection())
        using (var cmd = _dpf.CreateCommand())
        {
            conn.ConnectionString = _connStr;
            conn.Open();
            cmd.Connection = conn;
            cmd.CommandType = CommandType.StoredProcedure;
            cmd.CommandText = spName;
            cmd.Parameters.AddRange(sqlParams);
            var reader = cmd.ExecuteReader();
            while (reader.Read())
            {
                result.Add(map(reader));
            }
        }
        return result;
    }

    public string GetUserEmail(string userId)
    {
        return ExecuteScalar("spGetEmailForUser", 
            new[] {CreateParameter("@userId", userId)}, 
            o => o as string);
    }

    public T ExecuteScalar<T>(string spName, 
      DbParameter[] sqlParams, Func<object, T> map)
    {
        using (var conn = _dpf.CreateConnection())
        using (var cmd = _dpf.CreateCommand())
        {
            conn.ConnectionString = _connStr;
            conn.Open();
            cmd.Connection = conn;
            cmd.CommandType = CommandType.StoredProcedure;
            cmd.CommandText = spName;
            cmd.Parameters.AddRange(sqlParams);
            return map(cmd.ExecuteScalar());
        }
    }

    public void StoreUser(User u)
    {
        ExecuteNonQuery("spInsertOrUpdateUser",
            new[]
                {
                    CreateParameter("@userId", u.Id),
                    CreateParameter("@user", u.UserName),
                    CreateParameter("@name", u.Name),
                    CreateParameter("@emailAddress", u.Email),
                    CreateParameter("@cellPhone", u.Phone),
                    CreateParameter("@zip", u.ZipCode)
                });
    }

    public void ExecuteNonQuery(string spName, 
      DbParameter[] sqlParams)
    {
        using (var conn = _dpf.CreateConnection())
        using (var cmd = _dpf.CreateCommand())
        {
            conn.ConnectionString = _connStr;
            conn.Open();
            cmd.Connection = conn;
            cmd.CommandType = CommandType.StoredProcedure;
            cmd.CommandText = spName;
            cmd.Parameters.AddRange(sqlParams);
            cmd.CommandType = CommandType.StoredProcedure;
            cmd.ExecuteNonQuery();
        }
    }
}
```

So we’ve bloated the code a little bit – in fact, we just doubled the number of methods. But we’re in a much better position to write new queries and commands. We’re done with connections and usings and what have you. Later on, we can just reuse the same generic methods.

However, we still have some glaring duplication hurting our eyes: the three execute methods are practically identical. So while the code is much DRYer than the original, there’s still some moisture in there. And moisture leads to smell and rot.

To wring those few remaining drops out of the code, we need to abstract over the execute methods. The solution? To go even more generic!

```csharp
public TResult Execute<T, TResult>(string spName, 
  DbParameter[] sqlParams, Func<IDbCommand, T> execute, 
  Func<T, TResult> map)
{
    using (var conn = _dpf.CreateConnection())
    using (var cmd = _dpf.CreateCommand())
    {
        conn.ConnectionString = _connStr;
        conn.Open();
        cmd.Connection = conn;
        cmd.CommandType = CommandType.StoredProcedure;
        cmd.CommandText = spName;
        cmd.Parameters.AddRange(sqlParams);
        cmd.CommandType = CommandType.StoredProcedure;
        return map(execute(cmd));
    }
}
```

So basically the solution is to pass in a function that specifies the execute method to run. The other execute methods can use this to get their stuff done. Now that we have our single, magical do-all database interaction method, let’s make make things a bit more reusable. We’ll cut the database code out of the client, and introduce a tiny abstraction. Let’s call it Database, since that’s what it is. In fact, for good measure, let’s throw in a new method that might be useful in the process: ExecuteRow. Here’s the code:

```csharp
public class Database
{
    private readonly string _connStr;

    private readonly DbProviderFactory _dpf;

    public Database(string connStr): this(connStr, 
      DbProviderFactories.GetFactory("System.Data.SqlClient"))
    {}

    public Database(string connStr, DbProviderFactory dpf)
    {
        _connStr = connStr;
        _dpf = dpf;
    }

    public IEnumerable<T> ExecuteReader<T>(string spName, 
      DbParameter[] sqlParams, Func<IDataRecord, T> map)
    {
        return Execute(spName, sqlParams, cmd => cmd.ExecuteReader(),
            r =>
            {
                var result = new List<T>();
                while (r.Read())
                {
                    result.Add(map(r));
                }
                return result;
            });
    }

    public T ExecuteRow<T>(string spName, 
      DbParameter[] sqlParams, Func<IDataRecord, T> map)
    {
        return ExecuteReader(spName, sqlParams, map).First();
    }

    public T ExecuteScalar<T>(string spName, 
      DbParameter[] sqlParams, Func<object, T> map)
    {
        return Execute(spName, sqlParams, 
            cmd => cmd.ExecuteScalar(), map);
    }

    public void ExecuteNonQuery(string spName, 
      DbParameter[] sqlParams)
    {
        Execute(spName, sqlParams, 
            cmd => cmd.ExecuteNonQuery(), 
            o => o);
    }

    public TResult Execute<T, TResult>(string spName, 
      DbParameter[] sqlParams, Func<IDbCommand, T> execute, 
      Func<T, TResult> map)
    {
        using (var conn = _dpf.CreateConnection())
        using (var cmd = _dpf.CreateCommand())
        {
            conn.ConnectionString = _connStr;
            conn.Open();
            cmd.Connection = conn;
            cmd.CommandType = CommandType.StoredProcedure;
            cmd.CommandText = spName;
            cmd.Parameters.AddRange(sqlParams);
            cmd.CommandType = CommandType.StoredProcedure;
            return map(execute(cmd));
        }
    }
}
```

ExecuteScalar is pretty straightforward, but there are a few interesting details concerning the others. First, ExecuteReader derives a map from IDataReader to IEnumerable from the user-supplied map from IDataRecord to T. Second, ExecuteNonQuery doesn’t really care about the result from calling DbCommand.ExecuteNonQuery against the database (which indicates the number of rows affected by the command/non-query). So we’re providing the simplest possible map – the identity map – to the Execute method.

So the execution code is pretty DRY now. Basically, you’re just passing in the stuff that varies. And there’s a single method actually creating connections and commands and executing them against the database. Good stuff.

Let’s attack redundancy in the client code. Here’s what it looks like at the moment:

```csharp
public class Client4
{
    private readonly Database _db;

    public Client4(Database db)
    {
        _db = db;
    }

    public IEnumerable<User> GetCompanyUsers(string company)
    {
        return _db.ExecuteReader("spGetCompanyUsers",
            new[] { new SqlParameter("@companyName", company) },
            r => new User
                     {
                         Id = (string)r["id"],
                         UserName = (string)r["user"],
                         Name = (string)r["name"],
                         Email = (string)r["emailAddress"],
                         Phone = (string)r["cellPhone"],
                         ZipCode = (string)r["zip"]
                     });
    }

    public string GetUserEmail(string userId)
    {
        return _db.ExecuteScalar("spGetEmailForUser",
            new[] { new SqlParameter("@userId", userId) },
            o => o as string);
    }

    public void StoreUser(User u)
    {
        _db.ExecuteNonQuery("spInsertOrUpdateUser",
            new[]
                {
                    new SqlParameter("@userId", u.Id),
                    new SqlParameter("@user", u.UserName),
                    new SqlParameter("@name", u.Name),
                    new SqlParameter("@emailAddress", u.Email),
                    new SqlParameter("@cellPhone", u.Phone),
                    new SqlParameter("@zip", u.ZipCode)
                });
    }
}
```

Actually, it’s not too bad, but I’m not happy about the repeated chanting of new SqlParameter. We’ll introduce a simple abstraction to DRY up that too, and give us a syntax that’s a bit more succinct and declarative-looking.

```csharp
public class StoredProcedure
{
    private readonly DbProviderFactory _dpf;
    private readonly DbCommand _sp;

    public StoredProcedure(DbCommand sp, DbProviderFactory dpf)
    {
        _sp = sp;
        _dpf = dpf;
    }

    public StoredProcedure this[string parameterName, 
      object value, int? size = null, DbType? type = null]
    {
        get { return AddParameter(parameterName, value, size, type); }
    }

    public StoredProcedure AddParameter(string parameterName, 
      object value, int? size = null, DbType? type = null)
    {
        var p = _dpf.CreateParameter();
        if (p != null)
        {
            p.ParameterName = parameterName;
            p.Value = value;
            if (size.HasValue)
            {
                p.Size = size.Value;
            }
            if (type.HasValue)
            {
                p.DbType = type.Value;
            }
            _sp.Parameters.Add(p);
        }
        return this;
    }
}
```

This is basically a sponge for parameters. It uses a little trick with a get-indexer with side-effects to do its thing. This allows for a simple fluent syntax to add parameters to a DbCommand object. Let’s refactor the generic Execute method to use it.

```csharp
public TResult Execute<T, TResult>(string spName, 
    Func<StoredProcedure, StoredProcedure> configure, 
    Func<IDbCommand, T> execute, 
    Func<T, TResult> map)
{
    using (var conn = _dpf.CreateConnection())
    using (var cmd = _dpf.CreateCommand())
    {
        conn.ConnectionString = _connStr;
        conn.Open();
        cmd.Connection = conn;
        cmd.CommandType = CommandType.StoredProcedure;
        cmd.CommandText = spName;
        configure(new StoredProcedure(cmd, _dpf));
        cmd.CommandType = CommandType.StoredProcedure;
        return map(execute(cmd));
    }
}
```

The refactoring ripples through to the other execute methods as well, meaning you pass in a Func instead of the parameter array. Now the interesting part is how the new abstraction affects the client code. Here’s how:

```csharp
public class Client5
{
    private readonly Database _db;

    public Client5(Database db)
    {
        _db = db;
    }

    public IEnumerable<User> GetCompanyUsers(string company)
    {
        return _db.ExecuteReader(
            "spGetCompanyUsers",
            sp => sp["@companyName", company],
            r => new User
                     {
                         Id = (string) r["id"],
                         UserName = (string) r["user"],
                         Name = (string) r["name"],
                         Email = (string) r["emailAddress"],
                         Phone = (string) r["cellPhone"],
                         ZipCode = (string) r["zip"]
                     });
    }

    public string GetUserEmail(string userId)
    {
        return _db.ExecuteScalar(
            "spGetEmailForUser",
            sp => sp["@userId", userId],
            o => o as string);
    }

    public void StoreUser(User u)
    {
        _db.ExecuteNonQuery(
            "spInsertOrUpdateUser",
            sp => sp["@userId", u.Id]
                    ["@user", u.UserName]
                    ["@name", u.Name]
                    ["@emailAddress", u.Email]
                    ["@cellPhone", u.Phone]
                    ["@zip", u.ZipCode]);
    }
}
```

Which is pretty much as DRY as it gets, at least in my book. We just grab the data and go. Wheee! Where’s my tea?
