## Force Item Battle Teams

I tried to recreate this plugin as closely as possible based on the BastiGHG video!

[![Watch on YouTube](https://img.youtube.com/vi/ddQ2lkCvvSM/maxresdefault.jpg)](https://www.youtube.com/watch?v=ddQ2lkCvvSM)


### Features:
- Countdown and item in the action bar.
- Item/block above the player.
- Item behind the player name in the tab list and name tag
- Number of jokers adjustable (config.yml)
- Round duration adjustable (config.yml)
- Team Lock (players can no longer join teams) (config.yml)
- Team Backpack
- Team Mode


### Commands:
```
/forceitembattle start
/forceitembattle stop
/forceitembattle restart
/forceitembattle settings
/forceitembattle backpack
/forceitembattle reveal
/forceitembattle overview
```

### Permissions:

```
fib.start
```

### Config:

```
challenge-seconds: 300

max-skips: 3

enable-backpack: true

team-lock: false

blacklist:
  - BEDROCK
  - COMMAND_BLOCK
  - BARRIER

teams:
  team1: []
  team2: []
  team3: []
  team4: []
  team5: []
  team6: []
  team7: []
  team8: []
```
