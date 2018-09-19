# rokuComfortEclipsePlugin
Adds additional useful features to eclipse when doing roku development.

# Overview
I want to spend my time turning my thoughts into code.
I therefore want as little navigation process as possible. I always aim for speed of thought navigation. Using the mouse or search hotkeys to find code I want to go to is always a huge dissapointment.

With Roku scenegraph development, one can find themselves having to constantly move between a few files. These are:

 - node file (.xml)
 - code behind file (.brs)
 - (if doing MVVM) a ViewModel file (.brs).
 
If I am on a particular element in a .xml file, such as a function definition, or a custom component, then I want to be able to go straight to that element with a keypress.

This is the point of this plugin.

# Features
 - Better Navigation (set to Apple+B) - will try to take you straight to the defnition of an element. Currently supports:
  - Custom components - If the cursor is on `<MyCustomComponent>`, you will navigate to `MyCustomComponent.xml`
  - Function definitions - if the cursor is in a `<function name="MyFunction"` tag, you will navigate to `MyFunction` in your code behind file
  - Component instances - if the cursor is on a field in your codebehind file, such as "m.loginButton" - it will navigate to the component with that id, in your xml file e.g. `<Button id="loginButton`
 - Switch between brs and xml files (Apple+2)
 - More to come soon - submit feature requests, and we'll see what we can do.
 
# Installation
 - In Eclipse's install new software window, add `rokucomfort/RokuComfortSite/` as a new site
 - Install the RokuComfortFeature
