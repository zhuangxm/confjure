## Motivate

While developing server application, how to config different things is
really annoying. Normal approach is to put all configable things into
a map, and load this map from a file. It is far from perfect because
this gigantic config file might be a nightmare to maintain. When there
are many developers in the team, they always keep on updating the same
file.

It seems better to allow the configuration be distributed. Everytime
you want to introduce a new configurable thing, you can define it in
your own file, and provide value in the distributed way.

This better for fast pace development and test, but for production
server, it is better to keep all the config into one file, which will
maintained by server administrator.

## Usage

This little library try to address this problem. To use it, put
`confjure 0.0.1-SNAPSHOT` into your dependencies. And require it in
your namespace:

    (ns myns
      (:require [confjure.core :as conf]))

To define a new configurable variable:

    (conf/introduce! :foo string?)
    (conf/introduce! :baz fn?)

Where a configurable variable is always a keyword, and you can provide
an optional validator.

While in developing, you can provide configurable values as:

    (conf/provide! :test {:foo "bar"})

You then load the value of the config anywhere in your program:

    (conf/value :test)

While for production, you may provide a config file to collect all:

    (ns production-conf
       (:require [confjure.core :as conf]))
    (conf/provide! :production
       {:foo "bar",
        :baz #(str "Hello, " %)})

You can safely require this production-conf in your bootstrap file,
because it will not affect anything unless you set the system property
config.env to "production".

## Features

### automatical check

The library will check all the configurable elements when the first
invocation of value. If any provided value not conform the
corresponding validator, it will throw a RuntimeException.
