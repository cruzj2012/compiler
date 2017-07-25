## Compiler Construction

This project contains all my work in the compiler course available in http://cs.fit.edu/~ryan/cse4251/. The compiler is able to compile real Java code written in a subset of the Java language. 

Everything I did follows what is described in the Compiler Construction book by Appel https://www.amazon.com/Modern-Compiler-Implementation-Andrew-Appel/dp/052182060X/ with a few changes made by Dr. Stansifer http://cs.fit.edu/~ryan

I have support for arrays. Handling local and global variables.

From Appel I can only run the program Factorial.java, and and altered version of BubbleSort.java
which goes included here. The only alterations I did, is the following:

I cannot do `array1[i] = array2[k]`. So what I did is:
```
aux = array2[k];
array1[i] = aux;
```
The Bubble contains all features I tried to implement. Local and Global variables access,
as well as handling arrays, and printing together with while and if statements and it
represents the best I can do so far.

I suppose with a bit more polishing I could successfully run more Appel's cases.

Smaller programs should run fine.
