# plzero
Simple JVM

The name "plzero" comes from the simplest possible programming language, PL/0.  I started making my own VM for it, and then decided to target the JVM platform.

This is intended to be fully functional with these limitations:
- does not handle longs or doubles
- does use or respect access flags, other than "static"
- does not use or handle Exceptions (or try, throw, or catch)
- does not enforce restrictions on types, specifically with fields or arrays. This will not be an issue with well-formed class files.
- limited to 5 local variables

Know bugs:
- can only handle 1 argument when passing arguments to "native" Java methods.  It is suprisingly difficult to find the number of parameters, the types, and to convert from the plzero generic type to the specific Java class.  But I plan on making this work.

It handles recursion, see test code.  This needs much more testing.

This depends on the BCEL code, available at: https://commons.apache.org/proper/commons-bcel/download_bcel.cgi
