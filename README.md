# data-server

In-memory REST server to use as a dummy or develop server/back-end.

This server will not persist any data from one run to the other, it just saves the data while running and allows POST, GET, PUT and DELETE operations over the in-memory data.

You can run it as a spring boot app or as a docker container as described bellow.

# Example

Say you want a rest server with 2 endpoints: `/posts` and `/comments`. The command to put it online is the following.


```
gradle bootRun --args="--endpoints='posts,comments'"
```

Each endpoint will respond to 4 methods:
* POST
* GET
* PUT
* DELETE

In the last 3 methods, you can pass query params that will be used to filter the targeted json objects.

But maybe you are running a webapp that handles pagination and you are passing `page` and `pageSize` query parameters.
These parameters shouldn't be used to filter your data, so you can ignore them with:
```
gradle bootRun --args="--endpoints='posts,comments' --ignoreQueryParams='page,pageSize'"
```

Talking about pagination, this feature is optional on this dummy server, and disabled by default. To enable pagination
you must set `usePagination` flag. Following our previous example
```
gradle bootRun --args="--endpoints='posts,comments' --ignoreQueryParams='page,pageSize' --usePagination=true"
```
In case you enable pagination, the returned format will be (default PageImp of Spring):

````
{
   "content": [],
   "pageable": {
      "sort": {
         "empty": true,
         "unsorted": true,
         "sorted": false
      },
      "offset": 0,
      "pageNumber": 0,
      "pageSize": 10,
      "paged": true,
      "unpaged": false
   },
   "totalPages": 0,
   "totalElements": 0,
   "last": true,
   "size": 10,
   "number": 0,
   "sort": {
      "empty": true,
      "unsorted": true,
      "sorted": false
   },
   "numberOfElements": 0,
   "first": true,
   "empty": true
}
````

## body format

**JSON**

All data must be formatted as JSON. For instante, to add a post entry in posts endpoints, the body of the http request should be something like:
```
{
    "title": "Hi, my name is",
    "content": "Slim Shady"
}
```

## ids

The default key field is `id`. That means if you make a GET request for `/posts/1`, a json object with `"id": 1` will be searched.

You can change the the name of this key field on the endpoints variable.

**Example for a custom id field:**

Say you want the id for the `/posts` endpoint to be `postId` (instead of id). The endpoints variable would be something like this: `--endpoints='posts[postId],comments'`. With this, when you request `/posts/1` a json object with `"postId": 1` will be searched.

Apart from the name of the id field, you can control if the ids are automatically generated or not through `autoGenerateIds` 
option. This variable is **enabled by default**, that means if you POST some data without the id field, it will be 
automatically created for you. If you send the id field, then the provided id will be kept. If you want to disable 
this behaviour, you can do it with the variable `autoGenerateIds=false`, the objects will be stored as received.

### notes

It's not mandatory to send an id field. Searches by id will be made according the id field, object without the id field will be ignored.

There's no integrity, unique check or validation of any kind. `rest-server` is a  "bag of things", labeled by the given endpoints names.

There's no nested querying. For now, the query params are applied to the fields of each existing json objects. So if you have a `posts` object with a property `title`, you can query it with `/posts?title=Hi%2C+my+name+is`. But if you have a `posts` object with an `author` object field, you can't query inside it.

You don't need to define any `endpoints`, just work in the root it that fits your needs.

## Docker

If you want to have it running, use my docker image at: https://hub.docker.com/r/rmpt/rest-server

`docker run 
   -e "endpoints=posts,comments" 
   -p 8080:8080 rmpt/rest-server:0.4`
