This repo can serve multiple purposes in connecting to our routes api.
# General conversion instructions
* You have to choose which lane you want to run this in. There are two different
  environment variables, one for the router api, one for the queues. Select np for the router api and
  stage for the queues for stage, and select prod for both for prod.
* Make sure that the runEndpointConversion method is uncommented, and that the revert method is
  commented out.
* Check the backup file to make sure it got all the queues and that the file was written.

# General revert instructions
* You will need to select the same lane in which you ran the code initially. For stage, this would
  be np for the router api and stage for the queues. For prod, both are prod.
* Comment out the runEndpointConversion method. Uncomment the revert method.
* Specify the file that you want the revert to use.

# Changing route destinations

## Change destinations from emx-to to endpoints
* In the runEndpointConversion method, make sure to call the method convertQueuesToEndpoints.
* There is some code for queues that don't fit the emx-to pattern; trash, archive,
and healthcheck.
* There is also code for an endpoint that is already in place. It shouldn't make
any changes to that route.
* Looking in Voyager on the routes page will allow you to see the changes. Make sure all the emx-to
queues are now endpoints.

## Rollback strategy(endpoints to emx-to queues)
* Make sure that in the revert method, that you call the method convertEndpointsToQueues.
* Run the code.
* Check Voyager to make sure that endpoints got converted back to emx-to queues 
correctly.

# Changing route rules

## Change rules to use endpoint parts
* In the runEndpointConversion method, make sure to call the method convertRuleToEndpointParts.
* The code uses String.replace. So route rules that use endpoints shouldn't be affected.
* After running the code and checking the backup file, looking in Voyager you can check that routes have
the rule change.

## Rollback strategy(endpoint parts to headers)
* Make sure that in the revert method, that you call the method convertRuleToHeaders
* Run the code.
* Check Voyager to make sure that rules got converted back to using headers.
