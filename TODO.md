# To-do List
Not exhaustive, but here are the things that still need to be accomplished in order of priority for MVP:

- Rendering of waypoints/locations visually in-game
- Find better way to determine when client is closing to avoid issues with it disconnecting randomly on join
- Keys to toggle streamer mode, increase/decrease waypoint distances, show same dimension
- Force location socket refresh when detected elections change

# Beyond MVP
- Color picker for group categories on UI
- Adapt UI to make standalone site would allow for making changes without logging into game.
- Implementation of Status Services which can be used to relay health, buff status, and other useful inventory
  information (ex. health pots, durability?)
- Status Service would need to have an intuitive in-game HUD. May be difficult to do properly, but possibly add
  the most critical (health + pots) as a part of the nametag?
- Implement name services for read/write of UUID-to-name mappings, may need to be privileged, but not entirely sure how
  this would be handled. Perhaps it could be a part of the claims set on a namespace basis? Possibly a free-for-all, but
  unsure of this. Note: moved this from MVP list because we are avoiding this by just sending the name in the location
  data. Group data UI will probably need to have people input UUID instead. We could maybe hook another API to do a
  name -> UUID lookup, but that'd have to be client-sided and only really for adding would it make sense :)
