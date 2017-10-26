# Amaris_Exercises
Amaris Exercises for Android Development

## Things to Improve

There are definitely things that need to improve in this exercise, mainly the following:

1. Exercise 1 and 2 loading
  * Example: While Exercise 1 is loading, Exercise 2 can't load the image because I'm using AsyncTasks on the same Executor
  * Next time I should either use a background service to load the data from Exercise 1 or using AsyncTasks but on different Executors

2. Exercise 3 Edit Text remove focus:
  * On Emulator, it removes focus correcly, on some phones it does not.

## Things missing

1. Exercise 2 Toolbar becoming transparent/opaque when scrolling up/down (Shortage of time)

Well, as they all say:

> There is never enough available time for doing all the things we want, but we do have to make the most of the time we have.

## Detected bugs
1. Exercise 1 caching seems to run two times
