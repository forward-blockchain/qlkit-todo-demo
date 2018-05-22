# qlkit-todo-demo

This is a "batteries included" demo of qlkit- [Try a live version here](http://forwardblockchain.com/qlkit-todomvc/)

Please read the [recommended qlkit introductory article](https://medium.com/p/79b7b118ddac) for a walkthrough of this application.

## Setup - Figwheel

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein do clean, cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL. 

## Setup - New Clojurescript CLI

To run figwheel:

    clojure -R:repl build.clj figwheel 3450
    
You can then connect cider via `cider-connect` `localhost` `3450`. Once you get to the clojure prompt from emace, type `(cljs-repl)` for the clojurescript repl.

To compile the clojurescript code:

    clojure -R:repl build.clj compile
    


---
_Copyright (c) Conrad Barski. All rights reserved._
_The use and distribution terms for this software are covered by the Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php), the same license used by Clojure._
