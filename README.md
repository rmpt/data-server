# data-server

## docker

If you just want to have it running, use my docker image at: https://hub.docker.com/repository/docker/rmpt/rest-server

## usage

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

## body format

**JSON**

All data must be formatted as JSON. For instante, to add a post entry in posts endpoints, the body of the http request should be something like:
```
{
    "title": "Hi, my name is",
    "content": "Slim Shady"
}
```

## customization

The default key field is `id`. That means if you make a request for `/posts/1` a json object with `"id": 1` will be searched. You can change the the name of this key field on the endpoints variable.

Example:

Say you want the id for the `/posts` endpoint to be `postId`. The endpoints variable would be something like this: `--endpoints='posts[postId],comments'`. With this, when you request `/posts/id` a json object with `"postId": 1` will be searched.

### notes

It's not mandatory to send an id field. Searches by id will be made according the id field, object without the id field will be ignored.

There's no integrity, unique check or validation of any kind. `rest-server` is a  "bag of things", labeled by the given endpoints names.

There's no nested querying. For now, the query params are applied to the fields of each existing json objects. So if you have a `posts` object with a property `title`, you can query it with `/posts?title=Hi%2C+my+name+is`. But if you have a `posts` object with an `author` object field, you can't query inside it.

You don't need to define any `endpoints`, just work in the root.
