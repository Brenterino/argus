# To-do List
Not exhaustive, but here are the things that still need to be accomplished in order of priority for MVP:

- Implement name services for read/write of UUID-to-name mappings, may need to be privileged, but not entirely sure how
  this would be handled. Perhaps it could be a part of the claims set on a namespace basis? Possibly a free-for-all, but
  unsure of this.
- Creation of UI which can be used locally to do any group configuration in lieu of trying to do it all using Minecraft's
  built-in UI stuff. Should be reusable for a standalone site that could have an integration with another auth mechanism
  (likely using Discord) to serve a refresh/claims token. Standalone site would allow for making changes without logging
  into game.
- Rendering of waypoints/locations visually in-game
- Name coloring/symbols with group configuration (multi-group precedence TBD)
- Use Hazelcast to cache location data (possibly embedded?), transmit all location data for groups on initial connect
  and only transmit delta instead of everything on time slice
- Refactor Scale mode of Location Services to use Hazelcast instead of RabbitMQ, so we can reduce amount of
  distinct technologies used. May be more useful when we want to incorporate caching.

# Beyond MVP
- Implementation of Status Services which can be used to relay health, buff status, and other useful inventory
  information (ex. health pots, durability?)
- Status Service would need to have an intuitive in-game HUD. May be difficult to do properly, but possibly add
  the most critical (health + pots) as a part of the nametag?
