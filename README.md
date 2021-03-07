<h1>Relational Databases Project - Social Media App</h1>
<h2>Short overview</h2>
JavaFX application allowing registering and logging into own user profile
as well as adding other users as friends, joining groups, editing own profile
and many others.

The data is being stored in a PostgreSQL database using <a href="https://www.elephantsql.com">ElephantSQL Service</a>.
The PostgreSQL JDBC Driver is being used for database connection.
<h3>Icons made by <a href="https://www.freepik.com" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/">www.flaticon.com</a>.</h3>

<h3>Main application functionalities include:</h3>
<ul>
    <li>registering as a new user with a name, last name, city, password and short profile description</li>
    <li>logging into the application with name, last name and password corresponding to an existing user</li>
    <li>searching for a specific user in the database</li>
    <li>adding users as friends</li>
    <li>editing / updating all user information</li>
    <li>joining groups</li>
    <li>following other users.</li>
</ul>

Application supports both English and Polish alphabet.

![main project view](https://github.com/atrria/Social-Media-App/blob/master/images/prtscn1.png "Main application view")
![profile view](https://github.com/atrria/Social-Media-App/blob/master/images/prtscn2.png "Profile view")
![followed users view](https://github.com/atrria/Social-Media-App/blob/master/images/prtscn3.png "Followed users view")

Users data have been randomly generated - any resemblance is unintentional.
Users' passwords are being hashed with MD5 algorithm. Changing hashing algorithm is marked as TODO.
