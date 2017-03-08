# **Gameplay Prototype**

## Objective
Playing as "dude" from Lab 4 (now with giant arm enhancements!) you must swing from vine to vine to reach the goal!


##Controls (_Requires Xbox Controller_)

###Grabbing
_Used to take hold of vines and other parts of the stage!_

- _**Left bumper (LB)**_: **grab** (_holding the bumper_) or **release** (_letting go of the bumper_) an object with **left** hand
- _**Right bumper (LB)**_: **grab** (_holding the bumper_) or **release** (_letting go of the bumper_) an object with **right** hand

####Rules for Grabbing

For the purpose of this prototype, you are only able to grab the vines (any part of them) and certain sections of the upper floor. _No walls or ceilings are grabbable at this time._


###Swinging
_Used to gain momentum on vines to swing yourself across the stage!_

- _**Left joystick**_: swing **left** arm in whichever diretion you tilt
- _**Right joystick**_: swing **right** arm in whichever diretion you tilt

_**Note:** 360 degrees of motion are possible here_

####Rules for Swinging

Due to a technical issue, there is a specific rule for swinging your arms: _you must be "grabbing" in order to move and arm_ (whether your character is actually grabbing an object/vine or not). **This rule is true at all times.**


###Misc.
- _**Start button**_: resets the game (_see Known Issues_)
- _**Any other button**_: probably crash the game

_**Note:** force vector on each hand will appear once you start moving its respective arm. The left hand is designated by the blue vector, and the right hand is designated by the red vector._


##Tip for Leaving the Starting Platform
If the rule for swinging didn't make sense, following these instructions may help you understand what's going on (and be able to leave the starting platform as a result). Once you get going, the rule will make sense and hopefully you'll find that it only causes trouble for gameplay at this particular spot.

1. Tilt both arms to the bottom left corner of the screen
2. Hold down the grab button for either hand (I find myself using the right bumper)
3. With the _**opposite arm**_ (the one you're not grabbing with), reach for the first vine and try to grab it (I find myself using the left joystick and left bumper for this)
4. If you grabbed it, use the other arm to start swinging on the vine!

- I recommend waiting until the vine has swung to the left of its swinging cycle two times, it's a bit easier to grab after that point. 
- It may take a few tries to get used to this and not keep falling off the platform. If this happens, you should be able to press _**Start**_ to restart the stage (_see Known Issues_). 


##Hints for Completing the Stage

- **You can grab any point on the vine.** If you find yourself being just short of reaching the vine or travelling to the vine too slowly, try swinging and launching yourself from a different height on the vine!
- **Your player is actually very mobile.** With 360 degrees of movement, your character is able to swing himself around when grabbing an object. This is particularly useful for climbing up vines or for swinging yourself around to a different side of the vine!


##Known Issues

- Resetting: This doesn't work beyond the first vine. We believe it's due to not removing all of our game entities properly, but we haven't found exactly where this is happening.

- Invisible Obstacle Objects: There's a certain key point where there is an invisible object you can run into that may impede your progress. It's located right below the vine on the very right of the screen, the one that divides the top and bottom parts of the stage. It is possible to grab and swing off of it, but more concerningly it's possible to run into it when swinging on the aforementioned vine, preventing you from gaining altitude. My advice here is to swing onto the vine segment third-highest from the bottom and to swing from there. If you get stuck on the inivisble point, it is possible to swing/hoist yourself over it or to grab onto the invisble object and grab onto the vine from there.


##Maximizing Fun
If you reach the goal, you can actually grab it's center point and swing around on it! Since we don't have a sequence for completing the stage, you can swing around it as much as you want! Yay! Congrats for reaching the victory spot!


##Hard Mode
Try to get from the victory point back to the start!
