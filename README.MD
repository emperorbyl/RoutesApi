This repo can serve multiple purposes in connecting to our routes api.

# Changing route destinations
Our current case for this is changing all of our routes from emx-to queues to be
endpoints. 
## Change destinations from emx-to to endpoints
* You have to choose which lane you want to run this in. There are two different 
environment variables, one for the router api, one for the queues. There are
comments next to those variables for your options. 
* You can check the backup file to make sure it got all the queues.
* There is some code for queues that don't fit the emx-to pattern; trash, archive,
and healthcheck.
* There is also code for an endpoint that is already in place. It shouldn't make
any changes to that route.
* Looking in Voyager on the routes page will allow you to see the changes.

## Rollback strategy(endpoints to emx-to queues)
* You will need to select the same lane in which you ran the code initially.
* There is a boolean to indicate whether you want to roll back the routes or not. 
Set this to true.
* Check Voyager to make sure that endpoints got converted back to emx-to queues 
correctly.