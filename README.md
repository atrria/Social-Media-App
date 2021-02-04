# Relational Databases Project - Social Media App
##Short overview
JavaFX application allowing registering and logging into own user profile
as well as adding other users as friends, joining groups, editing own profile
and many others.

The data is being stored in a PostgreSQL database using <a href="https://www.elephantsql.com">ElephantSQL Service</a>.
The PostgreSQL JDBC Driver is being used for database connection.
####Main application functionalities include:
- registering as a new user with a name, last name, city, password and short profile description
- logging into the application with the name, last name and password corresponding to existing user
- searching for a specific user in the database
- adding users as friends
- editing / updating all user information
- joining groups
- following other users.

Application supports both English and Polish alphabet.\
By now users' passwords are being hashed with MD5 algorithm. Changing hashing algorithm is marked as TODO.

####Icons made by <a href="https://www.freepik.com" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a>
