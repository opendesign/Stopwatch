package com.psrivastava.stopwatch;

interface IChronometerService { //
  void start();
  void stop();
  boolean isRunning();
  long getTime();
  void pause();
  void resume();
}