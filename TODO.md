# To-do List
Not exhaustive, but here are the things that still need to be accomplished in order of priority for MVP:

- Verifying everything works :)
- Verify status services are working properly, may want to adjust default configuration delays
  depending on feedback

# Beyond MVP
- Allow for banning of issuing tokens based on UUID
- Display nicknames (?) in waypoint tag
- Add sounds to pings
- Determine method for ordering precedence of group metadata
- Color picker for group categories on UI
- Adapt UI to make standalone site would allow for making changes without logging into game.
- Adapt slicing calc to take camera yaw as basis, I am thinking maybe this?: yaw - camera.yaw - sliceDeg / 2
- Possibly taking pitch into account to avoid drawing combined waypoints as separation becomes larger, not sure if this
  is necessary because it honestly might not be worth bothering with
- Being able to pull audit log for individual users to request permission changes (maybe?)

# Experimental
- Extension of status rendering to have an in-game HUD which can provide more detailed information if desired?
- Implement name services for read/write of UUID-to-name mappings, may need to be privileged, but not entirely sure how
  this would be handled. Perhaps it could be a part of the claims set on a namespace basis? Possibly a free-for-all, but
  unsure of this. Note: moved this from MVP list because we are avoiding this by just sending the name in the location
  data. Group data UI will probably need to have people input UUID instead. We could maybe hook another API to do a
  name -> UUID lookup, but that'd have to be client-sided and only really for adding would it make sense :)
- Maybe look at rendering colored names via teams?
