# IUers-PacMan-2.0

## Game state API

Game state endpoints require the same JWT bearer token as other authenticated APIs. A user has one active game session, stored in Redis and refreshed on use; it expires after 30 minutes of inactivity.

- `POST /api/game/state/session` starts or resets the user's state to `canonicalMask: 0`.
- `GET /api/game/state` returns the current canonical state. It returns `404` if no session is active.
- `POST /api/game/state/events` applies a game event and returns the updated canonical state.

Fruit collection request:

```json
{ "event": "fruitCollected", "value": "DragonFruit" }
```

Other supported events:

- `effectEnded` or `effectConsumed` with `value` set to `thorns`, `powerup`, `watermelon`, `chilli`, `dragonMode`, `disguised`, or `wallHack`.
- `wallHackActivated` and `wallHackEnded` do not require a value.

The response contains `canonicalMask` and the decoded player flags. Bit assignments are: thorns `1`, power-up `2`, watermelon `4`, chilli `8`, dragon mode `16`, disguise `32`, and wall-hack `64`. Bits combine with bitwise OR; clearing an effect removes only its bit.

Example response:

```json
{
  "canonicalMask": 16,
  "hasThorns": false,
  "hasPowerup": false,
  "hasWatermelon": false,
  "hasChilli": false,
  "dragonMode": true,
  "disguised": false,
  "wallHack": false
}
```

This API trusts authenticated clients to report collection and effect events; it maintains the canonical mask but does not validate positions or fruit collisions.