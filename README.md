# Hamilton: Automated Vessels for Kerbal Space Program

This repo contains `hamilton`, a simple and fun orbital computer system for Kerbal Space Program. The goal is to have
fun and automate what I find to be the most boring part of Kerbal Space Program: getting into low orbit around various
bodies from the surface. The only purpose of this repo is to have fun and pretend I'm doing rocket science.

This program uses the excellent and super-fun [kRPC Mod](https://krpc.github.io/krpc/index.html) to talk to a running
instance of KSP. All of the dependencies required to talk to `kRPC` are checked-in to this repository.

## Building and Running

I dunno, use IntellJ, that's what I do. You'll want to add all of the JARs in the `third-party` directory as libraries
in IntelliJ so that `javac` works correctly. With a correct library setup, you can run any vessel from the IDE and
it'll connect to a running kRPC on your machine. Have fun!