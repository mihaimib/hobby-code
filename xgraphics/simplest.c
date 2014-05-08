#include<stdlib.h>
#include<unistd.h>
#include<X11/Xlib.h>

#define NCOLORS 3

void main (int argc, char *argv[])
{

  /* connect to the X server and make a window */
  Display *dpy = XOpenDisplay (getenv ("DISPLAY"));
  Window w = XCreateSimpleWindow (dpy, DefaultRootWindow (dpy),
                                  200, 200, 400, 400, 1,
                                  BlackPixel (dpy, DefaultScreen (dpy)),
                                  WhitePixel (dpy, DefaultScreen (dpy)));
  XStoreName (dpy, w, "simplest.c : a cut down x client");

  /* raise it and wait */
  XSelectInput (dpy, w, StructureNotifyMask);
  XMapRaised (dpy, w);
  for(XEvent e; XWindowEvent (dpy, w, StructureNotifyMask, &e); ( e.type != MapNotify ));

  /* create a GC for drawing in the window */
  GC g = XCreateGC (dpy, w, 0, NULL);

  /* look up colours by name (defined in /usr/share/X11/rgb.txt) */
  char *colors[NCOLORS]={"navy", "cornflower blue", "blue"};
  XColor xcolors[NCOLORS];

  for(int c=0; c<NCOLORS; c++) {
    XColor xc, sc;

    XAllocNamedColor(dpy,
                     DefaultColormapOfScreen(DefaultScreenOfDisplay(dpy)),
                     colors[c],
                     &sc, &xc);
    xcolors[c]=sc;
    
  }


  /* get attributes of the window */
  XWindowAttributes wa;
  XGetWindowAttributes(dpy, w, &wa);

  /* draw something */
  while (1)
    {
      /* set a random foreground color */
      XSetForeground(dpy, g, xcolors[random()%NCOLORS].pixel);


      /* draw a square */
      XFillRectangle (dpy, w, g, random()%(wa.width-50),
                      random()%(wa.height-40), 50, 40);


      /* once in a while, clear all */
      if( random()%500<1 )
        XClearWindow(dpy, w);


      /* flush changes and sleep */
      XFlush(dpy);
      usleep (10);
    }


  XCloseDisplay (dpy);
}