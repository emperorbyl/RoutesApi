This repo can serve multiple purposes in connecting to our routes api.

# Changing route destinations
Our current case for this is changing all of our routes from emx-to queues to be
endpoints. 
## Change destinations from emx-to to endpoints
* You have to choose which lane you want to run this in. There are two different 
environment variables, one for the router api, one for the queues. Select np for the router api and 
stage for the queues for stage, and select prod for both for prod.
* Make sure that the runEndpointConversion method is uncommented, and that the revert method is
commented out.
* There is some code for queues that don't fit the emx-to pattern; trash, archive,
and healthcheck.
* There is also code for an endpoint that is already in place. It shouldn't make
any changes to that route.
* Check the backup file to make sure it got all the queues and that the file was written.
* Looking in Voyager on the routes page will allow you to see the changes. Make sure all the emx-to
queues are now endpoints.

## Rollback strategy(endpoints to emx-to queues)
* You will need to select the same lane in which you ran the code initially. For stage, this would
be np for the router api and stage for the queues. For prod, both are prod.
* Comment out the runEndpointConversion method. Uncomment the revert method.
* Specify the file that you want the revert to use.
* Run the code.
* Check Voyager to make sure that endpoints got converted back to emx-to queues 
correctly.