# herokru
A way for rutgers students to easily deploy a side project for free.

## how to use this
simply package you application and push it to docker hub
then make an account and ask us to run your container.
**you must have an @scarletmail.rutgers.edu email**

## how it works
using the web console, our server pulls your container and runs it
locally with docker/docker swarm. configure the env variables for
any secrets you cant put on docker hub. then you get a nice url you
can go to and see your application running

## frontend
coming soon

## future plans
- add support for persistent storage with a sqlite volume
- add suport for MongoDB and MariaDB(MySQL) with sidecar containers
- better scalling with docker-compose
- integrated image repositor
- rename to containRU to avoid lawsuit oMo

## API
see [api.md](api.md)


## License

Copyright © 2020 Mickey J Winters
this program is under the terms of the GNU Affero General Public License
See [LICENSE](LICENSE)
