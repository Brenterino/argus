# Tweaks
- Fix to resolve UUID/Name in case of server creating temporary/fake player with same name, but different UUID
- Extract pre-rendering out into separate thread. There may be some funkiness with this, but it should
  speed up actual rendering and reduce possible pauses

# Beyond MVP
- Determine method for ordering precedence of group metadata
- Display nicknames (?) in waypoint tag
- Adapt UI to make standalone site would allow for making changes without logging into game.
- Adapt slicing calc to take camera yaw as basis, I am thinking maybe this?: yaw - camera.yaw - sliceDeg / 2
- Possibly taking pitch into account to avoid drawing combined waypoints as separation becomes larger, not sure if this
  is necessary because it honestly might not be worth bothering with
- Color picker for group categories on UI
- Being able to pull audit log for individual users to request permission changes (maybe?)
- Integration with mapping solutions such as JourneyMap, Voxel, Xaero's
- Add sounds to pings

# Experimental
- Extension of status rendering to have an in-game HUD which can provide more detailed information if desired?
- Implement name services for read/write of UUID-to-name mappings, may need to be privileged, but not entirely sure how
  this would be handled. Perhaps it could be a part of the claims set on a namespace basis? Possibly a free-for-all, but
  unsure of this. Note: moved this from MVP list because we are avoiding this by just sending the name in the location
  data. Group data UI will probably need to have people input UUID instead. We could maybe hook another API to do a
  name -> UUID lookup, but that'd have to be client-sided and only really for adding would it make sense :)
- Maybe look at rendering colored names via teams?
