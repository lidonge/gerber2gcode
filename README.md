# Gerber2GCode

This project is for converting PCB  *gerber* files to *Gcode* files.  It is based on 

[gerber2png]: https://github.com/dgonner/gerber2png

for making PCB at home.

## Features

* Read from gerber and drill file
* Generate G-Code for axidraw machine and CNC, to paint double side PCB.
* Using new painting algorithm, not the traditional fitting by scan-lines.  It draw polygon from outside to inside, and draw lines by its track.
* Shrink PCB to fit the size, and generate locating drill hole.
* Mirro vertical or horizontal
* provide different speed for circle and line， for more precision should be more slower.



## Make and Usage

`git clone https://github.com/lidonge/gerber2gcode.git`

`cd gerber2gcode` 

`mvn compile`

`mvn jar`

`java -jar  ./target/gerber2gcode-0.0.1-SNAPSHOT.jar`