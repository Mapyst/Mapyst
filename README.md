Mapyst
======

Mapyst gives users indoor and outdoor directions around Carnegie Mellon's Pittsburgh campus. It also can be used as a starting point for similar applications for other campuses. The applications were created so that by only changing the incoming data files and small changes to the frontend source code, the applications will work for any campus.

To create the data for the Carnegie Mellon campus, the Mapyst team started with the detailed blueprints of the buildings. The blueprints were simplified, rotated according to their actual orientation over Google Maps, and compressed. Points and lines, representing a graph, were drawn over the blueprints to create the map of where a person could walk. Each line/edge has a time associated with it indicating the amount of time it takes an average person to walk between those two points. (Note that the data for Carnegie Mellon's campus has not been distributed.)

Creating the data and making sure everything is formatted correctly is complicated and requires tools. 


Android
-------
The Android application uses binary data files and a campus JSON file to display a route to the user given his/her inputs. It uses the Route Library locally to compute the optimal route and stores the data files locally in the assets folder. It also uses an ActionBar library which is distributed in this project. The app was written to target Android API Level 7 (Android 2.1) and requires the Google API's Level 7 for the Google Maps API.


Webapp
------

The webapp folder contains the frontend code for the Mapyst webapp. The frontend displays the popular locations on campus, allows the user to search or get directions based on their preferences, displays a route over google maps, and allows the user to print out the directions. It receives information about CMU's campus, blueprints of the buildings' floors, and route directions from a web api. The directions are calculated on our server using the Route Library. It is written in HTML5, javascript, and css. It uses the Google Maps Web API and jQuery.

The javascript code is compiled using Google's Closure Compiler which can be downloaded at https://developers.google.com/closure/compiler/. To compile either modify (the path to the compiler jar) and run the batch file in the js folder or follow Google's Closure Compiler instructions to compile the same files compiled in the batch file.


Route Library
-------------
The Route Library computes the optimal route (based on the user's input and preferences) between two locations on campus. The library also interprets the user's input, accepting a wide variety of forms. And it breaks up the calculated route into a list of directions that are intended to be easy to understand by the user.

It requires binary data files that follow the Mapyst file specification as described in mapyst_file_spec.txt (in the data_format folder).

It also requires a JSON file with information about the campus. See the Campus JSON File section.


Campus JSON File
----------------
The campus JSON file stores information about the campus including: all the buildings, floors, and locations. See the example file called example_campus.json in the data_format folder. The following notes describe some of the parameters in the file:
* "x" represents longitude and is equal to the longitude x 1,000,000 so that it can be represented as an integer
* "y" represents latitude and is equal to the latitude x 1,000,000 so that it can be represented as an integer
* Building
    * "type" is a constant describing the type of a building (can be looked up in the source code: Building.java)
* Floor
    * "load_if_close" determines whether a floor will be loaded if the floor is close to the start or end locations (or in between). This parameter improves the algorithm performance by not loading floor which will never contain any part of the shortest path in a scenario. For example, the top floor of a building (that is not connected to another building) will never contain the shortest path if the start and end locations are not in that building.
    * "northWest" is the top left corner of the floor and "southEast" is the bottom right corner of the floor. This lets the algorithm know where the floor is for optimizations (and tells the frontend where to draw the floor).
* Location
    * "hours" tells the frontend when the location is open in the format "days:hours days:hours ...". See examples:
        * MT:8:30am-5pm WF:8:30am-5pm Sa:10am-5pm
        * MTWThF:8am-8pm
        * MTWThF:6am-Midnight, Midnight-2am SaS:9am-Midnight, Midnight-2am
        * MTWTh:24 Hours F:Closes 9pm Sa:10am-5pm S:Opens Noon
    * "is_direction_end" determines whether to break up a route if the route passes through one of the location's waypoints
    * "waypoints" is a list of waypoints associated with this location (every location must have at least one waypoint associated with it)
        * A waypoint is identified by its index in the list of buildings, index in the list of floors, and the index of that point in the list of points of that floor. The list of points is located in the floor's binary file.
